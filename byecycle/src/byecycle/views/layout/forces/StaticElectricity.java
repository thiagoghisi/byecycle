package byecycle.views.layout.forces;

public class StaticElectricity extends DistanceBasedForce {

    public float intensityGiven(float distance) {
        return -1.2f / (float)(Math.pow(distance, 2.7));  //TODO Play with this formula.
    	//return 0;
    }

}
