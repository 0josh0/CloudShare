package cn.ac.iscas.oncecloudshare.service.extensions.login.dao;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import cn.ac.iscas.oncecloudshare.service.extensions.login.model.LdapUser;

public interface LdapUserDao extends PagingAndSortingRepository<LdapUser, Long>, JpaSpecificationExecutor<LdapUser> {
	
	@Query("from LdapUser where ldapPrincipal = ?1")
	public LdapUser findByLdapPrincipal(String ldapPrincipal);
}