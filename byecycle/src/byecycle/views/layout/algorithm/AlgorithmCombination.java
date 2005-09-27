package byecycle.views.layout.algorithm;

import byecycle.dependencygraph.Node;
import byecycle.views.layout.CartesianLayout;
import byecycle.views.layout.NodeSizeProvider;
import byecycle.views.layout.algorithm.random.RandomAverage;
import byecycle.views.layout.algorithm.relaxer.InertialRelaxer;
import byecycle.views.layout.criteria.NodeElement;

public class AlgorithmCombination<T> extends LayoutAlgorithm<T> {

	private final InertialRelaxer<T> _relaxer;
	private final RandomAverage<T> _randomAverage;

	public AlgorithmCombination(Iterable<Node<T>> graph, CartesianLayout initialLayout, NodeSizeProvider sizeProvider) {
		super(graph, initialLayout, sizeProvider);
		
		_relaxer = new InertialRelaxer<T>(graph, initialLayout, sizeProvider);
		_randomAverage = new RandomAverage<T>(graph, initialLayout, sizeProvider);
	}

	@Override
	public void improveLayoutStep() {
		_relaxer.improveLayoutStep();
		layout(_relaxer.layoutMemento());
	}

	@Override
	protected NodeElement createNodeElement(Node node) {
		return new NodeElement(node, _stressMeter);
	}

}
