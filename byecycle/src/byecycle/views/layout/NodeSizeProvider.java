package byecycle.views.layout;

import byecycle.dependencygraph.Node;


public interface NodeSizeProvider {

	public FloatRectangle sizeGiven(Node<?> node);

}
