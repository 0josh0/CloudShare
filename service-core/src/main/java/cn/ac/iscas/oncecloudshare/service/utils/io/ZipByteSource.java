package cn.ac.iscas.oncecloudshare.service.utils.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.io.ByteSource;


public class ZipByteSource extends ByteSource{

	private List<ZipEntry> entries=Lists.newArrayList();
	
	public ZipByteSource(){
		
	}
	
	public ZipByteSource(List<ZipEntry> entries){
		this.entries.addAll(entries);
	}
	
	public void addEntry(ZipEntry entry){
		entries.add(entry);
	}

	@Override
	public InputStream openStream() throws IOException{
		return new ZipInputStream();
	}

	public static class ZipEntry {
		private final ByteSource source;
		private final String filename;
		private final String path;
		private final boolean isDir;
		
		public ZipEntry(ByteSource source, String filename) {
			this(source, filename, StringUtils.EMPTY);
		}

		public ZipEntry(ByteSource source, String filename, String path) {
			this(source, filename, path, false);
		}
		
		public ZipEntry(ByteSource source, String filename, String path, boolean isDir) {
			this.source = source;
			this.filename = filename;
			this.path = path;
			this.isDir = isDir;
		}
	}
	
	private class ZipInputStream extends InputStream{
		
		File theZipFile;
		InputStream zipFileInputStream;
		
		public ZipInputStream() throws IOException{
			String tempDir=System.getProperty("java.io.tmpdir");
			tempDir=Objects.firstNonNull(tempDir,".");
			String tempFilename=RandomStringUtils.randomAlphanumeric(32);
			try{
				theZipFile=new File(tempDir,tempFilename);
				ZipFile zipFile=new ZipFile(theZipFile);
				for(ZipEntry entry:entries){
					ZipParameters parameters=new ZipParameters();
					parameters.setCompressionMethod(Zip4jConstants.COMP_STORE);
					parameters.setSourceExternalStream(true);
					if (entry.isDir){
						parameters.setFileNameInZip(entry.path + entry.filename + "/");
					} else {
						parameters.setFileNameInZip(entry.path + entry.filename);
					}
					InputStream in=entry.source.openStream();
					zipFile.addStream(in,parameters);
					in.close();
				}
				zipFileInputStream=new FileInputStream(zipFile.getFile());
			}
			catch(IOException e){
				throw e;
			}
			catch(Exception e){
				//should never happen
				throw new RuntimeException(e);
			}
		}

		@Override
		public int read() throws IOException{
			return zipFileInputStream.read();
		}
		
		@Override
		public int read(byte[] b,int off,int len) throws IOException{
			return zipFileInputStream.read(b,off,len);
		}
		
		@Override
		public void close() throws IOException{
			zipFileInputStream.close();
			FileUtils.forceDelete(theZipFile);
		}
	}
}
