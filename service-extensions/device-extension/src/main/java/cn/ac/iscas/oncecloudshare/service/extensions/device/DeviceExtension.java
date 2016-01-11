package cn.ac.iscas.oncecloudshare.service.extensions.device;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import cn.ac.iscas.oncecloudshare.service.event.login.LoginEvent;
import cn.ac.iscas.oncecloudshare.service.extensions.device.dto.DeviceDto;
import cn.ac.iscas.oncecloudshare.service.extensions.device.service.DeviceService;
import cn.ac.iscas.oncecloudshare.service.system.extension.ListenerExtension;
import cn.ac.iscas.oncecloudshare.service.system.extension.event.SubscribeEvent;
import cn.ac.iscas.oncecloudshare.service.utils.spring.SpringUtil;

import com.google.common.collect.Sets;

public class DeviceExtension implements ListenerExtension {
	private static DeviceExtension instance;

	private List<String> supports = new ArrayList<String>();

	public DeviceExtension() {
		if (instance != null) {
			throw new RuntimeException("只允许一个DeviceExtension实例");
		}
		instance = this;
	}

	public static DeviceExtension getInstance() {
		if (instance == null) {
			new DeviceExtension();
		}
		return instance;
	}

	@Override
	public Set<Object> getListeners() {
		Set<Object> listeners = Sets.newHashSet();
		listeners.add(this);
		return listeners;
	}

	public List<String> getSupports() {
		return supports;
	}

	@SubscribeEvent
	public void handleLogin(LoginEvent event) {
		DeviceDto device = DeviceDto.RequestTransformer.apply(event.getRequest());
		((DeviceService) SpringUtil.getBean(DeviceService.class)).userLogin(event.getUserId(), device, getIpFromHeader(event.getRequest()));
	}

	public static String getIpFromHeader(HttpServletRequest request) {
		String agentIP = request.getHeader("X-IP");
		if (StringUtils.isEmpty(agentIP)) {
			agentIP = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (StringUtils.isEmpty(agentIP)) {
			agentIP = request.getRemoteAddr();
		}
		return agentIP;
	}
}