package cn.ac.iscas.oncecloudshare.messaging.service.authc.restclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;

import cn.ac.iscas.oncecloudshare.messaging.model.authc.UserInfo;
import cn.ac.iscas.oncecloudshare.messaging.service.authc.restclient.RestOperator.RestAction;
import cn.ac.iscas.oncecloudshare.messaging.utils.gson.Gsons;

@Component
public class RestClient{

	private static Logger logger=LoggerFactory.getLogger(RestClient.class);
	
	private static RestClient instance=null;
	
	public static RestClient getInstance(){
		return instance;
	}

	private RestOperator operator;
	
	private Gson gson=Gsons.gsonForLogging();
	
	private UrlBuilder urlBuilder;
	
	@Autowired
	public RestClient(
			@Value(value="${rest.server}") String restServerAddr,
			@Value(value="${rest.msg_secret_key}") String msgSecretKey){
		this.urlBuilder=new UrlBuilder(restServerAddr);
		operator=new RestOperator(msgSecretKey);
	}
	
	private void logError(HttpClientErrorException e){
		if(!e.getStatusCode().equals(HttpStatus.NOT_FOUND)){
			String resp=e.getResponseBodyAsString();
			logger.error("error communicating serivce: \n Response: "+resp,e);
		}
	}
	
	public UserInfo getUserInfo(final long tenantId,final long userId){
		return operator.execute(new RestAction<UserInfo>(){

			@Override
			public UserInfo apply(RestTemplate restTemplate){
				String response=restTemplate.getForObject(
						urlBuilder.getUserInfo(tenantId,userId),
						String.class);
				return gson.fromJson(response,UserInfo.class);
			}

			@Override
			public UserInfo handleClientError(HttpClientErrorException e){
				logError(e);
				return null;
			}

		});
	}
	
	public UserInfo getUserInfoByTicket(final long tenantId,final String ticket){
		return operator.execute(new RestAction<UserInfo>(){

			@Override
			public UserInfo apply(RestTemplate restTemplate){
				String response=restTemplate.getForObject(
						urlBuilder.verifyTicket(tenantId,ticket),
						String.class);
				return gson.fromJson(response,UserInfo.class);
			}

			@Override
			public UserInfo handleClientError(HttpClientErrorException e){
				logError(e);
				return null;
			}
		});
	}
	
	private class UrlBuilder {
		
		String restServerAddr;
		
		public UrlBuilder(String restServerAddr){
			this.restServerAddr=restServerAddr;
		}
				
		public String restServerAddr(){
			return restServerAddr;
		}
		
		public String getUserInfo(long tenantId,long userId){
			return restServerAddr()+String.format(
					"/api/v2/messaging/userInfo?x-tenant-id=%s&userId=%s",tenantId,userId);
		}
		
		public String verifyTicket(long tenantId,String ticket){
			return restServerAddr()+String.format(
					"/api/v2/messaging/ticketInfo?x-tenant-id=%s&ticket=%s",tenantId,ticket);
		}
	}
}
