package byecycle.views.layout.algorithm.random;

import byecycle.dependencygraph.Node;
import byecycle.views.layout.criteria.NodeElement;
import byecycle.views.layout.criteria.StressMeter;


public class AveragingNode extends NodeElement {

	private static final float IMPETUS = 1f;

	private float _time = 0;

	private float _totalX = 0;
	private float _totalY = 0;


	AveragingNode(Node<?> node, StressMeter stressMeter) {
		super(node, stressMeter);
	}

	void takeAveragePosition(float timeFrame) {
		timeFrame = timeFrame * IMPETUS;
		_totalX = _totalX + (_pendingForceX * timeFrame);
		_totalY = _totalY + (_pendingForceY * timeFrame);

		_time += timeFrame; // TODO Optimize: keep a single clock instead of once in every node.
		
		position(_x + (_totalX / _time), _y + (_totalY / _time));
	}

	void startFresh() {
		_totalX = 0;
		_totalY = 0;
		
		_time = 0;
	}

}
