package byecycle.views.layout;

import java.util.*;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import byecycle.dependencygraph.Node;


public class GraphCanvas extends Canvas {

	public GraphCanvas(Composite parent) {
		super(parent, SWT.FILL | SWT.NO_BACKGROUND);
		new LightweightSystem(this).setContents(_graphFigure);
		_graphFigure.setLayoutManager(_contentsLayout);
	}

	
	private NodeFigure[] _graph;
	
	private final IFigure _graphFigure = new Figure();
	private final XYLayout _contentsLayout = new XYLayout();
	
	private final Random _random = new Random();


	public void setGraph(Node[] nodeGraph) {
		clearGraphFigure();
		
		initGraphFigure(nodeGraph);
		randomizeLayout();
	}
	
    private void clearGraphFigure() {
		Object[] children = _graphFigure.getChildren().toArray();
		for (int i = 0; i < children.length; i++) {
			IFigure figure = (IFigure) children[i];
			_graphFigure.remove(figure);
		}
	}

	public void improveLayout() {
		if (_graph == null) return;

		for (int i = 0; i < _graph.length; i++) {
	        NodeFigure figure1 = _graph[i];
	        
            for (int j = i + 1; j < _graph.length; j++) {
    	        NodeFigure figure2 = _graph[j];

    	        figure1.reactTo(figure2);
            }
        }
	    
		makeInvertedDependenciesRed();

		for (int i = 0; i < _graph.length; i++) {
            NodeFigure figure = _graph[i];
            figure.positionYourselfIn(_contentsLayout);
        }
		
		_graphFigure.revalidate();
		_graphFigure.repaint();
	}

	private NodeFigure randomNodeFigure() {
		return _graph[_random.nextInt(_graph.length)];
	}


	private void makeInvertedDependenciesRed() {
		Iterator children = _graphFigure.getChildren().iterator();  //TODO Optimize.
		while (children.hasNext()) {
			IFigure child = (IFigure) children.next();

			if (child instanceof PolylineConnection) {
				PolylineConnection dependency = (PolylineConnection) child;
				Color redOrBlack = dependency.getStart().y > dependency.getEnd().y
					? ColorConstants.red
					: ColorConstants.black;
				dependency.setForegroundColor(redOrBlack);
			}
		}
	}
	
	private void initGraphFigure(Node[] nodeGraph) {
		Map nodeFiguresByNode = new HashMap();
		
		for (int i = 0; i < nodeGraph.length; i++) {
			Node node = nodeGraph[i];
			IFigure dependentFigure = produceNodeFigureFor(node, nodeFiguresByNode);
			Iterator providers = node.providers();
			while (providers.hasNext()) {
				Node provider = (Node)providers.next();
				IFigure providerFigure = produceNodeFigureFor(provider, nodeFiguresByNode);
				addDependencyFigure(dependentFigure, providerFigure);
			}
		}

		_graph = new NodeFigure[nodeFiguresByNode.size()];
		Iterator it = nodeFiguresByNode.values().iterator();
		int j = 0;
		while (it.hasNext()) _graph[j++] = (NodeFigure)it.next();  
	}

	private NodeFigure produceNodeFigureFor(Node node, Map nodeFiguresByNode) {
		NodeFigure result = (NodeFigure) nodeFiguresByNode.get(node);
		if (result != null)	return result;

		result = new NodeFigure(node);
		nodeFiguresByNode.put(node, result);
		_graphFigure.add(result);
		return result;
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

	private void randomizeLayout() {
		for (int i = 0; i < _graph.length; i++) {
			NodeFigure nodeFigure = _graph[i];
			int x = 180 + _random.nextInt(41);
			int y = 180 + _random.nextInt(41);
			nodeFigure.position(x, y);
		}
    }

}
