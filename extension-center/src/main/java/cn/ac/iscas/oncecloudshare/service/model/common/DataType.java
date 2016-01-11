package cn.ac.iscas.oncecloudshare.service.model.common;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

public enum DataType {
	BOOLEAN {
		@Override
		public String encode(Object obj) {
			Preconditions.checkNotNull(obj);
			Preconditions.checkArgument(obj instanceof Boolean);
			return obj.toString();
		}

		@Override
		public Object decode(String s) {
			return StringUtils.isNotEmpty(s) && ("1".equals(s) || "true".equalsIgnoreCase(s));
		}
	},
	INT {
		@Override
		public String encode(Object obj) {
			Preconditions.checkNotNull(obj);
			Preconditions.checkArgument(obj instanceof Integer);
			return obj.toString();
		}

		@Override
		public Object decode(String s) {
			return StringUtils.isEmpty(s) ? null : Ints.tryParse(s);
		}
	},
	LONG {
		@Override
		public String encode(Object obj) {
			Preconditions.checkNotNull(obj);
			Preconditions.checkArgument(obj instanceof Long);
			return obj.toString();
		}

		@Override
		public Object decode(String s) {
			return StringUtils.isEmpty(s) ? null : Longs.tryParse(s);
		}
	},
	DOUBLE {
		@Override
		public String encode(Object obj) {
			Preconditions.checkNotNull(obj);
			Preconditions.checkArgument(obj instanceof Double);
			return obj.toString();
		}

		@Override
		public Object decode(String s) {
			return StringUtils.isEmpty(s) ? null : Doubles.tryParse(s);
		}
	},
	STRING {
		@Override
		public String encode(Object obj) {
			Preconditions.checkNotNull(obj);
			Preconditions.checkArgument(obj instanceof String);
			return (String) obj;
		}

		@Override
		public Object decode(String s) {
			return s;
		}
	},
	;

	public abstract String encode(Object obj);

	public abstract Object decode(String s);
}
