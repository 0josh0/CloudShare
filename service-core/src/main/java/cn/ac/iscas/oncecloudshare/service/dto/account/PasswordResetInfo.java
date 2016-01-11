package cn.ac.iscas.oncecloudshare.service.dto.account;



public class PasswordResetInfo {

	public final String email;
	public final String token;
	public final long expiresAt;
	
	public PasswordResetInfo(String email, String token, long expiresAt){
		this.email=email;
		this.token=token;
		this.expiresAt=expiresAt;
	}
	
}
