//Copyright (C) 2005 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.

package byecycle.views.layout.criteria;

import byecycle.views.layout.Coordinates;


public abstract class GraphElement {

	public abstract Coordinates position();

	public void addForceComponents(float x, float y, GraphElement counterpart) {
		addForceComponents(x, y);
		counterpart.addForceComponents(-x, -y);
	}

	protected abstract void addForceComponents(float f, float g);
}
