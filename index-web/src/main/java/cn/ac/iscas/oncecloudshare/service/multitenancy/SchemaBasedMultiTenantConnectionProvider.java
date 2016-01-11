package cn.ac.iscas.oncecloudshare.service.multitenancy;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.service.jdbc.connections.internal.DatasourceConnectionProviderImpl;
import org.hibernate.service.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.hibernate.service.spi.Configurable;
import org.hibernate.service.spi.ServiceRegistryAwareService;
import org.hibernate.service.spi.ServiceRegistryImplementor;

import cn.ac.iscas.oncecloudshare.service.utils.Constants;

import com.google.common.base.Strings;

public class SchemaBasedMultiTenantConnectionProvider implements
		MultiTenantConnectionProvider, ServiceRegistryAwareService,
		Configurable {

	private static final long serialVersionUID=4887023639518774163L;

	private ConnectionProvider connectionProvider=null;
	
	public static ThreadLocal<Connection> currentConnection = new ThreadLocal<Connection>(); 

	public boolean isUnwrappableAs(
			@SuppressWarnings ("rawtypes") Class unwrapType){
		return false;
	}

	public <T>T unwrap(Class<T> unwrapType){
		return null;
	}

	public void injectServices(ServiceRegistryImplementor serviceRegistry){
	}

	public Connection getAnyConnection() throws SQLException{
		final Connection connection=connectionProvider.getConnection();
		return connection;
	}

	public void releaseAnyConnection(Connection connection) throws SQLException{
		try{
			connection.createStatement().execute("USE "+Constants.TENANT_SCHEMA_DEFAULT);
		}
		catch(SQLException e){
			throw new HibernateException(
					"Could not alter JDBC connection to default schema", e);
		}
		currentConnection.remove();
		connectionProvider.closeConnection(connection);
	}

	public Connection getConnection(String tenantIdentifier)
			throws SQLException{
		final Connection connection=connectionProvider.getConnection();
		String schema=null;
		if(Strings.isNullOrEmpty(tenantIdentifier)){
			schema=Constants.TENANT_SCHEMA_DEFAULT;
		}
		else{
			schema=Constants.TENANT_SCHEMA_PREFIX+tenantIdentifier;
		}
		connection.createStatement().execute("USE "+schema);
		currentConnection.set(connection);
		return connection;
	}

	public void releaseConnection(String tenantIdentifier, Connection connection)
			throws SQLException{
		releaseAnyConnection(connection);
	}

	public boolean supportsAggressiveRelease(){
		return false;
	}

	public void configure(@SuppressWarnings ("rawtypes") Map configurationValues){
		DatasourceConnectionProviderImpl dcp=new DatasourceConnectionProviderImpl();
		dcp.configure(configurationValues);

		connectionProvider=dcp;
	}

}
