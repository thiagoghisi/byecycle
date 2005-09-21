package byecycle.views.layout.algorithm.relaxer;

import java.util.Random;

import byecycle.dependencygraph.Node;
import byecycle.views.layout.CartesianLayout;
import byecycle.views.layout.NodeSizeProvider;
import byecycle.views.layout.algorithm.LayoutAlgorithm;
import byecycle.views.layout.criteria.NodeElement;


public class NudgeNudge<T> extends LayoutAlgorithm<T> {

	private static final Random RANDOM = new Random();

	private Relaxer _relaxer;


	public NudgeNudge(Iterable<Node<T>> graph, CartesianLayout initialLayout, NodeSizeProvider sizeProvider) {
		super(graph, initialLayout, sizeProvider);
	}

	Relaxer relaxer() {
		if (_relaxer == null) _relaxer = newRelaxer();
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
		for (NodeElement node : _nodeElements)
			node.translateBy(nudge(), nudge());
	}

	private void nudgeNudge() {
		float nudgeX = nudge();
		float nudgeY = nudge();

		NodeElement node1 = randomNode();
		NodeElement node2 = anotherRandomNode(node1);

		node1.translateBy(nudgeX, nudgeY);
		node2.translateBy(-nudgeX, -nudgeY);
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

	public boolean improveLayoutStep() {
		relaxer().step();

		if (_relaxer.hasConverged()) return true; // FIXME Use StressMeter

		if (_stressMeter.reading() < _lowestStressEver) {
			_lowestStressEver = _stressMeter.reading();
			return true;
		}
		return false;
	}

	protected NodeElement createNodeElement(Node node) {
		return new RelaxerNode(node, _stressMeter);
	}

}
