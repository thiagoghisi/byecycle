package byecycle.views.layout.algorithm.forces;

import byecycle.views.layout.Coordinates;
import byecycle.views.layout.algorithm.GraphElement;


public abstract class DistanceBasedForce implements Force {

	abstract float intensityGiven(float distance);

	protected float intensityGiven(GraphElement element1, GraphElement element2, float distance) {
		return intensityGiven(distance);
	}

	public void applyTo(GraphElement element1, GraphElement element2) {
		Coordinates p1 = element1.position();
		Coordinates p2 = element2.position();

		float distance = (float)Math.max(p1.getDistance(p2), 0.1);
		float intensity = this.intensityGiven(element1, element2, distance);

		float xComponent = ((p2._x - p1._x) / distance) * intensity;
		float yComponent = ((p2._y - p1._y) / distance) * intensity;

		element1.addForceComponents(xComponent, yComponent, element2);
	}

}
