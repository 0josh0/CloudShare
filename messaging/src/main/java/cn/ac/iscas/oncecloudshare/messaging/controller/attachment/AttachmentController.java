package cn.ac.iscas.oncecloudshare.messaging.controller.attachment;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import cn.ac.iscas.oncecloudshare.messaging.controller.BaseController;
import cn.ac.iscas.oncecloudshare.messaging.dto.BasicErrorCode;
import cn.ac.iscas.oncecloudshare.messaging.dto.attachment.AudioAttachmentDto;
import cn.ac.iscas.oncecloudshare.messaging.dto.attachment.ImageAttachmentDto;
import cn.ac.iscas.oncecloudshare.messaging.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.messaging.model.attachment.Attachment;
import cn.ac.iscas.oncecloudshare.messaging.service.attachment.AttachmentService;
import cn.ac.iscas.oncecloudshare.messaging.service.attachment.AudioAttamentService;
import cn.ac.iscas.oncecloudshare.messaging.service.attachment.ImageAttamentService;

import com.google.common.io.ByteSource;

@Controller
public class AttachmentController extends BaseController {
	
	@SuppressWarnings("unused")
	private static Logger logger=LoggerFactory.getLogger(AttachmentController.class);

	private static final String AUDIO_DL_URL_PREFIX="/api/attachments/audio/";
	private static final String IMAGE_DL_URL_PREFIX="/api/attachments/image/";
	private static final String THUMBNAIL_DL_URL_PREFIX="/api/attachments/image/thumbnail/";
	
	@Autowired
	AudioAttamentService aService;
	
	@Autowired
	ImageAttamentService iService;
	
	private void validateFile(MultipartFile file,String extension,
			AttachmentService service){
		if(file.isEmpty()){
			throw new RestException(BasicErrorCode.BAD_REQUEST,
					"file is empty");
		}
		if(file.getSize()>service.getSizeLimit()){
			throw new RestException(BasicErrorCode.BAD_REQUEST,
					"file size too large");
		}
		if(service.acceptFileExtension(extension)==false){
			throw new RestException(BasicErrorCode.BAD_REQUEST,
					"unacceptable file type");
		}
	}
	
	@RequestMapping(value="/api/attachments/audio",method=RequestMethod.POST,headers="content-type=multipart/*")
	@ResponseBody
	public String uploadAudio(@RequestParam("file") MultipartFile file) throws IOException{
		String ext=FilenameUtils.getExtension(file.getOriginalFilename());
		validateFile(file,ext,aService);
		Attachment attachment=aService.saveAttachment(
				new MultipartFileByteSource(file),ext);
		
		String url=AUDIO_DL_URL_PREFIX+"/"+attachment.getKey();
		return gson().toJson(new AudioAttachmentDto(url));
	}
	
	@RequestMapping(value="/api/attachments/image",method=RequestMethod.POST,headers="content-type=multipart/*")
	@ResponseBody
	public String uploadImage(@RequestParam("file") MultipartFile file) throws IOException{
		String ext=FilenameUtils.getExtension(file.getOriginalFilename());
		validateFile(file,ext,iService);
		Attachment attachment=iService.saveAttachment(new MultipartFileByteSource(file),ext);
		String imageUrl=IMAGE_DL_URL_PREFIX+"/"+attachment.getKey();
		String thumbnailUrl=THUMBNAIL_DL_URL_PREFIX+"/"+attachment.getKey();
		
		return gson().toJson(new ImageAttachmentDto(
				imageUrl,thumbnailUrl));
	}
	
	@RequestMapping(value=AUDIO_DL_URL_PREFIX+"**",method=RequestMethod.GET)
	public void downloadAudio(
			HttpServletRequest request,
			HttpServletResponse response) throws IOException{
		String key=((String)request.getAttribute(
		        HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE))
		        .substring(AUDIO_DL_URL_PREFIX.length());
		Attachment attachment=aService.getAttachment(key);
		if(attachment==null){
			throw new RestException(BasicErrorCode.NOT_FOUND,
					"audio file not exists");
		}
		download(request,response,attachment.getContent(),
				attachment.getFilename());
	}
	
	@RequestMapping(value=IMAGE_DL_URL_PREFIX+"**",method=RequestMethod.GET)
	public void downloadImage(
			HttpServletRequest request,
			HttpServletResponse response) throws IOException{
		String key=((String)request.getAttribute(
		        HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE))
		        .substring(IMAGE_DL_URL_PREFIX.length());
		Attachment attachment=iService.getAttachment(key);
		if(attachment==null){
			throw new RestException(BasicErrorCode.NOT_FOUND,
					"image file not exists");
		}
		download(request,response,attachment.getContent(),
				attachment.getFilename());
	}
	
	@RequestMapping(value=THUMBNAIL_DL_URL_PREFIX+"**",method=RequestMethod.GET)
	public void downloadImageThumbnail(
			HttpServletRequest request,
			HttpServletResponse response) throws IOException{
		String key=((String)request.getAttribute(
		        HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE))
		        .substring(THUMBNAIL_DL_URL_PREFIX.length());
		Attachment attachment=iService.getThumbnailAttachment(key);
		if(attachment==null){
			throw new RestException(BasicErrorCode.NOT_FOUND,
					"image file not exists");
		}
		download(request,response,attachment.getContent(),
				attachment.getFilename());
	}
	
	
	public static class MultipartFileByteSource extends ByteSource {

		MultipartFile file;

		public MultipartFileByteSource(MultipartFile file){
			this.file=file;
		}

		@Override
		public InputStream openStream() throws IOException{
			return file.getInputStream();
		}

		@Override
		public long size() throws IOException{
			return file.getSize();
		}

	}
}
