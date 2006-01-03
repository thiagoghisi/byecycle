package byecycle.views.layout.algorithm.random;

import byecycle.dependencygraph.Node;
import byecycle.views.layout.criteria.NodeElement;
import byecycle.views.layout.criteria.StressMeter;


public class AveragingNode extends NodeElement {

	private float _totalX = 0;
	private float _totalY = 0;


	AveragingNode(Node<?> node, StressMeter stressMeter) {
		super(node, stressMeter);
	}

	void takeAveragePosition(float timeFrame) {
		_totalX = _pendingForceX * timeFrame;
		_totalY = _pendingForceY * timeFrame;
	
		position(_x + _totalX, _y + _totalY);
	}

}
