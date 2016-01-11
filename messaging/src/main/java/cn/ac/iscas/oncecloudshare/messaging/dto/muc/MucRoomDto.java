package cn.ac.iscas.oncecloudshare.messaging.dto.muc;

import java.util.Date;

import com.google.common.base.Function;

import cn.ac.iscas.oncecloudshare.messaging.model.muc.MucRoom;


public class MucRoomDto {

	public static final Function<MucRoom,MucRoomDto> TRANSFORMER=
			new Function<MucRoom,MucRoomDto>(){

				@Override
				public MucRoomDto apply(MucRoom input){
					return new MucRoomDto(input);
				}
	};
	
	public final Long id;
	public final String subject;
	public final Long owner;
	public final boolean special;
	public final Date createTime;
//	public final int nOccupants;
	
	private MucRoomDto(MucRoom mucRoom){
		this.id=mucRoom.getId();
		this.subject=mucRoom.getSubject();
		this.owner=mucRoom.getOwner();
		this.special=mucRoom.getSpecial();
		this.createTime=mucRoom.getCreateTime();
	}
	
	public static MucRoomDto of(MucRoom mucRoom){
		return TRANSFORMER.apply(mucRoom);
	}
}
