package cn.ac.iscas.oncecloudshare.service.dto.common;

import cn.ac.iscas.oncecloudshare.service.model.common.Config;
import cn.ac.iscas.oncecloudshare.service.model.common.Config.AccessMode;
import cn.ac.iscas.oncecloudshare.service.model.common.Config.DataType;
import cn.ac.iscas.oncecloudshare.service.model.common.GlobalConfig;
import cn.ac.iscas.oncecloudshare.service.model.common.TenantConfig;

import com.google.common.base.Function;

public class ConfigDto {

	public static final Function<TenantConfig, ConfigDto> TENANT_ANON_TRANSFORMER = new Function<TenantConfig, ConfigDto>() {

		@Override
		public ConfigDto apply(TenantConfig input) {
			ConfigDto dto = new ConfigDto();
			dto.key = input.getKey();
			dto.value = input.getValue();
			dto.dataType = input.getDataType();
			dto.description = input.getDescription();
			return dto;
		}
	};

	public static final Function<TenantConfig, ConfigDto> TENANT_ADMIN_TRANSFORMER = new Function<TenantConfig, ConfigDto>() {

		@Override
		public ConfigDto apply(TenantConfig input) {
			ConfigDto dto = TENANT_ANON_TRANSFORMER.apply(input);
			dto.adminAccessMode = input.getAdminAccessMode();
			dto.normalUserReadable = input.getNormalUserReadable();
			dto.displayGroup = input.getDisplayGroup();
			return dto;
		}
	};

	public static final Function<GlobalConfig, ConfigDto> ANON_TRANSFORMER = new Function<GlobalConfig, ConfigDto>() {

		@Override
		public ConfigDto apply(GlobalConfig input) {
			ConfigDto dto = new ConfigDto();
			dto.key = input.getKey();
			dto.value = input.getValue();
			dto.dataType = input.getDataType();
			dto.description = input.getDescription();
			return dto;
		}
	};

	public static final Function<GlobalConfig, ConfigDto> ADMIN_TRANSFORMER = new Function<GlobalConfig, ConfigDto>() {

		@Override
		public ConfigDto apply(GlobalConfig input) {
			ConfigDto dto = ANON_TRANSFORMER.apply(input);
			dto.adminAccessMode = input.getAdminAccessMode();
			dto.normalUserReadable = input.getNormalUserReadable();
			dto.displayGroup = input.getDisplayGroup();
			return dto;
		}
	};

	public String key;
	public String value;
	public DataType dataType;
	public String description;

	public AccessMode adminAccessMode;
	public Boolean normalUserReadable;

	public String displayGroup;

	public static ConfigDto forAnon(Config config) {

		if (config instanceof TenantConfig)
			return TENANT_ANON_TRANSFORMER.apply((TenantConfig) config);
		return ANON_TRANSFORMER.apply((GlobalConfig) config);
	}

	public static ConfigDto forAdmin(Config config) {
		if (config instanceof TenantConfig)
			return TENANT_ANON_TRANSFORMER.apply((TenantConfig) config);
		return ANON_TRANSFORMER.apply((GlobalConfig) config);
	}
}
