package cn.ac.iscas.oncecloudshare.service.extensions.msg.service;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import cn.ac.iscas.oncecloudshare.service.messaging.exceptions.MessageException;
import cn.ac.iscas.oncecloudshare.service.messaging.model.Room;
import cn.ac.iscas.oncecloudshare.service.service.multitenancy.TenantService;
import cn.ac.iscas.oncecloudshare.service.utils.Constants;
import cn.ac.iscas.oncecloudshare.service.utils.spring.Builders;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;

@Service
public class MessageService implements cn.ac.iscas.oncecloudshare.service.messaging.service.MessageService {
	private static String URL_ROOMS;
	private static String URL_ROOM;
	private static String URL_OCCUPANTS;
	private static String URL_OCCUPANTS_REMOVE;

	@Resource
	private RestTemplate restTemplate;
	@Resource
	private NotifConfigs notifConfigs;
	@Resource
	private TenantService tenantService;

	@PostConstruct
	public void init() {
		String serverUrl = notifConfigs.getMsgServerUrl();
		URL_ROOMS = serverUrl + "adminapi/muc/rooms";
		URL_ROOM = serverUrl + "adminapi/muc/rooms/{id}";
		URL_OCCUPANTS = serverUrl + "adminapi/muc/rooms/{id}/occupants";
		URL_OCCUPANTS_REMOVE = URL_OCCUPANTS + "?occupantIds={occupantIds}";
	}

	private HttpHeaders defaultHeaders() {
		return Builders.httpHeadersBuilder()
				.add("x-msg-secret-key", Constants.getMsgSecretKey())
				.add("x-tenant-id", String.valueOf(tenantService.getCurrentTenant().getId()))
				.add("content-type", MediaType.APPLICATION_FORM_URLENCODED_VALUE).build();
	}

	public Room createRoom(String subject, long ownerId, List<Long> occupantIds, boolean special) {
		try {
			HttpHeaders headers = defaultHeaders();
			MultiValueMap<String, Object> params = Builders.multiValueMapBuilder()
					.add("subject", subject)
					.add("ownerId", String.valueOf(ownerId))
					.add("occupantIds", Joiner.on(',').join(occupantIds))
					.add("special", String.valueOf(special))
					.build();
			HttpEntity<?> entity = new HttpEntity<Object>(params, headers);
			HttpEntity<Room> response = restTemplate.exchange(URL_ROOMS, HttpMethod.POST, entity, Room.class, params);
			return response.getBody();
		} catch (Exception e) {
			throw new MessageException(e);
		}
	}

	@Override
	public void deleteRoom(long roomId) {
		try {
			HttpEntity<String> entity = new HttpEntity<String>(defaultHeaders());
			Map<String, String> params = Maps.newHashMap();
			params.put("id", String.valueOf(roomId));
			restTemplate.exchange(URL_ROOM, HttpMethod.DELETE, entity, Map.class, params);
		} catch (Exception e) {
			throw new MessageException(e);
		}
	}

	@Override
	public Room findRoom(long roomId) {
		try {
			HttpEntity<String> entity = new HttpEntity<String>(defaultHeaders());
			Map<String, String> params = Maps.newHashMap();
			params.put("id", String.valueOf(roomId));
			HttpEntity<Room> response = restTemplate.exchange(URL_ROOM, HttpMethod.GET, entity, Room.class, params);
			return response.getBody();
		} catch (Exception e) {
			throw new MessageException(e);
		}
	}
	
	@Override
	public void changeOwner(long roomId, long ownerId) throws MessageException {
		try {
			MultiValueMap<String, Object> params = Builders.multiValueMapBuilder().add("ownerId", String.valueOf(ownerId)).build();
			HttpEntity<?> entity = new HttpEntity<Object>(params, defaultHeaders());
			Map<String, String> urlVars = Maps.newHashMap();
			urlVars.put("id", String.valueOf(roomId));
			restTemplate.exchange(URL_ROOM, HttpMethod.GET, entity, Map.class, urlVars);
		} catch (Exception e) {
			throw new MessageException(e);
		}
	}

	public void addOccupants(long roomId, List<Long> occupantIds) {
		try {
			// headers
			HttpHeaders headers = defaultHeaders();
			// body参数
			MultiValueMap<String, Object> params = Builders.multiValueMapBuilder().add("occupantIds", Joiner.on(',').join(occupantIds)).build();
			HttpEntity<?> entity = new HttpEntity<Object>(params, headers);
			// url参数
			Map<String, String> urlVars = Maps.newHashMap();
			urlVars.put("id", String.valueOf(roomId));
			restTemplate.exchange(URL_OCCUPANTS, HttpMethod.POST, entity, Map.class, urlVars);
		} catch (Exception e) {
			throw new MessageException(e);
		}
	}

	public void removeOccupants(long roomId, List<Long> occupantIds) {
		try {
			// headers
			HttpHeaders headers = defaultHeaders();
			HttpEntity<?> entity = new HttpEntity<Object>(headers);
			// url参数
			Map<String, String> urlVars = Maps.newHashMap();
			urlVars.put("id", String.valueOf(roomId));
			urlVars.put("occupantIds", Joiner.on(',').join(occupantIds));
			restTemplate.exchange(URL_OCCUPANTS_REMOVE, HttpMethod.DELETE, entity, Map.class, urlVars);
		} catch (Exception e) {
			throw new MessageException(e);
		}
	}
}