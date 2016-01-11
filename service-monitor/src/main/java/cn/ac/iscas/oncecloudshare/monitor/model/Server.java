package cn.ac.iscas.oncecloudshare.monitor.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import cn.ac.iscas.oncecloudshare.service.model.IdEntity;

@Entity
@Table(name = "ocm_server")
public class Server extends IdEntity {
	@NotNull
	@Length(min = 1, max = 64)
	private String host;
	@NotNull
	private int port;
	@NotNull
	@Length(min = 1, max = 64)
	private String name;
	private Boolean crashed = Boolean.TRUE;
	private Long checkTime;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getCrashed() {
		return crashed;
	}

	public void setCrashed(Boolean crashed) {
		this.crashed = crashed;
	}

	public Long getCheckTime() {
		return checkTime;
	}

	public void setCheckTime(Long checkTime) {
		this.checkTime = checkTime;
	}

	public void setCheckTime(Date checkTime) {
		this.checkTime = checkTime.getTime();
	}
}
