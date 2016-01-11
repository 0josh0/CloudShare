package cn.ac.iscas.oncecloudshare.service.filestorage.advance.service.storageclient;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;

public class Path{

	/** The directory separator, a slash. */
	public static final String SEPARATOR="/";
	public static final char SEPARATOR_CHAR='/';

	public static final String CUR_DIR=".";

	static final boolean WINDOWS=System.getProperty("os.name")
			.startsWith("Windows");

	private URI uri; // a hierarchical uri

	public Path(Path parent, String child){
		this(parent,new Path(child));
	}
	
	/** Resolve a child path against a parent path. */
	public Path(Path parent, Path child){
		// Add a slash to parent's path so resolution is compatible with URI's
		URI parentUri=parent.uri;
		String parentPath=parentUri.getPath();
		if(!(parentPath.equals("/") || parentPath.equals(""))){
			try{
				parentUri=new URI(parentUri.getScheme(),
						parentUri.getAuthority(),parentUri.getPath()+"/",null,
						parentUri.getFragment());
			}
			catch(URISyntaxException e){
				throw new IllegalArgumentException(e);
			}
		}
		URI resolved=parentUri.resolve(child.uri);
		initialize(resolved.getScheme(),resolved.getAuthority(),
				resolved.getPath(),resolved.getFragment());
	}

	private void checkPathArg(String path){
		// disallow construction of a Path from an empty string
		if(StringUtils.isBlank(path)){
			throw new IllegalArgumentException(
					"Can not create a Path from a null or empty string");
		}
	}

	/**
	 * Construct a path from a String. Path strings are URIs, but with unescaped
	 * elements and some additional normalization.
	 */
	public Path(String pathString){
		checkPathArg(pathString);

		// We can't use 'new URI(String)' directly, since it assumes things are
		// escaped, which we don't require of Paths.

		// add a slash in front of paths with Windows drive letters
		if(hasWindowsDrive(pathString,false))
			pathString="/"+pathString;

		// parse uri components
		String scheme=null;
		String authority=null;

		int start=0;

		// parse uri scheme, if any
		int colon=pathString.indexOf(':');
		int slash=pathString.indexOf('/');
		if( (colon!=-1) && ((slash==-1) || (colon<slash)) ){ // has a scheme
			scheme=pathString.substring(0,colon);
			start=colon+1;
		}

		// parse uri authority, if any
		if( pathString.startsWith("//",start) && 
				(pathString.length()-start>2) ){ // has authority
			int nextSlash=pathString.indexOf('/',start+2);
			int authEnd=nextSlash>0 ? nextSlash : pathString.length();
			authority=pathString.substring(start+2,authEnd);
			start=authEnd;
		}

		// uri path is the rest of the string -- query & fragment not supported
		String path=pathString.substring(start,pathString.length());

		initialize(scheme,authority,path,null);
	}

	/**
	 * Construct a path from a URI
	 */
	public Path(URI aUri){
		uri=aUri.normalize();
	}

	/** Construct a Path from components. */
	public Path(String scheme, String authority, String path){
		checkPathArg(path);
		initialize(scheme,authority,path,null);
	}

	private void initialize(String scheme,String authority,String path,
			String fragment){
		try{
			this.uri=new URI(scheme,authority,normalizePath(path),null,fragment)
					.normalize();
		}
		catch(URISyntaxException e){
			throw new IllegalArgumentException(e);
		}
	}

	private String normalizePath(String path){
		// remove double slashes & backslashes
		path=StringUtils.replace(path,"//","/");
		if(Path.WINDOWS){
			path=StringUtils.replace(path,"\\","/");
		}

		// trim trailing slash from non-root path (ignoring windows drive)
		int minLength=hasWindowsDrive(path,true) ? 4 : 1;
		if(path.length()>minLength && path.endsWith("/")){
			path=path.substring(0,path.length()-1);
		}

		return path;
	}

