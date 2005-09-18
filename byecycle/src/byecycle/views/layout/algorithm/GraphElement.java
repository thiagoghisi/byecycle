//Copyright (C) 2005 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.

package byecycle.views.layout.algorithm;

import org.eclipse.draw2d.IFigure;
import byecycle.views.layout.algorithm.Coordinates;


public abstract class GraphElement {

    private IFigure _figure;

	public abstract Coordinates candidatePosition();

	public abstract void addForceComponents(float f, float g);
	
	IFigure figure() {
		return _figure == null
			? _figure = produceFigure()
			: _figure;
	}

	abstract IFigure produceFigure();
	
}
