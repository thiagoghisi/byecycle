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
		_stressMeter.addStress((float) Math.hypot(x, y));
	}

	/** "Give: To yield to physical force." Dictionary.com */
	void give(float impetus) { 		//TODO: Consider implementing inertia.
		_x += _pendingForceX * impetus;
		_y += _pendingForceY * impetus;
		
		_pendingForceX = 0;
		_pendingForceY = 0;
	}

	void position(Coordinates coordinates) {
		_x = coordinates._x;
		_y = coordinates._y;
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


	void translateBy(float dx, float dy) {
		_x += dx;
		_y += dy;
		positionAura();
	}

	private void positionAura() {
		_aura._x = _x - Constants.AURA_THICKNESS;
		_aura._y = _y - Constants.AURA_THICKNESS;
	}

}
