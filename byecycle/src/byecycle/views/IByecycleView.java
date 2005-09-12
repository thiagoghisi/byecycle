/*
 * Created on 2005年8月1日
 * $id$
 */
package byecycle.views;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

public interface IByecycleView extends ISelectionListener, IWorkbenchPart{

	public static final String PERSPECTIVE_ID = "byecycle.views.ByecycleView";

	public abstract void selectionChanged(IWorkbenchPart part, ISelection selection);

	public abstract void togglePaused(boolean pause);

}