package byecycle.views.layout.algorithm.random;

import byecycle.dependencygraph.Node;
import byecycle.views.layout.criteria.NodeElement;
import byecycle.views.layout.criteria.StressMeter;


public class AveragingNode extends NodeElement {

	private static final float IMPETUS = 0.05f;  //1f

	private float _time = 0;

	private float _totalX = 0;
	private float _totalY = 0;

	private float _previousX;
	private float _previousY;


	AveragingNode(Node<?> node, StressMeter stressMeter) {
		super(node, stressMeter);
	}

	void takeAveragePosition(float timeFrame) {
		//timeFrame = timeFrame * IMPETUS;
		_totalX = _totalX + (_pendingForceX * timeFrame * IMPETUS);
		_totalY = _totalY + (_pendingForceY * timeFrame * IMPETUS);

		_time += timeFrame; // TODO Optimize: keep a single clock instead of once in every node.

		rememberPreviousPosition();
		position(_x + (_totalX / _time), _y + (_totalY / _time));
	}

	private void rememberPreviousPosition() {
		_previousX = _x;
		_previousY = _y;
	}

	boolean hasMoved() {
		if (Math.abs(_x - _previousX) > 0.5f) return true;
		if (Math.abs(_y - _previousY) > 0.5f) return true;
		return false;
	}

	
	void startFresh() {
		_totalX = 0;
		_totalY = 0;
		
		_time = 0;
	}

}
