package byecycle.views.layout.algorithm.forces;

public class StaticElectricity extends DistanceBasedForce {

    public float intensityGiven(float distance) {
        return -5.2f / (float)(Math.pow(distance, 2.7));  //TODO Play with this formula.
    	//return 0;
    }

}
