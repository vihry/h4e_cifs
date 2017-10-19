package h4e.cifs4rse.subsystems;

import java.util.Vector;

import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolManager;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.SystemStartHere;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.SubSystem;
import org.eclipse.rse.services.clientserver.NamePatternMatcher;

import h4e.cifs4rse.model.CIFSResource;
import h4e.cifs4rse.model.CIFSResourceAdapterFactory;

public class CIFSSubSystem extends SubSystem {
	
	private CIFSResource[] teams; // faked-out master list of teams
	private Vector devVector = new Vector(); // faked-out master list of developers
	private static int employeeId = 123456; // employee Id factory	

	/**
	 * @param host
	 * @param connectorService
	 */
	public CIFSSubSystem(IHost host, IConnectorService connectorService) {
		super(host, connectorService);
		
		ISystemRegistry isr = SystemStartHere.getSystemRegistry();
		
		//RSEUIPlugin.getTheSystemRegistryUI();
		
		System.out.println( "CIFSSubSystem" );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystem#initializeSubSystem(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void initializeSubSystem(IProgressMonitor monitor) {
		
		// initiate default filter
		// see CIFSSubSystemConfiguration.createDefaultFilterPool
		ISubSystemConfiguration c = getSubSystemConfiguration();
		ISystemFilterPoolManager m = c.getFilterPoolManager( getSystemProfile() );
		
		IAdapterManager manager = Platform.getAdapterManager();
		CIFSResourceAdapterFactory factory = new CIFSResourceAdapterFactory();
		manager.registerAdapters(factory, CIFSResource.class);

		System.out.println( "init" );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.ISubSystem#uninitializeSubSystem(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void uninitializeSubSystem(IProgressMonitor monitor) {
		System.out.println( "uninit" );
	}

	/**
	 * For drag and drop, and clipboard support of remote objects.
	 *   
	 * Return the remote object within the subsystem that corresponds to
	 * the specified unique ID.  Because each subsystem maintains it's own
	 * objects, it's the responsability of the subsystem to determine
	 * how an ID (or key) for a given object maps to the real object.
	 * By default this returns null. 
	 */
	public Object getObjectWithAbsoluteName(String key) {
		
		System.out.println( "key: " + key );
		
		//  Functional opposite of getAbsoluteName(Object) in our resource adapters
//		if (key.startsWith("Team_")) //$NON-NLS-1$
//		{
//			String teamName = key.substring(5);
//			TeamResource[] allTeams = getAllTeams();
//			for (int idx=0; idx < allTeams.length; idx++)
//			   if (allTeams[idx].getName().equals(teamName))
//			     return allTeams[idx];
//		}
//		else if (key.startsWith("Devr_")) //$NON-NLS-1$
//		{
//			String devrId = key.substring(5);
//			DeveloperResource[] devrs = getAllDevelopers();
//			for (int idx=0; idx < devrs.length; idx++)
//			  if (devrs[idx].getId().equals(devrId))
//			    return devrs[idx];            	
//		}
		return null; 
	}

	/**
	 * When a filter is expanded, this is called for each filter string in the filter.
	 * Using the criteria of the filter string, it must return objects representing remote resources.
	 * For us, this will be an array of TeamResource objects.
	 * 
	 * @param monitor - the progress monitor in effect while this operation performs
	 * @param filterString - one of the filter strings from the expanded filter.
	 */
	protected Object[] internalResolveFilterString(String filterString, IProgressMonitor monitor)
         throws java.lang.reflect.InvocationTargetException,
                java.lang.InterruptedException {
		
		System.out.println( "filterString: " + filterString );
		
		// Fake it out for now and return dummy list. 
		// In reality, this would communicate with remote server-side code/data.
		CIFSResource[] all_shares = getAllShares();
		
		// Now, subset master list, based on filter string...
		NamePatternMatcher subsetter = new NamePatternMatcher(filterString);
		Vector v = new Vector();
		for (int idx = 0; idx < all_shares.length; idx++)
		{
			if (subsetter.matches(all_shares[idx].getName()))
			  v.addElement(all_shares[idx]);
		}		
		CIFSResource[] teams = new CIFSResource[v.size()];
		for (int idx=0; idx < v.size(); idx++)
		   teams[idx] = (CIFSResource)v.elementAt(idx);
		return teams;
	}

	/**
	 * When a remote resource is expanded, this is called to return the children of the resource, if
	 * the resource's adapter states the resource object is expandable.
	 * For us, it is a Team resource that was expanded, and an array of Developer resources will be returned.
	 * 
	 * @param monitor - the progress monitor in effect while this operation performs
	 * @param parent - the parent resource object being expanded
	 * @param filterString - typically defaults to "*". In future additional user-specific quick-filters may be supported.
	 */
	protected Object[] internalResolveFilterString(Object parent, String filterString, IProgressMonitor monitor)
         throws java.lang.reflect.InvocationTargetException,
                java.lang.InterruptedException
	{
		System.out.println( "filterString: " + filterString );
		
		// typically we ignore the filter string as it is always "*" 
		//  until support is added for "quick filters" the user can specify/select
		//  at the time they expand a remote resource.
		
//		TeamResource team = (TeamResource)parent;
//		return team.getDevelopers();
		
		return null;
	}

	// ------------------	
	// Our own methods...
	// ------------------

	/**
	 * Get the list of all teams. Normally this would involve a trip the server, but we 
	 *  fake it out and return a hard-coded local list. 
	 */
	public CIFSResource[] getAllShares()
	{
		if (teams == null) 
		  teams = createTeams("Share ", 4);
		return teams;		
	}
//
//	/**
//	 * Get the list of all developers. Normally this would involve a trip the server, but we 
//	 *  fake it out and return a hard-coded local list. 
//	 */
//	public DeveloperResource[] getAllDevelopers()
//	{
//		DeveloperResource[] allDevrs = new DeveloperResource[devVector.size()];
//		for (int idx = 0; idx < allDevrs.length; idx++)
//		  allDevrs[idx] = (DeveloperResource)devVector.elementAt(idx);
//		return allDevrs;		
//	}

	/*
	 * Create and return a dummy set of teams
	 */
	private CIFSResource[] createTeams(String prefix, int howMany) {
		
		

		CIFSResource[] devrs = new CIFSResource[ howMany ];
		for (int idx=0; idx < devrs.length; idx++)
		{
			devrs[idx] = new CIFSResource(this);
			devrs[idx].setName(prefix + (idx+1));
			devrs[idx].setId(Integer.toString(employeeId++));
			devrs[idx].setDeptNbr(Integer.toString((idx+1)*100));
			devVector.add(devrs[idx]); // update master list
		}
		return devrs;
	}

}
