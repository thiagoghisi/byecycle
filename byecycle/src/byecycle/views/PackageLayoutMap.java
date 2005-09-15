package byecycle.views;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import byecycle.views.layout.GraphLayoutMemento;

public class PackageLayoutMap {

	private final Map<IPackageFragment, GraphLayoutMemento> _contents = new HashMap<IPackageFragment, GraphLayoutMemento>();

	public GraphLayoutMemento getLayoutFor(IPackageFragment aPackage) {
		//TODO: implement an LRU cache.
		GraphLayoutMemento result = _contents.get(aPackage);
		return result == null ? new GraphLayoutMemento() : result;
	}

	public void keep(IPackageFragment aPackage, GraphLayoutMemento memento) {
		_contents.put(aPackage, memento);
		writeFileForPackageFragment(aPackage);
	}
	
	private void writeFileForPackageFragment(IPackageFragment aPackage) {

		try {
			IProject project = aPackage.getJavaProject().getProject();

			IFolder byecycleFolder = project.getFolder(".byecycle");
			if (!byecycleFolder.exists()) byecycleFolder.create(false, false, null);
			
			IFolder cacheFolder = byecycleFolder.getFolder("layoutcache");
			if (!cacheFolder.exists()) cacheFolder.create(false, false, null);
		
			IPackageFragmentRoot root = getPackageFragmentRoot(aPackage);
			String rootName = root == null ? "" : root.getElementName();

			IFile file = cacheFolder.getFile(rootName + "..." + aPackage.getElementName());
			if (file.exists()) {
				//file.setContents
			} else {				
				file.create(new ByteArrayInputStream("Hello".getBytes()), false, null);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return a IPackageFragmentRoot representing a source folder, jar file, zip file or null if the package is directly in the root of an Eclipse project.
	 */
	private IPackageFragmentRoot getPackageFragmentRoot(IJavaElement element) {
		if (element == null) return null;
		return element instanceof IPackageFragmentRoot
			? (IPackageFragmentRoot) element
			: getPackageFragmentRoot(element.getParent());
	}

}
