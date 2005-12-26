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
		//layout(_lastStableLayout);
		randomize();
		_stressMeter.applyForcesTo(_averagingNodes, _graphElements);

		float smallestTimeFrame = minimumTimeToMoveOnePixel();
		takeAveragePosition(smallestTimeFrame);
	}
	
//	@Override
//	protected void adaptToFailure() {
//		_randomAmplitude += 0.001;
//		System.out.println("Random amplitude: " + _randomAmplitude);
//	}
//
	@Override
	protected void adaptToSuccess() {
		System.out.println("> > > > > > > > SUCCESS!!!!! " + _stressMeter.reading());
//		_lastStableLayout = layoutMemento();
//		for (AveragingNode node : _averagingNodes)
//			node.startFresh();
//		_randomAmplitude = 3;
	}

	private float minimumTimeToMoveOnePixel() {
		float smallestTimeFrame = 100000;
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
