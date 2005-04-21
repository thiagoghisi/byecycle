//Copyright (C) 2005 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.

package byecycle.views.layout;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;


abstract class GraphElement {

	protected static final float IMPETUS = 900;

	protected static final Force WEAK_REPULSION = new Force() {
        public float intensityGiven(float distance) {
            return -IMPETUS * 0.93f / (float)(Math.pow(distance, 2.7));  //TODO Play with this formula.
            //return distance < 50 ? -100 : -100 / (distance * distance);
        }
    };

    private IFigure _figure;

	abstract Point candidatePosition();

	abstract void addForceComponents(float f, float g);

	protected void reactTo(GraphElement other) {
	     if (other == this) throw new IllegalArgumentException();
	     
	     reactTo(other, WEAK_REPULSION);
	}

	protected void reactTo(GraphElement other, Force force) {
		Point p1 = candidatePosition();
		Point p2 = other.candidatePosition(); 
	
		float distance = (float)Math.max(p1.getDistance(p2), 2);
		float intensity = force.intensityGiven(distance);
	
		float xComponent = ((p2.x - p1.x) / distance) * intensity;
		float yComponent = ((p2.y - p1.y) / distance) * intensity;
	
		addForceComponents(xComponent, yComponent);
		other.addForceComponents(-xComponent, -yComponent);
	}

	IFigure figure() {
		return _figure == null
			? _figure = produceFigure()
			: _figure;
	}

	abstract IFigure produceFigure();

	
}
