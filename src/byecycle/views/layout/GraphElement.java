//Copyright (C) 2005 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.

package byecycle.views.layout;

import org.eclipse.draw2d.geometry.Point;


abstract class GraphElement {

	abstract Point position();

	abstract void addForceComponents(float f, float g);

	protected void reactTo(GraphElement other) {
	     if (other == this) throw new IllegalArgumentException();
	
	     reactTo(other, NodeFigure.REPULSION);
	}

	protected void reactTo(GraphElement other, Force force) {
		Point p1 = position();
		Point p2 = other.position();
	
		float distance = (float)Math.max(p1.getDistance(p2), 2);
		float intensity = force.intensityGiven(distance);
	
		float xComponent = ((p2.x - p1.x) / distance) * intensity;
		float yComponent = ((p2.y - p1.y) / distance) * intensity;
	
		addForceComponents(xComponent, yComponent);
		other.addForceComponents(-xComponent, -yComponent);
	}

}
