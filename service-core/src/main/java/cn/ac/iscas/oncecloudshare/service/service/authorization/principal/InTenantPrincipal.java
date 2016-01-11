package cn.ac.iscas.oncecloudshare.service.service.authorization.principal;

import javax.annotation.Nullable;


public interface InTenantPrincipal extends Principal{

	@Nullable Long getTenantId();
}
