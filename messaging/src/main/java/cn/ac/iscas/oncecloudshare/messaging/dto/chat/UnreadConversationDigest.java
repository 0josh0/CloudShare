package cn.ac.iscas.oncecloudshare.messaging.dto.chat;

public class UnreadConversationDigest {

	public final Long userId;

	public final Long oppositeId;

	public final Long unreadCount;
	
	public final ChatMessageDto lastMessage;

	public UnreadConversationDigest(Long userId, Long oppositeId,
			Long unreadCount, ChatMessageDto lastMessage){
		this.userId=userId;
		this.oppositeId=oppositeId;
		this.unreadCount=unreadCount;
		this.lastMessage=lastMessage;
	}
	
	
}
