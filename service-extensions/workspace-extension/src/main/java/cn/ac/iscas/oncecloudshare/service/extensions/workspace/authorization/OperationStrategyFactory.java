package cn.ac.iscas.oncecloudshare.service.extensions.workspace.authorization;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
public class OperationStrategyFactory {
	private static final Logger _logger = LoggerFactory.getLogger(OperationStrategyFactory.class);

	private List<OperationStrategyHandler> handlers = Lists.newCopyOnWriteArrayList();
	private OperationStrategyHandler defaultHandler = new RoleOperationStrategy();

	public OperationStrategyFactory() {
		addOperationStrategyHandler(new AnonOperationStrategy());
		addOperationStrategyHandler(new UserOperationStrategy());
		addOperationStrategyHandler(defaultHandler);
	}

	public OperationStrategyFactory(List<OperationStrategyHandler> handlers) {
		this.handlers.addAll(handlers);
	}

	public OperationStrategyHandler create(String strategyName) {
		OperationStrategyHandler tmp = getOperationStrategyHandler(strategyName);
		if (tmp == null) {
			tmp = defaultHandler;
		}
		if (tmp.isSingleton()) {
			return tmp;
		} else {
			return (OperationStrategyHandler) tmp.clone();
		}
	}

	public void addOperationStrategyHandler(OperationStrategyHandler operationStrategy) {
		OperationStrategyHandler tmp = getOperationStrategyHandler(operationStrategy.getName());
		if (tmp == null) {
			handlers.add(operationStrategy);
		} else {
			_logger.warn("忽略添加操作策略:{},原因：已由同名策略", operationStrategy.getName());
		}
	}

	protected OperationStrategyHandler getOperationStrategyHandler(String name) {
		for (OperationStrategyHandler tmp : handlers) {
			if (StringUtils.equalsIgnoreCase(tmp.getName(), name)) {
				return tmp;
			}
		}
		return null;
	}

	public OperationStrategyHandler getDefaultHandler() {
		return defaultHandler;
	}

	public void setDefaultHandler(OperationStrategyHandler defaultHandler) {
		this.defaultHandler = defaultHandler;
	}
}
