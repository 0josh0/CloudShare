package cn.ac.iscas.oncecloudshare.messaging.xmpp.account;

import org.apache.vysper.xmpp.addressing.Entity;
import org.apache.vysper.xmpp.addressing.EntityImpl;
import org.apache.vysper.xmpp.authorization.AccountCreationException;
import org.apache.vysper.xmpp.authorization.AccountManagement;
import org.apache.vysper.xmpp.authorization.UserAuthorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.ac.iscas.oncecloudshare.messaging.service.Services;
import cn.ac.iscas.oncecloudshare.messaging.utils.Constants;
import cn.ac.iscas.oncecloudshare.messaging.utils.JIDUtil;


public class OcsUserAuthorization implements UserAuthorization, AccountManagement {
	
	private static Logger logger=LoggerFactory.getLogger(OcsUserAuthorization.class);
	
	@Override
	public void addUser(Entity username, String password)
			throws AccountCreationException{
		throw new AccountCreationException("addUser not supported");
	}

	@Override
	public void changePassword(Entity username, String password)
			throws AccountCreationException{
		throw new AccountCreationException("changePassword not supported");
	}

	@Override
	public boolean verifyAccountExists(Entity jid){
		if(JIDUtil.isNotifEntity(jid)){
			return true;
		}
		return Services.getAccountService()
				.verifyUserExists(JIDUtil.parseTenantUser(jid));
	}

	@Override
	public boolean verifyCredentials(Entity jid, String passwordCleartext,
			Object credentials){
		if(JIDUtil.isNotifEntity(jid) &&
				Constants.notifPassword().equals(passwordCleartext)){
			return true;
		}
		return Services.getAccountService()
				.verifyTicket(JIDUtil.parseTenantUser(jid),passwordCleartext);
	}

	@Override
	public boolean verifyCredentials(String username, String passwordCleartext,
			Object credentials){
		return verifyCredentials(EntityImpl.parseUnchecked(username),
				passwordCleartext,credentials);
	}

}
