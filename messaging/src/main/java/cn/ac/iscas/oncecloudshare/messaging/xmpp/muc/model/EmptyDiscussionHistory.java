package cn.ac.iscas.oncecloudshare.messaging.xmpp.muc.model;

import java.util.Calendar;
import java.util.List;

import org.apache.vysper.xmpp.modules.extension.xep0045_muc.model.DiscussionHistory;
import org.apache.vysper.xmpp.modules.extension.xep0045_muc.model.Occupant;
import org.apache.vysper.xmpp.modules.extension.xep0045_muc.stanzas.History;
import org.apache.vysper.xmpp.stanza.Stanza;

import com.google.common.collect.ImmutableList;

/**
 * 不在内存中保存历史消息记录
 *
 * @author Chen Hao
 */
public class EmptyDiscussionHistory extends DiscussionHistory {
	
	public static final EmptyDiscussionHistory INSTANCE=
			new EmptyDiscussionHistory();
	
	private EmptyDiscussionHistory(){
	}
	
	@Override
	public void append(Stanza stanza, Occupant sender) {
		//do nothing;
    }

	@Override
    public void append(Stanza stanza, Occupant sender, Calendar timestamp) {
        //do nothing;
    }

    @Override
    public List<Stanza> createStanzas(Occupant receiver, boolean includeJid, History history) {
        return ImmutableList.of();
    }
}
