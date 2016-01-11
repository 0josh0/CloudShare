package cn.ac.iscas.oncecloudshare.service.dto.file;

import java.util.Date;

import cn.ac.iscas.oncecloudshare.service.model.filemeta.FileVersion;

import com.google.common.base.Function;


public class FileVersionDto{
	
	public static Function<FileVersion,FileVersionDto> TRANSFORMER=
			new Function<FileVersion,FileVersionDto>(){

				@Override
				public FileVersionDto apply(FileVersion input){
					if(input==null){
						return null;
					}
					FileVersionDto dto=new FileVersionDto();
					dto.id=input.getId();
					dto.version=input.getVersion();
					dto.size=input.getSize();
					dto.md5=input.getMd5();
					dto.createTime=input.getCreateTime();
					dto.updateTime=input.getUpdateTime();
					return dto;
				}
		
	};

	public Long id;
	public Integer version;
	public Long size;
	public String md5;
	public Date createTime;
	public Date updateTime;
	
	public static FileVersionDto of(FileVersion fv){
		return TRANSFORMER.apply(fv);
	}
}
