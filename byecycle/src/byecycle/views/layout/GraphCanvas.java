//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.

package byecycle.views.layout;

import java.util.ArrayList;
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
import byecycle.views.layout.forces.Attraction;
import byecycle.views.layout.forces.Aversion;
import byecycle.views.layout.forces.Force;
import byecycle.views.layout.forces.ProviderGravity;
import byecycle.views.layout.forces.SpreadingOut;


public class GraphCanvas<T> extends Canvas implements StressMeter {

	public interface Listener<LT> {
		void nodeSelected(Node<LT> node);
	}

	private static final Force SPREADING_OUT = new SpreadingOut();
	
	private static final Force ATTRACTION = new Attraction();
	
	private static final Force PROVIDER_GRAVITY = new ProviderGravity();
	
	private static final Force AVERSION = new Aversion();
	
	public GraphCanvas(Composite parent, Listener<T> listener) {
		super(parent, SWT.FILL | SWT.NO_BACKGROUND | SWT.H_SCROLL | SWT.V_SCROLL);  //FIXME: The scrollbars appear but do nothing.
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
	private final Listener<T> _listener;
	private final Map<IFigure, Node<T>> _nodesByIFigure = new HashMap<IFigure, Node<T>>();

	private static long lastNudge;

	public void setGraph(Iterable<Node<T>> nodeGraph) {
		initGraphElements(nodeGraph);
		initGraphFigure();
		randomizeLayout();
		
		_smallestStressEver = Float.MAX_VALUE;
		_graphFigure.addMouseListener(new MouseListener.Stub() {
			public void mouseDoubleClicked(MouseEvent e) {
				IFigure target = _graphFigure.findFigureAt(e.x, e.y);
				Node<T> node = null;
				do {
					node = _nodesByIFigure.get(target);
					target = target.getParent();
				} while (node == null && target != null);
				
				_listener.nodeSelected(node);
			}
		});
	}
	
	public void tryToImproveLayout() {
		if (_nodeFigures == null || 0 == _nodeFigures.length) return;

		seekBetterTargetForAWhile();
		if (betterTargetFound())  //TODO Comment this line to see the animation.
			lockOnNewTarget();

		pursueTargetStep();
	}

	private void seekBetterTargetForAWhile() {
		long start = System.nanoTime();
		do {
			seekBetterTargetStep();
		} while (System.nanoTime() - start < 1000000); //One millisecond.
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
		if (_nodesInPursuit.isEmpty()) return;

		Iterator<NodeFigure> it = _nodesInPursuit.iterator();
		while (it.hasNext()) {
			NodeFigure node = it.next();
			node.pursueTarget(_contentsLayout);
	 		if (node.onTarget()) it.remove();
		}

		refreshDependencies();
		
		_graphFigure.revalidate();
		_graphFigure.repaint();
	}

	private void seekBetterTargetStep() {
		for (int i = 0; i < _graphElements.size(); i++) {
	        GraphElement element1 = _graphElements.get(i);
	        
            for (int j = i + 1; j < _graphElements.size(); j++) {
            	GraphElement element2 = _graphElements.get(j);

            	SPREADING_OUT.actUpon(element1, element2);
				ATTRACTION.actUpon(element1, element2);
				PROVIDER_GRAVITY.actUpon(element1, element2);
				AVERSION.actUpon(element1, element2);
            }
        }

		int moving = 0;		
		for (int i = 0; i < _nodeFigures.length; i++) {
            NodeFigure figure = _nodeFigures[i];
            figure.give();

            if (figure.isMoving()) moving++;
        }
		
		//if (moving == 0) {  //TODO: COrrect nudge logic.
		if (System.currentTimeMillis() - lastNudge > 8000) {
			lastNudge = System.currentTimeMillis();
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


	private void refreshDependencies() {
		for (int i = 0; i < _dependencyFigures.length; i++)
			_dependencyFigures[i].refresh();
	}
	
	private void initGraphFigure() {
		clearGraphFigure();

		Iterator elements = _graphElements.iterator();
		while (elements.hasNext()) {
			GraphElement element = (GraphElement)elements.next();
			_graphFigure.add(element.figure());
		}
	}

	private void initGraphElements(Iterable<Node<T>> nodeGraph) {
		_nodesByIFigure.clear();
		
		Map<Node, NodeFigure> nodeFiguresByNode = new HashMap<Node, NodeFigure>();
		List<DependencyFigure> dependencyFigures = new ArrayList<DependencyFigure>();
		
		for (Node<T> node : nodeGraph) {
			NodeFigure dependentFigure = produceNodeFigureFor(node, nodeFiguresByNode);
			Iterator<Node<T>> providers = node.providers();
			while (providers.hasNext()) {
				Node<T> provider = providers.next();
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
		Object[] children = _graphFigure.getChildren().toArray();
		for (int i = 0; i < children.length; i++) {
			IFigure figure = (IFigure) children[i];
			_graphFigure.remove(figure);
		}
	}

	private NodeFigure produceNodeFigureFor(Node<T> node, Map<Node, NodeFigure> nodeFiguresByNode) {
		NodeFigure result = (NodeFigure) nodeFiguresByNode.get(node);
		if (result != null)	return result;

		result = new NodeFigure(node, this);
		nodeFiguresByNode.put(node, result);
		_nodesByIFigure .put(result.figure(), node);
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
