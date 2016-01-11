package cn.ac.iscas.oncecloudshare.service.controller.sample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.ac.iscas.oncecloudshare.service.exceptions.rest.RestException;
import cn.ac.iscas.oncecloudshare.service.system.RuntimeContext;

@Controller
@RequestMapping(value="/sample")
public class SampleController {
	
	@Autowired
	RuntimeContext context;
	
	@RequestMapping(value="/error",method=RequestMethod.GET)
	@ResponseBody
	public String exceptionDemo(){
		//抛出的异常在RestExceptionHandler里处理
		throw new RestException(HttpStatus.NOT_FOUND,"error description");
	}
	
	@RequestMapping(value="/listener",method=RequestMethod.GET)
	@ResponseBody
	public String listenerDemo(@RequestParam String code){
		{
			//核心模块的controller通过EventBus抛出事件
			context.getEventBus().post(new SampleEvent(code));
		}
		System.out.println("正常执行 code="+code);
		return "OK";
	}
	
	@RequestMapping(value="/interceptor",method=RequestMethod.GET)
	@ResponseBody
	public String interceptorDemo(@RequestParam String code){
		{
			//如果抛出的是可中断事件，那么请求可能被listener中断
			context.getEventBus().post(new SampleInterceptableEvent(code));
		}
		System.out.println("正常执行 code="+code);
		return "OK";
	}
}
