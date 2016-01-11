package cn.ac.iscas.oncecloudshare.service.extensions.sample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 扩展中可以自定义controller
 * @author Chen Hao
 */
@Controller
public class SampleExtController {
	
	@Autowired
	SampleService sampleService;

	@RequestMapping(value="/extension/sample")
	@ResponseBody
	public String sample(){
		return sampleService.hello();
	}
}
