package cn.ac.iscas.oncecloudshare.service.model.common;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.io.ByteSource;

public class Mail {
	@NotNull
	@Size(min = 1, max = 256)
	private String subject;
	@NotNull
	private String content;
	private String type = "text/plain;charset=utf-8";
	private List<Attachment> attachments = Lists.newArrayList();
	
	public Mail(){
	}

	public Mail(String subject, String content) {
		this.subject = subject;
		this.content = StringUtils.isEmpty(content) ? subject : content;
	}

	/**
	 * 替换template中${key}格式的字符串
	 * 
	 * @param subjectTemplate
	 * @param contentTemplate
	 * @param parameters 邮件参数，如果使用了嵌套参数，请注意list中参数的顺序
	 */
	public Mail(String subjectTemplate, String contentTemplate, List<EmailParameter> parameters) {
		this.subject = replaceParameters(subjectTemplate, parameters);
		this.content = replaceParameters(contentTemplate, parameters);
	}

	private String replaceParameters(String template, List<EmailParameter> parameters) {
		String result = template;
		for (EmailParameter param : parameters) {
			String key = param.key;
			key = "${" + key + "}";
			result = StringUtils.replace(result, key, param.value);
		}
		return result;
	}

	public String getSubject() {
		return subject;
	}

	public String getContent() {
		return content;
	}

	public void addAttachment(String name, ByteSource source) {
		this.attachments.add(new Attachment(name, source));
	}

	public List<Attachment> getAttachments() {
		return attachments;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
	}

	public static class EmailParameter {
		public final String key;
		public final String value;

		public EmailParameter(String key, Object value) {
			this.key = key;
			this.value = value.toString();
		}
	}

	public static class Attachment {
		public final String name;
		public final ByteSource source;

		public Attachment(String name, ByteSource source) {
			super();
			this.name = name;
			this.source = source;
		}
	}
}
