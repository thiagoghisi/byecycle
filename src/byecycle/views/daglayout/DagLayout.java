package byecycle.views.daglayout;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class DagLayout {

    public static void main(String args[]) {
        new DagLayout();
    }

    private final GraphNode[] _graph = graph();

    private final Display _display = new Display(); //Has to be initialized before the _graphFigure although there is no explicit dependency, or else ColorConstants. :(
    private final IFigure _graphFigure = new Figure();
    // private final Display display = new Display(); //Uncomment this line to get the error and comment the same line above.
    private final XYLayout _contentsLayout = new XYLayout();

    private final Map _nodeFiguresByNode = new HashMap();
    
   
    private DagLayout() {
        Shell shell = new Shell(_display);
        shell.setText("Byecycle");
        shell.setSize(400, 400);

        LightweightSystem lws = new LightweightSystem(shell);
        initGraphFigure();
        lws.setContents(_graphFigure);

        shell.open();

        while (!shell.isDisposed()) {
            while (!_display.readAndDispatch()) {
                improveLayout();
                _display.sleep();
            }
        }
    }

    private void initGraphFigure() {
        for (int i = 0; i < _graph.length; i++) {
            GraphNode node = _graph[i];
            IFigure dependentFigure = produceNodeFigureFor(node);
            Iterator providers = node.providers();
            while (providers.hasNext()) {
                IFigure providerFigure = produceNodeFigureFor((GraphNode)providers.next());
                addDependencyFigure(dependentFigure, providerFigure);
            }
        }
    }

    private void addDependencyFigure(IFigure dependentFigure, IFigure providerFigure) {
        PolylineConnection dependency = new PolylineConnection();
        dependency.setSourceAnchor(new ChopboxAnchor(dependentFigure));
        dependency.setTargetAnchor(new ChopboxAnchor(providerFigure));

        PolygonDecoration arrowHead = new PolygonDecoration();
        PointList decorationPointList = new PointList();
        decorationPointList.addPoint( 0,  0);
        decorationPointList.addPoint(-1,  1);
        decorationPointList.addPoint(-1,  0);
        decorationPointList.addPoint(-1, -1);
        arrowHead.setTemplate(decorationPointList);
        dependency.setTargetDecoration(arrowHead);
        
        _graphFigure.add(dependency);
    }

    private IFigure produceNodeFigureFor(GraphNode node) {
        IFigure result = (IFigure)_nodeFiguresByNode.get(node);
        if (result != null) return result;
        
        result = new NodeFigure(node);
        _nodeFiguresByNode.put(node, result);
        _graphFigure.add(result);
        return result;
    }

    private GraphNode[] graph() {
        return GraphNode.create(new String[]{"Node1", "Node2", "Node3", "Node4"});
    }

    int i; 
    private void improveLayout() {
        IFigure nodeFigure;

        nodeFigure = produceNodeFigureFor(_graph[0]);
        _contentsLayout.setConstraint(nodeFigure, new Rectangle(i, i, -1, -1));

        nodeFigure = produceNodeFigureFor(_graph[1]);
        _contentsLayout.setConstraint(nodeFigure, new Rectangle(i + 100, i + 200, -1, -1));
        
        nodeFigure = produceNodeFigureFor(_graph[2]);
        _contentsLayout.setConstraint(nodeFigure, new Rectangle(i + 200, i + 300, -1, -1));

        nodeFigure = produceNodeFigureFor(_graph[3]);
        _contentsLayout.setConstraint(nodeFigure, new Rectangle(i + 300, i + 100, -1, -1));
        
        _graphFigure.setLayoutManager(_contentsLayout);
    }

}
