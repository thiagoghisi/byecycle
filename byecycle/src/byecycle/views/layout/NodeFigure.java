//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.
package byecycle.views.layout;

import java.util.Random;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import byecycle.JavaType;
import byecycle.dependencygraph.Node;

public class NodeFigure<T> extends GraphElement {

	private static final float VISCOSITY = 0.85f; // TODO Play with this. :)
	private static final float IMPETUS = 900;

	private static final Random RANDOM = new Random();
	private static final int AURA_THICKNESS = 10;

	NodeFigure(Node<T> node, StressMeter stressMeter) {
		_node = node;
		_stressMeter = stressMeter;
	}

	private Label label(String text, Image icon) {
		return icon == null
			? new Label(" " + text, icon)
			: new Label(text, icon);
	}

	IFigure produceFigure() {
		IFigure result;

		String name = name();
		if (name.length() < 20) {
			result = label(name, imageForNode(_node));
		} else {
			result = new CompartmentFigure();
			int cut = (name.length() / 2) - 1;
			result.add(label(name.substring(0, cut), imageForNode(_node)));
			result.add(label(name.substring(cut), null));
		}
		
		result.setBorder(new LineBorder());
		result.setBackgroundColor(pastelColorDeterminedBy(name));
		result.setOpaque(true);
		
		return result;
	}

	String name() {
		String result = _node.name();
		if (_node.kind2() == JavaType.PACKAGE)
			return result;
		return result.substring(result.lastIndexOf('.') + 1);
	}

	static class CompartmentFigure extends Figure {
		public CompartmentFigure() {
			ToolbarLayout layout = new ToolbarLayout();
			layout.setMinorAlignment(ToolbarLayout.ALIGN_TOPLEFT);
			layout.setStretchMinorAxis(false);
			setLayoutManager(layout);
		}
	}

	private static Image imageForNode(Node<?> node) {
		String resourcename = node.kind2().getResourceName();
		return JavaUI.getSharedImages().getImage(resourcename);
	}

	private static Color pastelColorDeterminedBy(String string) {
		Random random = new Random(string.hashCode() * 713);
		int r = 210 + random.nextInt(46);
		int g = 210 + random.nextInt(46);
		int b = 210 + random.nextInt(46);
		return new Color(null, r, g, b);
	}

	private final Node<T> _node;

	private float _currentX;

	private float _currentY;

	private float _targetX;

	private float _targetY;

	float _candidateX;

	float _candidateY;

	private float _forceComponentX;

	private float _forceComponentY;

	private FloatRectangle _aura;

	private final StressMeter _stressMeter;

	Node node() {
		return _node;
	}

	public float candidateY() {
		return _candidateY;
	}

	public void up(float thrust) {
		addForceComponents(0, -thrust);
	}

	public void down(float thrust) {
		addForceComponents(0, thrust);
	}

	public Coordinates candidatePosition() {
		return new Coordinates(_candidateX, _candidateY);
	}

	public void addForceComponents(float x, float y) {
		_forceComponentX += x * IMPETUS;
		_forceComponentY += y * IMPETUS;
		_stressMeter.addStress((float) Math.hypot(x, y));
	}

	private static float dampen(float value) {
		return Math.max(Math.min(value * VISCOSITY, 20), -20);
	}

	/** "Give: To yield to physical force." Dictionary.com */
	boolean give() {
		float previousX = _candidateX;
		float previousY = _candidateY;
		
		_candidateX += _forceComponentX;
		_candidateY += _forceComponentY;
		
		_forceComponentX = dampen(_forceComponentX); // TODO: Keeping these forces from one step to the next is a weird poor man's form of inertia. Experiment with proper inertia or removing inertia altogether (removing inertia will make converging to local minimum faster, I believe). Klaus.
		_forceComponentY = dampen(_forceComponentY);

		if (Math.abs(_candidateX - previousX) > 0.02) return true;
		if (Math.abs(_candidateY - previousY) > 0.02) return true;
		return false;
	}

	void nudgeNudge() {
		addForceComponents(nudge(), nudge());
	}

	private float nudge() {
		return (RANDOM.nextFloat() - 0.5f) * 0.1f;
	}

	void position(Point point) {
		_currentX = point.x;
		_currentY = point.y;
		
		_targetX = point.x;
		_targetY = point.y;
		
		_candidateX = point.x;
		_candidateY = point.y;
		
		layout();
	}

	void lockOnTarget() {
		_targetX = Math.round(_candidateX);
		_targetY = Math.round(_candidateY);
	}

	void pursueTarget() {
		float dX = Math.max(Math.min(_targetX - _currentX, 3), -3);
		float dY = Math.max(Math.min(_targetY - _currentY, 3), -3);
		
		_currentX += dX;
		_currentY += dY;
		
		layout();
	}

	private void layout() {
		this.figure().setLocation(new Point(_currentX, _currentY));
	}

	boolean onTarget() {
		return _currentX == _targetX && _currentY == _targetY;
	}

	public boolean dependsOn(NodeFigure other) {
		return _node.dependsDirectlyOn(other.node());
	}

	public FloatRectangle aura() {
		Coordinates candidatePosition = candidatePosition();

		if (_aura == null) {
			_aura = new FloatRectangle();
			_aura._width = figure().getBounds().width + AURA_THICKNESS;
			_aura._height = figure().getBounds().height + AURA_THICKNESS;
		}

		_aura._x = candidatePosition._x - AURA_THICKNESS;
		_aura._y = candidatePosition._y - AURA_THICKNESS;

		return _aura;
	}

	Point targetPosition() {
		return new Point(_targetX, _targetY);
	}

	void translateBy(float dx, float dy) {
		_candidateX += dx;
		_candidateY += dy;
	}
}
