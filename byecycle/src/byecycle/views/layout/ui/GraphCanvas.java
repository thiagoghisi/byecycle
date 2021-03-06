//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.

package byecycle.views.layout.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseListener.Stub;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.widgets.Composite;

import byecycle.JavaType;
import byecycle.dependencygraph.Node;
import byecycle.views.layout.CartesianLayout;
import byecycle.views.layout.Coordinates;
import byecycle.views.layout.FloatRectangle;
import byecycle.views.layout.NodeSizeProvider;


public class GraphCanvas<T> extends FigureCanvas implements NodeSizeProvider {

	private static final float MARGIN_PIXELS = 3;


	public interface Listener<LT> {
		void nodeSelected(Node<LT> node);
	}


	public GraphCanvas(Composite parent, Collection<Node<T>> graph, CartesianLayout initialLayout, Listener<T> listener) {
		super(parent, new LightweightSystem());
		setScrollBarVisibility(FigureCanvas.AUTOMATIC);

		this.setContents(_graphFigure);

		if (listener == null) throw new IllegalArgumentException("listener");
		_listener = listener;
		_graphFigure.addMouseListener(backgroundDoubleClickListener());

		initGraphFigures(graph);
		initRootGraphFigure();

		initialLayout(translateToOrigin(initialLayout));
		recomputeGraphFigureSize();
	}


	private final MouseListener _nodeDoubleClickListener = nodeDoubleClickListener();
	
	//tjennings - handle for the single click listener
	private final MouseListener _nodeSingleClickListener = nodeSingleClickListener();

	private final IFigure _graphFigure = new Figure();
	private NodeFigure<T> _selectedNodeFigure;

