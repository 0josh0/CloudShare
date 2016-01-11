package cn.ac.iscas.oncecloudshare.service.model.contact;

/**
 * 邀请码常量
 * 
 * @author One
 * 
 */
public class InviteCodeConstance {
	/**
	 * 邀请码使用状态
	 * 
	 * @author One
	 * 
	 */
	public enum InviteCodeStatus {

		/**
		 * 已经使用
		 */
		USED,
		/**
		 * 未使用
		 */
		UNUSED;
	}

	/**
	 * 邀请码映射表
	 * 
	 * @author One
	 * 
	 */
	public enum InviteCodeMapping {
		a("1"), b("2"), u("3"), L("4"), o("5"), I("6"), M("7"), F("8"), c("9"), U("0");
		private String value;

		private InviteCodeMapping(String value) {
			this.value = value;
		}

		public static String toVaule(String name) {
			InviteCodeMapping[] mapping = InviteCodeMapping.values();

			for (InviteCodeMapping inviteCodeMapping : mapping) {
				if (inviteCodeMapping.name().equals(name)) {
					return inviteCodeMapping.value;
				}
			}
			throw new IllegalArgumentException(name);
		}

		public static String toName(String value) {
			InviteCodeMapping[] mapping = InviteCodeMapping.values();

			for (InviteCodeMapping inviteCodeMapping : mapping) {
				if (inviteCodeMapping.value.equals(value)) {
					return inviteCodeMapping.name();
				}
			}
			throw new IllegalArgumentException(value + "");
		}
	}
}
