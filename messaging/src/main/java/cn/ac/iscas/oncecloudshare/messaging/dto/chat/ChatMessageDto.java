package cn.ac.iscas.oncecloudshare.messaging.dto.chat;

import com.google.common.base.Function;

import cn.ac.iscas.oncecloudshare.messaging.model.ChatMessageType;
import cn.ac.iscas.oncecloudshare.messaging.model.chat.ChatMessage;


public final class ChatMessageDto {
	
	public static final Function<ChatMessage,ChatMessageDto> TRANSFORMER=
			new Function<ChatMessage,ChatMessageDto>(){

				@Override
				public ChatMessageDto apply(ChatMessage input){
					return new ChatMessageDto(input);
				}
			};

	public final Long id;
	public final ChatMessageType type;
	public final String content;
	public final Long ts;
	public final Long receiver;
	public final Long sender;
	public final Long seq;
	
	private ChatMessageDto(ChatMessage message){
		id=message.getId();
		type=message.getType();
		content=message.getContent();
		ts=message.getTs();
		receiver=message.getReceiver();
		sender=message.getSender();
		seq=message.getSeq();
	}
	
	public static ChatMessageDto of(ChatMessage message){
		return TRANSFORMER.apply(message);
	}
}
