package cn.ac.iscas.oncecloudshare.service.model.common;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import cn.ac.iscas.oncecloudshare.service.utils.Constants;

/**
 * 全局系统配置
 * 
 * @author One
 */
@Entity
@Table(name = "global_config", schema = Constants.GLOBAL_SCHEMA)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class GlobalConfig extends Config {

}
