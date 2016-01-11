package cn.ac.iscas.oncecloudshare.service.service.multitenancy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.ac.iscas.oncecloudshare.service.dao.multitenancy.GlobalUserDao;
import cn.ac.iscas.oncecloudshare.service.model.multitenancy.GlobalUser;

@Service
public class GlobalUserServiceImpl implements GlobalUserService {

	@Autowired
	GlobalUserDao guDao;

	@Autowired
	TenantService tService;

	public GlobalUser findByEmail(String email) {
		return guDao.findByEmail(email);
	}

	@Override
	public Long findTenantId(String email) {
		GlobalUser gu = findByEmail(email);
		return gu == null ? null : gu.getTenantId();
	}

	@Override
	public boolean add(String email) {
		try {
			GlobalUser gu = new GlobalUser();
			gu.setEmail(email);
			gu.setTenantId(tService.getCurrentTenant().getId());

			guDao.save(gu);
			return true;
		} catch (RuntimeException e) {
			return false;
		}
	}

	@Override
	public boolean delete(String email) {
		return guDao.deleteByEmail(email) > 0;
	}

	@Override
	public Long findUserId(String email) {
		GlobalUser gu = findByEmail(email);
		return gu == null ? null : gu.getId();
	}
}
