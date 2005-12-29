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
	private float _randomAmplitude = 0;

	private final List<AveragingNode> _averagingNodes;


	public RandomAverage(Iterable<Node<T>> graph, CartesianLayout initialLayout, NodeSizeProvider sizeProvider) {
		super(graph, initialLayout, sizeProvider);

		_averagingNodes = new ArrayList<AveragingNode>(_nodeElements.size()); // Necessary only to avoid casting all the time.
		for (NodeElement element : _nodeElements)
			_averagingNodes.add((AveragingNode)element);
	}

	
	@Override
	public void improveLayoutStep() {
		randomize();
		_stressMeter.applyForcesTo(_averagingNodes, _graphElements);

		float smallestTimeFrame = minimumTimeToMoveOnePixel();
		takeAveragePosition(smallestTimeFrame);
	}

	
	private float minimumTimeToMoveOnePixel() {
		float smallestTimeFrame = 100f;
		for (AveragingNode node : _averagingNodes)
			if (node.timeNeededToMoveOnePixel() < smallestTimeFrame) smallestTimeFrame = node.timeNeededToMoveOnePixel();
		return smallestTimeFrame;
	}

	
	private void takeAveragePosition(float timeFrame) {
		for (AveragingNode node : _averagingNodes)
			node.takeAveragePosition(timeFrame);
	}

	float _dampener = 10;
	private void randomize() {
		if (_randomAmplitude < 1) {
			_randomAmplitude = 1000;
			_dampener = _dampener *= 0.5;
			System.out.println(_dampener);
			startFresh();
		}
		_randomAmplitude -= _dampener;
		for (AveragingNode node : _averagingNodes)
			node.position(node._x + random(), node._y + random());
	}


	private void startFresh() {
		for (AveragingNode node : _averagingNodes)
			node.startFresh();
	}


	private float random() {
		return RANDOM.nextFloat() * _randomAmplitude;
	}

	
	protected NodeElement createNodeElement(Node<?> node) {
		return new AveragingNode(node, _stressMeter);
	}

}
