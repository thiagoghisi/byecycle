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

    
	private static final int MARGIN_PIXELS = 2;
	private static final float VISCOSITY = 0.85f;  //TODO Play with this. :)
    
    private static final float DEPENDENCY_THRUST = 0.0003f * IMPETUS;
	public static final Force STRONG_REPULSION = new Force() {
        public float intensityGiven(float distance) {
            return -IMPETUS * 9.3f / (float)(Math.pow(distance, 2.7));  //TODO Play with this formula.
            //return distance < 50 ? -100 : -100 / (distance * distance);
        }
    };

    static final Force ATTRACTION = new Force() {
        public float intensityGiven(float distance) {
            return  -(10 - distance) * 0.000002f * IMPETUS; //TODO Play with this formula. Zero it to see REPULSION acting alone.
            //return 0;
        }
    };

    private static final Random RANDOM = new Random();
    
	NodeFigure(Node node, StressMeter stressMeter) {
        _node = node;
        _stressMeter = stressMeter;
    }
   
    IFigure produceFigure() {
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

	private float _currentX;
	        float _currentY;

	private float _targetX;
	private float _targetY;

	private float _candidateX;
    private float _candidateY;
    private float _forceComponentX;
    private float _forceComponentY;
    
	private final StressMeter _stressMeter;

	
    Node node() {
        return _node;
    }

    protected void reactTo(GraphElement otherElement) {
    	super.reactTo(otherElement);

    	if (!(otherElement instanceof NodeFigure)) return;
    	NodeFigure other = (NodeFigure)otherElement; 
    	
        if (_node.dependsOn(other.node())) {
            reactToProvider(other);
        }
        
        if (other.node().dependsOn(_node)) {
            other.reactToProvider(this);
        }
	    if (collidesWith(other)) reactTo(other, NodeFigure.STRONG_REPULSION); 

	}

    private boolean collidesWith(NodeFigure other) {
		Point c1 = candidatePosition();
		Point c2 = other.candidatePosition(); 
	
		Rectangle r1 = new Rectangle(c1, figure().getBounds().getSize());
		Rectangle r2 = new Rectangle(c2, other.figure().getBounds().getSize());
    	
    	return r1.intersects(r2); //TODO: Use size of r1.getIntersection(r2) to determine the force.
    }

	private void reactToProvider(NodeFigure other) {
		reactTo(other, ATTRACTION);

		float dY = Math.abs(other._candidateY - _candidateY);  
		boolean inverted = other._candidateY < _candidateY;
		
		float thrust = DEPENDENCY_THRUST * (inverted
			? 1 + (dY / 20)
			: 10 / (10 + dY)
		);
		up(thrust);
		other.down(thrust);
	}

	private void up(float thrust) {
        addForceComponents(0, -thrust);
    }

    private void down(float thrust) {
        addForceComponents(0, thrust);
    }
    
	Point candidatePosition() {
		return new Point(_candidateX, _candidateY);
	}

	void addForceComponents(float x, float y) {
        _forceComponentX += x;
        _forceComponentY += y;
        _stressMeter.accumulateStress(x);
        _stressMeter.accumulateStress(y);
    }

    private static float dampen(float value) {
        return Math.max(Math.min(value * VISCOSITY, 20), -20);
    }

    /** "To yield to physical force." Dictionary.com */
    void give() {
		_candidateX += _forceComponentX;
    	_candidateY += _forceComponentY;

    	_forceComponentX = dampen(_forceComponentX);
		_forceComponentY = dampen(_forceComponentY);

	//	if (!isMoving()) nudgeNudge();
		
		stayAround();
	}

	boolean isMoving() {
        return _forceComponentX > 0.1 || _forceComponentY > 0.1;
    }

    void nudgeNudge() {
        addForceComponents(nudge(), nudge());
    }

    private float nudge() {
		return (RANDOM.nextFloat() - 0.5f) * 0.1f * IMPETUS;
	}

	private void stayAround() {
    	addForceComponents( (-_candidateX * 0.0000002f) * IMPETUS, (-_candidateY * 0.0000002f) * IMPETUS);
    	
    	Rectangle availableSpace = figure().getParent().getClientArea();
    	Rectangle me = figure().getBounds();
    	
    	int maxX = availableSpace.width - me.width - MARGIN_PIXELS;
    	int maxY = availableSpace.height - me.height - MARGIN_PIXELS;
    	
        if (_candidateX < MARGIN_PIXELS) _candidateX = MARGIN_PIXELS;
        if (_candidateX >   maxX) _candidateX = maxX;

        if (_candidateY < MARGIN_PIXELS) _candidateY = MARGIN_PIXELS;
        if (_candidateY >   maxY) _candidateY = maxY;
    }

    void position(float x, float y) {
        _currentX = x;
        _currentY = y;

        _targetX = x;
		_targetY = y;

        _candidateX = x;
        _candidateY = y;
    }

	void lockOnTarget() {
		_targetX = _candidateX;
		_targetY = _candidateY;
	}

	void pursueTarget(XYLayout layout) {
		float dX = Math.max(Math.min(_targetX - _currentX, 3), -3);
		float dY = Math.max(Math.min(_targetY - _currentY, 3), -3);
		
		_currentX += dX;
		_currentY += dY;
		
		layout.setConstraint(this.figure(), new Rectangle(Math.round(_currentX), Math.round(_currentY), -1, -1));
	}

	boolean onTarget() {
		return _currentX ==_targetX && _currentY == _targetY;
	}

}
