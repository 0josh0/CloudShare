package cn.ac.iscas.oncecloudshare.service.rest;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;  
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;  

import org.junit.Test;


//@ActiveProfiles("development")
public class DemoRestTest extends BaseRestTest{
	
	static final String PREFIX="/api/v2/users";
	
	static final String REGISTRATION=PREFIX+"/registration?email={email}&password={password}&name={name}";
	
	@Test 
	public void register() throws Exception{
		String email="";
		String password="111111";
		String name="normal user";
		mockMvc.perform(post(REGISTRATION,email,password,name))
//			.andDo(print())
			.andExpect(status().isOk());
	}

	@Test
	public void getAllUsers()throws Exception{
		mockMvc.perform(get(PREFIX))
//			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.entries",hasSize(2)))
			;
	}
	
	@Test
	public void getMyUserInfo()throws Exception{
		String email=USER_EMAIL;
		login(email);
		mockMvc.perform(get(PREFIX+"/me"))
//			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.email",is(email)));
	}
}
