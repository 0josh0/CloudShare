package cn.ac.iscas.oncecloudshare.service.authorization;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springside.modules.test.spring.SpringTransactionalTestCase;

import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.service.account.UserService;

@Ignore
@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration (locations={ "/applicationContext.xml" })
public class UserServiceTest extends SpringTransactionalTestCase {

	@Autowired
	UserService userService;
	
	@Ignore
	@Test
	public void testSave() throws Exception{
		User user=new User();
		user.setName("hehe");
		user.setEmail("s@dfs.com");
		user.setPlainPassword("1232324");
		user.setQuota(1232424L);
		userService.addUser(user);
		System.out.println(user.getId());
	}
	
	@Before
	public void init() throws Exception{
		testSave();
	}
	
	@Test
	public void testLoad() throws Exception{
		for(int i=0;i<10;i++){
			new Thread(new Runnable(){
				
				@Override
				public void run(){
					User user=userService.find(1L);
					System.out.println(user);
				}
			}).start();
		}
	}
}