	private DependencyFigure[] _dependencyFigures;
	private final Map<Node<T>, NodeFigure<T>> _nodeFiguresByNode = new HashMap<Node<T>, NodeFigure<T>>();
	private final Map<IFigure, Node<T>> _nodeByFigure = new HashMap<IFigure, Node<T>>();
	
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
			public void mouseDoubleClicked(MouseEvent e) {
				Node<T> node = _nodeByFigure.get((IFigure)e.getSource());
				_listener.nodeSelected(node);
				e.consume();
			}
		};
	}
	
	/**
	 * tjennings -
	 * Single click on a node to highlight its dependencies
	 */
	private Stub nodeSingleClickListener() {
		return new MouseListener.Stub() {
			public void mousePressed(MouseEvent e) {
				selectNode((IFigure) e.getSource());
				e.consume();
			}
		};
	}	
	
	/**
	 * tjennings
	 * clear the existing highlighting, if necessary.
	 * find the selected Nnecessary.
	 * find the NodeFigure for our IFigure selection, and highlight it.odeFigure and highlight it.
	 */
	private void selectNode(IFigure figure) {
		if (null == figure) return;  //FIXME playing it safe, this probably never happens				

		if (null != _selectedNodeFigure)
			_selectedNodeFigure.notifyNodeDeselected();
		
		//FIXME this sucks and I'm not sure I want to create another map.
		Node<T> node = this._nodeByFigure.get(figure);
		NodeFigure<T> nodeFigure = _nodeFiguresByNode.get(node);
		nodeFigure.notifyNodeSelected();				
		_selectedNodeFigure = nodeFigure;
	}

	public void animationStep() {
		animationStep(3);
	}

	private void refreshDependencies() {
		for (DependencyFigure dependencyFigure : _dependencyFigures)
			dependencyFigure.refresh();
	}

	private void initRootGraphFigure() {
		for (NodeFigure<?> nodeFigure : nodeFigures()) {
			IFigure figure = nodeFigure.figure();
			_graphFigure.add(figure);
			figure.setSize(figure.getPreferredSize());
		}

		for (DependencyFigure dependencyFigure : _dependencyFigures)
			_graphFigure.add(dependencyFigure.figure());
	}

	private Collection<NodeFigure<T>> nodeFigures() {
		return _nodeFiguresByNode.values();
	}

	private void initGraphFigures(Iterable<Node<T>> nodeGraph) {

		List<DependencyFigure> dependencyFigures = new ArrayList<DependencyFigure>();

		for (Node<T> node : nodeGraph) {
			NodeFigure<T> dependentFigure = produceNodeFigureFor(node);

			for (Node<T> provider : node.providers()) {
				NodeFigure<T> providerFigure = produceNodeFigureFor(provider);
				DependencyFigure nodeDependency = new DependencyFigure(dependentFigure, providerFigure);
				dependencyFigures.add(nodeDependency);
			}
		}

		_dependencyFigures = new DependencyFigure[dependencyFigures.size()];
		_dependencyFigures = dependencyFigures.toArray(_dependencyFigures);
	}

	private NodeFigure<T> produceNodeFigureFor(Node<T> node) {
		NodeFigure<T> result = _nodeFiguresByNode.get(node);
		if (result != null) return result;

		result = new NodeFigure<T>(node);
		_nodeFiguresByNode.put(node, result);
		final IFigure figure = result.figure();

		if (node.kind2() == JavaType.PACKAGE) {
			figure.addMouseListener(_nodeDoubleClickListener);
		}

		figure.addMouseListener(_nodeSingleClickListener); //tjennings - add a single click (mouse down) listener
		_nodeByFigure.put(figure, node);
		return result;
	}

	private void initialLayout(CartesianLayout initialLayout) {

		for (NodeFigure<?> figure : nodeFigures()) {
			Coordinates coordinates = initialLayout.coordinatesFor(figure.name());
			figure.position(new Point(coordinates._x, coordinates._y));
		}
	}

	public void useLayout(CartesianLayout newLayout) {
		CartesianLayout translatedLayout = translateToOrigin(newLayout);
		_morpher = new GraphMorpher(nodeFigures(), translatedLayout);
	}

	public FloatRectangle sizeGiven(Node<?> node) {
		Rectangle bounds = _nodeFiguresByNode.get(node).figure().getBounds();

		FloatRectangle result = new FloatRectangle();
		result._width = bounds.width;
		result._height = bounds.height;
		return result;
	}

	private static CartesianLayout translateToOrigin(CartesianLayout layout) {
		float smallestX = Float.MAX_VALUE;
		float smallestY = Float.MAX_VALUE;

		for (String nodeName : layout.nodeNames()) {
			Coordinates coordinates = layout.coordinatesFor(nodeName);
			if (coordinates._x < smallestX) smallestX = coordinates._x;
			if (coordinates._y < smallestY) smallestY = coordinates._y;
		}

		float dx = -smallestX + MARGIN_PIXELS;
		float dy = -smallestY + MARGIN_PIXELS;

		CartesianLayout result = new CartesianLayout();
		for (String nodeName : layout.nodeNames()) {
			Coordinates coordinates = layout.coordinatesFor(nodeName);
			result.keep(nodeName, coordinates.translatedBy(dx, dy));
		}
		return result;
	}

	public void animationStep(int size) {
		if (_morpher == null) return;
		_morpher.morphingStep(size);
		if (_morpher.done()) _morpher = null;

		refreshDependencies();

		recomputeGraphFigureSize();
	}

	private void recomputeGraphFigureSize() {
		// Recompute the size of the _graphFigure so scrolling will work
		int maxX = 0;
		int maxY = 0;
		for (NodeFigure<T> figure : _nodeFiguresByNode.values())
		{
			Rectangle figureBounds = figure.figure().getBounds();
			maxX = Math.max(maxX, figureBounds.x + figureBounds.width);
			maxY = Math.max(maxY, figureBounds.y + figureBounds.height);
		}
		_graphFigure.setSize(maxX, maxY);
		_graphFigure.setPreferredSize(_graphFigure.getSize());
	}

}
