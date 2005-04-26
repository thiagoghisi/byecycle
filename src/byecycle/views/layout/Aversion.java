package byecycle.views.layout;

import org.eclipse.draw2d.geometry.Rectangle;

public class Aversion implements Force {
	
	public void actUpon(GraphElement element1, GraphElement element2) {
		if (!(element1 instanceof NodeFigure)) return;
		if (!(element2 instanceof NodeFigure)) return;
		
    	NodeFigure figure1 = (NodeFigure)element1;
		NodeFigure figure2 = (NodeFigure)element2;
		
		Rectangle intersection = figure1.intersection(figure2);
	}
}
