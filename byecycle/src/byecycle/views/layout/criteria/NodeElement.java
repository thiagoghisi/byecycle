//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.
package byecycle.views.layout.criteria;

import byecycle.dependencygraph.Node;
import byecycle.views.layout.Coordinates;
import byecycle.views.layout.FloatRectangle;


public class NodeElement extends GraphElement {

	public NodeElement(Node node, StressMeter stressMeter) {
		_node = node;
		_stressMeter = stressMeter;

		_aura = createAura();
		positionAura();
	}


	private final Node<?> _node;

	public float _x;
	public float _y;

	protected float _pendingForceX;
	protected float _pendingForceY;

	private final StressMeter _stressMeter;

	private int _width;
	private int _height;

	private final FloatRectangle _aura;


	public Node node() {
		return _node;
	}

	public String name() {
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

	public void position(Coordinates coordinates) {
		position(coordinates._x, coordinates._y);
	}

	public void position(float x, float y) {
		_x = x;
		_y = y;

		positionAura();
	}

	public void translateBy(float dx, float dy) {
		position(_x + dx, _y + dy);
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

	public void clearPendingForces() {
		_pendingForceX = 0;
		_pendingForceY = 0;
	}

}
