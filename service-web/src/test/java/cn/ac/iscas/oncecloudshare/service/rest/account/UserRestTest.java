package cn.ac.iscas.oncecloudshare.service.rest.account;

import org.junit.Test;

import cn.ac.iscas.oncecloudshare.service.rest.BaseRestTest;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;  
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;  
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;  

public class UserRestTest extends BaseRestTest{

	static final String PREFIX="/api/v2/users";
	static final String SEARCH=PREFIX+"/search?q={q}";
	
	@Test
	public void anonSearch() throws Exception{
		mockPrincipal(new Object());
		//invalid
		mockMvc.perform(get(SEARCH,"id::eq::1"))
			.andExpect(status().isOk())
			.andDo(print())
			.andExpect(jsonPath("$.entries",hasSize(0)))
			;
		//valid
		mockMvc.perform(get(SEARCH,"email::eq::"+USER_EMAIL))
			.andExpect(status().isOk())
			.andDo(print())
			.andExpect(jsonPath("$.entries",hasSize(1)))
			.andExpect(jsonPath("$.entries[0]",hasKey("email")))
			;
	}
}
