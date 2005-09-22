package byecycle.views.layout.algorithm.random;

import byecycle.dependencygraph.Node;
import byecycle.views.layout.criteria.NodeElement;
import byecycle.views.layout.criteria.StressMeter;


public class AveragingNode extends NodeElement {

	private float _time = 1; // Doesn't matter in the long run. Avoids divide by zero in the beginning.

	private float _totalX;
	private float _totalY;


	AveragingNode(Node<?> node, StressMeter stressMeter) {
		super(node, stressMeter);
	}

	void giveInitialLayoutSomeCredit() {
	// _time = 100; //100 iterations approx.
	// _totalX = _x * _time;
	// _totalY = _y * _time;
	}

	float timeNeededToMoveOnePixel() {
		return 1 / Math.max(Math.abs(_pendingForceX), Math.abs(_pendingForceY));
	}

	void takeAveragePosition(float timeFrame) {
		_totalX = _totalX + (_pendingForceX * timeFrame * 300); // TODO Normalize this impetus with the results produces by the NudgeNudge algorithm.
		_totalY = _totalY + (_pendingForceY * timeFrame * 300);

		_time += timeFrame; // TODO Optimize: keep a single clock instead of once in every node.
		float sqrt = (float)Math.sqrt(_time);
		position(_totalX / sqrt, _totalY / sqrt); // Divided by sqrt of time to avoid dispersion due to brownian motion.
	}

}
