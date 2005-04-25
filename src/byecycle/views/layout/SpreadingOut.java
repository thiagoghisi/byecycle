package byecycle.views.layout;

public class SpreadingOut extends AbstractForce {

    public float intensityGiven(float distance) {
        return 0.093f / (float)(Math.pow(distance, 2.7));  //TODO Play with this formula.
    	//return 0;
    }

}
