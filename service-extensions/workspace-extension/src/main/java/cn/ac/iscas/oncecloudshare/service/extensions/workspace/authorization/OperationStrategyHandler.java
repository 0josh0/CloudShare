package cn.ac.iscas.oncecloudshare.service.extensions.workspace.authorization;

public abstract class OperationStrategyHandler implements Cloneable {
	public abstract String getName();

	public abstract boolean isSingleton();

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException();
		}
	}

	public abstract boolean isPermit(Long userId, String role, String operation);
}
