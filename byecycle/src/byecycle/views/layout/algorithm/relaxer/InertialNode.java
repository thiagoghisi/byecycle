package byecycle.views.layout.algorithm.relaxer;

import byecycle.dependencygraph.Node;
import byecycle.views.layout.criteria.NodeElement;
import byecycle.views.layout.criteria.StressMeter;

class InertialNode extends NodeElement {

	private float _velocityX;
	private float _velocityY;

	public InertialNode(Node node, StressMeter stressMeter) {
		super(node, stressMeter);
	}

	@Override
	public void give(float timeFrame) {
		super.give(timeFrame);

		//if (_pendingForceX < 0 == _velocityX < 0) _pendingForceX *= 0.95f;
		//if (_pendingForceY < 0 == _velocityY < 0) _pendingForceY *= 0.95f;

		_velocityX += (_pendingForceX * timeFrame * 0.97f);
		_velocityY += (_pendingForceY * timeFrame * 0.97f);
		
		_velocityX *= 0.97f;
		_velocityY *= 0.97f;
	}

	@Override
	protected float velocityX() {
		return _velocityX + (_pendingForceX * 0.97f);
	}

	@Override
	protected float velocityY() {
		return _velocityY + (_pendingForceY * 0.97f);
	}

	@Override
	public float timeNeededToMoveOnePixel() {
		float timeToMove = super.timeNeededToMoveOnePixel();
		float timeToAccelerate = 1 / Math.max(Math.abs(_pendingForceX), Math.abs(_pendingForceX));
		return Math.min(timeToMove, timeToAccelerate);
	}

	
	
}
