//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.

package byecycle.views.layout;

import java.util.Random;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import byecycle.JavaType;
import byecycle.dependencygraph.Node;

public class NodeFigure<T> extends GraphElement {

    private static final int MARGIN_PIXELS = 3;

    private static final float VISCOSITY = 0.85f; // TODO Play with this. :)

    private static final float IMPETUS = 900;

    private static final Random RANDOM = new Random();

    NodeFigure(Node<T> node, StressMeter stressMeter, XYLayout layoutManager) {
        _node = node;
        _stressMeter = stressMeter;
        _layoutManager = layoutManager;
    }

    private Label label(String text, Image icon) {
		return icon == null
			? new Label(" " + text, icon)
			: new Label(      text, icon);
    }

    IFigure produceFigure() {
        IFigure result;
        String name = name();
        Color color = getPastelColor(_node);
        if (name.length() < 20) {
            result = label(name, imageForNode(_node));
        } else {
            result = new CompartmentFigure(color);
            int cut = (name.length() / 2) - 1;
            result.add(label(name.substring(0, cut), imageForNode(_node)));
            result.add(label(name.substring(cut), null));
        }

        result.setBorder(new LineBorder());
        result.setBackgroundColor(color);
        result.setOpaque(true);
        return result;
    }

	String name() {
		String result = _node.name();
		if (_node.kind2() == JavaType.PACKAGE) return result;
		return result.substring(result.lastIndexOf('.') + 1);
	}

    static class CompartmentFigure extends Figure {
        public CompartmentFigure(Color color) {
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


    private Color getPastelColor(Node<?> node) { // TODO: Rename
    	Random random = new Random(node.name().hashCode() * 713);
        int r = 210 + random.nextInt(46);
        int g = 210 + random.nextInt(46);
        int b = 210 + random.nextInt(46);
        return new Color(null, r, g, b);
    }


    private final Node<T> _node;

    private int _currentX;

    private int _currentY;

    private int _targetX;

    private int _targetY;

    private float _candidateX;

    private float _candidateY;

    private float _forceComponentX;

    private float _forceComponentY;

    private Rectangle _aura;

    private final StressMeter _stressMeter;

	private final XYLayout _layoutManager;

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

    public Point candidatePosition() {
        return new Point(_candidateX, _candidateY);
    }

    public void addForceComponents(float x, float y) {
        _forceComponentX += x * IMPETUS;
        _forceComponentY += y * IMPETUS;
        _stressMeter.addStress((float) Math.sqrt((x * x) + (y * y)));
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
        respectMargin(); //TODO: This can be removed once the southEastWind is removed and the graph is free in space (See related TO DO comment in this file).

        _forceComponentX = dampen(_forceComponentX); //TODO: Keeping these forces from one step to the next is a weird poor man's form of inertia. Experiment with proper inertia or removing inertia altogether (removing inertia will make converging to local minimum faster, I believe). Klaus.
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

    void southEastWind() {
    	addForceComponents( (-_candidateX * 0.0000002f), (-_candidateY * 0.0000002f));
    }

	private void respectMargin() {
		if (_candidateX < MARGIN_PIXELS) _candidateX = MARGIN_PIXELS;
        if (_candidateY < MARGIN_PIXELS) _candidateY = MARGIN_PIXELS;
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
        int dX = Math.max(Math.min(_targetX - _currentX, 3), -3);
        int dY = Math.max(Math.min(_targetY - _currentY, 3), -3);

        _currentX += dX;
        _currentY += dY;

		layout();
    }

	private void layout() {
		_layoutManager.setConstraint(this.figure(), new Rectangle(_currentX, _currentY, -1, -1));
	}

    boolean onTarget() {
        return _currentX == _targetX && _currentY == _targetY;
    }

    public boolean dependsOn(NodeFigure other) {
        return _node.dependsDirectlyOn(other.node());
    }

    public Rectangle aura() {
        int auraThickness = 10;
        Point candidatePosition = candidatePosition();

        if (_aura == null) {
			_aura = new Rectangle(candidatePosition, figure().getBounds().getSize());
            _aura.x -= auraThickness;
            _aura.y -= auraThickness;
            _aura.width += auraThickness;
            _aura.height += auraThickness;
		}
		else {
            _aura.x = candidatePosition.x - auraThickness;
            _aura.y = candidatePosition.y - auraThickness;
        }
        return _aura;

    }

	Point targetPosition() {
		return new Point(_targetX, _targetY);
	}

}
