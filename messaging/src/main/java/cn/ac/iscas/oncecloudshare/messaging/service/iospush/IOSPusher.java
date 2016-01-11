package cn.ac.iscas.oncecloudshare.messaging.service.iospush;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import javapns.devices.Device;
import javapns.devices.exceptions.InvalidDeviceTokenFormatException;
import javapns.devices.implementations.basic.BasicDevice;
import javapns.notification.AppleNotificationServer;
import javapns.notification.AppleNotificationServerBasicImpl;
import javapns.notification.Payload;
import javapns.notification.PushNotificationManager;
import javapns.notification.PushNotificationPayload;

import javax.annotation.PostConstruct;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import cn.ac.iscas.oncecloudshare.messaging.model.iospush.IOSDevice;

import com.google.common.collect.Lists;

@Service
public class IOSPusher {
	
	private static final int ALERT_MAX_LENGTH=30;
	
	private static Logger logger=LoggerFactory.getLogger(IOSPusher.class);
	
	@Autowired
	IOSDeviceService dService;
	
	private boolean ready=false;
	
	@Value("${apns.production}")
	private boolean production;

	@Value("${apns.keystore_path}")
	private String keystorePath;
	
	@Value("${apns.password}")
	private String password;
	
	private PushNotificationManager pushManager;
	private AppleNotificationServer notifServer;
	
	private ExecutorService executor;
	
	@PostConstruct
	private void init(){
		File keystoreFile=null;
		try{
			keystoreFile=new ClassPathResource(keystorePath).getFile();
		}
		catch(IOException e){
			logger.error("error reading keystore file: "+keystorePath,e);
			return;
		}
		
		pushManager=new PushNotificationManager();
		try{
			notifServer=new AppleNotificationServerBasicImpl(
					keystoreFile,password,production);
		}
		catch(KeystoreException e){
			logger.error("KeystoreException",e);
		}
		
		int coreThreadCount=10;
		int maxThreadCount=100;
		int threadTimeoutSeconds=1*60;
		this.executor=new ThreadPoolExecutor(coreThreadCount, maxThreadCount,
				threadTimeoutSeconds, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>());
		
		ready=true;
	}
	
	List<Device> findDevices(long tenantId,Long userId){
		List<Device> devices=Lists.newArrayList();
		for(IOSDevice iosDevice:dService.findByUserId(tenantId,userId,null)){
			try{
				devices.add(new BasicDevice(iosDevice.getDeviceToken()));
			}
			catch(InvalidDeviceTokenFormatException e){
				logger.warn("invalid device token {} for user {}",
						iosDevice.getDeviceToken(),userId);
			}
		}
		return devices;
	}
	
	List<Device> findDevices(long tenantId,List<Long> userIdList){
		List<Device> devices=Lists.newArrayListWithCapacity(userIdList.size());
		for(Long userId:userIdList){
			for(IOSDevice iosDevice:dService.findByUserId(tenantId,userId,null)){
				try{
					devices.add(new BasicDevice(iosDevice.getDeviceToken()));
				}
				catch(InvalidDeviceTokenFormatException e){
					logger.warn("invalid device token {} for user {}",
							iosDevice.getDeviceToken(),userId);
				}
			}
		}
		return devices;
	}
	
	public void pushNotification(long tenantId,String message,Long toUserId){
		if(!ready){
			logger.warn("iOS Pusher not ready");
			return;
		}
		List<Device> devices=findDevices(tenantId,toUserId);
		if(devices.size()>0){
			executor.submit(new PushTask(message,devices));
		}
	}
	
	public void pushNotification(long tenantId,String message,List<Long> toUserIdList){
		if(!ready){
			logger.warn("iOS Pusher not ready");
			return;
		}
		List<Device> devices=findDevices(tenantId,toUserIdList);
		if(devices.size()>0){
			executor.submit(new PushTask(message,devices));
		}
	}
	
	private class PushTask implements Callable<Boolean>{

		private Payload payload;
		private List<Device> devices;
		
		public PushTask(String message,List<Device> devices){
			if(message.length()>ALERT_MAX_LENGTH){
				message=message.substring(0,ALERT_MAX_LENGTH-3)+"...";
			}
			payload=null;
			try{
				payload=new PushNotificationPayload(message,1,"");
			}
			catch(JSONException e){
				logger.error("should not reach here");
				return;
			}
			this.devices=devices;
		}
		
		@Override
		public Boolean call() throws Exception{
			if(devices.isEmpty()){
				return true;
			}
			try{
				pushManager.initializeConnection(notifServer);
				pushManager.sendNotifications(payload,devices);
				return true;
			}
			catch(CommunicationException e){
				logger.error("failed to push iOS notification",e);
			}
			catch(KeystoreException e){
				logger.error("failed to push iOS notification",e);
			}
			finally{
				pushManager.stopConnection();
			}
			return false;
		}
	}
}
