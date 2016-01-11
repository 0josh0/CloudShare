package cn.ac.iscas.oncecloudshare.messaging.service.iospush;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;

import cn.ac.iscas.oncecloudshare.messaging.model.iospush.IOSDevice;
import cn.ac.iscas.oncecloudshare.messaging.repository.IOSDeviceDao;
import cn.ac.iscas.oncecloudshare.messaging.service.BaseService;

@Service
public class IOSDeviceService extends BaseService{

	private static final int VALID_DEVICE_TOKEN_LENGTH=64;
	
	@Autowired
	IOSDeviceDao dao;
	
	public IOSDevice find(Long id){
		return dao.findOne(id);
	}
	
	public Page<IOSDevice> findByUserId(Long userId,Pageable pageable){
		return dao.findByUserId(userId,pageable);
	}
	
	@Transactional
	public Page<IOSDevice> findByUserId(long tenantId,Long userId,Pageable pageable){
		changeTenantSchema(tenantId);
		return dao.findByUserId(userId,pageable);
	}
	
	public void addDevice(Long userId,String deviceToken,String description){
		userId=Preconditions.checkNotNull(userId);
		description=Preconditions.checkNotNull(description);
		deviceToken=Preconditions.checkNotNull(deviceToken);
		Preconditions.checkArgument(deviceToken.length()==VALID_DEVICE_TOKEN_LENGTH,
				"invalid device token");
		
		if(dao.findByUserIdAndDeviceToken(userId,deviceToken)!=null){
			return;
		}
		
		IOSDevice device=new IOSDevice();
		device.setUserId(userId);
		device.setDeviceToken(deviceToken);
		device.setDescription(description);
		
		dao.save(device);
	}
	
	@Transactional
	public boolean deleteDevice(Long id){
		IOSDevice device=dao.findOne(id);
		if(device!=null){
			dao.delete(device);
			return true;
		}
		else{
			return false;
		}
	}
	
	@Transactional
	public void deleteByUserIdAndDeviceToken(Long userId,String deviceToken){
		dao.deleteByUserIdAndDeviceToken(userId,deviceToken);
	}
}
