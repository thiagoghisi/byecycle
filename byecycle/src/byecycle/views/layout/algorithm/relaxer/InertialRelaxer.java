package byecycle.views.layout.algorithm.relaxer;

import byecycle.dependencygraph.Node;
import byecycle.views.layout.CartesianLayout;
import byecycle.views.layout.NodeSizeProvider;
import byecycle.views.layout.algorithm.LayoutAlgorithm;
import byecycle.views.layout.criteria.NodeElement;


public class InertialRelaxer<T> extends LayoutAlgorithm<T> {

	private float _timeFrame = 10;

	@SuppressWarnings("unchecked")
	public InertialRelaxer(Iterable<Node<T>> graph, CartesianLayout initialLayout, NodeSizeProvider sizeProvider) {
		super(graph, initialLayout, sizeProvider);
	}

	public void improveLayoutStep() {
		if (hasConverged()) return;
		
System.out.println(_timeFrame);
		give(minimumTimeNeededToMoveOnePixel() * _timeFrame);
	}

	@Override
	protected void adaptToFailure() {
		_timeFrame *= 0.996f;
	}

	@Override
	protected void adaptToSuccess() {
		_timeFrame = Math.min(_timeFrame * 1.1f, 100); 
	}

	private boolean hasConverged() {
		return _timeFrame < Constants.MINIMUM_TIME_FRAME;
	}

	protected NodeElement createNodeElement(Node node) {
		return new InertialNode(node, _stressMeter);
	}
	
}
