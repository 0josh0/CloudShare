package cn.ac.iscas.oncecloudshare.service.model.log;

import java.util.Collection;

public interface TargetType {
	String getCode();
	
	String getName();
	
	void addActionType(ActionType actionType);
	
	Collection<ActionType> getActionTypes();
}