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
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.swt.widgets.Composite;
import byecycle.JavaType;
import byecycle.dependencygraph.Node;
import byecycle.views.layout.forces.Attraction;
import byecycle.views.layout.forces.Aversion;
import byecycle.views.layout.forces.Force;
import byecycle.views.layout.forces.ProviderGravity;
import byecycle.views.layout.forces.SpreadingOut;


public class GraphCanvas<T> extends FigureCanvas {

	public interface Listener<LT> {
		void nodeSelected(Node<LT> node);
	}

	private static final Force SPREADING_OUT = new SpreadingOut();
	
	private static final Force ATTRACTION = new Attraction();
	
	private static final Force PROVIDER_GRAVITY = new ProviderGravity();
	
	private static final Force AVERSION = new Aversion();
	
	public GraphCanvas(Composite parent, Listener<T> listener) {
		super(parent);
		
		this.setContents(_graphFigure);
		_graphFigure.setLayoutManager(_contentsLayout);

		if (listener == null) throw new IllegalArgumentException("listener");
		_listener = listener;
		_mouseListener = new MouseListener.Stub() {
			public void mouseDoubleClicked(MouseEvent e) {
				Node<T> node = _nodesByIFigure.get(e.getSource());
				_listener.nodeSelected(node);
				e.consume();
			}
		};
		_graphFigure.addMouseListener(new MouseListener.Stub() {
			public void mouseDoubleClicked(MouseEvent e) {
				_listener.nodeSelected(null);
				e.consume();
			}
		});
	}

	private final MouseListener _mouseListener;
	
	private final IFigure _graphFigure = new Figure();
	private final XYLayout _contentsLayout = new XYLayout();

	private final List<GraphElement> _graphElements = new ArrayList<GraphElement>();
	private List<NodeFigure<T>> _nodeFigures;
	private DependencyFigure[] _dependencyFigures;
	private final Map<IFigure, Node<T>> _nodesByIFigure = new HashMap<IFigure, Node<T>>();

	private final List<NodeFigure<T>> _nodesInPursuit = new LinkedList<NodeFigure<T>>();
	
	private boolean _firstTime;
	private float _smallestStressEver;
	private final MyStressMeter _stressMeter = new MyStressMeter();

	private final Listener<T> _listener;

	private final Random _random = new Random();


	public void setGraph(Iterable<Node<T>> nodeGraph, GraphLayoutMemento layoutHint) {
		initGraphElements(nodeGraph);
		initGraphFigure();
		
		if (layoutHint == null) {
			_firstTime = true;
			layoutHint = GraphLayoutMemento.random();
		}
		layoutHint.layout(_nodeFigures);
		measureInitialStress();
	}

	private void measureInitialStress() {
		seekLocalStressMinimumStep();
		_smallestStressEver = _stressMeter._stressValue;
	}
	
	public boolean tryToImproveLayout() {
		if (_nodeFigures == null || _nodeFigures.isEmpty()) return false;

		//lockOnNewTarget();  //TODO Fun: Uncomment this line to see the animation.  :)
		pursueTargetStep(); //TODO Refactoring: Separate display logic from graph layout algorithm logic.

		boolean localMinimumFound = seekLocalStressMinimumForAWhile();

		float stress = _stressMeter._stressValue;
		boolean improved = stress < _smallestStressEver;
		if (_firstTime && improved) lockOnNewTarget();
		
		if (!localMinimumFound) return false;
		prepareToSeekAnotherMinimum();  //Interferes with the stress meter so has to be done here, after measuring the stress.
		_firstTime = false;

		if (improved) {
			_smallestStressEver = stress; //FIXME: Find out why the local minimum (not moving) graphs don't always coincide with the least stress (and often much worse visually) graphs. This is why _smallestStressEver is updated only after a local minimum is found. Discrepancy might end when graphs stop being pressed against the margin. If this discrepancy is solved, the _firstTime flag can be removed and lockOnNewTarget/smallestEver logic can become independent of the localMinimum/nudgeNudge logic. Since we have to wait for the local minimum (because of discrepancy) the _firstTime flag exists only to give the user some feedback because it might take a long time to find the first local minimum for huge graphs.
			lockOnNewTarget();
		}
		
		return improved;
	}

	private void prepareToSeekAnotherMinimum() {
		randomNodeFigure().nudgeNudge();
	}


	private boolean seekLocalStressMinimumForAWhile() {
		long start = System.nanoTime();
		do {
			if (seekLocalStressMinimumStep()) return true;
		} while (System.nanoTime() - start < 1000000); //One millisecond at least.
		return false;
	}

	private void lockOnNewTarget() {
		_nodesInPursuit.clear();
		
		for (NodeFigure<T> node : _nodeFigures) {
			node.lockOnTarget();
			if (!node.onTarget()) _nodesInPursuit.add(node);
		}
	}

	private void pursueTargetStep() {
		if (_nodesInPursuit.isEmpty()) return;

		Iterator<NodeFigure<T>> it = _nodesInPursuit.iterator();
		while (it.hasNext()) {
			NodeFigure<T> node = it.next();
			node.pursueTarget();
	 		if (node.onTarget()) it.remove();
		}

		refreshDependencies();
		
		_graphFigure.revalidate();
		_graphFigure.repaint();
	}

	private boolean seekLocalStressMinimumStep() {
		_stressMeter.reset();

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

		for (NodeFigure<T> figure : _nodeFigures)
			figure.southEastWind(); //TODO: Add some kind of weak gravity and let the graph free in space. Simply transalate graph to origin, instead of blowing it there.

		boolean moving = false;
		for (NodeFigure<T> figure : _nodeFigures) {
            if (figure.give()) moving = true;
        }

		return !moving;
	}

	private NodeFigure<T> randomNodeFigure() {
		int randomIndex = _random.nextInt(_nodeFigures.size());
		return _nodeFigures.get(randomIndex);
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

		_nodeFigures = new ArrayList<NodeFigure<T>>(nodeFiguresByNode.size());
		_nodeFigures.addAll(nodeFiguresByNode.values());
	}

	private void clearGraphFigure() {
		Object[] children = _graphFigure.getChildren().toArray();
		for (int i = 0; i < children.length; i++) {
			IFigure figure = (IFigure) children[i];
			_graphFigure.remove(figure);
		}
	}

	private NodeFigure produceNodeFigureFor(Node<T> node, Map<Node, NodeFigure> nodeFiguresByNode) {
		NodeFigure result = nodeFiguresByNode.get(node);
		if (result != null)	return result;

		result = new NodeFigure<T>(node, _stressMeter);
		nodeFiguresByNode.put(node, result);
		_nodesByIFigure .put(result.figure(), node);
		if(node.kind2()==JavaType.PACKAGE)
			result.figure().addMouseListener(_mouseListener);
		return result;
	}

	public void setGraph(Iterable<Node<T>> _graph) {
		setGraph(_graph, null);
	}

	public GraphLayoutMemento layoutMemento() {
		return new GraphLayoutMemento(_nodeFigures);
	}

	private static class MyStressMeter implements StressMeter {
		private float _stressValue;

		public void addStress(float stress) {
			_stressValue += stress;
		}
		
		private void reset() {
			_stressValue = 0;
		}
	}

}
