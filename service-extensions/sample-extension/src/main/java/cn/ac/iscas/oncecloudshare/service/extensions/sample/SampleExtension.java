package cn.ac.iscas.oncecloudshare.service.extensions.sample;

import java.util.Set;

import org.springframework.stereotype.Component;

import cn.ac.iscas.oncecloudshare.service.system.extension.Extension;
import cn.ac.iscas.oncecloudshare.service.system.extension.ListenerExtension;

import com.google.common.collect.Sets;


public class SampleExtension implements ListenerExtension{

	@Override
	public Set<Object> getListeners(){
		return Sets.<Object>newHashSet(new SampleListener());
	}

}
