package byecycle.views.daglayout;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class DagLayout {

	public static void main(String args[]) {
		new DagLayout();
	}

	private final GraphNode[] _graph = graph();

	private final Display _display = new Display(); //Has to be initialized
													// before the _graphFigure
													// although there is no
													// explicit dependency, or
													// else ColorConstants will
													// throw a
													// NullPointerException. :(

	private DagLayout() {
		Shell shell = new Shell(_display);
		shell.setText("Byecycle");
		shell.setSize(400, 400);
		shell.open();
		
		GraphCanvas canvas = new GraphCanvas(shell);
		canvas.setSize(400, 400);
		canvas.setGraph(_graph);

		while (!shell.isDisposed()) {
			while (!_display.readAndDispatch()) {
				canvas.improveLayout();
				_display.sleep();
			}
		}
	}


	private GraphNode[] graph() {
		return GraphNode.create(new String[] { "Node1", "Node2", "Node3",
				"Node4" });
	}	

}