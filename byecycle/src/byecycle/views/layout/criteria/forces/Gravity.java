package byecycle.views.layout.criteria.forces;

import byecycle.views.layout.criteria.GraphElement;
import byecycle.views.layout.criteria.NodeElement;


public class Gravity extends DistanceBasedForce {

	@Override
	public float intensityGiven(float distance) {
		return 0.8f / (float)(Math.pow(distance, 2)); // TODO Play with this formula.
	}

	@Override
	public void applyTo(GraphElement element1, GraphElement element2) {
		if (element1 instanceof NodeElement && element2 instanceof NodeElement) {
			super.applyTo(element1, element2);
		}
	}

}
