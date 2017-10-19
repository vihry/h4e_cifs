package h4e.cifs4rse.model;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.model.ISystemResourceSet;
import org.eclipse.rse.core.model.ISystemViewInputProvider;
import org.eclipse.rse.core.model.SystemRemoteResourceSet;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.view.AbstractSystemViewAdapter;
import org.eclipse.rse.ui.view.IContextObject;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.osgi.framework.Bundle;
import h4e.cifs4rse.plugin.Activator;
import h4e.cifs4rse.subsystems.CIFSSubSystem;

public class CIFSResourceAdapter extends AbstractSystemViewAdapter
	implements ISystemRemoteElementAdapter {

	/**
	 * Constructor.
	 */
	public CIFSResourceAdapter() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#addActions(org.eclipse.rse.ui.SystemMenuManager,
	 * org.eclipse.jface.viewers.IStructuredSelection, org.eclipse.swt.widgets.Shell, java.lang.String)
	 */
	public void addActions(SystemMenuManager menu,
			IStructuredSelection selection, Shell parent, String menuGroup)
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#getImageDescriptor(java.lang.Object)
	 */
	public ImageDescriptor getImageDescriptor(Object element)
	{
		//return CIFS4RSEPlugin.getDefault().getImageDescriptor("ICON_ID_TEAM");
		//return CIFS4RSEPlugin.getDefault().getPluginImage( "icons/sample.png" );
		//URL url = BundleUtility.find( CIFS4RSEPlugin.getDefault(), "icons/palette_brush.png" );
		//URL url = FileLocator.find( (Bundle)CIFS4RSEPlugin.getDefault(), new Path("icons/sample.png"), null );

		try {
			URL url = FileLocator.find( new URL("file://./icons/sample.png") );
			return ImageDescriptor.createFromURL(url);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#getText(java.lang.Object)
	 */
	public String getText(Object element)
	{
		return ((CIFSResource)element).getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#getAbsoluteName(java.lang.Object)
	 */
	public String getAbsoluteName(Object object)
	{
		CIFSResource team = (CIFSResource)object;
		return "S_"+team.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#getType(java.lang.Object)
	 */
	public String getType(Object element)
	{
		return "AAA";//Activator.getResourceString("property.team_resource.type");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#getParent(java.lang.Object)
	 */
	public Object getParent(Object element)
	{
		return null; // not really used, which is good because it is ambiguous
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element)
	{
		return false;
	}

//	/**
//	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#internalGetPropertyDescriptors()
//	 */
//	protected IPropertyDescriptor[] internalGetPropertyDescriptors()
//	{
//		return null;
//	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.AbstractSystemViewAdapter#internalGetPropertyValue(java.lang.Object)
	 */
	protected Object internalGetPropertyValue(Object key)
	{
		return null;
	}

	/**
	 * Intercept of parent method to indicate these objects can be renamed using the RSE-supplied
	 *  rename action.
	 */
	@Override
	public boolean canRename(Object element)
	{
		return true;
	}
	
//	@Override
//	public boolean showProperties(Object element) {
//		return true;
//	}
	
	@Override
	public boolean canDelete(Object element) {
		return true;
	}
	
	/**
	 * Intercept of parent method to actually do the rename. RSE supplies the rename GUI, but
	 *  defers the action work of renaming to this adapter method.
	 */
	@Override
	public boolean doRename(Shell shell, Object element, String name, IProgressMonitor monitor) throws Exception {
		return doRename(shell, element, name);
	}
	
	public boolean doRename(Shell shell, Object element, String newName)
	{
		((CIFSResource)element).setName(newName);
		return true;
	}
	
	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		IPropertyDescriptor p[] = { createSimplePropertyDescriptor( "A", "B", "A+B") };
		
		return p;
	}
	
	// --------------------------------------
	// ISystemRemoteElementAdapter methods...
	// --------------------------------------

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getAbsoluteParentName(java.lang.Object)
	 */
	public String getAbsoluteParentName(Object element)
	{
		return "root"; // not really applicable as we have no unique hierarchy
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getSubSystemConfigurationId(java.lang.Object)
	 */
	public String getSubSystemConfigurationId(Object element)
	{
		return "samples.subsystems.factory"; // as declared in extension in plugin.xml
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getRemoteTypeCategory(java.lang.Object)
	 */
	public String getRemoteTypeCategory(Object element)
	{
		return "developers"; // Course grained. Same for all our remote resources.
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getRemoteType(java.lang.Object)
	 */
	public String getRemoteType(Object element)
	{
		return "team"; // Fine grained. Unique to this resource type.
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getRemoteSubType(java.lang.Object)
	 */
	public String getRemoteSubType(Object element)
	{
		return null; // Very fine grained. We don't use it.
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#refreshRemoteObject(java.lang.Object, java.lang.Object)
	 */
	public boolean refreshRemoteObject(Object oldElement, Object newElement)
	{
		CIFSResource oldTeam = (CIFSResource)oldElement;
		CIFSResource newTeam = (CIFSResource)newElement;
		newTeam.setName(oldTeam.getName());
		return false; // If developer objects held references to their team names, we'd have to return true
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getRemoteParent(org.eclipse.swt.widgets.Shell, java.lang.Object)
	 */
	public Object getRemoteParent(Shell shell, Object element) throws Exception
	{
		return null; // maybe this would be a Project or Roster object, or leave as null if this is the root
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getRemoteParentNamesInUse(org.eclipse.swt.widgets.Shell, java.lang.Object)
	 */
	public String[] getRemoteParentNamesInUse(Shell shell, Object element)
			throws Exception
	{
		//CIFSSubSystem ourSS = (CIFSSubSystem)getSubSystem(element);
		CIFSSubSystem ourSS = (CIFSSubSystem)element;
		CIFSResource[] allTeams = ourSS.getAllShares();
		String[] allNames = new String[allTeams.length];
		for (int idx = 0; idx < allTeams.length; idx++)
		  allNames[idx] = allTeams[idx].getName();
		return allNames; // Return list of all team names 	
	}

	@Override
	public Object[] getChildren(IAdaptable arg0, IProgressMonitor arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasChildren(IAdaptable arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected IPropertyDescriptor[] internalGetPropertyDescriptors() {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
//	public String getName(Object arg0) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public String getRemoteSourceType(Object arg0) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public String getRemoteSubSubType(Object arg0) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public ISubSystem getSubSystem(Object arg0) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public boolean canEdit(Object arg0) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	@Override
//	public ISystemEditableRemoteObject getEditableRemoteObject(Object arg0) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public String getFilterStringFor(Object arg0) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public Object getRemoteParent(Object arg0, IProgressMonitor arg1) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getRemoteParentNamesInUse(Object arg0, IProgressMonitor arg1) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public ISchedulingRule getRule(Object element) {
		return super.getRule(element);
	}

}
