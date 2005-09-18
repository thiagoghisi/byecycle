package byecycle.views.layout.algorithm.forces;

import byecycle.views.layout.GraphElement;
import byecycle.views.layout.NodeFigure;

public class MutualExclusion extends DistanceBasedForce {
	
	public void actUpon(GraphElement element1, GraphElement element2) {
		if (!(element1 instanceof NodeFigure)) return;
		if (!(element2 instanceof NodeFigure)) return;
		super.actUpon(element1, element2);
	}

	@Override
	protected float intensityGiven(GraphElement element1, GraphElement element2, float ignored) {
    	NodeFigure<?> figure1 = (NodeFigure)element1;
		NodeFigure<?> figure2 = (NodeFigure)element2;
		
		float area = figure1.aura().areaOfIntersection(figure2.aura());
		return -0.0000001f * (float)Math.pow(area, 1.5);
	}

	float intensityGiven(float distance) {
		throw new IllegalStateException();
	}
}
