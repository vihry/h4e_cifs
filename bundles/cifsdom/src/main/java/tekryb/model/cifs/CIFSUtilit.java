package tekryb.model.cifs;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
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
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.TreeWalker;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * 
 */
public class CIFSUtilit  {
	
	public static final String CIFSDOM_ROOT = "cifs";
	
	public static final String CIFSDOM_CONNECTION = "connection";
	
	public static final String CIFSDOM_OPLOG = "log";
	
	public static final String CIFSDOM_FSTREE = "fsTree";
	
	public static final String CIFSDOM_EXECUTOR = "threadPoolExecutor";
	
	protected Element _nodeConnection;
	
	protected Element _nodeFileSystemTree;
	
	protected Element _domThreadExecutor;
	
	protected Attr _attrActive;
	
	protected Attr _attrURI;
	
	protected SmbFile _smbRoot;
	
	protected static ThreadPoolExecutor _executor;
	
	protected List<InsertHandler> _insertHandlers = new ArrayList<InsertHandler>();
	
	protected List<DeleteHandler> _removeHandlers = new ArrayList<DeleteHandler>();

	protected CIFSUtilit() {
		
		_insertHandlers.add( new InsertHandler() );
		_insertHandlers.add( new InsertHandler() );
		_insertHandlers.add( new InsertHandler() );
		
		_removeHandlers.add( new DeleteHandler() );
		_removeHandlers.add( new DeleteHandler() );
		_removeHandlers.add( new DeleteHandler() );
		
	}
	
	// test only
	public static void main(String[] args) throws URISyntaxException {
		Document doc = CIFSUtilit.open( new URI( "smb://tunnel:tunnel@192.168.2.99/" ) );
		//Document doc = CIFSUtilit.open( new URI( "smb://tunnel:tunnel@192.168.2.99/temp/" ) );
		
		Element elem = doc.getDocumentElement();
		NodeList nl = doc.getElementsByTagName( CIFSDOM_CONNECTION );
		if ( nl.getLength()>0 ) {
			Element e = (Element) nl.item(0);
			e.setAttribute( "active", "true" );
		}
		
		// this cast is checked on Apache implementation (Xerces):
	    //DocumentTraversal traversal = (DocumentTraversal) doc;
	    //TreeWalker walker = traversal.createTreeWalker( document.getDocumentElement(), NodeFilter.SHOW_ELEMENT, null, true);
	    
		doSomething( doc.getDocumentElement(), "" );
		
		int cnt = doc.getChildNodes().getLength();
		System.out.println( "doc with child length: " + cnt );
	}
	
