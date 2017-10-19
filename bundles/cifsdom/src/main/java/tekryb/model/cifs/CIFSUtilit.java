package tekryb.model.cifs;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;
import org.w3c.dom.events.DocumentEvent;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MutationEvent;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * Techryb -> tekryb
 * 
 * Technology Rock Your Body.
 * 
 */
public class CIFSUtilit  {
	
	public static final String CIFSDOM_ROOT = "cifs";
	
	public static final String CIFSDOM_CONNECTION = "connection";
	
	public static final String CIFSDOM_FSTREE = "filesysTree";
	
	protected Element _connection;
	
	protected Element _fileSystemTree;
	
	protected Attr _conn_active;
	
	protected SmbFile _smbRoot;

	protected CIFSUtilit() {}
	
	// test only
	public static void main(String[] args) throws URISyntaxException {
		Document doc = CIFSUtilit.open( new URI( "smb://user:pass@localhost/tmp/test.xml" ) );
		
		int cnt = doc.getChildNodes().getLength();
		
		System.out.println( "doc with child length: " + cnt );
	}
	
	public static Document open(URI uri) {
		
		final CIFSUtilit that = new CIFSUtilit();

		String username="", password = "";
		
		try {
			
			DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
			DocumentBuilder b = f.newDocumentBuilder();
			
			Document doc = b.newDocument();
			if ( !( doc instanceof EventTarget && doc instanceof DocumentEvent ) ) {
				System.out.println( "dom implementation does not suppoert events" );
				return null;
			}

			that._smbRoot = new SmbFile( "smb://user:pass@localhost/tmp/test.xml",
					new NtlmPasswordAuthentication("", username, password) );

			final Node root = doc.appendChild( doc.createElement( CIFSDOM_ROOT ) );
			that._connection = (Element)root.appendChild( doc.createElement( CIFSDOM_CONNECTION ) );
			that._fileSystemTree = (Element)root.appendChild( doc.createElement( CIFSDOM_FSTREE ) );
			
			that._conn_active = doc.createAttribute( "active" );
			that._conn_active.setValue( "false" );
			that._connection.setAttributeNode( that._conn_active );

			// populateTree( doc );
			
			//DOMAttrModified,DOMCharacterDataModified,DOMNodeInserted,DOMNodeInsertedIntoDocument
			//DOMNodeRemoved,DOMNodeRemovedFromDocument,DOMSubtreeModified
			((EventTarget) root).addEventListener( "DOMAttrModified",
					new EventListener() {
				public void handleEvent(Event evt) {
					System.out.println( "attr changed" + evt );
				} }, true );
			
			//doc.renameNode(elem, elem.getNamespaceURI(), toTag);
//			that._conn_active.setUserData( "active", that, new UserDataHandler() {
//				public void handle(short operation, String key, Object data, Node src, Node dst) {
//					System.out.println( "data changed" );
//					switch ( operation ) {
//					case NODE_DELETED:
//					}
//					
//					//try {
//						//String[] list = that._smbRoot.list(); //oldFile.copyTo(newFile);
//					//} catch () {}
//					
//				}} );
			
			//that._conn_active.set
			//that._conn_active.setValue( "true" );
			
			
//			EventTarget et_active = (EventTarget) active;
//			et_active.addEventListener( "DOMSubtreeModified", new EventListener() {
//				public void handleEvent(Event evt) {
//					 if ( evt instanceof MutationEvent ) {
//					        MutationEvent me = (MutationEvent) evt;
//					        
//					        System.out.println("type: " + me.getType() + ", dest: " + me.getTarget());
//					        
//					        String value = active.getNodeValue();
//					        if ( value.equalsIgnoreCase("true") ) {
//					        	
//					        }
//					 }
//				} }, false );
			
//			NodeList nodeList = document.getElementsByTagName("Item");
//	        for(int x=0,size= nodeList.getLength(); x<size; x++) {
//	            System.out.println(nodeList.item(x).getAttributes().getNamedItem("name").getNodeValue());
//	        }
			
//			Element e = doc.getDocumentElement();
//			if ( e instanceof EventTarget && doc instanceof DocumentEvent ) {
//				EventListener myModificationListener =
//						  new EventListener() {
//						    public void handleEvent(Event e) {
//						      if (e instanceof MutationEvent) {
//						        MutationEvent me = (MutationEvent) e;
//						        System.out.println("type: " + me.getType() + ", dest: " + me.getTarget());
//						      }
//						    }
//
//						  };
//						  
//				((EventTarget)e).addEventListener("DOMSubtreeModified", myModificationListener, true);
//			} else {
//				System.out.println( "w3c document implementation does not support events!" );
//			}
//			
//			
//			SmbFile root = new SmbFile( "smb://someone_pc/tmp/test.xml",
//					new NtlmPasswordAuthentication("", username, password) );
//			UserDataHandler udh = new UserDataHandler() {
//				public void handle(short operation, String key, Object data, Node src, Node dst) {
//					switch ( operation ) {
//					case NODE_DELETED:
//					}
//				}
//			};
//			e.setUserData( "smbfile", root, udh );
//			
//			NodeList nl = e.getChildNodes();
//			
//			DocumentEvent doc_event = (DocumentEvent)doc;
//			Event event = doc_event.createEvent("DOMEvent");
//			event.initEvent( "DOMEvent", true, true );
//			((EventTarget)e).dispatchEvent( event );
			
			return doc;

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		}
//		catch (SmbException e1) {
//			e1.printStackTrace();
//		}
		
		return null;
	}
	
	protected boolean connect() {
		return false;
	}
	
	protected void refresh() {

	}
	
	// https://jcifs.samba.org/src/docs/api/overview-summary.html#scp
	
//	The SmbFile, SmbFileInputStream , and SmbFileOutputStream classes are analogous to the File, FileInputStream, and FileOutputStream classes so if you know how to use those it should be quite obvious how to use jCIFS provided you set any necessary properties (i.e. a WINS server) and understand the smb:// URL syntax.
//
//		Here's an example to retrieve a file:
//
//		    import jcifs.smb.*;
//
//		    jcifs.Config.setProperty( "jcifs.netbios.wins", "192.168.1.220" );
//		    NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication("domain", "username", "password");
//		    SmbFileInputStream in = new SmbFileInputStream("smb://host/c/My Documents/somefile.txt", auth);
//		    byte[] b = new byte[8192];
//		    int n;
//		    while(( n = in.read( b )) > 0 ) {
//		        System.out.write( b, 0, n );
//		    }
//
//		You can also read/write, delete, make directories, rename, list contents of a directory, list the workgroups/ntdomains and servers on the network, list the shares of a server, open named pipes, authenticate web clients ...etc.
//


}
