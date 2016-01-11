package cn.ac.iscas.oncecloudshare.service.messaging.service;

import java.util.List;

import cn.ac.iscas.oncecloudshare.service.messaging.exceptions.MessageException;
import cn.ac.iscas.oncecloudshare.service.messaging.model.Room;
import cn.ac.iscas.oncecloudshare.service.system.service.ServiceProvider;

public interface MessageService extends ServiceProvider {
	Room createRoom(String subject, long ownerId, List<Long> occupantIds, boolean special) throws MessageException;
	
	void deleteRoom(long roomId);

	Room findRoom(long id) throws MessageException;
	
	void changeOwner(long roomId, long ownerId) throws MessageException;

	void addOccupants(long roomId, List<Long> occupantIds) throws MessageException;

	void removeOccupants(long roomId, List<Long> occupantIds) throws MessageException;
}