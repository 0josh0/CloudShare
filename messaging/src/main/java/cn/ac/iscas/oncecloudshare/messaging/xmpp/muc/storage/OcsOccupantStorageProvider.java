package cn.ac.iscas.oncecloudshare.messaging.xmpp.muc.storage;

import java.util.List;
import java.util.Map;
import org.apache.vysper.xmpp.addressing.Entity;
import org.apache.vysper.xmpp.modules.extension.xep0045_muc.model.Occupant;
import org.apache.vysper.xmpp.modules.extension.xep0045_muc.storage.OccupantStorageProvider;

import cn.ac.iscas.oncecloudshare.messaging.model.muc.MucOccupant;
import cn.ac.iscas.oncecloudshare.messaging.service.muc.OccupantService;
import cn.ac.iscas.oncecloudshare.messaging.utils.JIDUtil;
import cn.ac.iscas.oncecloudshare.messaging.xmpp.muc.model.XmppRoom;

import com.google.common.collect.ImmutableMap;


public class OcsOccupantStorageProvider implements OccupantStorageProvider{
	
	OccupantService oService;
	
	public OcsOccupantStorageProvider(OccupantService oService){
		this.oService=oService;
	}

	@Override
	public void initialize(){
		
	}

//	public Set<Occupant> findOccupantsByRoomId(final XmppRoom room){
//		return ImmutableSet.copyOf(findOccupantsMapByRoomId(room).values());
//	}
	
	/**
	 * Key: userId
	 * Value-Occupant:
	 *     jid: {tennatId}-{userId}@cloudshare.com
	 *     nickName: userId 
	 * @param tenantId
	 * @param room
	 * @return
	 */
	public Map<Long,Occupant> findOccupantsMapByRoomId(long tenantId,final XmppRoom room){
		List<MucOccupant> occupants=oService
				.findByRoomId(tenantId,room.getRoomId(),null)
				.getContent();
		ImmutableMap.Builder<Long,Occupant> mapBuilder=ImmutableMap.builder();
		for(MucOccupant o:occupants){
			Entity jid=JIDUtil.buildEntity(tenantId,o.getUserId());
			mapBuilder.put(o.getUserId(),
					new Occupant(jid,o.getUserId()+"",room,o.getRole()));
		}
		return mapBuilder.build();
	}
}
