package cn.ac.iscas.oncecloudshare.service.model.common;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

public class Mail {

	private final String subject;
	private final String content;

	public Mail(String subject, String content){
		this.subject=subject;
		this.content=content;
	}

	/**
	 * 替换template中${key}格式的字符串
	 * 
	 * @param subjectTemplate
	 * @param contentTemplate
	 * @param parameters 邮件参数，如果使用了嵌套参数，请注意list中参数的顺序
	 */
	public Mail(String subjectTemplate, String contentTemplate,
			List<EmailParameter> parameters){
		this.subject=replaceParameters(subjectTemplate,parameters);
		this.content=replaceParameters(contentTemplate,parameters);
	}

	private String replaceParameters(String template,
			List<EmailParameter> parameters){
		String result=template;
		for(EmailParameter param:parameters){
			String key=param.key;
			key="${"+key+"}";
			result=StringUtils.replace(result,key,param.value);
		}
		return result;
	}

	public String getSubject(){
		return subject;
	}

	public String getContent(){
		return content;
	}

	public static class EmailParameter{
		public final String key;
		public final String value;
		
		public EmailParameter(String key, Object value){
			this.key=key;
			this.value=value.toString();
		}
	}
}
