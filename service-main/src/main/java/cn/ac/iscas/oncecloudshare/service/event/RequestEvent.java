package cn.ac.iscas.oncecloudshare.service.event;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

public class RequestEvent {

	private final HttpServletRequest request;
	private final Date timestamp = new Date();

	public RequestEvent(HttpServletRequest request){
		this.request=request;
	}

	public HttpServletRequest getRequest(){
		return request;
	}
	
	public Date getTimestamp(){
		return timestamp;
	}
}
