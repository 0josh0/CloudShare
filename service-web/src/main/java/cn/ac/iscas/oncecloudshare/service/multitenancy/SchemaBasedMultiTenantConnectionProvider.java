package cn.ac.iscas.oncecloudshare.service.multitenancy;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.hibernate.service.jdbc.connections.internal.DatasourceConnectionProviderImpl;
import org.hibernate.service.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.hibernate.service.spi.Configurable;
import org.hibernate.service.spi.ServiceRegistryAwareService;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.ac.iscas.oncecloudshare.service.utils.Constants;

import com.google.common.base.Strings;

public class SchemaBasedMultiTenantConnectionProvider implements
		MultiTenantConnectionProvider, ServiceRegistryAwareService,
		Configurable {
	
	private static Logger logger=LoggerFactory.getLogger(SchemaBasedMultiTenantConnectionProvider.class);

	private static final long serialVersionUID=4887023639518774163L;

	private ConnectionProvider connectionProvider=null;
	
	@Override
	public boolean isUnwrappableAs(
			@SuppressWarnings ("rawtypes") Class unwrapType){
		return false;
	}

	@Override
	public <T>T unwrap(Class<T> unwrapType){
		return null;
	}

	@Override
	public void injectServices(ServiceRegistryImplementor serviceRegistry){
	}

	private void switchSchema(Connection connection,String schema) throws SQLException{
		Statement stmt=null;
		try{
			stmt=connection.createStatement();
			stmt.execute("USE "+schema);
		}
		catch(SQLException e){
			logger.error("could not switch schema to "+schema,e);
			throw e;
		}
		finally{
			if(stmt!=null){
				try{
					stmt.close();
				}
				catch(SQLException e){
					logger.error("could not close stmt",e);
				}
			}
		}
	}
	
	@Override
	public Connection getAnyConnection() throws SQLException{
		final Connection connection=connectionProvider.getConnection();
		return connection;
	}

	@Override
	public void releaseAnyConnection(Connection connection) throws SQLException{
		switchSchema(connection,Constants.TENANT_SCHEMA_DEFAULT);
		connectionProvider.closeConnection(connection);
	}

	@Override
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
		switchSchema(connection,schema);
		return connection;
	}

	@Override
	public void releaseConnection(String tenantIdentifier, Connection connection)
			throws SQLException{
		releaseAnyConnection(connection);
	}

	@Override
	public boolean supportsAggressiveRelease(){
		return false;
	}

	@Override
	public void configure(@SuppressWarnings ("rawtypes") Map configurationValues){
		DatasourceConnectionProviderImpl dcp=new DatasourceConnectionProviderImpl();
		dcp.configure(configurationValues);
		connectionProvider=dcp;
	}

}
