package byecycle.views.layout.criteria.forces;

public class StaticElectricity extends DistanceBasedForce {

	public float intensityGiven(float distance) {
		return -3.2f / (float)(Math.pow(distance, 2.2)); // TODO Play with this formula.
		// return 0;
	}

}
