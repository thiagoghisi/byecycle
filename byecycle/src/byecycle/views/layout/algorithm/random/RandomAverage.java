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
	private float _randomAmplitude = 3;

	private final List<AveragingNode> _averagingNodes;

	private CartesianLayout _lastStableLayout;


	public RandomAverage(Iterable<Node<T>> graph, CartesianLayout initialLayout, NodeSizeProvider sizeProvider) {
		super(graph, initialLayout, sizeProvider);

		_averagingNodes = new ArrayList<AveragingNode>(_nodeElements.size()); // Necessary only to avoid casting all the time.
		for (NodeElement element : _nodeElements)
			_averagingNodes.add((AveragingNode)element);

		_lastStableLayout = initialLayout;
	}

	@Override
	public void improveLayoutStep() {
		layout(_lastStableLayout);
		randomize();
		_stressMeter.applyForcesTo(_averagingNodes, _graphElements);

		float smallestTimeFrame = minimumTimeToMoveOnePixel();
		takeAveragePosition(smallestTimeFrame);
		System.out.print(".");
	}

	@Override
	protected void adaptToFailure() {
		if (!hasStabilizedAtLocalMininum()) return;

		_lastStableLayout = layoutMemento();
		for (AveragingNode node : _averagingNodes)
			node.startFresh();

		_randomAmplitude = 100f;
		System.out.println();
		System.out.println("> > > > > > > > FAILURE!!!! Random: " + _randomAmplitude);
		
	}

	@Override
	protected void adaptToSuccess() {
		System.out.println();
		System.out.println("> > > > > > > > SUCCESS!!!!! " + _stressMeter.reading());

		_lastStableLayout = layoutMemento();
		for (AveragingNode node : _averagingNodes)
			node.startFresh();

		_randomAmplitude = 3;
	}

	private float minimumTimeToMoveOnePixel() {
		float smallestTimeFrame = 0.01f;
		for (AveragingNode node : _averagingNodes)
			if (node.timeNeededToMoveOnePixel() < smallestTimeFrame) smallestTimeFrame = node.timeNeededToMoveOnePixel();
		System.out.println("Time: " + smallestTimeFrame);
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

	private int _stepsWithoutMoving = 0;
	
	private boolean hasStabilizedAtLocalMininum() {
		_stepsWithoutMoving++;
		if (hasMovedThisTime()) _stepsWithoutMoving = 0;
		return _stepsWithoutMoving == 10;
	}

	private boolean hasMovedThisTime() {
		for (AveragingNode node : _averagingNodes)
			if (node.hasMoved()) return true;
		return false;
	}

	private float random() {
		float randomAmplitude = RANDOM.nextFloat() * _randomAmplitude; //Favor long-ranged and short-ranged forces equally???
		return RANDOM.nextFloat() * randomAmplitude; //Note this is same as random * random * amplitude.
	}

	protected NodeElement createNodeElement(Node<?> node) {
		return new AveragingNode(node, _stressMeter);
	}

}
