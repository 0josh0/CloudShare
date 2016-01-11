package cn.ac.iscas.oncecloudshare.messaging.service.authc.restclient;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import cn.ac.iscas.oncecloudshare.messaging.exceptions.RestServerException;
import cn.ac.iscas.oncecloudshare.messaging.utils.http.MediaTypes;

import com.google.common.collect.ImmutableList;

public class RestOperator {

	private static Logger logger=LoggerFactory.getLogger(RestOperator.class);
	
	private static final String MSG_SECRET_KEY_PARAM="x-msg-secret-key";
	
	private static final int SLEEP_TIME_UNIT=1000;
	private static final int MAX_RETRY_TIME=2;
	
	private RestTemplate restTemplate=null;
	
	private String msgSecretKey="";
	
	public RestOperator(String msgSecretKey){
		this.msgSecretKey=msgSecretKey;
		restTemplate=new RestTemplate();
		ClientHttpRequestInterceptor interceptor=new AuthInterceptor();
		restTemplate.setInterceptors(ImmutableList.of(interceptor));
		
	}
	
	public <T> T execute(RestAction<T> action){
		int retryTime=0;
		while(true){
			try{
				return action.apply(restTemplate);
			}
			catch(HttpClientErrorException e){
				return action.handleClientError(e);
			}
			catch(HttpServerErrorException e){
				/*
				 * 如果是5xx错误，则采用“指数后退”的方式重试MAX_RETRY_TIME遍
				 */
				retryTime++;
				if(retryTime>MAX_RETRY_TIME){
					throw new RestServerException(e);
				}
				try{
					Thread.sleep((long)(Math.pow(2,retryTime-1)*SLEEP_TIME_UNIT));
				}
				catch(InterruptedException e1){
					logger.error("error in retry sleep",e1);
				}
			}
		}
	}
	
	
	public static interface RestAction<T>{
		
		T apply(RestTemplate restTemplate);
		
		T handleClientError(HttpClientErrorException e);
	}
	
	public class AuthInterceptor implements ClientHttpRequestInterceptor {

		@Override
		public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
				throws IOException {
			request.getHeaders().set(MSG_SECRET_KEY_PARAM,msgSecretKey);
			request.getHeaders().set(com.google.common.net.HttpHeaders.ACCEPT,
					MediaTypes.JSON_UTF8);
			return execution.execute(request, body);
		}
	}
	
}
