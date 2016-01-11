package cn.ac.iscas.oncecloudshare.service.service.authorization.principal;


public class AnonPrincipal implements Principal {
	
	private static final long serialVersionUID=-6689736114508965953L;
	
	private static final AnonPrincipal INSTANCE=new AnonPrincipal(); 

	private AnonPrincipal() {
	}
	
	public static AnonPrincipal of(){
		return INSTANCE;
	}
}