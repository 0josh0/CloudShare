package cn.ac.iscas.oncecloudshare.service.utils.guava;

import cn.ac.iscas.oncecloudshare.service.model.IdEntity;

import com.google.common.base.Function;

public class Functions{
	public static final Function<IdEntity, Long> IDENTITY_TO_ID = new Function<IdEntity, Long>() {
		@Override
		public Long apply(IdEntity input) {
			return input.getId();
		}
	};
}