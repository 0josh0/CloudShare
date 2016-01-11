package cn.ac.iscas.oncecloudshare.service.model.common;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * 租户系统配置
 * 
 * @author One
 */
@Entity
@Table(name = "ocs_config")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TenantConfig extends Config {

}
