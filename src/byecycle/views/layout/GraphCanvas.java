//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.


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

	
	private NodeFigure[] _nodeFigures;
	private Dependency[] _dependencies = new Dependency[]{};
	
	private final IFigure _graphFigure = new Figure();
	private final XYLayout _contentsLayout = new XYLayout();
	
	private final Random _random = new Random();



	public void setGraph(Node[] nodeGraph) {
		initGraphFigure(nodeGraph);
		randomizeLayout();
	}
	
	public void tryToImproveLayout() {
		if (_nodeFigures == null) return;

		tryToImproveLayoutBehindTheScenes();

		if (isLayoutBetter()) render();
	}

	private void tryToImproveLayoutBehindTheScenes() {
		for (int i = 0; i < _nodeFigures.length; i++) {
	        NodeFigure figure1 = _nodeFigures[i];
	        
            for (int j = i + 1; j < _nodeFigures.length; j++) {
    	        NodeFigure figure2 = _nodeFigures[j];

    	        figure1.reactTo(figure2);
            }
        }
	    
		for (int i = 0; i < _dependencies.length; i++) {
			Dependency dependency1 = _dependencies[i];

			for (int j = i + 1; j < _dependencies.length; j++) {
				Dependency dependency2 = _dependencies[j];
			
				dependency1.reactTo(dependency2);
			}

			for (int j = 0; j < _nodeFigures.length; j++) {
		        NodeFigure figure = _nodeFigures[j];
		        dependency1.reactTo(figure);
			}

		}
		
		for (int i = 0; i < _nodeFigures.length; i++) {
            NodeFigure figure = _nodeFigures[i];
            figure.give();
        }
	}

	private boolean isLayoutBetter() {
		return true;
	}

	private void render() {
		for (int i = 0; i < _nodeFigures.length; i++) {
            NodeFigure figure = _nodeFigures[i];
    		figure.positionYourselfIn(_contentsLayout);
        }

		makeInvertedDependenciesRed();
		
		_graphFigure.revalidate();
		_graphFigure.repaint();
	}

	private NodeFigure randomNodeFigure() {
		return _nodeFigures[_random.nextInt(_nodeFigures.length)];
	}


	private void makeInvertedDependenciesRed() {
		Iterator children = _graphFigure.getChildren().iterator();
		while (children.hasNext()) {
			IFigure child = (IFigure) children.next();

			if (child instanceof PolylineConnection) {   //TODO Optimize. Iterate only on the PolylineConnections. 
				PolylineConnection dependency = (PolylineConnection) child;
				Color redOrBlack = dependency.getStart().y > dependency.getEnd().y
					? ColorConstants.red
					: ColorConstants.black;
				dependency.setForegroundColor(redOrBlack);
			}
		}
	}
	
	private void initGraphFigure(Node[] nodeGraph) {
		clearGraphFigure();

		Map nodeFiguresByNode = new HashMap();
		
		for (int i = 0; i < nodeGraph.length; i++) {
			Node node = nodeGraph[i];
			IFigure dependentFigure = produceNodeFigureFor(node, nodeFiguresByNode);
			Iterator providers = node.providers();
			while (providers.hasNext()) {
				Node provider = (Node)providers.next();
				IFigure providerFigure = produceNodeFigureFor(provider, nodeFiguresByNode);
				addDependencyFigure(dependentFigure, providerFigure);
				//TODO add dependency to _dependencies.
			}
		}

		_nodeFigures = new NodeFigure[nodeFiguresByNode.size()];
		Iterator it = nodeFiguresByNode.values().iterator();
		int j = 0;
		while (it.hasNext()) _nodeFigures[j++] = (NodeFigure)it.next();  
	}

	private void clearGraphFigure() {
		Object[] children = _graphFigure.getChildren().toArray();
		for (int i = 0; i < children.length; i++) {
			IFigure figure = (IFigure) children[i];
			_graphFigure.remove(figure);
		}
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
		for (int i = 0; i < _nodeFigures.length; i++) {
			NodeFigure nodeFigure = _nodeFigures[i];
			int x = 180 + _random.nextInt(41);
			int y = 180 + _random.nextInt(41);
			nodeFigure.position(x, y);
		}
    }

}