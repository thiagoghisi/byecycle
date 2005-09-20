package byecycle.views.layout.algorithm.forces;

import byecycle.views.layout.algorithm.GraphElement;
import byecycle.views.layout.algorithm.NodeElement;


public class SuperiorityComplex implements Force {

	private static final float DEPENDENCY_THRUST = 0.0003f;


	public void applyTo(GraphElement element1, GraphElement element2) {

		if (!(element1 instanceof NodeElement))
			return;
		if (!(element2 instanceof NodeElement))
			return;

		NodeElement node1 = (NodeElement)element1;
		NodeElement node2 = (NodeElement)element2;

		if (node1.dependsDirectlyOn(node2)) {
			actUponDependentAndProvider(node1, node2);
		}

		if (node2.dependsDirectlyOn(node1)) {
			actUponDependentAndProvider(node2, node1);
		}
	}

	private void actUponDependentAndProvider(NodeElement dependent, NodeElement provider) {

		float dY = Math.abs(provider._y - dependent._y);
		boolean inverted = provider._y < dependent._y;

		float thrust = (float)(DEPENDENCY_THRUST * (inverted ? 1 + (dY / 20) : 10 / (10 + dY)));
		dependent.addForceComponents(0, -thrust, provider);
	}

}
