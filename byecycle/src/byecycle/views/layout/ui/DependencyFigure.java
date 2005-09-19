// Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
// This is free software. See the license distributed along with this file.

package byecycle.views.layout.ui;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.swt.graphics.Color;


class DependencyFigure extends GraphFigure {

	private final NodeFigure<?> _dependent;
	private final NodeFigure<?> _provider;

	private PolylineConnection _arrow;
	private ChopboxAnchor _sourceAnchor;
	private ChopboxAnchor _targetAnchor;

	DependencyFigure(NodeFigure dependent, NodeFigure provider) {
		_dependent = dependent;
		_provider = provider;
	
	}
	
	IFigure produceFigure() {
		_arrow = new PolylineConnection();

		_sourceAnchor = new ChopboxAnchor(_dependent.figure());
		_targetAnchor = new ChopboxAnchor(_provider.figure());
		_arrow.setSourceAnchor(_sourceAnchor);
		_arrow.setTargetAnchor(_targetAnchor);

		PolygonDecoration arrowHead = new PolygonDecoration();
		PointList decorationPointList = new PointList();
		decorationPointList.addPoint(0, 0);
		decorationPointList.addPoint(-1, 1);
		decorationPointList.addPoint(-1, 0);
		decorationPointList.addPoint(-1, -1);
		arrowHead.setTemplate(decorationPointList);
		_arrow.setTargetDecoration(arrowHead);
		
		Color redOrBlack = _provider.node().dependsOn(_dependent.node())
			? ColorConstants.red
			: ColorConstants.black;
		_arrow.setForegroundColor(redOrBlack);
		
		return _arrow;
	}

	void refresh() {
		correctOverlapInversion();
	}

	private void correctOverlapInversion() {
		//Draw2D correctly inverts arrows when the boxes overlap. We will "uninvert" them for a more intuitive result.
		IFigure source = _dependent.figure();
		IFigure target = _provider.figure();
		if (source.intersects(target.getBounds())) {
			IFigure temp = source;
			source = target;
			target = temp;
		}
		_sourceAnchor.setOwner(source);
		_targetAnchor.setOwner(target);
	}

}