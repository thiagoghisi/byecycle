package byecycle.views.layout.algorithm;

import byecycle.dependencygraph.Node;

public interface NodeSizeProvider {
	
	public FloatRectangle sizeGiven(Node node);
	
}
