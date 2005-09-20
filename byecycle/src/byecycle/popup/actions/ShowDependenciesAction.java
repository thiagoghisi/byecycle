//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.
package byecycle.popup.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import byecycle.views.IByecycleView;


public class ShowDependenciesAction implements IViewActionDelegate {

	private ISelection _selection;


	public void init(IViewPart view) {
	// Apparently never called.
	}

	public void run(IAction ignored) {
		if (_selection == null) return;
		byecycleView().showDependencies(_selection);
	}

	private IByecycleView byecycleView() {
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		IByecycleView result = null;
		try {
			result = (IByecycleView)activePage.showView(IByecycleView.PERSPECTIVE_ID);
		} catch (PartInitException e) {
			e.printStackTrace();
			return null;
		}

		activePage.activate(result);
		return result;
	}

	public void selectionChanged(IAction ignored, ISelection selection) {
		_selection = selection;
	}
}
