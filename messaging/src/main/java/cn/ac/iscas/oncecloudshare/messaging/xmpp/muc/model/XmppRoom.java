package cn.ac.iscas.oncecloudshare.messaging.xmpp.muc.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.vysper.xmpp.addressing.Entity;
import org.apache.vysper.xmpp.modules.extension.xep0045_muc.model.DiscussionHistory;
import org.apache.vysper.xmpp.modules.extension.xep0045_muc.model.Occupant;
import org.apache.vysper.xmpp.modules.extension.xep0045_muc.model.Room;
import org.apache.vysper.xmpp.modules.extension.xep0045_muc.model.RoomType;
import org.apache.vysper.xmpp.modules.servicediscovery.management.InfoRequest;
import org.apache.vysper.xmpp.modules.servicediscovery.management.Item;
import org.apache.vysper.xmpp.modules.servicediscovery.management.ServiceDiscoveryRequestException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Longs;

import cn.ac.iscas.oncecloudshare.messaging.model.multitenancy.TenantRoom;
import cn.ac.iscas.oncecloudshare.messaging.model.multitenancy.TenantUser;
import cn.ac.iscas.oncecloudshare.messaging.utils.JIDUtil;
import cn.ac.iscas.oncecloudshare.messaging.xmpp.muc.storage.OcsOccupantStorageProvider;


public class XmppRoom extends Room {
	
	private static final RoomType[] ROOM_TYPES={
		RoomType.Moderated,RoomType.MembersOnly
	};
	
	OcsOccupantStorageProvider occupantProvider;
	
	private Map<Long,Occupant> occupantMap;
	
	private TenantRoom tenantRoom;

	public XmppRoom(TenantRoom room,OcsOccupantStorageProvider occupantProvider){
		super(JIDUtil.buildRoomEntity(room.getTenantId(),room.getRoomId()),
				room.getTenantId()+"-"+room.getRoomId(),ROOM_TYPES);
		this.tenantRoom=room;
		this.occupantProvider=occupantProvider;
		//TODO 优化 cahe result
		occupantMap=occupantProvider.findOccupantsMapByRoomId(room.getTenantId(),this);
	}
	
	public long getRoomId(){
		return tenantRoom.getRoomId();
	}

	@Override
	public Occupant addOccupant(Entity occupantJid, String name){
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Occupant findOccupantByJID(Entity occupantJid) {
		TenantUser tenantUser=JIDUtil.parseTenantUser(occupantJid);
		if(tenantUser==null){
			return null;
		}
        return findOccupantByUserId(tenantUser.getUserId());
    }
	
	@Override
	public Occupant findOccupantByNick(String nick) {
        return findOccupantByUserId(Longs.tryParse(nick));
    }
	
	private Occupant findOccupantByUserId(Long userId){
		return occupantMap.get(userId);
	}
	
	@Override
	public Set<Occupant> getModerators() {
        return ImmutableSet.of();
    }
	
	@Override
	public void removeOccupant(Entity occupantJid) {
        throw new UnsupportedOperationException();
    }
	
	@Override
	public int getOccupantCount() {
        return occupantMap.size();
    }
	
	@Override
	public boolean isEmpty(){
		return getOccupantCount()!=0;
	}
	
	@Override
	public Set<Occupant> getOccupants() {
		return ImmutableSet.copyOf(occupantMap.values());
    }
	
	@Override
	public List<Item> getItemsFor(InfoRequest request) throws ServiceDiscoveryRequestException {
        return ImmutableList.of();
    }
	
	@Override
	public DiscussionHistory getHistory(){
		return EmptyDiscussionHistory.INSTANCE;
	}
}
