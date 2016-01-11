package cn.ac.iscas.oncecloudshare.messaging.dto.muc;

import java.util.Date;

import org.apache.vysper.xmpp.modules.extension.xep0045_muc.model.Role;

import com.google.common.base.Function;

import cn.ac.iscas.oncecloudshare.messaging.model.muc.MucOccupant;


public class MucOccupantDto {

	public static final Function<MucOccupant,MucOccupantDto> TRANSFORMER=
			new Function<MucOccupant,MucOccupantDto>(){

				@Override
				public MucOccupantDto apply(MucOccupant input){
					return new MucOccupantDto(input);
				}
	};
	
//	public final Long id;
	public final Long userId;
	public final Role role;
	public final Date createTime;
	public final Long readSeq;
	
	private MucOccupantDto(MucOccupant o){
//		this.id=o.getId();
		this.userId=o.getUserId();
		this.role=o.getRole();
		this.createTime=o.getCreateTime();
		this.readSeq=o.getReadSeq();
	}
	
	public static MucOccupantDto of(MucOccupant o){
		return TRANSFORMER.apply(o);
	}
}
