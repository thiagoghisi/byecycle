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
import byecycle.dependencygraph.Node;
import byecycle.views.layout.GraphCanvas;

public class ByecycleView extends ViewPart implements ISelectionListener {

	private GraphCanvas _canvas;
	private IViewSite _site;
	
	/**
	 * The constructor.
	 */
	public ByecycleView() {
	}
	
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		_site = site;
		_site.getPage().addSelectionListener(this);
	}
	
	public void dispose() {
		//_site.getPage().removeSelectionListener(this);
		super.dispose();
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

	private Node[] graph() {
		return Node.createGraph(new String[] { "Node1", "Node2", "Node3",
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
						Collection nodes = new PackageDependencyAnalysis(selectedPackage, monitor).nodes().values();
						final Node[] graph = (Node[]) nodes.toArray(new Node[nodes.size()]);
						dumpGraph(graph);
						UIJob job = new UIJob("package analysis display") {
							public IStatus runInUIThread(IProgressMonitor monitor) {
								_canvas.setGraph(graph);
								for (int i=0; i<100; ++i) {
									_canvas.improveLayout();
								}
								return Status.OK_STATUS;
							}
						};
						job.schedule();
					} catch (Exception x) {
						x.printStackTrace();
					}
					return Status.OK_STATUS;
				}
			};
			job.schedule();
		}
	}
	
	private void dumpGraph(Node[] graph) {
		System.out.println("*********");
		for (int i=0; i<graph.length; ++i) {
			System.out.println(graph[i].name());
		}
	}
}