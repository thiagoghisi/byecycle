// Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
// This is free software. See the license distributed along with this file.

package byecycle.views.layout;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;


class Dependency {

	private final NodeFigure _dependentFigure;
	private final NodeFigure _providerFigure;

	Dependency(NodeFigure dependentFigure, NodeFigure providerFigure) {
		_dependentFigure = dependentFigure;
		_providerFigure = providerFigure;
	}

    void reactTo(Dependency other) {
        if (other == this) throw new IllegalArgumentException();

        //reactTo(other, NodeFigure.REPULSION);
	}


	void reactTo(NodeFigure other) {
        reactTo(other, NodeFigure.REPULSION);
	}

	
    private void reactTo(NodeFigure other, Force force) {
		Point p1 = position();
		Point p2 = other.position();

		float distance = (float)Math.max(p1.getDistance(p2), 2);
		float intensity = force.intensityGiven(distance);

        float xComponent = ((p2.x - p1.x) / distance) * intensity;
        float yComponent = ((p2.y - p1.y) / distance) * intensity;

        addForceComponents(xComponent, yComponent);
        other.addForceComponents(-xComponent, -yComponent);
	}

	private Point position() {
		Point p1 = _dependentFigure.position();
		Point p2 = _providerFigure.position();
		int centerX = (p1.x + p2.x) / 2;
		int centerY = (p1.y + p2.y) / 2;
		return new Point(centerX, centerY);
	}

	void addForceComponents(float x, float y) {
        float halfX = x / 2;
		float halfY = y / 2;
		_dependentFigure.addForceComponents(halfX, halfY);
        _providerFigure.addForceComponents(halfX, halfY);
    }
	
	IFigure figure() {
		PolylineConnection result = new PolylineConnection();
		result.setSourceAnchor(new ChopboxAnchor(_dependentFigure));
		result.setTargetAnchor(new ChopboxAnchor(_providerFigure));

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