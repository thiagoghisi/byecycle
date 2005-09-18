package byecycle.views.layout.forces;

import byecycle.views.layout.Coordinates;
import byecycle.views.layout.GraphElement;

public abstract class DistanceBasedForce implements Force {

	abstract float intensityGiven(float distance);

	protected float intensityGiven(GraphElement element1, GraphElement element2, float distance) {
		return intensityGiven(distance);
	}

	public void actUpon(GraphElement element1, GraphElement element2) {
		Coordinates p1 = element1.candidatePosition();  //TODO: Point uses ints internally for x and y values. Consider calculating distance ourselves.
		Coordinates p2 = element2.candidatePosition(); 
		
		float distance = (float)Math.max(p1.getDistance(p2), 2);
		float intensity = this.intensityGiven(element1, element2, distance);
		
		float xComponent = ((p2._x - p1._x) / distance) * intensity;
		float yComponent = ((p2._y - p1._y) / distance) * intensity;
		
		element1.addForceComponents(xComponent, yComponent);
		element2.addForceComponents(-xComponent, -yComponent);
	}


}
