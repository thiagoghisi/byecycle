package byecycle.views.layout.algorithm.random;

import byecycle.dependencygraph.Node;
import byecycle.views.layout.criteria.NodeElement;
import byecycle.views.layout.criteria.StressMeter;


public class AveragingNode extends NodeElement {

	private static final float AMPLITUDE = 10000;

	AveragingNode(Node<?> node, StressMeter stressMeter) {
		super(node, stressMeter);
	}
	
	private float _totalX = 0;
	private float _totalY = 0;

	void takeAveragePosition(float ellapsedTime, float nextTimeFrame) {
		_totalX += _pendingForceX * nextTimeFrame;
		_totalY += _pendingForceY * nextTimeFrame;
		
		//position(_x + _totalX, _y + _totalY);
		position(_totalX  * AMPLITUDE / ellapsedTime, _totalY  * AMPLITUDE / ellapsedTime);
		//position(_totalX, _totalY);
	}

}
