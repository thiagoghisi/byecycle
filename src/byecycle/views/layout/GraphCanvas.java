//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.


package byecycle.views.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import byecycle.dependencygraph.Node;


public class GraphCanvas extends Canvas implements StressMeter {

	public interface Listener {
		void nodeSelected(Node node);
	}

	private static final Force SPREADING_OUT = new SpreadingOut();
	
	public GraphCanvas(Composite parent, Listener listener) {
		super(parent, SWT.FILL | SWT.NO_BACKGROUND);
		if (listener == null) throw new IllegalArgumentException("listener");
		new LightweightSystem(this).setContents(_graphFigure);
		_graphFigure.setLayoutManager(_contentsLayout);
		_listener = listener;
	}

	
	private final List<GraphElement> _graphElements = new ArrayList<GraphElement>();
	private NodeFigure[] _nodeFigures;
	private DependencyFigure[] _dependencyFigures;
	
	private final IFigure _graphFigure = new Figure();
	private final XYLayout _contentsLayout = new XYLayout();
	
	private final Random _random = new Random();
	private float _currentStress;
	private float _smallestStressEver;
	private final List<NodeFigure> _nodesInPursuit = new LinkedList<NodeFigure>();
	private final Listener _listener;
	private final Map<IFigure, Node> _nodesByIFigure = new HashMap<IFigure, Node>();

	public void setGraph(Iterable<Node> nodeGraph) {
		initGraphElements(nodeGraph);
		initGraphFigure();
		randomizeLayout();
		
		_smallestStressEver = Float.MAX_VALUE;
		_graphFigure.addMouseListener(new MouseListener.Stub() {
			public void mouseDoubleClicked(MouseEvent e) {
				IFigure target = _graphFigure.findFigureAt(e.x, e.y);
				Node node = _nodesByIFigure.get(target);
				_listener.nodeSelected(node);
			}
		});
	}
	
	public void tryToImproveLayout() {
		if (_nodeFigures == null || 0 == _nodeFigures.length) return;

		int guiRelief = 0;

		do {
			seekBetterTargetStep();
			//if (betterTargetFound())  //TODO Comment this line to see the animation.
				lockOnNewTarget();
		} while (_nodesInPursuit.isEmpty() && guiRelief++ < 50);
		
		pursueTargetStep();
	}

	private void lockOnNewTarget() {
		_nodesInPursuit.clear();
		
		for (int i = 0; i < _nodeFigures.length; i++) {
			NodeFigure node = _nodeFigures[i];
			node.lockOnTarget();
			if (!node.onTarget()) _nodesInPursuit.add(node);
		}
	}

	private void pursueTargetStep() {
		Iterator<NodeFigure> it = _nodesInPursuit.iterator();
		while (it.hasNext()) {
			NodeFigure node = it.next();
			node.pursueTarget(_contentsLayout);
	 		if (node.onTarget()) it.remove();
		}

		makeInvertedDependenciesRed();
		
		_graphFigure.revalidate();
		_graphFigure.repaint();
	}

	private void seekBetterTargetStep() {
		for (int i = 0; i < _graphElements.size(); i++) {
	        GraphElement element1 = (GraphElement)_graphElements.get(i);
	        
            for (int j = i + 1; j < _graphElements.size(); j++) {
            	GraphElement element2 = (GraphElement)_graphElements.get(j);

            	SPREADING_OUT.actUpon(element1, element2);
    	        element1.reactTo(element2);
            }
        }

		int moving = 0;		
		for (int i = 0; i < _nodeFigures.length; i++) {
            NodeFigure figure = _nodeFigures[i];
            figure.give();

            if (figure.isMoving()) moving++;
        }
		
		if (moving == 0) {
			randomNodeFigure().nudgeNudge();
		}
	}

	private boolean betterTargetFound() {
		boolean result = _currentStress < _smallestStressEver;
		if (result) _smallestStressEver = _currentStress;
		_currentStress = 0;
		return result;
	}

	private NodeFigure randomNodeFigure() {
		return _nodeFigures[_random.nextInt(_nodeFigures.length)];
	}


	private void makeInvertedDependenciesRed() {
		for (int i = 0; i < _dependencyFigures.length; i++)
			_dependencyFigures[i].drawArrow();
	}
	
	private void initGraphFigure() {
		clearGraphFigure();

		Iterator elements = _graphElements.iterator();
		while (elements.hasNext()) {
			GraphElement element = (GraphElement)elements.next();
			_graphFigure.add(element.figure());
		}
	}

	private void initGraphElements(Iterable<Node> nodeGraph) {
		_nodesByIFigure.clear();
		
		Map nodeFiguresByNode = new HashMap();
		List dependencyFigures = new ArrayList();
		
		for (Node node : nodeGraph) {
			NodeFigure dependentFigure = produceNodeFigureFor(node, nodeFiguresByNode);
			Iterator providers = node.providers();
			while (providers.hasNext()) {
				Node provider = (Node)providers.next();
				NodeFigure providerFigure = produceNodeFigureFor(provider, nodeFiguresByNode);
				dependencyFigures.add(new DependencyFigure(dependentFigure, providerFigure));
			}
		}

		_graphElements.clear();
		_graphElements.addAll(nodeFiguresByNode.values());
		_graphElements.addAll(dependencyFigures);

		_dependencyFigures = new DependencyFigure[dependencyFigures.size()];
		System.arraycopy(dependencyFigures.toArray(), 0, _dependencyFigures, 0, _dependencyFigures.length);

		_nodeFigures = new NodeFigure[nodeFiguresByNode.size()];
		System.arraycopy(nodeFiguresByNode.values().toArray(), 0, _nodeFigures, 0, _nodeFigures.length);
	}

	private void clearGraphFigure() {
		System.out.println("clearGraphFigure");
		Object[] children = _graphFigure.getChildren().toArray();
		for (int i = 0; i < children.length; i++) {
			IFigure figure = (IFigure) children[i];
			_graphFigure.remove(figure);
		}
	}

	private NodeFigure produceNodeFigureFor(Node node, Map nodeFiguresByNode) {
		NodeFigure result = (NodeFigure) nodeFiguresByNode.get(node);
		if (result != null)	return result;

		result = new NodeFigure(node, this);
		nodeFiguresByNode.put(node, result);
		_nodesByIFigure .put(result.figure(), node);
		System.out.println(result.figure());
		return result;
	}

	private void randomizeLayout() {
		for (int i = 0; i < _nodeFigures.length; i++) {
			NodeFigure nodeFigure = _nodeFigures[i];
			int x = 180 + _random.nextInt(41);
			int y = 180 + _random.nextInt(41);
			nodeFigure.position(x, y);
		}
    }

	public void addStress(float stress) {
		_currentStress += stress;
	}
}
