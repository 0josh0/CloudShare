package cn.ac.iscas.oncecloudshare.messaging.model;

public enum ChatMessageType {

	TEXT, AUDIO, PIC, FILE, GEO ;

	public static ChatMessageType of(String stringIdentifier){
		for(ChatMessageType type: values()){
			if(type.name().equalsIgnoreCase(stringIdentifier)){
				return type;
			}
		}
		return null;
	}
}
