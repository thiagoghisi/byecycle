package byecycle.views;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
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
		_site.getPage().removeSelectionListener(this);
		super.dispose();
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		_canvas = new GraphCanvas(parent);
		
		scheduleImproveLayoutJob();
	}

	private void scheduleImproveLayoutJob() {
		Job improveLayoutJob = new Job("byecyle update job") {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					improveLayoutLoop(monitor);
				} catch (Exception x) {
					x.printStackTrace();
				}
				return Status.OK_STATUS;
			}

			private void improveLayoutLoop(IProgressMonitor monitor) throws InterruptedException {
				while (true) {
					if (_canvas.isDisposed() || monitor.isCanceled()) {
						break;
					}
					Thread.sleep(100);
					UIJob job = new UIJob("package analysis display") {
						public IStatus runInUIThread(IProgressMonitor monitor) {
							try {
								_canvas.improveLayout();
							} catch (Exception x) {
								x.printStackTrace();
							}
							return Status.OK_STATUS;
						}
					};
					job.setSystem(true);
					job.schedule();

				}
			}
		};
		improveLayoutJob.setPriority(Job.LONG);
		improveLayoutJob.setSystem(true);
		improveLayoutJob.schedule();
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		//viewer.getControl().setFocus();
	}
	
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}

		IStructuredSelection structured = (IStructuredSelection) selection;
		Object selected = structured.getFirstElement();
		
		try {
			if (selected instanceof IPackageFragment) {
				IPackageFragment selectedPackage = (IPackageFragment) selected;
				analyze(selectedPackage.getElementName(), selectedPackage.getCompilationUnits());
			} else if (selected instanceof ICompilationUnit) {
				ICompilationUnit compilationUnit = (ICompilationUnit)selected;
				analyze(compilationUnit.getElementName(), new ICompilationUnit[] { compilationUnit });
			}
		} catch (Exception x) {
			x.printStackTrace();
		}
	}
	
	private void analyze(final String elementName, final ICompilationUnit[] compilationUnits) {
		Job job = new Job("'" + elementName + "' analysis") {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					Collection nodes = new PackageDependencyAnalysis(compilationUnits, monitor).nodes().values();
					if (!monitor.isCanceled()) {
						final Node[] graph = (Node[]) nodes.toArray(new Node[nodes.size()]);
						//dumpGraph(graph);
						UIJob job = new UIJob("package analysis display") {
							public IStatus runInUIThread(IProgressMonitor monitor) {
								try {
									_canvas.setGraph(graph);
								} catch (Exception x) {
									x.printStackTrace();
								}
								return Status.OK_STATUS;
							}
						};
						job.schedule();
					}
				} catch (Exception x) {
					x.printStackTrace();
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		
	}

	private void dumpGraph(Node[] graph) {
		System.out.println("*********");
		for (int i=0; i<graph.length; ++i) {
			System.out.println(graph[i].name());
		}
	}
}