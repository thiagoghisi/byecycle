package byecycle.views;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
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
			IFolder cacheFolder = produceCacheFolder(aPackage);
		
			String rootName = rootNameFor(aPackage).replaceAll("/", "__");
		
			String packageName = aPackage.isDefaultPackage()
				? "(default package)"
				: aPackage.getElementName();

			IFile file = cacheFolder.getFile(rootName + "__" + packageName);
			if (file.exists()) {
				//file.setContents
			} else {				
				file.create(new ByteArrayInputStream("Hello".getBytes()), false, null);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	static private String rootNameFor(IPackageFragment aPackage) throws JavaModelException {
		IPackageFragmentRoot root = getPackageFragmentRoot(aPackage);
		if (root == null) return "";
		
		IResource correspondingResource;
		try {
			correspondingResource = root.getCorrespondingResource();
		} catch (JavaModelException ignored) {
			return "";
		}
		if (correspondingResource == null) return "";
		
		return correspondingResource.getProjectRelativePath().toString();
	}

	static private IFolder produceCacheFolder(IPackageFragment aPackage) throws CoreException {
		IProject project = aPackage.getJavaProject().getProject();

		IFolder byecycleFolder = project.getFolder(".byecycle");
		if (!byecycleFolder.exists()) byecycleFolder.create(false, false, null);
		
		IFolder result = byecycleFolder.getFolder("layoutcache");
		if (!result.exists()) result.create(false, false, null);
		
		return result;
	}

	/**
	 * @return a IPackageFragmentRoot representing a source folder, jar file, zip file or null if the package is directly in the root of an Eclipse project.
	 */
	static private IPackageFragmentRoot getPackageFragmentRoot(IJavaElement element) {
		if (element == null) return null;
		return element instanceof IPackageFragmentRoot
			? (IPackageFragmentRoot) element
			: getPackageFragmentRoot(element.getParent());
	}

}
