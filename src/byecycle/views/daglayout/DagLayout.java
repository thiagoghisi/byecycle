package byecycle.views.daglayout;
import java.util.Iterator;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class DagLayout {

    public static void main(String args[]) {
        new DagLayout();
    }

    private final GraphNode[] _graph = graph();

    private final Display _display = new Display(); //Has to be initialized before the _graphFigure although there is no explicit dependency, or else ColorConstants. :(
    private final Color _nodeColor = new Color(null, 240, 255, 210);
    private final Figure _graphFigure = graphFigure();
    // private final Display display = new Display(); //Uncomment this line to get the error and comment the same line above.
    private final XYLayout _contentsLayout = new XYLayout();
    
   
    private DagLayout() {

        Shell shell = new Shell(_display);
        shell.setText("Byecycle");
        shell.setSize(400, 400);

        LightweightSystem lws = new LightweightSystem(shell);
        lws.setContents(_graphFigure);

        shell.open();

        while (!shell.isDisposed()) {
            while (!_display.readAndDispatch()) {
                improveLayout();
                _display.sleep();
            }
        }
    }

    private GraphNode[] graph() {
        return GraphNode.create(new String[]{"Node1", "Node2", "Node3", "Node4"});
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

        child = (Figure)iterator.next();
        _contentsLayout.setConstraint(child, new Rectangle(i, i, -1,-1));

        child = (Figure)iterator.next();
        _contentsLayout.setConstraint(child, new Rectangle(i + 15, i + 15, -1,-1));
        
        child = (Figure)iterator.next();
        _contentsLayout.setConstraint(child, new Rectangle(i + 30, i + 30, -1,-1));

        child = (Figure)iterator.next();
        _contentsLayout.setConstraint(child, new Rectangle(i + 45, i + 45, -1,-1));
        
        _graphFigure.setLayoutManager(_contentsLayout);
    }

}
