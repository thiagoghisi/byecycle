//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.
package byecycle.views;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import byecycle.PackageDependencyAnalysis;
import byecycle.dependencygraph.Node;
import byecycle.views.layout.GraphCanvas;

public class ByecycleView extends ViewPart implements IByecycleView {

	private static final int ONE_MILLISECOND = 1000000;
	private static final int TEN_SECONDS = 10 * 1000000000;

	private GraphCanvas<IBinding> _canvas;

	private IViewSite _site;

	private IStructuredSelection _selection;
	private IStructuredSelection _packageBeingAnalysed;

	private UIJob _layoutJob;
	private long _timeLastLayoutJobStarted;


	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		_site = site;
		_site.getPage().addSelectionListener(this);

		_layoutJob = new UIJob("Package dependency layout") {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				GraphCanvas canvas = _canvas;
				if (canvas == null || canvas.isDisposed() || monitor.isCanceled())
					return Status.OK_STATUS;
				try {
					_timeLastLayoutJobStarted = System.nanoTime();
					_canvas.tryToImproveLayout();
					this.schedule(nanosecondsToSleep() / 1000000);
				} catch (Exception rx) {
					return UIJob.errorStatus(rx);
				}
				return Status.OK_STATUS;
			}
		};
		_layoutJob.setSystem(true);
	}

	@Override
	public void dispose() {
		_site.getPage().removeSelectionListener(this);
		super.dispose();
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		_canvas = new GraphCanvas<IBinding>(parent, new GraphCanvas.Listener<IBinding>() {
			public void nodeSelected(Node<IBinding> node) {
				setSelection(node);
			}
		});
	}

	private long nanosecondsToSleep() {
		long currentTime = System.nanoTime();
		long timeLastLayoutJobTook = currentTime - _timeLastLayoutJobStarted;
		if (timeLastLayoutJobTook < 0)
			timeLastLayoutJobTook = 0; // This can happen due to rounding from nanos to millis.
		long timeToSleep = timeLastLayoutJobTook * 4; // The more things run in parallel with byecycle, the less greedy byecycle
		// will be. Byecycle is proud to be a very good citizen. :)
		if (timeToSleep > TEN_SECONDS)
			timeToSleep = TEN_SECONDS;
		if (timeToSleep < ONE_MILLISECOND)
			timeToSleep = ONE_MILLISECOND;
		_timeLastLayoutJobStarted = currentTime + timeToSleep;
		return timeToSleep;
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		// viewer.getControl().setFocus();
	}

	public void selectionChanged(IWorkbenchPart ignored, ISelection newSelection) {
		if (!(newSelection instanceof IStructuredSelection)) return;
		IStructuredSelection structured = (IStructuredSelection)newSelection;
		if (!(structured.getFirstElement() instanceof IPackageFragment)) return;
		
		if (_selection != null && _selection.equals(structured)) return;
		_selection = structured;
		
		if (isPaused()) return;
		pause();
		refresh();
		resume();
	}


	private void refresh() {
		_packageBeingAnalysed = _selection;
		try {
			IPackageFragment packageFragment = (IPackageFragment) _packageBeingAnalysed.getFirstElement();
			ICompilationUnit[] compilationUnits = packageFragment.getCompilationUnits();
				
			writeFileForPackageFragment(packageFragment);
			analyze(packageFragment.getElementName(), compilationUnits);
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	private void writeFileForPackageFragment(IPackageFragment p) {
		IPackageFragmentRoot root = getPackageFragmentRoot(p);
		
		try {
			IResource resource = root.getCorrespondingResource();
			System.out.println(resource);
			IFolder folder = (IFolder)resource; //FIXME: This works only with sourcefolders, not with source directly in the project root.
			IFolder cache = folder.getFolder(".byecyclelayoutcache");
			if (!cache.exists()) cache.create(false, false, null);
			IFile file = cache.getFile("foo.txt");
			if (file.exists()) {
				//file.setContents
			} else {				
				file.create(new ByteArrayInputStream("Hello".getBytes()), false, null);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private IPackageFragmentRoot getPackageFragmentRoot(IJavaElement element) {
		return element instanceof IPackageFragmentRoot
			? (IPackageFragmentRoot) element
			: getPackageFragmentRoot(element.getParent());
	}

	private void analyze(final String elementName, final ICompilationUnit[] compilationUnits) {
		Job job = new Job("'" + elementName + "' analysis") {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					final Collection<Node<IBinding>> nodes = new PackageDependencyAnalysis(compilationUnits, monitor)
							.dependencyGraph();
					if (!monitor.isCanceled()) {
						UIJob job = new UIJob("package analysis display") {
							public IStatus runInUIThread(IProgressMonitor monitor) {
								try {
									_canvas.setGraph((Collection<Node<IBinding>>) nodes);
								} catch (Exception x) {
									UIJob.errorStatus(x);
								}
								return Status.OK_STATUS;
							}
						};
						job.schedule();
					}
				} catch (Exception x) {
					UIJob.errorStatus(x);
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	private void setSelection(Node<IBinding> selection) {
		if (null == selection) {
			drillUp();
		} else {
			drillDown(selection);
		}
	}

	private void drillDown(Node<IBinding> selection) {
		IBinding binding = selection.payload();
		IJavaElement element = binding.getJavaElement();
		if (null != element) {
			selectionChanged(null, new StructuredSelection(element));
		}
	}

	private void drillUp() {
		IJavaElement element = (IJavaElement) _selection.getFirstElement();
		selectionChanged(null, new StructuredSelection(element.getParent()));
	}

	public void togglePaused(boolean pause) {
		if (pause) {
			pause();
		} else {
			resume();
		}
	}

	private void pause() {
		_layoutJob.sleep();
	}

	private void resume() {
		if (_packageBeingAnalysed != _selection) {
			refresh();
		}
		_layoutJob.wakeUp();
		_layoutJob.schedule();
	}

	private boolean isPaused() {
		return _layoutJob.getState() == UIJob.SLEEPING;
	}

}