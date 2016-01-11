package cn.ac.iscas.oncecloudshare.service.extensions.workspace.authorization;


public class AnonOperationStrategy extends OperationStrategyHandler {
	@Override
	public String getName() {
		return "anon";
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public boolean isPermit(Long userId, String role, String operation) {
		return true;
	}
}
