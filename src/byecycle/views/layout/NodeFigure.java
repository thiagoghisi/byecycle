//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.

package byecycle.views.layout;

import java.io.InputStream;
import java.util.Random;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import byecycle.dependencygraph.Node;

public class NodeFigure extends GraphElement {

    
private static final int MARGIN = 2;
	private static final float IMPETUS = 300; //TODO Play with this. :)
    private static final float VISCOSITY = 0.95f;  //TODO Play with this. :)
    
    private static final float DEPENDENCY_THRUST = 0.0003f * IMPETUS;

    static final Force ATTRACTION = new Force() {
        public float intensityGiven(float distance) {
            return  -(10 - distance) * 0.000005f * IMPETUS; //TODO Play with this formula. Zero it to see REPULSION acting alone.
            //return 0;
        }
    };

    static final Force REPULSION = new Force() {
        public float intensityGiven(float distance) {
            return -IMPETUS * 0.13f / (distance * distance);  //TODO Play with this formula.
            //return distance < 50 ? -100 : -100 / (distance * distance);
        }
    };

    private static final Random RANDOM = new Random();
    
	private final Label _figure;

    
    NodeFigure(Node node) {
        _node = node;
        _figure = produceFigure();
    }
   
    private Label produceFigure() {
		Label result = new Label(text(_node), imageForNode(_node));
        result.setBorder(new LineBorder());
        result.setBackgroundColor(randomPastelColor());
        result.setOpaque(true);
        return result;
	}

	private static String text(Node node) {
        String result = node.name();
	    if (node.kind().equals("package")) return result;
	    return result.substring(result.lastIndexOf('.') + 1);
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

    Node node() {
        return _node;
    }

    protected void reactTo(GraphElement otherElement) {
    	super.reactTo(otherElement);

    	if (!(otherElement instanceof NodeFigure)) return;
    	NodeFigure other = (NodeFigure)otherElement; 
    	
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
    
	Point position() {
		return new Point(_x, _y);
	}

	void addForceComponents(float x, float y) {
        _forceComponentX += x;
        _forceComponentY += y;
    }

    private static float dampen(float value) {
        return Math.max(Math.min(value * VISCOSITY, 20), -20);
    }

    /** "To yield to physical force." Dictionary.com */
    void give() {
		_x += _forceComponentX;
    	_y += _forceComponentY;

    	_forceComponentX = dampen(_forceComponentX);
		_forceComponentY = dampen(_forceComponentY);

		if (!isMoving()) nudgeNudge();

		stayAround();
	}

	void positionYourselfIn(XYLayout layout) {
		layout.setConstraint(this.figure(), new Rectangle(Math.round(_x), Math.round(_y), -1, -1));
	}

	private boolean isMoving() {
        return _forceComponentX + _forceComponentY > 1;
    }

    private void nudgeNudge() {
        if (RANDOM.nextInt(3000) != 0) return;
        _forceComponentX = (RANDOM.nextFloat() - 0.5f) * 0.1f * IMPETUS;
        _forceComponentY = (RANDOM.nextFloat() - 0.5f) * 0.1f * IMPETUS;
    }

    private void stayAround() {
    	Rectangle availableSpace = figure().getParent().getClientArea();
    	Rectangle me = figure().getBounds();
    	
    	int maxX = availableSpace.width - me.width - MARGIN;
    	int maxY = availableSpace.height - me.height - MARGIN;
    	
        if (_x < MARGIN) _x = MARGIN;
        if (_x >   maxX) _x = maxX;

        if (_y < MARGIN) _y = MARGIN;
        if (_y >   maxY) _y = maxY;
    }

    void position(float x, float y) {
        _x = x;
        _y = y;
    }

	public IFigure figure() {
		return _figure;
	}

}
