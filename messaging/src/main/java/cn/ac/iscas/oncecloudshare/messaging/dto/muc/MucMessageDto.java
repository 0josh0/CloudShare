package cn.ac.iscas.oncecloudshare.messaging.dto.muc;

import com.google.common.base.Function;

import cn.ac.iscas.oncecloudshare.messaging.model.ChatMessageType;
import cn.ac.iscas.oncecloudshare.messaging.model.muc.MucMessage;


public final class MucMessageDto {
	
	public static final Function<MucMessage,MucMessageDto> TRANSFORMER=
			new Function<MucMessage,MucMessageDto>(){

				@Override
				public MucMessageDto apply(MucMessage input){
					return new MucMessageDto(input);
				}
			};

	public final Long id;
	public final ChatMessageType type;
	public final String content;
	public final Long ts;
	public final Long roomId;
	public final Long sender;
	public final Long seq;
	
	private MucMessageDto(MucMessage message){
		id=message.getId();
		type=message.getType();
		content=message.getContent();
		ts=message.getTs();
		roomId=message.getRoomId();
		sender=message.getSender();
		seq=message.getSeq();
	}
	
	public static MucMessageDto of(MucMessage message){
		return TRANSFORMER.apply(message);
	}
}
