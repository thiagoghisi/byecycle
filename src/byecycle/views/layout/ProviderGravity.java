package byecycle.views.layout;

public class ProviderGravity implements Force {
	
	private static final float DEPENDENCY_THRUST = 0.0003f;

	public void actUpon(GraphElement element1, GraphElement element2) {
		
    	if (!(element1 instanceof NodeFigure)) return;
		if (!(element2 instanceof NodeFigure)) return;
		
    	NodeFigure figure1 = (NodeFigure)element1;
		NodeFigure figure2 = (NodeFigure)element2;
    	
        if (figure1.dependsOn(figure2)) {
            reactToProvider(figure1, figure2);
        }
        
        if (figure2.dependsOn(figure1)) {
            reactToProvider(figure2, figure1);
        }
	}
	
	private void reactToProvider(NodeFigure figure1, NodeFigure figure2) {
		
		float dY = Math.abs(figure2.candidateY() - figure1.candidateY());  
		boolean inverted = figure2.candidateY() < figure1.candidateY();
		
		float thrust = DEPENDENCY_THRUST * (inverted
			? 1 + (dY / 20)
			: 10 / (10 + dY)
		);
		figure1.up(thrust);
		figure2.down(thrust);
	}


}
