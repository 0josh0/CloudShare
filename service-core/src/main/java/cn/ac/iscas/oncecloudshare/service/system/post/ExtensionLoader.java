package cn.ac.iscas.oncecloudshare.service.system.post;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.ac.iscas.oncecloudshare.service.system.RuntimeContext;
import cn.ac.iscas.oncecloudshare.service.system.extension.Extension;
import cn.ac.iscas.oncecloudshare.service.utils.spring.SpringUtil;

/**
 * 系统启动后，载入所有Extension
 * 
 * @author Chen Hao
 */
@Component
public class ExtensionLoader {
	
	private static Logger logger=LoggerFactory.getLogger(ExtensionLoader.class);

	@Autowired
	RuntimeContext runtimeContext;
	
	@PostConstruct
	private void init(){
		URL url=this.getClass().getClassLoader().getResource("extensions");
		File dir=new File(url.getFile());
		if(dir.exists()==false){
			logger.warn("extension folder not exists");
			return;
		}
		
		for(File file:dir.listFiles()){
			if(file.getName().endsWith(".properties")==false){
				continue;
			}
			Properties prop=new Properties();
			Reader reader=null;
			try{
				reader=new InputStreamReader(new FileInputStream(file),"utf-8");
				prop.load(reader);
			}
			catch(IOException e){
				logger.warn("error reading properties file "+file.getName(),e);
				continue;
			}
			finally{
				if(reader!=null){
					IOUtils.closeQuietly(reader);
				}
			}
			
			String name=prop.getProperty("name");
			String version=prop.getProperty("version");
			String className=prop.getProperty("class");
			String description=prop.getProperty("description","");
			
			try{
				Class<?> extCLass=Class.forName(className);
				Extension extension=null;
				try{
					//先尝试读SpringBean
					extension=SpringUtil.getBean(extCLass);
				}
				catch(Exception e){
				}
				if(extension==null){
					extension=(Extension)extCLass.newInstance();
				}
				runtimeContext.getExtensionManager().loadExtension(name,version,description,extension);
			}
			catch(ClassNotFoundException e){
				logger.warn("cannot find extension {}",name);
			}
			catch(Exception e){
				logger.warn("error loading extension "+name,e);
			}
		}
		
	}
}
