package byecycle.views;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
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
	}
	
	private void writeFileForPackageFragment(IPackageFragment p) {
		IPackageFragmentRoot root = getPackageFragmentRoot(p);
		
		try {
			IResource resource = root.getCorrespondingResource();
			System.out.println(resource);
			IFolder folder = (IFolder)resource; //FIXME: This works only with sourcefolders, not with source directly in the project root.
			IFolder cache = folder.getFolder(".byecyclelayoutcache");
			if (!cache.exists()) cache.create(false, false, null);
			IFile file = cache.getFile("foo.txt");
			if (file.exists()) {
				//file.setContents
			} else {				
				file.create(new ByteArrayInputStream("Hello".getBytes()), false, null);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private IPackageFragmentRoot getPackageFragmentRoot(IJavaElement element) {
		return element instanceof IPackageFragmentRoot
			? (IPackageFragmentRoot) element
			: getPackageFragmentRoot(element.getParent());
	}

}
