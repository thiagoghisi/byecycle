package byecycle.views;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

import byecycle.PackageDependencyAnalysis;
import byecycle.views.daglayout.GraphCanvas;
import byecycle.views.daglayout.GraphNode;

public class ByecycleView extends ViewPart implements ISelectionListener {

	private GraphCanvas _canvas;
	
	/**
	 * The constructor.
	 */
	public ByecycleView() {
	}
	
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		site.getPage().addSelectionListener(this);
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		_canvas = new GraphCanvas(parent);
		_canvas.setGraph(graph());
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		//viewer.getControl().setFocus();
	}

	private GraphNode[] graph() {
		return GraphNode.create(new String[] { "Node1", "Node2", "Node3",
				"Node4" });
	}

	
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}

		IStructuredSelection structured = (IStructuredSelection) selection;
		Object selected = structured.getFirstElement();
		if (selected instanceof IPackageFragment) {
			final IPackageFragment selectedPackage = (IPackageFragment) selected;
			Job job = new Job("'" + selectedPackage.getElementName() + "' analysis") {
				protected IStatus run(IProgressMonitor monitor) {
					try {
						Collection nodes = new PackageDependencyAnalysis(selectedPackage, null).nodes().values();
						final GraphNode[] graph = (GraphNode[]) nodes.toArray(new GraphNode[nodes.size()]);
						dumpGraph(graph);
						UIJob updateJob = new UIJob("graph update") {
							public IStatus runInUIThread(IProgressMonitor monitor) {
								_canvas.setGraph(graph);
								for (int i=0; i<100; ++i) {
									_canvas.improveLayout();
								}
								return Status.OK_STATUS;
							}
						};
						updateJob.schedule();
						
					} catch (Exception x) {
						x.printStackTrace();
					}
					return Status.OK_STATUS;
				}
			};
			job.schedule();
		}
	}
	
	private void dumpGraph(GraphNode[] graph) {
		System.out.println("*********");
		for (int i=0; i<graph.length; ++i) {
			System.out.println(graph[i].name());
		}
	}
}