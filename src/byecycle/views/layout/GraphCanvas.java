package byecycle.views.layout;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import byecycle.dependencygraph.Node;


public class GraphCanvas extends Canvas {

    private Node[] _graph;
	
	private final IFigure _graphFigure = new Figure();

	private final XYLayout _contentsLayout = new XYLayout();

	private final Map _nodeFiguresByNode = new HashMap();
	
	private final LightweightSystem _lws;
	
	private final Random _random = new Random(0);

	public GraphCanvas(Composite parent) {
		super(parent, SWT.FILL | SWT.NO_BACKGROUND);
		_lws = new LightweightSystem(this);
		_lws.setContents(_graphFigure);
	}

	public void setGraph(Node[] graph) {
		clearGraphFigure();
		
		_graph = graph;
		
		initGraphFigure();
		randomizeLayout();
	}
	

	private void randomizeLayout() {
		for (int i = 0; i < _graph.length; i++) {
			Node node = _graph[i];
			int x = 180 + _random.nextInt(41);
			int y = 180 + _random.nextInt(41);
			NodeFigure nodeFigure = produceNodeFigureFor(node);
			nodeFigure.position(x, y);
		}
    }

    private void clearGraphFigure() {
		_nodeFiguresByNode.clear();
		Object[] children = _graphFigure.getChildren().toArray();
		for (int i = 0; i < children.length; i++) {
			IFigure figure = (IFigure) children[i];
			_graphFigure.remove(figure);
		}
	}

	public void improveLayout() {

	    int interactions = _graph.length ^ 2;
	    while (interactions-- > 0) {
	        NodeFigure figure1 = randomNodeFigure();
	        NodeFigure figure2 = randomNodeFigure();
	        
	        figure1.reactTo(figure2);
	    }
	    
		makeInvertedDependenciesRed();

		for (int i = 0; i < _graph.length; i++) {
            NodeFigure figure = produceNodeFigureFor(_graph[i]);
            figure.positionYourselfIn(_contentsLayout);
        }
		
		_graphFigure.revalidate();
		_graphFigure.repaint();
	}

	private NodeFigure randomNodeFigure() {
		return produceNodeFigureFor(Node.drawOneFrom(_graph));
	}


	private void makeInvertedDependenciesRed() {
		Iterator children = _graphFigure.getChildren().iterator();  //TODO Optimize.
		while (children.hasNext()) {
			IFigure child = (IFigure) children.next();

			if (child instanceof PolylineConnection) {
				PolylineConnection dependency = (PolylineConnection) child;
				dependency
						.setForegroundColor(dependency.getStart().y > dependency
								.getEnd().y ? ColorConstants.red
								: ColorConstants.black);
			}
		}
	}
	
	private void initGraphFigure() {
		for (int i = 0; i < _graph.length; i++) {
			Node node = _graph[i];
			IFigure dependentFigure = produceNodeFigureFor(node);
			Iterator providers = node.providers();
			while (providers.hasNext()) {
				IFigure providerFigure = produceNodeFigureFor((Node) providers
						.next());
				addDependencyFigure(dependentFigure, providerFigure);
			}
		}
		_graphFigure.setLayoutManager(_contentsLayout);
	}

	private void addDependencyFigure(IFigure dependentFigure,
			IFigure providerFigure) {
		PolylineConnection dependency = new PolylineConnection();
		dependency.setSourceAnchor(new ChopboxAnchor(dependentFigure));
		dependency.setTargetAnchor(new ChopboxAnchor(providerFigure));

		PolygonDecoration arrowHead = new PolygonDecoration();
		PointList decorationPointList = new PointList();
		decorationPointList.addPoint(0, 0);
		decorationPointList.addPoint(-1, 1);
		decorationPointList.addPoint(-1, 0);
		decorationPointList.addPoint(-1, -1);
		arrowHead.setTemplate(decorationPointList);
		dependency.setTargetDecoration(arrowHead);

		_graphFigure.add(dependency);
	}

	private NodeFigure produceNodeFigureFor(Node node) {
		NodeFigure result = (NodeFigure) _nodeFiguresByNode.get(node);
		if (result != null)
			return result;

		result = new NodeFigure(node);
		_nodeFiguresByNode.put(node, result);
		_graphFigure.add(result);
		return result;
	}


}
