//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.
package byecycle.views.layout;

import java.util.Random;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import byecycle.JavaType;
import byecycle.dependencygraph.Node;

public class NodeFigure<T> extends GraphElement {

	public NodeFigure(Node<T> node) {
		_node = node;
	}

	private final Node<T> _node;

	private Label label(String text, Image icon) {
		return icon == null
			? new Label(" " + text, icon)
			: new Label(text, icon);
	}

	IFigure produceFigure() {
		IFigure result;

		String name = name();
		if (name.length() < 20) {
			result = label(name, imageForNode(_node));
		} else {
			result = new CompartmentFigure();
			int cut = (name.length() / 2) - 1;
			result.add(label(name.substring(0, cut), imageForNode(_node)));
			result.add(label(name.substring(cut), null));
		}
		
		result.setBorder(new LineBorder());
		result.setBackgroundColor(pastelColorDeterminedBy(name));
		result.setOpaque(true);

		//result.setSize(getPreferredSize()); //TODO Is this necessary?
		
		return result;
	}

	String name() {
		String result = _node.name();
		if (_node.kind2() == JavaType.PACKAGE)
			return result;
		return result.substring(result.lastIndexOf('.') + 1);
	}

	static class CompartmentFigure extends Figure {
		public CompartmentFigure() {
			ToolbarLayout layout = new ToolbarLayout();
			layout.setMinorAlignment(ToolbarLayout.ALIGN_TOPLEFT);
			layout.setStretchMinorAxis(false);
			setLayoutManager(layout);
		}
	}

	private static Image imageForNode(Node<?> node) {
		String resourcename = node.kind2().getResourceName();
		return JavaUI.getSharedImages().getImage(resourcename);
	}

	private static Color pastelColorDeterminedBy(String string) {
		Random random = new Random(string.hashCode() * 713);
		int r = 210 + random.nextInt(46);
		int g = 210 + random.nextInt(46);
		int b = 210 + random.nextInt(46);
		return new Color(null, r, g, b);
	}

	Node node() {
		return _node;
	}

	void position(Point point) {
		this.figure().setLocation(point);
	}

	public Point position() {
		return this.figure().getBounds().getLocation();
	}

}
