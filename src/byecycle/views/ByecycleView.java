package byecycle.views;

import java.util.Iterator;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import byecycle.views.daglayout.GraphNode;

public class ByecycleView extends ViewPart {

	private Canvas _canvas;

	private LightweightSystem _lws;

	private final GraphNode[] _graph = graph();

	private final Color _nodeColor = new Color(null, 240, 255, 210);

	private final Figure _graphFigure = graphFigure();

	private final XYLayout _contentsLayout = new XYLayout();

	/**
	 * The constructor.
	 */
	public ByecycleView() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		_canvas = new Canvas(parent, SWT.NO_BACKGROUND);
		_lws = new LightweightSystem(_canvas);
		_lws.setContents(_graphFigure);
		improveLayout();
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		//viewer.getControl().setFocus();
	}

	private GraphNode[] graph() {
		return GraphNode.create(new String[] { "Node1", "Node2", "Node3",
				"Node4" });
	}

	private Figure graphFigure() {
		Figure result = new Figure();

		for (int i = 0; i < _graph.length; i++)
			result.add(nodeFigure(_graph[i].name()));

		return result;
	}

	private Figure nodeFigure(String text) {
		Label result = new Label(" " + text);
		result.setBorder(new LineBorder());
		result.setBackgroundColor(_nodeColor);
		result.setOpaque(true);
		return result;
	}

	int i;
	private void improveLayout() {
		Figure child;
		Iterator iterator = _graphFigure.getChildren().iterator();

		child = (Figure) iterator.next();
		_contentsLayout.setConstraint(child, new Rectangle(i, i, -1, -1));

		child = (Figure) iterator.next();
		_contentsLayout.setConstraint(child, new Rectangle(i + 15, i + 15, -1,
				-1));

		child = (Figure) iterator.next();
		_contentsLayout.setConstraint(child, new Rectangle(i + 30, i + 30, -1,
				-1));

		child = (Figure) iterator.next();
		_contentsLayout.setConstraint(child, new Rectangle(i + 45, i + 45, -1,
				-1));

		_graphFigure.setLayoutManager(_contentsLayout);
	}
}