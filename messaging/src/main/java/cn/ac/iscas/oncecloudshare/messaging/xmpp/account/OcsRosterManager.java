package cn.ac.iscas.oncecloudshare.messaging.xmpp.account;

import org.apache.vysper.xmpp.addressing.Entity;
import org.apache.vysper.xmpp.modules.roster.MutableRoster;
import org.apache.vysper.xmpp.modules.roster.Roster;
import org.apache.vysper.xmpp.modules.roster.RosterException;
import org.apache.vysper.xmpp.modules.roster.RosterItem;
import org.apache.vysper.xmpp.modules.roster.persistence.AbstractRosterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OcsRosterManager extends AbstractRosterManager{

	private static Logger logger=LoggerFactory.getLogger(OcsRosterManager.class);
	
	private final MutableRoster globalRoster=new MutableRoster();
	
	public OcsRosterManager(){
	}
	
//	public void init(){
//		globalRoster=new MutableRoster();
//		
//		UserService uService=SpringUtil.getBean(UserService.class);
//		for(Entity entity:uService.getAllEntities()){
//			globalRoster.addItem(new RosterItem(entity,SubscriptionType.BOTH));
//		}
//	}

	@Override
	public void addContact(Entity jid, RosterItem rosterItem)
			throws RosterException{
		throw new RosterException("addContact not supported");
	}
	
	@Override
	public void removeContact(Entity jidUser, Entity jidContact)
			throws RosterException{
		throw new RosterException("removeContact not supported");
	}
	
	@Override
	protected Roster retrieveRosterInternal(Entity bareJid){
		return globalRoster;
	}

	@Override
	protected Roster addNewRosterInternal(Entity jid){
		return null;
	}

}
