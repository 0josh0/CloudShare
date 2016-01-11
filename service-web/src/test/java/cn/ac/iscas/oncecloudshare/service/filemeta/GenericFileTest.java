package cn.ac.iscas.oncecloudshare.service.filemeta;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springside.modules.test.spring.SpringTransactionalTestCase;

import cn.ac.iscas.oncecloudshare.service.dao.authorization.UserDao;
import cn.ac.iscas.oncecloudshare.service.dao.filemeta.FileDao;
import cn.ac.iscas.oncecloudshare.service.model.account.User;
import cn.ac.iscas.oncecloudshare.service.model.filemeta.File;
import cn.ac.iscas.oncecloudshare.service.utils.gson.Gsons;

import com.google.gson.Gson;

@Ignore
@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration (locations={ "/applicationContext.xml" })
public class GenericFileTest extends SpringTransactionalTestCase {

	@PersistenceContext
	EntityManager em;
	
	@Autowired
	FileDao fileDao;
	
	@Autowired
	UserDao userDao;
	
	@Ignore
	@Test
	@Transactional
	public void test(){
		Gson gson=Gsons.defaultGson();
		
//		em.getTransaction().begin();
		
		User user=new User();
		em.persist(user);	
		
		File parent=new File();
		em.persist(parent);
		
		File file=new File();
		file.setParent(parent);
		file.setOwner(user);
		em.persist(file);
		System.out.println(gson.toJson(file));
		File f=(File)file.getParent();
		System.out.println(gson.toJson(f));
		
//		em.getTransaction().commit();
		
		em.flush();
		
		List files=em.createQuery("select f from File f ").getResultList();
		System.out.println(gson.toJson(files));
		
		parent=em.find(File.class,parent.getId());
		System.out.println(parent.getChildren());

	}
	
	@Test
	@Transactional
	public void test2(){
		Gson gson=Gsons.defaultGson();
		
//		em.getTransaction().begin();
		
		User user=new User();
		userDao.save(user);	
		
		File parent=new File();
		fileDao.save(parent);
		
		File file=new File();
		file.setParent(parent);
		file.setOwner(user);
		fileDao.save(file);
		System.out.println(gson.toJson(file));
		File f=(File)file.getParent();
		System.out.println(gson.toJson(f));
		
		List files=(List)fileDao.findAll();
		System.out.println(gson.toJson(files));
		
		parent=fileDao.findOne(parent.getId());
		System.out.println(parent.getChildren());
	}
}
