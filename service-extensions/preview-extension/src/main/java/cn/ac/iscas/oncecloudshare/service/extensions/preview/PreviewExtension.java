package cn.ac.iscas.oncecloudshare.service.extensions.preview;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.ac.iscas.oncecloudshare.service.system.extension.ListenerExtension;

public class PreviewExtension implements ListenerExtension{
	private static PreviewExtension instance;
	
	private List<String> supports = new ArrayList<String>();	
	
	public PreviewExtension(){
		if (instance != null){
			throw new RuntimeException("只允许一个PreviewExtension实例");
		}
		instance = this;
	}
	
	public static PreviewExtension getInstance(){
		if (instance == null){
			new PreviewExtension();
		}
		return instance;
	}
	
	@Override
	public Set<Object> getListeners() {
		return new HashSet<Object>(0);
	}

	public List<String> getSupports() {
		return supports;
	}
}