package byecycle;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import byecycle.dependencygraph.Node;


public class PackageDependencyAnalysis {
	
	public static final String PACKAGE = "package";
	
	public static final String CLASS = "class";
	
	public static final String INTERFACE = "interface";

	private final Map _nodes = new HashMap();
	
	public PackageDependencyAnalysis(ICompilationUnit[] compilationUnits, IProgressMonitor monitor) throws JavaModelException {
		
		if (null == monitor) {
			monitor = new NullProgressMonitor();
		}

		ASTParser parser = ASTParser.newParser(AST.JLS2);

		DependencyVisitor visitor = new DependencyVisitor();
		
		monitor.beginTask("dependency analysis", compilationUnits.length);
		for (int i = 0; i < compilationUnits.length; i++) {
			ICompilationUnit each = compilationUnits[i];
			parser.setResolveBindings(true);
			parser.setSource(each);
			
			monitor.subTask(each.getElementName());
			
			CompilationUnit node = (CompilationUnit) parser.createAST(monitor); 
			node.accept(visitor);
			
			monitor.worked(1);
			if (monitor.isCanceled()) {
				break;
			}
		}
	}
	
	public Map nodes() {
		return _nodes;
	}

	private Node getNode(String nodeName, String kind) {
		Node node = (Node)_nodes.get(nodeName);
		if (null == node) {
			node = new Node(nodeName, kind);
			_nodes.put(nodeName, node);
		}
		return node;
	}

	class DependencyVisitor extends ASTVisitor {
		
		Node _currentNode;
        private String _currentPackageName;
		
		public boolean visit(TypeDeclaration node) {
			Node saved = _currentNode;
			String savedPackage = _currentPackageName;
			
			_currentNode = getNode(node.resolveBinding().getQualifiedName(), node.isInterface() ? INTERFACE : CLASS);
			_currentPackageName = node.resolveBinding().getPackage().getName();
			
			for (Iterator iter = node.bodyDeclarations().iterator(); iter.hasNext();) {
				ASTNode child = (ASTNode) iter.next();
				child.accept(this);
			}
			_currentNode = saved;
			_currentPackageName = savedPackage;
			return false;
		}
		
		public boolean visit(SimpleType node) {
			addProvider(node.resolveBinding());
			return true;
		}

		public boolean visit(Expression node) {
			addProvider(node.resolveTypeBinding());
			return true;
		}

		public boolean visit(MethodInvocation node) {
			addProvider(node.resolveTypeBinding());
			return true;
		}

		public boolean visit(ClassInstanceCreation node) {
			addProvider(node.resolveTypeBinding());
			return true;
		}

		private void addProvider(ITypeBinding type) {
			if (null == type)
				return;
			if (type.isArray())
				type = type.getElementType();
			if (type.isPrimitive())
				return;
            if (type.getQualifiedName().equals(""))
				return; //TODO: Check why this happens.
            
			String packageName = type.getPackage().getName();
			if (packageName.equals(_currentPackageName))
			    return;
			if (packageName.equals("java.lang"))
			    return;
            _currentNode.addProvider(getNode(packageName, PACKAGE));
		}

	}
}