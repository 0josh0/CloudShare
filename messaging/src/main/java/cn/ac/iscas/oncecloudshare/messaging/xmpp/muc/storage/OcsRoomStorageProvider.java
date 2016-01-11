package cn.ac.iscas.oncecloudshare.messaging.xmpp.muc.storage;

import java.util.Collection;

import org.apache.vysper.xmpp.addressing.Entity;
import org.apache.vysper.xmpp.modules.extension.xep0045_muc.model.Room;
import org.apache.vysper.xmpp.modules.extension.xep0045_muc.model.RoomType;
import org.apache.vysper.xmpp.modules.extension.xep0045_muc.storage.RoomStorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.ac.iscas.oncecloudshare.messaging.model.muc.MucRoom;
import cn.ac.iscas.oncecloudshare.messaging.model.multitenancy.TenantRoom;
import cn.ac.iscas.oncecloudshare.messaging.service.muc.OccupantService;
import cn.ac.iscas.oncecloudshare.messaging.service.muc.RoomService;
import cn.ac.iscas.oncecloudshare.messaging.utils.JIDUtil;
import cn.ac.iscas.oncecloudshare.messaging.xmpp.muc.model.XmppRoom;

import com.google.common.collect.ImmutableSet;


public class OcsRoomStorageProvider implements RoomStorageProvider {
	
	private static Logger logger=LoggerFactory.getLogger(OcsRoomStorageProvider.class);

	OcsOccupantStorageProvider occupantProvider;
	
	RoomService rService;
	OccupantService oService;
	
	public OcsRoomStorageProvider(OcsOccupantStorageProvider occupantProvider,
			RoomService rService,OccupantService oService){
		this.occupantProvider=occupantProvider;
		this.rService=rService;
		this.oService=oService;
	}
	
	@Override
	public void initialize(){
		
	}

	@Override
	public Room createRoom(Entity jid,String name,RoomType... roomTypes){
		throw new UnsupportedOperationException("createRoom not supported here");
	}

	@Override
	public Collection<Room> getAllRooms(){
//		return ImmutableSet.copyOf(Iterables.transform(
//				rService.findAll(null),
//				new Function<MucRoom,Room>(){
//					@Override
//					public XmppRoom apply(MucRoom input){
//						return new XmppRoom(input,occupantProvider);
//					}
//				}
//		));
		return ImmutableSet.of();
	}

	@Override
	public boolean roomExists(Entity jid){
		return findRoom(jid)!=null;
	}

	@Override
	public Room findRoom(Entity jid){
		TenantRoom room=JIDUtil.parseTenantRoom(jid);
		if(room==null){
			return null;
		}
		MucRoom mucRoom=rService.findOne(room.getTenantId(),room.getRoomId());
		if(mucRoom==null){
			return null;
		}
		return new XmppRoom(room,occupantProvider);
	}

	@Override
	public void deleteRoom(Entity jid){
		throw new UnsupportedOperationException("deleteRoom not supported here");
	}

}
