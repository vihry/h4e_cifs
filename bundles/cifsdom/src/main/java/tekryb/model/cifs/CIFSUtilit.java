package tekryb.model.cifs;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

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
	
	//protected static ThreadPoolExecutor _executor;
	ExecutorService _executorService = Executors.newFixedThreadPool(10);
	
	protected List<InsertHandler> _insertHandlers = new ArrayList<InsertHandler>();
	
	protected List<DeleteHandler> _removeHandlers = new ArrayList<DeleteHandler>();
	
	protected List<AttrEventHandler> _attrHandlers = new ArrayList<AttrEventHandler>();
	
	protected BlockingQueue<Event> _events = new LinkedBlockingQueue<Event>();
	
	protected Thread _eventsThread;
	
	protected volatile boolean _stopEventThread = false;

	protected CIFSUtilit() {
	}
	
	// test only
	public static void main(String[] args) throws URISyntaxException, InterruptedException {
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
		
		Thread.yield();
		Thread.sleep( 100 );
	    
		activateNext( doc.getDocumentElement(), "", "*" );
		activateNext( doc.getDocumentElement(), "", "**" );
		//activateNext( doc.getDocumentElement(), "", "***" );
		
		Thread.yield();
		Thread.sleep( 100 );
		
		doSomething2( doc.getDocumentElement(), "" );
		
		int cnt = elem.getChildNodes().getLength();
		System.out.println( "doc with child length: " + cnt );
		
		
		// clean tree and stop working thread
    	clearChildren( doc.getDocumentElement() );
    	// wait until thread stops
    	Thread.sleep( 3000 );
	}
	
	public static void activateNext(Node node, String tag, String activate_string ) {
	    NodeList nodeList = node.getChildNodes();
	    for (int i = 0; i < nodeList.getLength(); i++) {
	        Node currentNode = nodeList.item(i);
	        if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
	            //calls this method for all the children which is Element
	        	activateNext(currentNode, tag + "*", activate_string );
	            
	            if ( tag.equals( activate_string ) ) {
	            	currentNode.getAttributes().getNamedItem( "active" ).setNodeValue( "true" );
	            }
	        }
	    }
	}
	
	public static void doSomething2(Node node, String tag) {
	    // do something with the current node instead of System.out
		String suffix = "";
		if ( node!=null ? node.getUserData("file")!=null : false ) {
			SmbFile f = (SmbFile)node.getUserData("file");
			suffix = f.getName();
		}
	    System.out.println( tag + " " + node.getNodeName() + " [ " + suffix + " ]" );

	    NodeList nodeList = node.getChildNodes();
	    for (int i = 0; i < nodeList.getLength(); i++) {
	        Node currentNode = nodeList.item(i);
	        if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
	            //calls this method for all the children which is Element
	            doSomething2(currentNode, tag + "*" );
	        }
	    }
	}
	
	public static Document open(final URI uri) {
		
		final CIFSUtilit that = new CIFSUtilit();
		
		synchronized (that) {
			if ( that._eventsThread==null ) {
				that._eventsThread = that.createEventThread();
				that._eventsThread.start();
			}	
		}

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
					
					// TODO add better info what happens
					System.out.println( "attr changed " + evt );
					
					EventTarget t = evt.getTarget();

					if ( t==that._nodeConnection ) {
						that.connect( false );
						
					} else {
						that._events.offer( evt );
						
					}

					} }, true );
			
			// remove
			((EventTarget) root).addEventListener( "DOMNodeRemoved", new EventListener() {
				public void handleEvent(Event evt) {
					
					// TODO add better info what happens
					System.out.println( "node removed " + evt );
					
					if ( evt.getTarget()==that._nodeConnection ) {
						that._stopEventThread = true;
					}
					
					that._events.offer( evt );

					} }, true );
			
			// insert
			((EventTarget) root).addEventListener( "DOMNodeInserted", new EventListener() {
				public void handleEvent(Event evt) {
					
					// TODO add better info what happens
					System.out.println( "node inserted " + evt );
					
					that._events.offer( evt );

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
			

//			SmbFile root = new SmbFile( "smb://someone_pc/tmp/test.xml",
//					new NtlmPasswordAuthentication("", username, password) );


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
	
	private static final void clearChildren(Node node) {
		// clean tree
    	Node child = node.getFirstChild();
    	while( (child = node.getFirstChild())!=null ) {
    		node.removeChild( child );
    	}
	}
	
	private static final void appendFiles(Node node, SmbFile file, StringBuilder sb ) throws SmbException, DOMException {
		if ( !file.isDirectory() ) {
			return;
		} 
		
		Document doc = node.getOwnerDocument();
		SmbFile files[] = file.listFiles();
		for (SmbFile child : files) {
			// not accepted by XML
			//name = Base64.getEncoder().encodeToString( name.getBytes(StandardCharsets.UTF_8) );
			//name = URLEncoder.encode( name, StandardCharsets.UTF_8.name() ); 
			
			//String str = new String( DatatypeConverter.printBase64Binary( name.getBytes()) );
		    //String res = DatatypeConverter.parseBase64Binary(str);
			
			sb.setLength(0);
			sb.append( new File( child.getName() ).getName() );
			
			System.out.println( sb.toString() );
			
			String node_name = clearForXML( sb.toString(), sb ).toString(); sb.setLength(0);
			Element e = doc.createElement( node_name );
			e.setUserData( "file", child, null );
			
			Attr attr_active = doc.createAttribute( "active" );
			attr_active.setValue( "false" );
			e.setAttributeNode( attr_active );
			
			node.appendChild( e );

		}
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
				        	clearChildren( _nodeFileSystemTree );
				        	
				        	_nodeFileSystemTree.setUserData( "file", _smbRoot, null );
				        	
				        	Document doc = _nodeFileSystemTree.getOwnerDocument();
				        	
				        	Attr attr_active = doc.createAttribute( "active" );
							attr_active.setValue( "true" );
							_nodeFileSystemTree.setAttributeNode( attr_active );
				        
//				        	StringBuilder sb = new StringBuilder();
//				        	//_treeIterator.start( that._smbRoot );
//							appendFiles( _nodeFileSystemTree, _smbRoot, sb );
							
				        } else {
				        	
				        }
	
				        _prevActive = new_value;
				        
				        is_ok = true;
				        
			        } catch ( MalformedURLException e ) {
			        	
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
		if ( evt instanceof MutationEvent ) {
			MutationEvent me = (MutationEvent) evt;
			Node target = (Node) me.getTarget();
			//target.
		}
		
		System.out.println( "thread#" + Thread.currentThread().getId() + " onInsert" );
	}
	
	protected void onRemove( Event evt ) {
		if ( evt instanceof MutationEvent ) {
			MutationEvent me = (MutationEvent) evt;
			Node target = (Node) me.getTarget();
			//target.
		}
		
		//that.remove( false );
		
		System.out.println( "thread#" + Thread.currentThread().getId() + " onRemove" );

	}
	
	protected void onAttrChanged( Event evt ) {
		
		System.out.println( "thread#" + Thread.currentThread().getId() + " onAttrChanged" );
		
		Node target = (Node)evt.getTarget();
		Node parent = target.getParentNode();

		if ( target instanceof Element ) {
			System.out.println( "element" );
		}
		
		if ( evt instanceof MutationEvent ) {
			MutationEvent me = (MutationEvent) evt;
			Attr attr = (Attr) me.getRelatedNode();
			
			if ( attr.getName().equals( "active" ) ) {
				if ( attr.getValue().equals("true") && !"true".equals( me.getPrevValue() ) ) {
					SmbFile file = (SmbFile)target.getUserData( "file" );
					if ( file==null ) {
						return;
					}

					StringBuilder sb = new StringBuilder();
					try {
						appendFiles( target, file, sb );

					} catch (DOMException e ) {
						e.printStackTrace();
						attr.setValue("false");
						return;
						
					} catch (SmbException e) {
						e.printStackTrace();
						attr.setValue("false");
						return;
					}
				}
			}
		}

	}
	
	protected void refresh() {

	}
	
	private void invokeLater( Runnable r ) {
		invokeLater( r, true );
	}
	
	private void invokeLater( Runnable r, boolean concurrent ) {
		if ( concurrent ) {
			_executorService.submit( r );

		} else {
			r.run();
		}
	}
	

	public static StringBuilder clearForXML(CharSequence s, StringBuilder res) {
		if ( res==null ) {
			res = new StringBuilder();
		}
		res.setLength( 0 );
		
		if ( s.length()<1 ) {
			return res;
		}
		
		// test first char
		char c = s.charAt(0);
	    if( !( c < 31 || c > 126 || "\\.<>\"'\\&$".indexOf(c)>=0 ) ) {
	        res.append(c);
	    }
		
		for(int i = 1; i<s.length(); i++) {
		    c = s.charAt(i);
		    if( c < 31 || c > 126 || "<>\"'\\&$".indexOf(c)>=0 ) {
		        //out.append("&#" + (int) c + ";");
		        
		    } else {
		        res.append(c);
		    }
		}
		
		return res;
	}
	
//	class EventHandler implements Runnable {
//		
//		Event _event;
//		boolean _prevActive = false;
//		//TreeIterator _treeIterator = new TreeIterator();
//		
//		public void run() {
//			 
//			synchronized ( _attrActive ) {
//
//				boolean is_ok = true;
//		        try {
//					String value = _attrActive.getNodeValue();
//			        boolean new_value = value.equalsIgnoreCase("true");
//			        if ( _prevActive==new_value ) {
//			        	return;
//			        }
//			        
//			        if ( new_value ) {
//			        	
//			        	if ( _smbRoot==null ) {
//			        		String str_uri = _attrURI.getValue( );
//			        		_smbRoot = new SmbFile( str_uri );
//			        		//that._smbRoot = new SmbFile( "smb://tunnel:tunnel@192.168.2.99/temp",
//							//		new NtlmPasswordAuthentication("", username, password) );
//			        	}
//			        	
//			        	// clean tree
//			        	Node child = _nodeFileSystemTree.getFirstChild();
//			        	while( (child = _nodeFileSystemTree.getFirstChild())!=null ) {
//			        		_nodeFileSystemTree.removeChild( child );
//			        	}
//			        
//			        	StringBuilder sb = new StringBuilder();
//			        	//_treeIterator.start( that._smbRoot );
//			        	Document doc = _nodeFileSystemTree.getOwnerDocument();
//						String files[] = _smbRoot.list();
//						for (String name : files) {
//							// not accepted by XML
//							//name = Base64.getEncoder().encodeToString( name.getBytes(StandardCharsets.UTF_8) );
//							//name = URLEncoder.encode( name, StandardCharsets.UTF_8.name() ); 
//							
//							//String str = new String( DatatypeConverter.printBase64Binary( name.getBytes()) );
//						    //String res = DatatypeConverter.parseBase64Binary(str);
//							String node_name = clearForXML( name, sb ).toString(); sb.setLength(0);
//							Element e = doc.createElement( node_name );
//							if ( !node_name.equals(name) ) {
//								e.appendChild( doc.createCDATASection( name ) );
//							}
//							
//							_nodeFileSystemTree.appendChild( e );
//							
//						}
//						
//			        } else {
//			        	
//			        }
//
//			        _prevActive = new_value;
//			        
//			        is_ok = true;
//			        
//		        } catch ( MalformedURLException e ) {
//		        	
//		        	is_ok = false;
//		        	
//		        	e.printStackTrace();
//		        	
//		        } catch (SmbException e) {
//					
//		        	is_ok = false;
//					
//		        	e.printStackTrace();
//		        	
//				} catch (DOMException e1) {
//					
//					is_ok = false;
//					
//					e1.printStackTrace();
//					
//				} finally {
//					if ( !is_ok ) {
//						_prevActive = false;
//						_attrActive.setValue( "false" );
//					}
//				}
//			}
//		}
//	};

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


//	@Override
//    public synchronized String getVersion() {
//        if (version != null) {
//            return version;
//        }
//        InputStream is = null;
//        // try to load from maven properties first
//        try {
//            Properties p = new Properties();
//            is = getClass().getResourceAsStream("/META-INF/maven/org.apache.camel/camel-core/pom.properties");
//            if (is != null) {
//                p.load(is);
//                version = p.getProperty("version", "");
//            }
//        } catch (Exception e) {
//            // ignore
//        } finally {
//            if (is != null) {
//                IOHelper.close(is);
//            }
//        }
//
//        // fallback to using Java API
//        if (version == null) {
//            Package aPackage = getClass().getPackage();
//            if (aPackage != null) {
//                version = aPackage.getImplementationVersion();
//                if (version == null) {
//                    version = aPackage.getSpecificationVersion();
//                }
//            }
//        }
//
//        if (version == null) {
//            // we could not compute the version so use a blank
//            version = "";
//        }
//
//        return version;
//    }

	private Thread createEventThread() {
		
		//_executor = new ThreadPoolExecutor( 3, 9 , keepAliveTime, unit, workQueue);
		
		return new Thread( new Runnable() {
			public void run() {
				while ( !_stopEventThread ) {
					try {
						final Event event = _events.poll( 1000, TimeUnit.MILLISECONDS );
						if ( event==null ) continue;
						
						DOMEventHandler handler = null;
						
						if ( event.getType().equalsIgnoreCase( "DOMNodeRemoved" ) ) {
							Iterator<DeleteHandler> it = _removeHandlers.iterator();
							while( it.hasNext() ) {
								DeleteHandler item = it.next();
								Lock lock = item.getLock();
								if ( lock.tryLock( 100, TimeUnit.MILLISECONDS ) ) {
									handler = item;
									break;
								}
							}
							
							if ( handler==null ) {
								handler = new DeleteHandler() {
									public void run() {
										onRemove( getEvent() ); getLock().unlock();
									}
								};
								handler.getLock().lock();
								
								_removeHandlers.add( (DeleteHandler)handler );
							}
							
							// update event to process
							handler.setEvent( event );
							
						} else if ( event.getType().equalsIgnoreCase( "DOMNodeInserted" ) ) {
							Iterator<InsertHandler> it = _insertHandlers.iterator();
							while( it.hasNext() ) {
								InsertHandler item = it.next();
								Lock lock = item.getLock();
								if ( lock.tryLock( 100, TimeUnit.MILLISECONDS ) ) {
									handler = item;
									break;
								}
							}
							
							if ( handler==null ) {
								handler = new InsertHandler() {
									public void run() {
										onInsert( getEvent() ); getLock().unlock();
									}
								};
								handler.getLock().lock();
								
								_insertHandlers.add( (InsertHandler) handler );
							}
							
							// update event to process
							handler.setEvent( event );

						} else if ( event.getType().equalsIgnoreCase( "DOMAttrModified" ) ) {
							Iterator<AttrEventHandler> it = _attrHandlers.iterator();
							while( it.hasNext() ) {
								AttrEventHandler item = it.next();
								Lock lock = item.getLock();
								if ( lock.tryLock( 100, TimeUnit.MILLISECONDS ) ) {
									handler = item;
									break;
								}
							}
							
							if ( handler==null ) {
								handler = new AttrEventHandler() {
									public void run() {
										onAttrChanged( getEvent() ); getLock().unlock();
									}
								};
								handler.getLock().lock();
								
								_attrHandlers.add( (AttrEventHandler) handler );
							}
							
							// update event to process
							handler.setEvent( event );

						}
						
						boolean concurent = false;
						invokeLater( handler, concurent );
						
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}, "CIFSUTILIT THREAD" );
	}
}
