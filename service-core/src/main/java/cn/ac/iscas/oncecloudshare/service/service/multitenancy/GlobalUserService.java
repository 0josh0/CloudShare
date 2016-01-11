package cn.ac.iscas.oncecloudshare.service.service.multitenancy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import cn.ac.iscas.oncecloudshare.service.model.multitenancy.GlobalUser;

public interface GlobalUserService {
	@Nullable
	GlobalUser findByEmail(String email);
	
	@Nullable
	Long findTenantId(@Nullable String email);

	boolean add(@Nonnull String email);

	boolean delete(@Nonnull String email);

	@Nullable
	Long findUserId(@Nullable String email);
}
