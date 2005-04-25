package byecycle.views.layout;

public class AbstractForce implements Force {

	public float intensityGiven(float distance) {
		return 0;
	}

	public void actUpon(GraphElement element1, GraphElement element2) {
		element1.reactTo(element2, this);
	}

}
