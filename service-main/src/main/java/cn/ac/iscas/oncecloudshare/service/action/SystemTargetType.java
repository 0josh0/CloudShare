package cn.ac.iscas.oncecloudshare.service.action;

import java.util.Collection;
import java.util.Set;

import cn.ac.iscas.oncecloudshare.service.model.log.ActionType;
import cn.ac.iscas.oncecloudshare.service.model.log.TargetType;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public enum SystemTargetType implements TargetType {
	FILE("file", "文件"),;

	private String code;
	private String name;
	private Set<ActionType> actionTypes = Sets.newHashSet();

	SystemTargetType(String code, String name) {
		this.code = code;
		this.name = name;
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void addActionType(ActionType actionType) {
		actionTypes.add(actionType);
	}

	@Override
	public Collection<ActionType> getActionTypes() {
		return ImmutableSet.<ActionType>copyOf(actionTypes);
	}	
}