	public static void doSomething(Node node, String tag) {
	    // do something with the current node instead of System.out
		Node child = node.getFirstChild();
		String suffix = "";
		if ( child!=null ? child.getNodeType()==Node.CDATA_SECTION_NODE : false ) {
			suffix = child.getNodeValue();
		}
	    System.out.println( tag + " " + node.getNodeName() + " [ " + suffix + " ]" );

	    NodeList nodeList = node.getChildNodes();
	    for (int i = 0; i < nodeList.getLength(); i++) {
	        Node currentNode = nodeList.item(i);
	        if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
	            //calls this method for all the children which is Element
	            doSomething(currentNode, tag + "*" );
	        }
	    }
	}
	
	public static Document open(final URI uri) {
		
		final CIFSUtilit that = new CIFSUtilit();

		try {
			
			DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
			DocumentBuilder b = f.newDocumentBuilder();
			
			Document doc = b.newDocument();
			if ( !( doc instanceof EventTarget && doc instanceof DocumentEvent ) ) {
				System.out.println( "dom implementation does not suppoert events" );
				return null;
			}

			final Node root = doc.appendChild( doc.createElement( CIFSDOM_ROOT ) );
			that._nodeConnection = (Element)root.appendChild( doc.createElement( CIFSDOM_CONNECTION ) );
			that._nodeFileSystemTree = (Element)root.appendChild( doc.createElement( CIFSDOM_FSTREE ) );
			that._domThreadExecutor = (Element)root.appendChild( doc.createElement( CIFSDOM_EXECUTOR ) );
			
			that._attrActive = doc.createAttribute( "active" );
			that._attrActive.setValue( "false" );
			that._nodeConnection.setAttributeNode( that._attrActive );
			
			that._attrURI = doc.createAttribute( "uri" );
			that._attrURI.setValue( uri.toString() );
			that._nodeConnection.setAttributeNode( that._attrURI );

			// populateTree( doc );
			
			// DOMAttrModified,DOMCharacterDataModified,
			// DOMNodeInserted,DOMNodeInsertedIntoDocument
			// DOMNodeRemoved,DOMNodeRemovedFromDocument,
			// DOMSubtreeModified
			
			// connect
			((EventTarget) root).addEventListener( "DOMAttrModified", new EventListener() {
				public void handleEvent(Event evt) {
					
					System.out.println( "attr changed" + evt );

					that.connect( false );

					} }, true );
			
			// remove
			((EventTarget) root).addEventListener( "DOMNodeRemoved", new EventListener() {
				public void handleEvent(Event evt) {
					
					System.out.println( "node removed" + evt );
					
					that.onRemove( evt );

					} }, true );
			
			// insert
			((EventTarget) root).addEventListener( "DOMNodeInserted", new EventListener() {
				public void handleEvent(Event evt) {
					
					System.out.println( "node inserted" + evt );
					
					if ( evt instanceof MutationEvent ) {
						MutationEvent me = (MutationEvent) evt;
						Node target = (Node) me.getTarget();
						//target.
					}

					that.onInsert( evt );

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

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private Runnable _runConnect = new Runnable() {
			boolean _prevActive = false;
			//TreeIterator _treeIterator = new TreeIterator();
			
			public void run() {
				 
				synchronized ( _attrActive ) {

					boolean is_ok = true;
			        try {
						String value = _attrActive.getNodeValue();
				        boolean new_value = value.equalsIgnoreCase("true");
				        if ( _prevActive==new_value ) {
				        	return;
				        }
				        
				        if ( new_value ) {
				        	
				        	if ( _smbRoot==null ) {
				        		String str_uri = _attrURI.getValue( );
				        		_smbRoot = new SmbFile( str_uri );
				        		//that._smbRoot = new SmbFile( "smb://tunnel:tunnel@192.168.2.99/temp",
								//		new NtlmPasswordAuthentication("", username, password) );
				        	}
				        	
				        	// clean tree
				        	Node child = _nodeFileSystemTree.getFirstChild();
				        	while( (child = _nodeFileSystemTree.getFirstChild())!=null ) {
				        		_nodeFileSystemTree.removeChild( child );
				        	}
				        
				        	StringBuilder sb = new StringBuilder();
				        	//_treeIterator.start( that._smbRoot );
				        	Document doc = _nodeFileSystemTree.getOwnerDocument();
							String files[] = _smbRoot.list();
							for (String name : files) {
								// not accepted by XML
								//name = Base64.getEncoder().encodeToString( name.getBytes(StandardCharsets.UTF_8) );
								//name = URLEncoder.encode( name, StandardCharsets.UTF_8.name() ); 
								
								//String str = new String( DatatypeConverter.printBase64Binary( name.getBytes()) );
							    //String res = DatatypeConverter.parseBase64Binary(str);
								String node_name = clearForXML( name, sb ).toString(); sb.setLength(0);
								Element e = doc.createElement( node_name );
								if ( !node_name.equals(name) ) {
									e.appendChild( doc.createCDATASection( name ) );
								}
								
								_nodeFileSystemTree.appendChild( e );
								
							}
							
				        } else {
				        	
				        }
	
				        _prevActive = new_value;
				        
				        is_ok = true;
				        
			        } catch ( MalformedURLException e ) {
			        	
			        	is_ok = false;
			        	
			        	e.printStackTrace();
			        	
			        } catch (SmbException e) {
						
			        	is_ok = false;
						
			        	e.printStackTrace();
			        	
					} catch (DOMException e1) {
						
						is_ok = false;
						
						e1.printStackTrace();
						
					} finally {
						if ( !is_ok ) {
							_prevActive = false;
							_attrActive.setValue( "false" );
						}
					}
				}
			}
		};
	
	protected void connect( boolean concurent ) {
		invokeLater( _runConnect, concurent );
	}
	
	protected void onInsert( Event evt ) {
		boolean concurent = false;
		invokeLater( _runConnect, concurent );

		if ( evt instanceof MutationEvent ) {
			MutationEvent me = (MutationEvent) evt;
			Node target = (Node) me.getTarget();
			//target.
		}
	}
	
	protected void onRemove( Event evt ) {
		boolean concurent = false;
		invokeLater( _runConnect, concurent );

		if ( evt instanceof MutationEvent ) {
			MutationEvent me = (MutationEvent) evt;
			Node target = (Node) me.getTarget();
			//target.
		}
		//that.remove( false );

	}
	
	protected void refresh() {

	}
	
	private void invokeLater( Runnable r ) {
		invokeLater( r, true );
	}
	
	private void invokeLater( Runnable r, boolean concurrent ) {
		if ( concurrent ) {
			_executor.execute( r );
		} else {
			r.run();
		}
	}
	

	public static StringBuilder clearForXML(CharSequence s, StringBuilder res) {
		if ( res==null ) {
			res = new StringBuilder();
		}
		
		for(int i = 0; i<s.length(); i++) {
		    char c = s.charAt(i);
		    if( c < 31 || c > 126 || "<>\"'\\&$".indexOf(c)>=0 ) {
		        //out.append("&#" + (int) c + ";");
		        
		    } else {
		        res.append(c);
		    }
		}
		
		return res;
	}
	
	class EventHandler implements Runnable {
		
		Event _event;
		boolean _prevActive = false;
		//TreeIterator _treeIterator = new TreeIterator();
		
		public void run() {
			 
			synchronized ( _attrActive ) {

				boolean is_ok = true;
		        try {
					String value = _attrActive.getNodeValue();
			        boolean new_value = value.equalsIgnoreCase("true");
			        if ( _prevActive==new_value ) {
			        	return;
			        }
			        
			        if ( new_value ) {
			        	
			        	if ( _smbRoot==null ) {
			        		String str_uri = _attrURI.getValue( );
			        		_smbRoot = new SmbFile( str_uri );
			        		//that._smbRoot = new SmbFile( "smb://tunnel:tunnel@192.168.2.99/temp",
							//		new NtlmPasswordAuthentication("", username, password) );
			        	}
			        	
			        	// clean tree
			        	Node child = _nodeFileSystemTree.getFirstChild();
			        	while( (child = _nodeFileSystemTree.getFirstChild())!=null ) {
			        		_nodeFileSystemTree.removeChild( child );
			        	}
			        
			        	StringBuilder sb = new StringBuilder();
			        	//_treeIterator.start( that._smbRoot );
			        	Document doc = _nodeFileSystemTree.getOwnerDocument();
						String files[] = _smbRoot.list();
						for (String name : files) {
							// not accepted by XML
							//name = Base64.getEncoder().encodeToString( name.getBytes(StandardCharsets.UTF_8) );
							//name = URLEncoder.encode( name, StandardCharsets.UTF_8.name() ); 
							
							//String str = new String( DatatypeConverter.printBase64Binary( name.getBytes()) );
						    //String res = DatatypeConverter.parseBase64Binary(str);
							String node_name = clearForXML( name, sb ).toString(); sb.setLength(0);
							Element e = doc.createElement( node_name );
							if ( !node_name.equals(name) ) {
								e.appendChild( doc.createCDATASection( name ) );
							}
							
							_nodeFileSystemTree.appendChild( e );
							
						}
						
			        } else {
			        	
			        }

			        _prevActive = new_value;
			        
			        is_ok = true;
			        
		        } catch ( MalformedURLException e ) {
		        	
		        	is_ok = false;
		        	
		        	e.printStackTrace();
		        	
		        } catch (SmbException e) {
					
		        	is_ok = false;
					
		        	e.printStackTrace();
		        	
				} catch (DOMException e1) {
					
					is_ok = false;
					
					e1.printStackTrace();
					
				} finally {
					if ( !is_ok ) {
						_prevActive = false;
						_attrActive.setValue( "false" );
					}
				}
			}
		}
	};

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
