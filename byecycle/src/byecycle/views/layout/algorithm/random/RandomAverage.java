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

	private final List<AveragingNode> _averagingNodes;

	private static final Random RANDOM = new Random();
	private static final float INITIAL_RANDOM_AMPLITUDE = 10000;
	private float _randomAmplitude = INITIAL_RANDOM_AMPLITUDE;
	private float _impetus = 1f;

	public RandomAverage(Iterable<Node<T>> graph, CartesianLayout initialLayout, NodeSizeProvider sizeProvider) {
		super(graph, initialLayout, sizeProvider);

		_averagingNodes = new ArrayList<AveragingNode>(_nodeElements.size()); // Necessary only to avoid casting all the time.
		for (NodeElement element : _nodeElements)
			_averagingNodes.add((AveragingNode)element);
	}

	
	@Override
	public void improveLayoutStep() {
		CartesianLayout currentLayout = layoutMemento();
		randomize();
		_stressMeter.applyForcesTo(_averagingNodes, _graphElements);
		layout(currentLayout);

//		System.out.println(_impetus);
		float smallestTimeFrame = minimumTimeToMoveOnePixel();
		takeAveragePosition(smallestTimeFrame * _impetus);
		
		checkForNextStableState();
	}
	
	
	@Override
	protected void adaptToFailure() {
//		_impetus = 5;
		_impetus = 10;
	}


	@Override
	protected void adaptToSuccess() {
//		_impetus = 0.5f;
		_impetus = 10;
	}

	
	private int _counter;
	private int _counterLimit = 1;
	private void checkForNextStableState() {
		_counter++;
		if (_counter > _counterLimit) {
			_counter = 0;
			_randomAmplitude *= 0.2;
			System.out.println(" > > > > > > " + _randomAmplitude);
			if (_randomAmplitude < 0.5) {
				_randomAmplitude = INITIAL_RANDOM_AMPLITUDE;
				_counterLimit *= 4;
			}
		}
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

	
	private void randomize() {
		for (AveragingNode node : _averagingNodes)
			node.position(node._x + random(), node._y + random());
	}


	private float random() {
		return (RANDOM.nextFloat() - 0.5f) * _randomAmplitude;
	}

	
	protected NodeElement createNodeElement(Node<?> node) {
		return new AveragingNode(node, _stressMeter);
	}

}
