package cn.ac.iscas.oncecloudshare.service.service.share;

import java.util.List;

import cn.ac.iscas.oncecloudshare.service.model.share.ReceivedShare;
import cn.ac.iscas.oncecloudshare.service.model.share.Share;
import cn.ac.iscas.oncecloudshare.service.model.share.ShareRecipient;

public interface ShareRecipientHandler {	
	public List<String> getSupportedRecipientTypes();
	
	public List<ReceivedShare> generateReceivedShares(Share share, ShareRecipient target);
}
