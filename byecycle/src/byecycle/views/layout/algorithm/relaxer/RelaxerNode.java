package byecycle.views.layout.algorithm.relaxer;

import byecycle.dependencygraph.Node;
import byecycle.views.layout.criteria.NodeElement;
import byecycle.views.layout.criteria.StressMeter;

public class RelaxerNode extends NodeElement {

	private float _previousX;
	private float _previousY;
	
	private float _previousPendingForceX;
	private float _previousPendingForceY;

	RelaxerNode(Node node, StressMeter stressMeter) {
		super(node, stressMeter);
	}

	/** "Give: To yield to physical force." Dictionary.com */
	void give(float impetus) {
		float impetusX = impetus * _pendingForceX;
		float impetusY = impetus * _pendingForceY;
		
		float newX = _x + impetusX;
		float newY = _y + impetusY;
		position(newX, newY);
	}

	public void checkpoint() {
		_previousX = _x;
		_previousY = _y;
	
		_previousPendingForceX = _pendingForceX;
		_previousPendingForceY = _pendingForceY;
	}

	void rollback() {
		 position(_previousX, _previousY);
		 
		 _pendingForceX = _previousPendingForceX;
		 _pendingForceY = _previousPendingForceY;
	}

}
