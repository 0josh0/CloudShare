package cn.ac.iscas.oncecloudshare.service.rest;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.service.account.UserService;
import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.AnonPrincipal;
import cn.ac.iscas.oncecloudshare.service.service.authorization.principal.UserPrincipal;

import com.google.common.base.Preconditions;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {
		"classpath:applicationContext.xml",
		"classpath:applicationContext-shiro.xml",
		"file:src/main/webapp/WEB-INF/spring-mvc.xml",
		}) 
public class BaseRestTest{
	
	public static final String ADMIN_EMAIL="revassez@163.com";
	public static final String USER_EMAIL="chenhao12@otcaix.iscas.ac.cn";
	
	@Autowired  
    protected WebApplicationContext wac; 
	
	protected MockMvc mockMvc;
	
	@Autowired
	protected UserService userService;
	
	@Before
	public void initMockMvc(){
		// this.mockMvc =
		// webAppContextSetup(this.wac).alwaysExpect(status().isOk()).build();
		this.mockMvc=MockMvcBuilders.webAppContextSetup(wac).build();
	}
	
	protected void login(String email){
		User user=userService.findByEmail(email);
		login(user);
	}
	
	protected void login(long id){
		User user=userService.find(id);
		login(user);
	}
	
	protected void login(User u){
		Preconditions.checkNotNull(u);
		UserPrincipal principal=new UserPrincipal(u,0L);
		mockPrincipal(principal);
	}
	
	protected void asAnon(){
		mockPrincipal(AnonPrincipal.of());
	}
	
	protected void mockPrincipal(Object principal){
		ShiroTestUtils.mockSubject(principal);
	}
}

//public class BaseRestTest {
//
//	protected static Server jettyServer;
//
//	protected static SimpleDriverDataSource dataSource;
//
//	protected static PropertiesLoader propertiesLoader=new PropertiesLoader(
//			"classpath:/application.properties",
//			"classpath:/application.test.properties");
//
//	private static Logger logger=LoggerFactory.getLogger(BaseRestTest.class);
//
//	protected static URL baseUrl;
//	
//	@BeforeClass
//	public static void beforeClass() throws Exception{
//		String baseUrlStr=propertiesLoader.getProperty("baseUrl","");
//
//		baseUrl=new URL(baseUrlStr);
//		
//		Boolean isEmbedded=baseUrl.getHost().equals("localhost");
//
//		if(isEmbedded){
//			startJettyOnce();
//		}
//
//		buildDataSourceOnce();
//		reloadSampleData();
//	}
//
//	/**
//	 * 启动Jetty服务器, 仅启动一次.
//	 */
//	protected static void startJettyOnce() throws Exception{
//		if(jettyServer==null){
//			// 设定Spring的profile
//			System.setProperty("spring.profiles.active","test");
//
//			jettyServer=JettyFactory.createServerInSource(
//					baseUrl.getPort(),baseUrl.getPath());
////			JettyFactory.setTldJarNames(jettyServer,"");
//			jettyServer.start();
//
//			logger.info("Jetty Server started at "+baseUrl);
//		}
//	}
//
//	/**
//	 * 构造数据源，仅构造一次.
//	 */
//	protected static void buildDataSourceOnce() throws ClassNotFoundException{
//		if(dataSource==null){
//			dataSource=new SimpleDriverDataSource();
//			dataSource.setDriverClass((Class<? extends Driver>)Class
//					.forName(propertiesLoader.getProperty("jdbc.driver")));
//			dataSource.setUrl(propertiesLoader.getProperty("jdbc.url"));
//			dataSource.setUsername(propertiesLoader
//					.getProperty("jdbc.username"));
//			dataSource.setPassword(propertiesLoader
//					.getProperty("jdbc.password"));
//
//		}
//	}
//
//	/**
//	 * 载入默认数据.
//	 */
//	protected static void reloadSampleData() throws Exception{
////		DataFixtures.executeScript(dataSource,
////				"classpath:data/cleanup-data.sql",
////				"classpath:data/import-data.sql");
//	}
//
//}
