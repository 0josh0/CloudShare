package cn.ac.iscas.oncecloudshare.service.model.notif;

import java.util.List;

import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;

import com.google.common.collect.ImmutableList;

public class Notification {
	private final NotificationType type;
	private final String content;
	private final String attributes;
	private final List<Long> to;

	/**
	 * 
	 * @param type 通知类型
	 * @param content 内容
	 * @param to 目标用户id
	 */
	public Notification(NotificationType type, String content, Object attributes, Long to) {
		this(type, content, attributes, ImmutableList.of(to));
	}

	/**
	 * 
	 * @param type 通知类型
	 * @param content 内容
	 * @param to 目标用户id
	 */
	public Notification(NotificationType type, String content, Object attributes, List<Long> to) {
		super();
		this.type = type;
		this.content = content;
		this.attributes = attributes == null ? null : Gsons.defaultGsonNoPrettify().toJson(attributes);
		this.to = to;
	}

	public NotificationType getType() {
		return type;
	}

	public String getContent() {
		return content;
	}

	public List<Long> getTo() {
		return to;
	}

	public String getAttributes() {
		return attributes;
	}
}
