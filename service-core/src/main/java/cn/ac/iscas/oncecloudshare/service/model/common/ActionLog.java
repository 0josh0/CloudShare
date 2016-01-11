package cn.ac.iscas.oncecloudshare.service.model.common;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import cn.ac.iscas.oncecloudshare.service.model.IdEntity;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;

import com.google.gson.JsonObject;

@Entity
@Table(name = "ocs_action_log")
public class ActionLog extends IdEntity {
	// 操作人
	private User user;
	// 操作类型
	private String type;
	// 操作对象的类型
	private String targetType;
	// 操作对象的标识
	private String targetId;
	// 操作的参数
	private String params;
	
	private JsonObject jsonParams = new JsonObject();
	
	private String description;
	
	@ManyToOne
	@JoinColumn(name = "user_id")
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getParams() {
		if (params == null && jsonParams != null){
			params = Gsons.defaultGsonNoPrettify().toJson(jsonParams);
		}
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTargetType() {
		return targetType;
	}

	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}

	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	@Transient
	public JsonObject getJsonParams() {
		return jsonParams;
	}
	
	public void addParam(String name, Object value){
		jsonParams.add(name, Gsons.defaultGson().toJsonTree(value));
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
