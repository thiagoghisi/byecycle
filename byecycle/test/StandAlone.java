import java.util.Collection;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import byecycle.dependencygraph.Node;
import byecycle.views.layout.CartesianLayout;
import byecycle.views.layout.algorithm.LayoutAlgorithm;
import byecycle.views.layout.algorithm.random.RandomAverage;
import byecycle.views.layout.ui.GraphCanvas;


public class StandAlone {

	public static void main(String args[]) {
		new StandAlone();
	}


	private final Collection<Node<String>> _graph = graph();

	private final Display _display = new Display(); // Has to be initialized before the _graphFigure although there is no explicit dependency, or else ColorConstants will throw a NullPointerException. :(

	private StandAlone() {
		Shell shell = new Shell(_display);
		shell.setText("Byecycle");
		shell.setSize(1000, 1000);

		FillLayout layout = new FillLayout();
		shell.setLayout(layout);

		GraphCanvas<String> canvas = new GraphCanvas<String>(shell, _graph, new CartesianLayout(), new GraphCanvas.Listener<String>() {
			public void nodeSelected(Node<String> node) {
				System.out.println("Node:" + node);
			}
		});
		 LayoutAlgorithm<String> algorithm = new RandomAverage<String>(_graph, new CartesianLayout(), canvas);
		//LayoutAlgorithm<String> algorithm = new InertialRelaxer<String>(_graph, CartesianLayout.random(), canvas);
		//LayoutAlgorithm<String> algorithm = new AlgorithmCombination<String>(_graph, CartesianLayout.random(), canvas);

		shell.open();
		shell.layout();

		while (!shell.isDisposed()) {
			while (!_display.readAndDispatch()) {
				// try {
				// Thread.sleep(50);
				// } catch (InterruptedException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				// }

				//algorithm.improveLayoutStep();
				boolean improved = algorithm.improveLayoutForAWhile();
				//if (improved)
					canvas.useLayout(algorithm.layoutMemento());

				canvas.animationStep();

			//	_display.sleep();
			}
		}
	}

	private Collection<Node<String>> graph() {
		String[] names = new String[4];
		for (int i = 0; i < names.length; i++) {
			names[i] = "Node " + i;
		}
		return Node.createGraph(names);
	}

	// private Collection<Node<String>> graph() {
	// Collection<Node<String>> result = new ArrayList<Node<String>>();
	// Node<String> nodeA = new Node<String>("A");
	// Node<String> nodeB = new Node<String>("B");
	// Node<String> nodeC = new Node<String>("C");
	// nodeA.addProvider(nodeB);
	// nodeB.addProvider(nodeA);
	// nodeB.addProvider(nodeC);
	// result.add(nodeA);
	// result.add(nodeB);
	// result.add(nodeC);
	// return result;
	// }

}