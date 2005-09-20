//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.
package byecycle.views.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import byecycle.views.ByecycleView;
import byecycle.views.IByecycleView;


public class ToggleActiveAction implements IViewActionDelegate {

	private IByecycleView _view;


	public void init(IViewPart view) {
		_view = (ByecycleView)view;
	}

	public void run(IAction action) {
		_view.togglePaused(action.isChecked());
	}

	public void selectionChanged(IAction ignored, ISelection ignoredToo) {}

}
