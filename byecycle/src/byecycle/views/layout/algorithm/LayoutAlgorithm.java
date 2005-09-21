package byecycle.views.layout.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import byecycle.dependencygraph.Node;
import byecycle.views.layout.CartesianLayout;
import byecycle.views.layout.NodeSizeProvider;
import byecycle.views.layout.criteria.DependencyElement;
import byecycle.views.layout.criteria.GraphElement;
import byecycle.views.layout.criteria.NodeElement;
import byecycle.views.layout.criteria.StressMeter;


public abstract class LayoutAlgorithm<T> {

	protected final List<NodeElement> _nodeElements;
	protected final List<DependencyElement> _dependencyElements;
	protected final ArrayList<GraphElement> _graphElements;

	protected final StressMeter _stressMeter = new StressMeter();
	protected float _lowestStressEver;

	protected LayoutAlgorithm(Iterable<Node<T>> graph, CartesianLayout initialLayout, NodeSizeProvider sizeProvider) {
		_dependencyElements = new ArrayList<DependencyElement>();
		_nodeElements = new ArrayList<NodeElement>();
		initGraphElements(graph);
		
	
		_graphElements = new ArrayList<GraphElement>();
		_graphElements.addAll(_nodeElements);
		_graphElements.addAll(_dependencyElements);
		
		layout(initialLayout);
	
//		_lowestStressEver = measureStress();
		_lowestStressEver = Float.MAX_VALUE;
	}

	public abstract boolean improveLayoutStep();
	
	public boolean improveLayoutForAWhile() {
		if (_nodeElements.size() <= 1) return false;
	
		long start = System.nanoTime();
		do {

			improveLayoutStep();
	
		} while (System.nanoTime() - start < 1000000); // One millisecond at least.

		float stress = measureStress();
		System.out.println(stress);
		if (stress < _lowestStressEver) {
			_lowestStressEver = stress;
			return true;
		}
		return false;
	}

	private float measureStress() {
		return _stressMeter.applyForcesTo(_nodeElements, _graphElements);
	}

	public CartesianLayout layoutMemento() {
		CartesianLayout result = new CartesianLayout();
		for (NodeElement node : _nodeElements)
			result.keep(node.name(), node.position());
		return result;
	}

	protected void layout(CartesianLayout layout) {
		for (NodeElement node : _nodeElements)
			node.position(layout.coordinatesFor(node.name()));
	}

	protected void initGraphElements(Iterable<Node<T>> graph) {
		Map<Node, NodeElement> nodeElementsByNode = new HashMap<Node, NodeElement>();
		List<DependencyElement> dependencyElements = new ArrayList<DependencyElement>();
	
		for (Node<T> node : graph) {
			NodeElement dependentElement = produceElementFor(node, nodeElementsByNode);
	
			for (Node<T> provider : node.providers()) {
				NodeElement providerElement = produceElementFor(provider, nodeElementsByNode);
				dependencyElements.add(new DependencyElement(dependentElement, providerElement));
			}
		}
	
		_nodeElements.addAll(nodeElementsByNode.values());
		_dependencyElements.addAll(dependencyElements);
	}

	private NodeElement produceElementFor(Node node, Map<Node, NodeElement> nodeElementsByNode) {
		NodeElement result = nodeElementsByNode.get(node);
		if (result != null) return result;
	
		result = createNodeElement(node);
		nodeElementsByNode.put(node, result);
		return result;
	}

	protected abstract NodeElement createNodeElement(Node<?> node);

}