	private boolean hasWindowsDrive(String path,boolean slashed){
		if(!WINDOWS)
			return false;
		int start=slashed ? 1 : 0;
		return path.length()>=start+2
				&& (slashed ? path.charAt(0)=='/' : true)
				&& path.charAt(start+1)==':'
				&& ((path.charAt(start)>='A'&&path.charAt(start)<='Z') ||
					(path.charAt(start)>='a'&&path.charAt(start)<='z'));
	}

	/** Convert this to a URI. */
	public URI toUri(){
		return uri;
	}

	/**
	 * Is an absolute path (ie a slash relative path part) AND a scheme is null
	 * AND authority is null.
	 */
	public boolean isAbsoluteAndSchemeAuthorityNull(){
		return isUriPathAbsolute() 
				&& uri.getScheme()==null 
				&& uri.getAuthority()==null;
	}

	/**
	 * True if the path component (i.e. directory) of this URI is absolute.
	 */
	public boolean isUriPathAbsolute(){
		int start=hasWindowsDrive(uri.getPath(),true) ? 3 : 0;
		return uri.getPath().startsWith(SEPARATOR,start);
	}

	/** True if the path component of this URI is absolute. */
	/**
	 * There is some ambiguity here. An absolute path is a slash relative name
	 * without a scheme or an authority. So either this method was incorrectly
	 * named or its implementation is incorrect. This method returns true even
	 * if there is a scheme and authority.
	 */
	public boolean isAbsolute(){
		return isUriPathAbsolute();
	}

	/** Returns the final component of this path. */
	public String getName(){
		String path=uri.getPath();
		int slash=path.lastIndexOf(SEPARATOR);
		return path.substring(slash+1);
	}

	/** Returns the parent of a path or null if at root. */
	public Path getParent(){
		String path=uri.getPath();
		int lastSlash=path.lastIndexOf('/');
		int start=hasWindowsDrive(path,true) ? 3 : 0;
		if((path.length()==start)|| // empty path
				(lastSlash==start&&path.length()==start+1)){ // at root
			return null;
		}
		String parent;
		if(lastSlash==-1){
			parent=CUR_DIR;
		}
		else{
			int end=hasWindowsDrive(path,true) ? 3 : 0;
			parent=path.substring(0,lastSlash==end ? end+1 : lastSlash);
		}
		return new Path(uri.getScheme(),uri.getAuthority(),parent);
	}

//	public String toString(){
//		// we can't use uri.toString(), which escapes everything, because we
//		// want
//		// illegal characters unescaped in the string, for glob processing, etc.
//		StringBuilder buffer=new StringBuilder();
//		if(uri.getScheme()!=null){
//			buffer.append(uri.getScheme());
//			buffer.append(":");
//		}
//		if(uri.getAuthority()!=null){
//			buffer.append("//");
//			buffer.append(uri.getAuthority());
//		}
//		if(uri.getPath()!=null){
//			String path=uri.getPath();
//			if( path.indexOf('/')==0 
//				&& hasWindowsDrive(path,true) // has windows drive
//				&& uri.getScheme()==null // but no scheme
//				&& uri.getAuthority()==null) // or authority
//				path=path.substring(1); // remove slash before drive
//			buffer.append(path);
//		}
//		if(uri.getFragment()!=null){
//			buffer.append("#");
//			buffer.append(uri.getFragment());
//		}
//		return buffer.toString();
//	}
	
	@Override
	public String toString(){
		return uri.toString();
	}
	
	public String getPath(){
		return uri.getPath();
	}

	public boolean equals(Object o){
		if(!(o instanceof Path)){
			return false;
		}
		Path that=(Path)o;
		return this.uri.equals(that.uri);
	}

	public int hashCode(){
		return uri.hashCode();
	}

	/** Return the number of elements in this path. */
	public int depth(){
		String path=uri.getPath();
		int depth=0;
		int slash=path.length()==1&&path.charAt(0)=='/' ? -1 : 0;
		while(slash!=-1){
			depth++;
			slash=path.indexOf(SEPARATOR,slash+1);
		}
		return depth;
	}

}
