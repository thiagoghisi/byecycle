package byecycle.views.layout.algorithm.relaxer;

import byecycle.dependencygraph.Node;
import byecycle.views.layout.CartesianLayout;
import byecycle.views.layout.NodeSizeProvider;
import byecycle.views.layout.algorithm.LayoutAlgorithm;
import byecycle.views.layout.criteria.NodeElement;


public class InertialRelaxer<T> extends LayoutAlgorithm<T> {

	@SuppressWarnings("unchecked")
	public InertialRelaxer(Iterable<Node<T>> graph, CartesianLayout initialLayout, NodeSizeProvider sizeProvider) {
		super(graph, initialLayout, sizeProvider);
	}

	private boolean _hasConverged;

	public void improveLayoutStep() {
		if (_hasConverged) return;
		
		measureStress();
		
		float timeFrame = minimumTimeNeededToMoveOnePixel();
System.out.println(timeFrame);
		if (timeFrame > Constants.MAXIMUM_TIME_FRAME) {
			_hasConverged = true;
			return;
		}
		give(Math.min(timeFrame, 1));
	}

	protected NodeElement createNodeElement(Node node) {
		return new InertialNode(node, _stressMeter);
	}
	
}
