// Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
// This is free software. See the license distributed along with this file.

package byecycle.views.layout;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;


class DependencyFigure extends GraphElement {

	private final NodeFigure _dependent;
	private final NodeFigure _provider;

	DependencyFigure(NodeFigure dependent, NodeFigure provider) {
		_dependent = dependent;
		_provider = provider;
	}

    Point position() {
		Point p1 = _dependent.position();
		Point p2 = _provider.position();
		int centerX = (p1.x + p2.x) / 2;
		int centerY = (p1.y + p2.y) / 2;
		return new Point(centerX, centerY);
	}

	void addForceComponents(float x, float y) {
        float halfX = x / 2;
		float halfY = y / 2;
		_dependent.addForceComponents(halfX, halfY);
        _provider.addForceComponents(halfX, halfY);
    }
	
	IFigure figure() {
		PolylineConnection result = new PolylineConnection();
		result.setSourceAnchor(new ChopboxAnchor(_dependent.figure()));
		result.setTargetAnchor(new ChopboxAnchor(_provider.figure()));

		PolygonDecoration arrowHead = new PolygonDecoration();
		PointList decorationPointList = new PointList();
		decorationPointList.addPoint(0, 0);
		decorationPointList.addPoint(-1, 1);
		decorationPointList.addPoint(-1, 0);
		decorationPointList.addPoint(-1, -1);
		arrowHead.setTemplate(decorationPointList);
		result.setTargetDecoration(arrowHead);
		
		return result;
	}

}