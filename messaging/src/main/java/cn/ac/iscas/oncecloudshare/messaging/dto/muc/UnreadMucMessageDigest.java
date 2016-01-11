package cn.ac.iscas.oncecloudshare.messaging.dto.muc;


public class UnreadMucMessageDigest {

	public final Long roomId;

	public final Long unreadCount;

	public final MucMessageDto lastMessage;

	
	public UnreadMucMessageDigest(Long roomId, Long unreadCount,
			MucMessageDto lastMessage){
		super();
		this.roomId=roomId;
		this.unreadCount=unreadCount;
		this.lastMessage=lastMessage;
	}

}
