package cn.ac.iscas.oncecloudshare.service.model.log;

public interface ActionType {
	String getCode();
	
	String getName();
	
	TargetType getTarget();
}