package cn.ac.iscas.oncecloudshare.messaging.model.attachment;

import com.google.common.io.ByteSource;

public interface Attachment {

	public String getKey();

	public String getFilename();

	public ByteSource getContent();

}
