package byecycle.views.layout.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import byecycle.dependencygraph.Node;
import byecycle.views.layout.CartesianLayout;
import byecycle.views.layout.Coordinates;
import byecycle.views.layout.NodeSizeProvider;


public class LayoutAlgorithm<T> {

	private static final Random RANDOM = new Random();

	private final List<NodeElement> _nodeElements;
	private final List<DependencyElement> _dependencyElements;

	private final StressMeter _stressMeter = new StressMeter();
	private float _lowestStressEver;

	private Relaxer _relaxer;


	public LayoutAlgorithm(Iterable<Node<T>> graph, CartesianLayout initialLayout, NodeSizeProvider sizeProvider) {
		_dependencyElements = new ArrayList<DependencyElement>();
		_nodeElements = new ArrayList<NodeElement>();
		initGraphElements(graph);

		layout(initialLayout);

		measureInitialStress();
	}

	private void layout(CartesianLayout layout) {
		for (NodeElement node : _nodeElements)
			node.position(layout.coordinatesFor(node.name()));
	}

	private void initGraphElements(Iterable<Node<T>> graph) {
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
		if (result != null)
			return result;

		result = new NodeElement(node, _stressMeter);
		nodeElementsByNode.put(node, result);
		return result;
	}

	private void measureInitialStress() {
		relaxer().step();
		_lowestStressEver = _stressMeter._reading;
	}

	private Relaxer relaxer() {
		if (_relaxer == null)
			_relaxer = newRelaxer();
		if (_relaxer.hasConverged()) {
			prepareToSeekNonLocalMinimum();
			_relaxer = newRelaxer();
		}
		return _relaxer;
	}

	private Relaxer newRelaxer() {
		return new Relaxer(_nodeElements, _dependencyElements, _stressMeter);
	}

	private void prepareToSeekNonLocalMinimum() {
		System.out.println("NUDGE ==============================================");
		System.out.println("NUDGE ==============================================");
		System.out.println("NUDGE ==============================================");
		System.out.println("NUDGE ==============================================");
		System.out.println("NUDGE ==============================================");
		System.out.println("NUDGE ==============================================");
		nudgeNudge();
	}

	private void nudgeNudge() {
		float nudgeX = nudge();
		float nudgeY = nudge();

		NodeElement node1 = randomNode();
		NodeElement node2 = anotherRandomNode(node1);

		node1.position(new Coordinates(nudgeX, nudgeY));
		node2.position(new Coordinates(-nudgeX, -nudgeY));
	}

	private float nudge() {
		return (RANDOM.nextFloat() - 0.5f) * Constants.NUDGE_INTENSITY;
	}

	private NodeElement randomNode() {
		int randomIndex = RANDOM.nextInt(_nodeElements.size());
		return _nodeElements.get(randomIndex);
	}

	private NodeElement anotherRandomNode(NodeElement unwanted) {
		assert _nodeElements.size() > 1;
		NodeElement result;
		do {
			result = randomNode();
		} while (result == unwanted);
		return result;
	}

	public boolean improveLayoutForAWhile() {
		if (_nodeElements.size() <= 1)
			return false;

		long start = System.nanoTime();
		do {

			if (improveLayoutStep())
				return true;

		} while (System.nanoTime() - start < 1000000); // One millisecond at least.
		return false;
	}

	public boolean improveLayoutStep() {
		relaxer().step();

		if (_relaxer.hasConverged())
			return true; // FIXME Use StressMeter

		if (_stressMeter._reading < _lowestStressEver) {
			_lowestStressEver = _stressMeter._reading;
			return true;
		}
		return false;
	}

	public CartesianLayout layoutMemento() {
		CartesianLayout result = new CartesianLayout();
		for (NodeElement node : _nodeElements)
			result.keep(node.name(), node.position());
		return result;
	}

}
