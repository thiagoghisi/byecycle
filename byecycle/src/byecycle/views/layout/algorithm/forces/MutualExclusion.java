package byecycle.views.layout.algorithm.forces;

import byecycle.views.layout.algorithm.GraphElement;
import byecycle.views.layout.algorithm.NodeElement;

public class MutualExclusion extends DistanceBasedForce {
	
	public void applyTo(GraphElement element1, GraphElement element2) {
		if (!(element1 instanceof NodeElement)) return;
		if (!(element2 instanceof NodeElement)) return;
		super.applyTo(element1, element2);
	}

	@Override
	protected float intensityGiven(GraphElement element1, GraphElement element2, float ignored) {
    	NodeElement Element1 = (NodeElement)element1;
		NodeElement Element2 = (NodeElement)element2;
		
		float area = Element1.aura().areaOfIntersection(Element2.aura());
		return -0.0000001f * (float)Math.pow(area, 1.5);
	}

	float intensityGiven(float distance) {
		throw new IllegalStateException();
	}
}
