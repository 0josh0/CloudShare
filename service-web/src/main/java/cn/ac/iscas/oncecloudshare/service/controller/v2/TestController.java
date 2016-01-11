package cn.ac.iscas.oncecloudshare.service.controller.v2;

import java.util.Date;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.service.redis.RedisOperator;

@Controller
@RequestMapping("/test")
public class TestController extends BaseController {
	
//	@Autowired
//	RuntimeContext context;
//	
//	@Autowired
//	FileDao fileDao;
//	
//	@Autowired
//	FileVersionDao fvDao;
//	
//	@Autowired
//	UserService userService;
	
//	@Autowired
//	MailService mailService;
//	
//	@RequestMapping(value="sendMail")
//	@ResponseBody
//	public String testSendMail(){
//		Mail mail=new Mail("测试","测试");
//		mailService.send("revassez@163.com",mail);
//		return "";
//	}
	
	@RequestMapping(value="setRedis")
	@ResponseBody
	public String testSetRedis(){
		String value=new Date().toLocaleString();
		RedisOperator.getInstance().setex("hehe",value,3);
		return ok();
	}
	
	@RequestMapping(value="getRedis")
	@ResponseBody
	public String testGetRedis(){
		return RedisOperator.getInstance().get("hehe");
	}
	
//	@RequestMapping(value="sendNotif")
//	@ResponseBody
//	public String testSendNotif(@RequestParam List<Long> to){
//		String content="hehe "+new Date().toLocaleString();
//		NotificationType type=new NotificationType(){
//
//			@Override
//			public String getType(){
//				return "test";
//			}
//		};
//		
//		runtimeContext.getNotifService()
//			.sendNotif(new Notification(type,content,to));
//		return ok();
//	}
	
//	static class Foo{
//		String name;
//
//		public String getName(){
//			return name;
//		}
//
//		public void setName(String name){
//			this.name=name;
//		}
//	}
//	
//	static class Bar{
//		String name;
//
//		public String getName(){
//			return name;
//		}
//
//		public void setName(String name){
//			this.name=name;
//		}
//	}
//	
//	@InitBinder("foo")//只针对foo参数
//	public void configureBindingOfUser(WebDataBinder binder) {
//	    binder.setAllowedFields(""); //随便填一个没有的fieldname
//	}
//	
//	@ModelAttribute("foo")
//	Foo getFoo(){
//		Foo foo=new Foo();
//		foo.setName("foo");
//		return foo;
//	}
//	
//	@RequestMapping(value="model")
//	@ResponseBody
//	public String testModelAttribute(@ModelAttribute("foo") Foo foo,
//			Bar bar,String name){
//		return foo.name+","+bar.name+","+name;
//	}
	
//	@RequestMapping(value="send")
//	@ResponseBody
//	public String testSendNotif(@RequestParam String content,
//			@RequestParam List<Long> to){
//		context.getNotifService().sendShareNotif(content,to);
//		return "hehe";
//	}
	
//	@RequestMapping(value="deleteUser",method=RequestMethod.GET)
//	@ResponseBody
//	public void testDeleteUser(@RequestParam Long id){
//		userService.deleteUser(id);
//	}
//	
//	@RequestMapping(value="deleteFile",method=RequestMethod.GET)
//	@ResponseBody
//	public void testDeleteFile(@RequestParam Long id){
//		File file=fileDao.findOne(id);
//		fileDao.delete(file);
//	}
//	
//	@RequestMapping(value="page",method=RequestMethod.GET)
//	@ResponseBody
//	public String testPageParam(PageParam pageParam){
//		return Gsons.defaultGson().toJson(pageParam);
//	}
//	
//	@RequestMapping(value="saveUser",method=RequestMethod.GET)
//	@ResponseBody
//	public String saveUser(){
//		User user=new User();
//		user.setName("hehe");
//		user.setEmail("s@dfs.com");
//		user.setPlainPassword("1232324");
//		user.setQuota(4L);
//		userService.addUser(user);
//		return Gsons.defaultGson().toJson(user);
//	}
//	
//	@RequestMapping(value="findUser",method=RequestMethod.GET)
//	@ResponseBody
//	public String findUser(final @RequestParam Long id) throws Exception{
//		for(int i=0;i<10;i++){
//			new Thread(new Runnable(){
//				
//				@Override
//				public void run(){
//					User user=userService.find(id);
//					try{
//						userService.incrRestQuota(user,-1);
////						System.out.println(Gsons.defaultGson().toJson(user));
//					}
//					catch(Exception e){
//						e.printStackTrace();
//					}
//					
//				}
//			}).start();
//		}
//		return "";
//	}
//	
//	@RequestMapping(value="test",method=RequestMethod.GET)
//	@ResponseBody
//	public String test(){
//		Gson gson=Gsons.defaultGson();
//		
//		File f=new File();
//		FileVersion fv=new FileVersion();
//		
//		fileDao.save(f);
//		
//		fv.setFile(f);
//		fvDao.save(fv);
//		
//		System.out.println(gson.toJson(fv));
//		
//		return "";
//	}
//
//	@RequestMapping(value="upload",method=RequestMethod.POST,headers="content-type=multipart/*")
//	@ResponseBody
//	public String upload(HttpServletRequest req,
//			HttpServletResponse resp,
//			MultipartRequest request) throws FileNotFoundException, IOException{
////		try{
////			FileSource fileSource=context.getFileStorageService().saveFile(null,
////					new MultipartFileByteSource(file));
////			return fileSource.getMd5();
////		}
////		catch(IOException e){
////			e.printStackTrace();
////		}
////		if(new Random().nextBoolean()){
////			return "hehe";
////		}
//		MultipartFile file=request.getFile("file");
//		System.out.println(file.getOriginalFilename());
//		IOUtils.copy(file.getInputStream(),new FileOutputStream("d:/temp"));
//		return file.getOriginalFilename();
//	}
//	
//	@RequestMapping(value="download",method=RequestMethod.GET)
//	public void download(HttpServletResponse response,@RequestParam String md5){
//		try{
//			ByteSource source=context.getFileStorageService().retrieveFileContent(md5);
//			response.setContentLength((int)source.size());
//			source.copyTo(response.getOutputStream());
//			response.flushBuffer();
//			response.getOutputStream().close();
//		}
//		catch(IOException e){
//			e.printStackTrace();
//		}
//	}
}
