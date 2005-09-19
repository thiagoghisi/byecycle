//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.

package byecycle.views.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.MouseListener.Stub;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.swt.widgets.Composite;
import byecycle.JavaType;
import byecycle.dependencygraph.Node;
import byecycle.views.layout.algorithm.CartesianLayout;


public class GraphCanvas<T> extends FigureCanvas {

	public interface Listener<LT> {
		void nodeSelected(Node<LT> node);
	}

	public GraphCanvas(Composite parent, Collection<Node<IBinding>> graph, CartesianLayout initialLayout, Listener<T> listener) {
		super(parent);

		this.setContents(_graphFigure);
		_graphFigure.setLayoutManager(new XYLayout());

		if (listener == null) throw new IllegalArgumentException("listener");
		_listener = listener;
			
		_graphFigure.addMouseListener(backgroundDoubleClickListener());
	}

	private final MouseListener _nodeDoubleClickListener = nodeDoubleClickListener();
	
	private final IFigure _graphFigure = new Figure();

	private DependencyFigure[] _dependencyFigures;
	private final Map<Node, NodeFigure<T>> _nodeFiguresByNode = new HashMap<Node, NodeFigure<T>>();
	
	private final Listener<T> _listener;

	private GraphMorpher _morpher;


	private Stub backgroundDoubleClickListener() {
		return new MouseListener.Stub() {
			public void mouseDoubleClicked(MouseEvent e) {
				_listener.nodeSelected(null);
				e.consume();
			}
		};
	}

	private Stub nodeDoubleClickListener() {
		return new MouseListener.Stub() {
			@SuppressWarnings("unchecked")
			public void mouseDoubleClicked(MouseEvent e) {
				Node<T> node = ((NodeFigure<T>)e.getSource()).node();
				_listener.nodeSelected(node);
				e.consume();
			}
		};
	}


	public void setGraph(Iterable<Node<T>> nodeGraph, CartesianLayout initialLayout) {
		initGraphElements(nodeGraph);
		initGraphFigure();

		useLayout(initialLayout);
	}
	
	public void animationStep() {
		if (_morpher != null) return;
		_morpher.morphingStep();
		if (_morpher.done()) _morpher = null;
		
		refreshDependencies();
		
		_graphFigure.revalidate();
		_graphFigure.repaint();
	}
	
	private void refreshDependencies() {
		for (int i = 0; i < _dependencyFigures.length; i++)
			_dependencyFigures[i].refresh();
	}
	
	private void initGraphFigure() {
		for (NodeFigure<?> nodeFigure : nodeFigures())
			_graphFigure.add(nodeFigure.figure());
		for (DependencyFigure dependencyFigure : _dependencyFigures)
			_graphFigure.add(dependencyFigure.figure());
	}

	private Collection<NodeFigure<T>> nodeFigures() {
		return _nodeFiguresByNode.values();
	}

	private void initGraphElements(Iterable<Node<T>> nodeGraph) {
		
		List<DependencyFigure> dependencyFigures = new ArrayList<DependencyFigure>();
		
		for (Node<T> node : nodeGraph) {
			NodeFigure dependentFigure = produceNodeFigureFor(node);
			Iterator<Node<T>> providers = node.providers();
			while (providers.hasNext()) {
				Node<T> provider = providers.next();
				NodeFigure providerFigure = produceNodeFigureFor(provider);
				dependencyFigures.add(new DependencyFigure(dependentFigure, providerFigure));
			}
		}

		_dependencyFigures = new DependencyFigure[dependencyFigures.size()];
		System.arraycopy(dependencyFigures.toArray(), 0, _dependencyFigures, 0, _dependencyFigures.length);
	}

	private NodeFigure produceNodeFigureFor(Node<T> node) {
		NodeFigure<T> result = _nodeFiguresByNode.get(node);
		if (result != null)	return result;

		result = new NodeFigure<T>(node);
		_nodeFiguresByNode.put(node, result);
		if(node.kind2() == JavaType.PACKAGE)
			result.figure().addMouseListener(_nodeDoubleClickListener);
		return result;
	}

	public void setGraph(Iterable<Node<T>> _graph) {
		setGraph(_graph, null);
	}

	public void useLayout(CartesianLayout newLayout) {
		_morpher = new GraphMorpher(nodeFigures(), newLayout);
	}

	public Rectangle sizeGiven(Node node) {
		return _nodeFiguresByNode.get(node).figure().getBounds();
	}

}
