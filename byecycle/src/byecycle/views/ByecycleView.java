//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.
package byecycle.views;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
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
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import byecycle.PackageDependencyAnalysis;
import byecycle.dependencygraph.Node;
import byecycle.views.layout.GraphCanvas;

public class ByecycleView extends ViewPart implements ISelectionListener, IByecycleView {
	private static final int ONE_MILLISECOND = 1000000;

	private static final int TEN_SECONDS = 10 * 1000000000;

	private GraphCanvas _canvas;

	private IViewSite _site;

	private Set<ISelectionChangedListener> _selectionListeners = new HashSet<ISelectionChangedListener>();

	private IStructuredSelection _selection;

	private long _timeLastLayoutJobStarted;

	private UIJob _job;

	private boolean _paused;

	public ByecycleView() {
	}

	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		_site = site;
		_site.getPage().addSelectionListener(this);
		_site.setSelectionProvider(this);
		_job = new UIJob("package analysis display") {
			@Override
			public boolean shouldSchedule() {
				return !_paused;
			}

			@Override
			public boolean shouldRun() {
				return !_paused;
			}

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				GraphCanvas canvas = _canvas;
				if (canvas == null || canvas.isDisposed() || monitor.isCanceled())
					return Status.OK_STATUS;
				try {
					_canvas.tryToImproveLayout();
					this.schedule(nanosecondsToSleep() / 1000000);
				} catch (Exception rx) {
					return UIJob.errorStatus(rx);
				}
				return Status.OK_STATUS;
			}
		};
		_job.setSystem(true);
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
		_canvas = new GraphCanvas(parent, new GraphCanvas.Listener() {
			public void nodeSelected(Node node) {
				setSelection(node);
			}
		});
	}

	private void scheduleImproveLayoutJob() {
		if (_job.getState() == UIJob.SLEEPING) {
			_job.wakeUp();
		} else {
			_job.schedule();
		}
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

	public void selectionChanged(IWorkbenchPart part, ISelection newSelection) {
		if (_paused) return;
		if (!(newSelection instanceof IStructuredSelection)) return;
		IStructuredSelection structured = (IStructuredSelection)newSelection;
		if (!(structured.getFirstElement() instanceof IPackageFragment)) return;
		
		if (_selection != null && _selection.equals(structured)) return;
		_selection = structured;
		refresh();
	}


	private void refresh() {
		try {
			IPackageFragment selectedPackage = (IPackageFragment) _selection.getFirstElement();
			ICompilationUnit[] compilationUnits = selectedPackage.getCompilationUnits();
				
			writeFileForPackageFragment(selectedPackage);
			analyze(selectedPackage.getElementName(), compilationUnits);
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
		_timeLastLayoutJobStarted = System.nanoTime();
		Job job = new Job("'" + elementName + "' analysis") {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					_paused = true;
					final Collection<Node<IBinding>> nodes = new PackageDependencyAnalysis(compilationUnits, monitor)
							.dependencyGraph();
					if (!monitor.isCanceled()) {
						// dumpGraph(graph);
						UIJob job = new UIJob("package analysis display") {
							public IStatus runInUIThread(IProgressMonitor monitor) {
								try {
									setPaused(false);
									scheduleImproveLayoutJob();
									_canvas.setGraph((Collection<Node>) nodes);
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

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		_selectionListeners.add(listener);
	}

	public ISelection getSelection() {
		return _selection;
	}

	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		_selectionListeners.remove(listener);
	}

	void setSelection(Node selection) {
		if (null == selection) {
			// drill up
			IStructuredSelection structured = (IStructuredSelection) selection;
			IJavaElement element = (IJavaElement) structured.getFirstElement();
			setSelection(new StructuredSelection(element.getParent()));
		} else {
			// drill down
			Node<IBinding> typedNode = (Node<IBinding>) selection;
			IBinding binding = typedNode.payload();
			IJavaElement element = binding.getJavaElement();
			if (null != element) {
				setSelection(new StructuredSelection(element));
			}
		}
	}

	public void setSelection(ISelection selection) {
		if (selection.equals(_selection)) return;
		fireSelectionChanged(selection);
	}

	private void fireSelectionChanged(ISelection selection) {
		SelectionChangedEvent event = new SelectionChangedEvent(this, selection);
		for (ISelectionChangedListener listener : _selectionListeners) {
			listener.selectionChanged(event);
		}
	}

	private void setPaused(boolean pause) {
		if (pause == _paused) return;
		_paused = pause;
		firePropertyChange(ACTIVITY);
	}

	public void toggleActive(boolean pause) {
		if (pause == _paused) return;

		if (_paused) {
			if (_selection != null) {
				refresh();
			} else {
				setPaused(false);
				scheduleImproveLayoutJob();
			}
		} else {
			_job.sleep();
			setPaused(true);
		}
	}

	public boolean isPaused() {
		return _paused;
	}
}