package cn.ac.iscas.oncecloudshare.messaging.dto.iospush;

import com.google.common.base.Function;

import cn.ac.iscas.oncecloudshare.messaging.model.iospush.IOSDevice;


public final class IOSDeviceDto {
	
	public static final Function<IOSDevice,IOSDeviceDto> TRANSFORMER=
			new Function<IOSDevice,IOSDeviceDto>(){

				@Override
				public IOSDeviceDto apply(IOSDevice input){
					return new IOSDeviceDto(input);
				}
			};

	public final Long id;
	public final String deviceToken;
	public final String description;
	
	private IOSDeviceDto(IOSDevice device){
		this.id=device.getId();
		this.deviceToken=device.getDeviceToken();
		this.description=device.getDescription();
	}
	
	public static IOSDeviceDto of(IOSDevice device){
		return TRANSFORMER.apply(device);
	}
}
