package cn.ac.iscas.oncecloudshare.service.model.multitenancy;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;

import cn.ac.iscas.oncecloudshare.service.model.IdEntity;
import cn.ac.iscas.oncecloudshare.service.utils.Constants;


@Entity
@Table(name = "ocs_plan" ,schema=  Constants.GLOBAL_SCHEMA)
public class Plan extends IdEntity {
	@Transient
	private static final String TABLE_NAME = "ocs_plan";

	@Enumerated(EnumType.STRING)
	private Type type;
	private Integer members;
	private Long quota;

	public Integer getMembers() {
		return members;
	}

	public void setMembers(Integer members) {
		this.members = members;
	}

	public Long getQuota() {
		return quota;
	}

	public void setQuota(Long quota) {
		this.quota = quota;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public enum Type {
		A, B, C;
		public static Type of(String input) {
			for (Type value : Type.values()) {
				if (value.name().equalsIgnoreCase(input))
					return value;
			}
			return null;
		}
	}
}
