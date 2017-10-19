package h4e.cifs4rse.subsystems;

import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.AbstractConnectorServiceManager;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;

public class CIFSConnectorServiceManager extends AbstractConnectorServiceManager {

	public CIFSConnectorServiceManager() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public IConnectorService createConnectorService(IHost host) {
		return new CIFSConnectorService(host);
	}

	@Override
	public Class getSubSystemCommonInterface(ISubSystem arg0) {
		return CIFSSubSystem.class;
	}

	@Override
	public boolean sharesSystem(ISubSystem arg0) {
		return (arg0 instanceof CIFSSubSystem);
	}

}
