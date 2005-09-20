//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.
package byecycle.views.layout.algorithm;

import byecycle.dependencygraph.Node;
import byecycle.views.layout.Coordinates;
import byecycle.views.layout.FloatRectangle;


public class NodeElement extends GraphElement {

	NodeElement(Node node, StressMeter stressMeter) {
		_node = node;
		_stressMeter = stressMeter;

		_aura = createAura();
		positionAura();
	}


	private final Node<?> _node;

	public float _x;
	public float _y;

	private float _pendingForceX;
	private float _pendingForceY;

	private final FloatRectangle _aura;

	private final StressMeter _stressMeter;

	private int _width;

	private int _height;

	private float _velocityX;
	private float _velocityY;


	Node node() {
		return _node;
	}

	String name() {
		return _node.name();
	}

	public Coordinates position() {
		return new Coordinates(_x, _y);
	}

	protected void addForceComponents(float x, float y) {
		_pendingForceX += x;
		_pendingForceY += y;
		_stressMeter.addStress((float)Math.hypot(x, y));
	}

	float pendingForceMagnitude() {
		return (float)Math.hypot(_pendingForceX, _pendingForceY);
	}

	/** "Give: To yield to physical force." Dictionary.com */
	void give(float timeFrame) {
		if (detectPotentialQuivering(_velocityX, _pendingForceX)) {
			_pendingForceX *= 0.5;
		}
		if (detectPotentialQuivering(_velocityY, _pendingForceY)) {
			_pendingForceY *= 0.5;
		}

		_velocityX = (_velocityX + (_pendingForceX * timeFrame)) * 0.8f;
		_velocityY = (_velocityY + (_pendingForceY * timeFrame)) * 0.8f;

		_pendingForceX = 0;
		_pendingForceY = 0;

		float newX = _x + (_velocityX * timeFrame);
		float newY = _y + (_velocityY * timeFrame);
		position(newX, newY);
	}

	private boolean detectPotentialQuivering(float velocity, float pendingForce) {
		boolean changingDirection = (velocity < 0) == (velocity + pendingForce < 0);
		return changingDirection;
	}

	void position(Coordinates coordinates) {
		position(coordinates._x, coordinates._y);
	}

	private void position(float x, float y) {
		_x = x;
		_y = y;
		positionAura();
	}

	public boolean dependsDirectlyOn(NodeElement other) {
		return _node.dependsDirectlyOn(other.node());
	}

	private FloatRectangle createAura() {
		FloatRectangle result = new FloatRectangle();
		result._width = _width + (Constants.AURA_THICKNESS * 2);
		result._height = _height + (Constants.AURA_THICKNESS * 2);
		return result;
	}

	public FloatRectangle aura() {
		return _aura;
	}

	private void positionAura() {
		_aura._x = _x - Constants.AURA_THICKNESS;
		_aura._y = _y - Constants.AURA_THICKNESS;
	}

}
