package cn.ac.iscas.oncecloudshare.service.utils.spring;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class Builders {
	public static HttpHeadersBuilder httpHeadersBuilder(){
		return new HttpHeadersBuilder();
	}
	
	public static MultiValueMapBuilder multiValueMapBuilder(){
		return new MultiValueMapBuilder();
	}
	
	public static class HttpHeadersBuilder {
		private HttpHeaders headers;	
		
		public HttpHeadersBuilder(){
			headers = new HttpHeaders();
		}
		
		public HttpHeadersBuilder(HttpHeaders headers){
			this.headers = headers;
		}
		
		public HttpHeadersBuilder add(String name, String value){
			if (StringUtils.isNotEmpty(name) && value != null){
				headers.add(name, value);
			}
			return this;
		}
		
		public HttpHeaders build(){
			return headers;
		}
	}
	
	public static class MultiValueMapBuilder {
		private MultiValueMap<String, Object> map;

		public MultiValueMapBuilder() {
			map = new LinkedMultiValueMap<String, Object>();
		}

		public MultiValueMapBuilder(MultiValueMap<String, Object> map) {
			this.map = map;
		}

		public MultiValueMapBuilder add(String name, Object value) {
			if (StringUtils.isNotEmpty(name) && value != null){
				map.add(name, value);
			}
			return this;
		}

		public MultiValueMap<String, Object> build() {
			return map;
		}
	}
}
