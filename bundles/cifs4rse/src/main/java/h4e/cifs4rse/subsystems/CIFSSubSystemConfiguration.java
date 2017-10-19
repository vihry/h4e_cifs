package h4e.cifs4rse.subsystems;


import java.util.Vector;

import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolManager;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.SubSystemConfiguration;
import org.eclipse.rse.services.clientserver.messages.SystemNetworkIOException;

/**
 * 
 * 
 * 
 *  category    => optionally identifies category to allow efficient subsetting of
 *   connection lists. Pre-defined categories are "files", "cmds" and "jobs".
 *   Can also specify your own category like "database".
 *
 */
public class CIFSSubSystemConfiguration extends SubSystemConfiguration {
	
	CIFSConnectorServiceManager _connectorManager = new CIFSConnectorServiceManager();
	
	IConnectorService _connectorService;

	public CIFSSubSystemConfiguration() {
		super();
	}

	@Override
	public ISubSystem createSubSystemInternal(IHost conn) {
		return new CIFSSubSystem(conn, getConnectorService(conn));
	}
	
	@Override
	public IConnectorService getConnectorService(IHost host) {
		if ( _connectorService==null ) {
			_connectorService = _connectorManager.createConnectorService(host);
		}

		return _connectorService;
	}
	

	/**
	 * Intercept of parent method that creates an initial default filter pool.
	 * We intercept so that we can create an initial filter in that pool, which will
	 *  list all teams.
	 */
	protected ISystemFilterPool createDefaultFilterPool(ISystemFilterPoolManager mgr) {
		ISystemFilterPool defaultPool = null;
		try {
			defaultPool = mgr.createSystemFilterPool(getDefaultFilterPoolName(mgr.getName(), getId()), true); // true=>is deletable by user
			Vector strings = new Vector();
			strings.add("*");
			mgr.createSystemFilter(defaultPool, "All shares", strings);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return defaultPool;
	}
	
	/**
	 * Intercept of parent method so we can supply our own value shown in the property
	 *  sheet for the "type" property when a filter is selected within our subsystem.
	 *
	 * Requires this line in rseSamplesResources.properties: property.type.teamfilter=Team filter
	 */
	public String getTranslatedFilterTypeProperty(ISystemFilter selectedFilter)
	{
	   	return "CIFS filter";//RSESamplesPlugin.getResourceString("property.type.teamfilter");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsUserId()
	 */
	public boolean supportsUserId() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsServerLaunchProperties(org.eclipse.rse.core.model.IHost)
	 */
	public boolean supportsServerLaunchProperties(IHost host) {
		return false;
	}
	
	@Override
	public boolean supportsFilters() {
		//System.out.println( super.supportsFilters() ); 
		return true; // super.supportsFilters();
	}

	@Override
	public boolean supportsCommands() {
		return false;//true;
	}
	
	@Override
	public boolean supportsProperties() {
		return false;//true;
	}
}
