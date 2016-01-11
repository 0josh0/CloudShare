package cn.ac.iscas.oncecloudshare.service.extensions.index;

import java.util.Set;

import cn.ac.iscas.oncecloudshare.service.extensions.index.listeners.DeleteIndexListener;
import cn.ac.iscas.oncecloudshare.service.extensions.index.listeners.UpdateIndexListener;
import cn.ac.iscas.oncecloudshare.service.system.extension.ListenerExtension;
import cn.ac.iscas.oncecloudshare.service.utils.spring.SpringUtil;

import com.google.common.collect.ImmutableSet;

public class IndexExtension implements ListenerExtension {

	@Override
	public Set<Object> getListeners() {
		return ImmutableSet.<Object> builder().add(SpringUtil.getBean(UpdateIndexListener.class))
				.add(SpringUtil.getBean(DeleteIndexListener.class)).build();
	}

}
