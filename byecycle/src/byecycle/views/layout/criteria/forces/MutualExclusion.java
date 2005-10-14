package byecycle.views.layout.criteria.forces;

import byecycle.views.layout.FloatRectangle;
import byecycle.views.layout.criteria.GraphElement;
import byecycle.views.layout.criteria.NodeElement;


public class MutualExclusion implements Force {

	public void applyTo(GraphElement element1, GraphElement element2) {
		if (!(element1 instanceof NodeElement)) return;
		if (!(element2 instanceof NodeElement)) return;

		applyTo((NodeElement)element1, (NodeElement)element2);
	}

	private void applyTo(NodeElement node, NodeElement node2) {
		FloatRectangle intersection = node.aura().intersection(node2.aura());

		float hypotenuse= (float)Math.hypot(intersection._height, intersection._width);
		if (hypotenuse < 0.1) return;
		
		float intensity = intersection.area() * -0.0003f;
		
		float xComponent = (intersection._height / hypotenuse) * intensity; //Yes. The x component is proportional to the >>HEIGHT<< of the intersection.
		float yComponent = (intersection._width / hypotenuse) * intensity; //Yes. The y component is proportional to the >>WIDTH<< of the intersection.
		
		node.addForceComponents(xComponent, yComponent, node2);
	}

}
