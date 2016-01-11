package cn.ac.iscas.oncecloudshare.messaging.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;

import cn.ac.iscas.oncecloudshare.messaging.dto.ResponseDto;
import cn.ac.iscas.oncecloudshare.messaging.model.authc.UserInfo;
import cn.ac.iscas.oncecloudshare.messaging.service.multitenancy.TenantService;
import cn.ac.iscas.oncecloudshare.messaging.utils.gson.Gsons;
import cn.ac.iscas.oncecloudshare.messaging.utils.http.MimeTypes;

import com.google.common.base.Strings;
import com.google.common.io.ByteSource;
import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;


public class BaseController {
	
	@Autowired
	protected TenantService tService;
	
	Gson gson=Gsons.defaultGson();

	protected Gson gson(){
		return Gsons.defaultGson();
	}
	
	/**
	 * 正确返回
	 * @return
	 */
	protected String ok(){
		return gson().toJson(ResponseDto.OK);
	}
	
	protected UserInfo currentUser(){
		return (UserInfo)SecurityUtils.getSubject().getPrincipal();
	}
	
	protected Long currentUserId(){
		return currentUser().getId();
	}
	
	protected long currentTenantId(){
		return tService.getCurrentTenant();
	}
	
	protected String encodeFilename(String userAgent,String filename){
		if(Strings.isNullOrEmpty(filename) ||
				Strings.isNullOrEmpty(filename)){
			return filename;
		}
		try{
			userAgent=userAgent.toLowerCase();
			String utf8Encoded=URLEncoder.encode(filename,"utf-8");
			// IE浏览器，只能采用URLEncoder编码
			if(userAgent.indexOf("msie")!=-1){
				return "filename=\""+utf8Encoded+"\"";
			}
			// Opera浏览器只能采用filename*
			else if(userAgent.indexOf("opera")!=-1){
				return "filename*=UTF-8''"+utf8Encoded;
			}
			// Safari浏览器，只能采用ISO编码的中文输出
			else if(userAgent.indexOf("safari")!=-1){
				return "filename=\""+new String(filename.getBytes("UTF-8"), "ISO8859-1")+"\"";
			}
			// Chrome浏览器，只能采用MimeUtility编码或ISO编码的中文输出
			else if(userAgent.indexOf("applewebkit")!=-1){
				utf8Encoded=MimeUtility.encodeText(filename,"UTF8","B");
				return "filename=\""+utf8Encoded+"\"";
			}
			// FireFox浏览器，可以使用MimeUtility或filename*或ISO编码的中文输出
			else if(userAgent.indexOf("mozilla")!=-1){
				return "filename*=UTF-8''"+utf8Encoded;
			}
			return "filename*=UTF-8''"+utf8Encoded;
		}
		catch(UnsupportedEncodingException e){
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 下载文件
	 * @param request
	 * @param response
	 * @param source
	 * @param filename 文件名，可为null
	 * @throws IOException
	 */
	protected void download(HttpServletRequest request,
			HttpServletResponse response, ByteSource source, String filename)
			throws IOException{
		response.reset();

		response.addHeader(HttpHeaders.CONTENT_LENGTH,""+source.size());
//		if(filename!=null){
//			response.setHeader(HttpHeaders.CONTENT_DISPOSITION,"inline; "
//					+encodeFilename(request.getHeader("User-Agent"),filename));
//		}
		
		if(filename!=null){
			response.setContentType(MimeTypes.getMimeTypeByFilename(filename));
		}
		
		OutputStream out=response.getOutputStream();
		try{
			source.copyTo(response.getOutputStream());
		}
		catch(IOException e) {
		}
		finally{
			IOUtils.closeQuietly(out);
		}
	}
}
