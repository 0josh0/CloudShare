package cn.ac.iscas.oncecloudshare.service.service.account;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.ac.iscas.oncecloudshare.service.dto.account.PasswordResetInfo;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.common.Mail;
import cn.ac.iscas.oncecloudshare.service.model.common.Mail.EmailParameter;
import cn.ac.iscas.oncecloudshare.service.model.common.TempItem;
import cn.ac.iscas.oncecloudshare.service.service.common.ConfigService;
import cn.ac.iscas.oncecloudshare.service.service.common.Configs;
import cn.ac.iscas.oncecloudshare.service.service.common.MailService;
import cn.ac.iscas.oncecloudshare.service.service.common.TempItemService;
import cn.ac.iscas.oncecloudshare.service.service.multitenancy.TenantService;

import com.google.common.collect.Lists;

@Service
public class PasswordResetService {

	private static final String TEMP_ITEM_TYPE="pw_reset";
	
	private static final long EXPIRE_TIME_IN_HOUR=24;
	
	@Autowired
	TempItemService tiService;
	
	@Autowired
	UserService uService;
	
	@Resource(name="globalConfigService")
	private ConfigService<?> configService;
	@Resource
	private TenantService tenantService;

	
	@Autowired
	MailService mailService;
	
	public PasswordResetInfo requestPaswordReset(User user){
		TempItem ti=tiService.save(TEMP_ITEM_TYPE,user.getEmail(),
				EXPIRE_TIME_IN_HOUR*DateUtils.MILLIS_PER_HOUR);
		sendEmail(user,ti.getKey());
		return new PasswordResetInfo(user.getEmail(),
				ti.getKey(),ti.getExpiresAt());
	}
	
	private void sendEmail(User user,String token){
		String url=configService.getConfig(Configs.Keys.CLIENT_WEB_URL,"")
				+configService.getConfig(Configs.Keys.RESET_PW_URL,"");
		// 多租户改变，需要在token中带有租户信息
		token = token + '-' + tenantService.getCurrentTenant().getId();
		List<EmailParameter> parameters=Lists.newArrayList(
				new EmailParameter("expireTimeInHour",EXPIRE_TIME_IN_HOUR),
				new EmailParameter("reset_password_url",url),
				new EmailParameter("token",token)
		);
		
		String subject=configService.getConfig(Configs.Keys.MAIL_RESET_PW_SUBJECT,"");
		String content=configService.getConfig(Configs.Keys.MAIL_RESET_PW_CONTENT,"");
		Mail mail=new Mail(subject,content,parameters);
		mailService.send(user.getEmail(),mail);
	}
	
	public PasswordResetInfo find(String token){
		TempItem ti=tiService.find(token);
		if(ti==null){
			return null;
		}
		return new PasswordResetInfo(ti.getContent(),
				ti.getKey(),ti.getExpiresAt());
	}
	
}
