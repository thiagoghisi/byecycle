//Copyright (C) 2004 Klaus Wuestefeld
//This is free software. It is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the license distributed along with this file for more details.
//Contributions: Rodrigo B de Oliveira.

package byecycle.views.daglayout;

import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.swt.graphics.Color;

public class NodeFigure extends Label {

    private static final Color NODE_COLOR = new Color(null, 240, 255, 210);
    
    private final GraphNode _node;

    public NodeFigure(GraphNode node) {
        super(" " + node.name());
        setBorder(new LineBorder());
        setBackgroundColor(NODE_COLOR);
        setOpaque(true);
        
        _node = node;
    }

}
