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

	private final List<AveragingNode> _averagingNodes;

	private float _randomAmplitude = 0;


	public RandomAverage(Iterable<Node<T>> graph, CartesianLayout initialLayout, NodeSizeProvider sizeProvider) {
		super(graph, initialLayout, sizeProvider);
		_averagingNodes = new ArrayList<AveragingNode>(_nodeElements.size()); // Necessary only to avoid casting all the time.
		for (NodeElement element : _nodeElements)
			_averagingNodes.add((AveragingNode)element);

		giveInitialLayoutSomeCredit();
	}

	private void giveInitialLayoutSomeCredit() {
		for (AveragingNode node : _averagingNodes)
			node.giveInitialLayoutSomeCredit(); // FIXME Make this work. :) Consider saving AveragingNode state in the memento.
	}

	@Override
	public void improveLayoutStep() {
		randomize();
		_stressMeter.applyForcesTo(_averagingNodes, _graphElements);

		float smallestTimeFrame = minimumTimeToMoveOnePixel();
		takeAveragePosition(smallestTimeFrame);

		float stress = _stressMeter.applyForcesTo(_averagingNodes, _graphElements);
		adjustRandomAmplitudeGiven(stress);
	}

	private void adjustRandomAmplitudeGiven(float stress) {
		// _randomAmplitude = ((float)RANDOM.nextGaussian()) * AVERAGE_RANDOM_AMPLITUDE; // This favours short-ranged and long-ranged forces equally;

		// if (stress < _lowestStressEver) _randomAmplitude /= 1.001;

		_randomAmplitude *= 0.99;

		if (_randomAmplitude <= 0.1) {
			_randomAmplitude = 1000;
		}

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
		for (AveragingNode node : _averagingNodes)
			node.position(node._x + random(), node._y + random());
	}

	private float random() {
		return RANDOM.nextFloat() * _randomAmplitude;
	}

	protected NodeElement createNodeElement(Node<?> node) {
		return new AveragingNode(node, _stressMeter);
	}

}
