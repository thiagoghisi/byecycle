package byecycle.views.layout.algorithm.forces;

import byecycle.views.layout.algorithm.GraphElement;
import byecycle.views.layout.algorithm.NodeElement;


public class DependencySpring extends DistanceBasedForce {

	@Override
	public float intensityGiven(float distance) {
		return (distance - 25) * 0.000005f; // TODO Play with this formula. Zero it to see REPULSION acting alone.
	}

	@Override
	public void applyTo(GraphElement element1, GraphElement element2) {

		if (element1 instanceof NodeElement && element2 instanceof NodeElement) {

			NodeElement Element1 = (NodeElement)element1;
			NodeElement Element2 = (NodeElement)element2;
			if (Element1.dependsDirectlyOn(Element2) || Element2.dependsDirectlyOn(Element1)) {
				super.applyTo(element1, element2);
			}
		}
	}

}
