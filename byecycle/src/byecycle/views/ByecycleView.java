//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.
package byecycle.views;

import java.util.Collection;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
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
import byecycle.views.layout.GraphLayoutMemento;

public class ByecycleView extends ViewPart implements IByecycleView {

	private static final int ONE_MILLISECOND = 1000000;
	private static final int TEN_SECONDS = 10 * 1000000000;

	private IViewSite _site;

	private GraphCanvas<IBinding> _canvas;

	private IPackageFragment _selectedPackage;
	private Collection<Node<IBinding>> _selectedPackageGraph;

	private final Object _graphChangeMonitor = new Object();
	
	private UIJob _layoutJob;
	private long _timeLastLayoutJobStarted;
	private final PackageLayoutMap _layoutCache = new PackageLayoutMap();

	private boolean _paused;
	private IJavaElement _deferredSelection;


	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		_site = site;
		_site.getPage().addSelectionListener(this);

		_layoutJob = new UIJob("Package dependency layout") {

			private IPackageFragment _packageBeingDisplayed;

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
			
				if (_canvas == null || _canvas.isDisposed() || monitor.isCanceled())
					return Status.OK_STATUS;

				checkForNewGraph();

				if (_paused) {
					this.sleep();
					return Status.OK_STATUS;
				}
				
				_timeLastLayoutJobStarted = System.nanoTime();
				boolean improved = _canvas.tryToImproveLayout();
				this.schedule(nanosecondsToSleep() / 1000000);

				if (improved) _layoutCache.keep(_packageBeingDisplayed, _canvas.layoutMemento());
				
				return Status.OK_STATUS;
			}

			private void checkForNewGraph() {
				if (_selectedPackageGraph == null) return;

				Collection<Node<IBinding>> myGraph;
				synchronized (_graphChangeMonitor) {
					_packageBeingDisplayed = _selectedPackage;
					myGraph = _selectedPackageGraph;
					_selectedPackageGraph = null;
				}
				GraphLayoutMemento bestLayoutTillNow = _layoutCache.getLayoutFor(_packageBeingDisplayed);
				_canvas.setGraph((Collection<Node<IBinding>>)myGraph, bestLayoutTillNow);
			}
		};
		_layoutJob.setSystem(true);
	}

	private void react() {
		_layoutJob.wakeUp();
		_layoutJob.schedule();
	}
	
	@Override
	public void dispose() {
		_site.getPage().removeSelectionListener(this);
		super.dispose();
	}

	public void createPartControl(Composite parent) {
		_canvas = new GraphCanvas<IBinding>(parent, new GraphCanvas.Listener<IBinding>() {
			public void nodeSelected(Node<IBinding> node) {
				selectNode(node);
			}
		});
	}

	public void selectionChanged(IWorkbenchPart ignored, ISelection selectionCandidate) { //FIXME: After the "Show Dependencies" popup menu action, this method is no longer called (Byecycle is no longer notified of selections changes and no longer changes the graph display). If focus is changed to another View and back, for example, everything comes back to normal. Is this an Eclipse bug?
		IJavaElement newSelection = validadeSelection(selectionCandidate);
		if (_paused) {
			_deferredSelection = newSelection; 
			return;
		}
		showJavaDependencies(newSelection);
	}

	public void showDependencies(ISelection selectionCandidate) {
		showJavaDependencies(validadeSelection(selectionCandidate));
	}
	
	private void showJavaDependencies(IJavaElement javaElement) {
		
		IPackageFragment newPackage = getPackage(javaElement);
		
		if (newPackage == null) return;
		if (newPackage == _selectedPackage) return;
		synchronized (_graphChangeMonitor) {
			_selectedPackage = newPackage;
			_selectedPackageGraph = null;
		}
		
		generateGraph(newPackage);
	}

	private void generateGraph(final IPackageFragment packageBeingGenerated) {
		final ICompilationUnit[] compilationUnits;
		try {
			compilationUnits = packageBeingGenerated.getCompilationUnits();
		} catch (JavaModelException x) {
			x.printStackTrace();
			return;
		}

		(new Job("'" + packageBeingGenerated.getElementName() + "' analysis") {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					Collection<Node<IBinding>> nextGraph = new PackageDependencyAnalysis(compilationUnits, monitor).dependencyGraph();
					
					synchronized (_graphChangeMonitor) {
						if (packageBeingGenerated != _selectedPackage) return Status.OK_STATUS;
 						_selectedPackageGraph = nextGraph;
					}
					react();
				} catch (Exception x) {
					UIJob.errorStatus(x);
				}
				return Status.OK_STATUS;
			}

		}).schedule();
	}

	private IPackageFragment getPackage(IJavaElement element) {
		if (element == null) return null;
		if (element instanceof IPackageFragment) return (IPackageFragment) element;
		return getPackage(element.getParent());
	}

	private void selectNode(Node<IBinding> selection) {
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

	private void drillUp() { //FIXME: drillUp is apparently not being called.
		showJavaDependencies(_selectedPackage.getParent());
	}

	public void togglePaused(boolean pause) {
		assert pause != _paused;
		_paused = pause;
		if (!_paused) showJavaDependencies(_deferredSelection);
		react();
	}

	private IJavaElement validadeSelection(ISelection candidate) {
		if (!(candidate instanceof IStructuredSelection)) return null;

		Object firstElement = ((IStructuredSelection)candidate).getFirstElement();
		if (!(firstElement instanceof IJavaElement)) return null;
		
		return (IJavaElement)firstElement;
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

	public void setFocus() {}

	
}