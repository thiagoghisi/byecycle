//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.

package byecycle.views.layout;

import java.io.InputStream;
import java.util.Random;

import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import byecycle.dependencygraph.Node;

public class NodeFigure extends Label {

    protected static final float IMPETUS = 200; //TODO Play with this. :)
    private static final float VISCOSITY = 0.95f;  //TODO Play with this. :)
    
    private static final float DEPENDENCY_THRUST = 0.0003f * IMPETUS;

    private static final Force ATTRACTION = new Force() {
        public float intensityGiven(float distance) {
            return  -(10 - distance) * 0.00001f * IMPETUS; //TODO Play with this formula. Zero it to see REPULSION acting alone.
            //return 0;
        }
    };

    private static final Force REPULSION = new Force() {
        public float intensityGiven(float distance) {
            return -IMPETUS / (distance * distance);  //TODO Play with this formula.
            //return distance < 50 ? -100 : -100 / (distance * distance);
        }
    };

    private static final Random RANDOM = new Random();

    
    public NodeFigure(Node node) {
        super(" " + node.name(), imageForNode(node));
        setBorder(new LineBorder());
        setBackgroundColor(randomPastelColor());
        setOpaque(true);
        
        _node = node;
    }
    
    private static Image imageForNode(Node node) {
		InputStream resource = NodeFigure.class.getResourceAsStream("icons/" + node.kind() + ".gif");
		return null == resource ? null : new Image(Display.getCurrent(), resource);
	}

	private Color randomPastelColor() {
        int r = 210 + RANDOM.nextInt(46);
        int g = 210 + RANDOM.nextInt(46);
        int b = 210 + RANDOM.nextInt(46);
        return new Color(null, r, g, b);
    }

    private final Node _node;

    private float _x;
    private float _y;

    private float _forceComponentX;
    private float _forceComponentY;

    public Node node() {
        return _node;
    }

    public void reactTo(NodeFigure other) {
//        if (other == this) throw new IllegalArgumentException();
        if (other == this) return;

        reactTo(other, REPULSION);

        if (_node.dependsOn(other.node())) {
            reactTo(other, ATTRACTION);
            up();
            other.down();
        }
        
        if (other.node().dependsOn(_node)) {
            reactTo(other, ATTRACTION);
            down();
            other.up();
        }

	}

    private void up() {
        _forceComponentY -= DEPENDENCY_THRUST;
    }

    private void down() {
        _forceComponentY += DEPENDENCY_THRUST;
    }

    
    
    private void reactTo(NodeFigure other, Force force) {
		Point p1 = new Point(_x, _y);
		Point p2 = new Point(other._x, other._y);

		float distance = (float)Math.max(p1.getDistance(p2), 0.001);
		float intensity = force.intensityGiven(distance);

        float xComponent = ((p2.x - p1.x) / distance) * intensity;
        float yComponent = ((p2.y - p1.y) / distance) * intensity;

        addForceComponents(xComponent, yComponent);
        other.addForceComponents(-xComponent, -yComponent);
	}

	private void addForceComponents(float x, float y) {
        _forceComponentX += x;
        _forceComponentY += y;
    }

    private static float dampen(float value) {
        //return Math.max(Math.min(value, 1), -1);
        return value * VISCOSITY;
    }

    public void positionYourselfIn(XYLayout layout) {
    	_x += _forceComponentX;
    	_y += _forceComponentY;

    	_forceComponentX = dampen(_forceComponentX);
		_forceComponentY = dampen(_forceComponentY);
		
		if (!moving()) nudgeNudge();

		keepInBounds();
	    layout.setConstraint(this, new Rectangle(Math.round(_x), Math.round(_y), -1, -1));
    }

    private boolean moving() {
        return _forceComponentX + _forceComponentY > 1;
    }

    private void nudgeNudge() {
        if (RANDOM.nextInt(3000) != 0) return;
        _forceComponentX = (RANDOM.nextFloat() - 0.5f) * 0.1f * IMPETUS;
        _forceComponentY = (RANDOM.nextFloat() - 0.5f) * 0.1f * IMPETUS;
    }

    private void keepInBounds() {
        if (_x <   0) _x =   0;
        if (_x > 300) _x = 300;

        if (_y <   0) _y =   0;
        if (_y > 300) _y = 300;
    }

    public void position(float x, float y) {
        _x = x;
        _y = y;
    }

}
