package cn.ac.iscas.oncecloudshare.messaging.xmpp.server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.vysper.xmpp.modules.Module;
import org.apache.vysper.xmpp.server.Endpoint;
import org.springframework.core.io.Resource;

public class OcsXmppServer extends CustomXMPPServer {

	protected final List<Module> listOfModules=new ArrayList<Module>();

	protected File certificateFile=null;
	protected String certificatePassword=null;

	public OcsXmppServer(String domain){
		super(domain);
	}

	public void setCertificateFile(Resource certificateFile) throws IOException{
		this.certificateFile=certificateFile.getFile();
	}

	public void setCertificatePassword(String certificatePassword){
		this.certificatePassword=certificatePassword;
	}

	public void setEndpoints(Collection<Endpoint> endpoints){
		for(Endpoint endpoint: endpoints){
			addEndpoint(endpoint);
		}
	}

	public void setModules(Collection<Module> modules){
		listOfModules.addAll(modules);
	}

	public void init() throws Exception{
		setTLSCertificateInfo(certificateFile,certificatePassword);
		start();
		if(listOfModules!=null){
			for(Module module: listOfModules){
				addModule(module);
			}
		}
	}

	public void destroy(){
		stop();
	}
	
}
