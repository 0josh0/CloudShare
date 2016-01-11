package cn.ac.iscas.oncecloudshare.service.extensions.company.space.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import cn.ac.iscas.oncecloudshare.service.extensions.company.space.utils.CompanyUtils;
import cn.ac.iscas.oncecloudshare.service.model.common.BaseSpace;

@Entity
@DiscriminatorValue(CompanyUtils.DOMAIN)
public class CompanySpace extends BaseSpace {
}