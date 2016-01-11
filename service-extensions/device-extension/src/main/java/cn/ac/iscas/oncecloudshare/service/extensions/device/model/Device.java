package cn.ac.iscas.oncecloudshare.service.extensions.device.model;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import cn.ac.iscas.oncecloudshare.service.model.IdEntity;

@Entity
@Table(name = "ocs_device", uniqueConstraints = { @UniqueConstraint(columnNames = { "mac" }) })
public class Device extends IdEntity {
	private String mac;
	private String net;
	private Type type;
	private String hardware;
	private String osType;
	private String osVersion;
	
	public Device(){
		super();		
	}
	
	public Device(Type type, String mac){
		super();
		this.type = type;
		this.mac = mac;
	}

	public Device(String mac, String net, Type type, String hardware, String osType, String osVersion) {
		super();
		this.mac = mac;
		this.net = net;
		this.type = type;
		this.hardware = hardware;
		this.osType = osType;
		this.osVersion = osVersion;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public String getNet() {
		return net;
	}

	public void setNet(String net) {
		this.net = net;
	}

	@Enumerated(EnumType.STRING)
	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getHardware() {
		return hardware;
	}

	public void setHardware(String hardware) {
		this.hardware = hardware;
	}

	public String getOsType() {
		return osType;
	}

	public void setOsType(String osType) {
		this.osType = osType;
	}

	public String getOsVersion() {
		return osVersion;
	}

	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

	public enum Type {
		web, web_admin, undefined, phone, pad, pc;

		public boolean equals(String s) {
			return this.toString().equals(s);
		}
	}

	public enum ClientType {
		iphone, android, pc
	}
}