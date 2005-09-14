import java.util.Collection;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import byecycle.dependencygraph.Node;
import byecycle.views.layout.GraphCanvas;

public class StandAlone {

	public static void main(String args[]) {
		new StandAlone();
	}

	private final Collection<Node<String> > _graph = graph();

	private final Display _display = new Display(); //Has to be initialized
													// before the _graphFigure
													// although there is no
													// explicit dependency, or
													// else ColorConstants will
													// throw a
													// NullPointerException. :(

	private StandAlone() {
		Shell shell = new Shell(_display);
		shell.setText("Byecycle");
		shell.setSize(500, 500);
		
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);

		GraphCanvas<String> canvas = new GraphCanvas<String>(shell, new GraphCanvas.Listener<String>(){
			public void nodeSelected(Node<String> node) {
				System.out.println("Node:" + node);
			}
		});
		canvas.setGraph(_graph, null);

		shell.open();
		shell.layout();

		while (!shell.isDisposed()) {
			while (!_display.readAndDispatch()) {
				canvas.tryToImproveLayout();
				_display.sleep();
			}
		}
	}

	private Collection<Node<String> > graph() {
	    String[] names = new String[36];
	    for (int i = 0; i < names.length; i++) {
            //names[i] = "Node skdjfhskdfh.sdkfskdlf.sdfksdfj" + i;
            names[i] = "Node " + i;
        }
		return Node.createGraph(names);
	}
	
}