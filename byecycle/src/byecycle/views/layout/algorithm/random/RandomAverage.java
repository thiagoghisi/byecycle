package byecycle.views.layout.algorithm.random;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import byecycle.dependencygraph.Node;
import byecycle.views.layout.CartesianLayout;
import byecycle.views.layout.NodeSizeProvider;
import byecycle.views.layout.algorithm.LayoutAlgorithm;
import byecycle.views.layout.criteria.NodeElement;


public class RandomAverage<T> extends LayoutAlgorithm<T> {

	private static final Random RANDOM = new Random();
	private static final float AVERAGE_RANDOM_AMPLITUDE = 300; //TODO Consider varying the random amplitude based on stress. More stress= more amplitude (go for global minimum) Less stress=less amplitude (go for local minimum).

	private final List<AveragingNode> _averagingNodes;

	private float _randomAmplitude;

	public RandomAverage(Iterable<Node<T>> graph, CartesianLayout initialLayout, NodeSizeProvider sizeProvider) {
		super(graph, initialLayout, sizeProvider);
		_averagingNodes = new ArrayList<AveragingNode>(_nodeElements.size());
		for (NodeElement element : _nodeElements)
			_averagingNodes.add((AveragingNode)element);
	}

	@Override
	public boolean improveLayoutStep() {
		randomize();
		_stressMeter.applyForcesTo(_averagingNodes, _graphElements);

		float smallestTimeFrame = minimumTimeToMoveOnePixel();
		takeAveragePosition(smallestTimeFrame);

		return false;
	}

	private float minimumTimeToMoveOnePixel() {
		float smallestTimeFrame = Float.MAX_VALUE;
		for (AveragingNode node : _averagingNodes)
			if (node.timeNeededToMoveOnePixel() < smallestTimeFrame) smallestTimeFrame = node.timeNeededToMoveOnePixel();
		return smallestTimeFrame;
	}

	private void takeAveragePosition(float timeFrame) {
		for (AveragingNode node : _averagingNodes)
			node.takeAveragePosition(timeFrame);
	}

	private void randomize() {
		_randomAmplitude = ((float)RANDOM.nextGaussian()) * AVERAGE_RANDOM_AMPLITUDE; //This favours short-ranged and long-ranged forces equally;
		for (AveragingNode node : _averagingNodes)
			node.position((node._x) + random(), (node._y) + random());
	}


	private float random() {
		return ((float)RANDOM.nextGaussian()) * _randomAmplitude;
	}

	protected NodeElement createNodeElement(Node<?> node) {
		return new AveragingNode(node, _stressMeter);
	}

}
