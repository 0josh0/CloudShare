package cn.ac.iscas.oncecloudshare.messaging.utils.gson;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;


/**
 * 
 * @author Chen Hao
 *
 */
public class Gsons {

	private static GsonBuilder basicGsonBuilder(){
		return new GsonBuilder()
//			.setPrettyPrinting()
//			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
		
			//Date类型按时间戳序列化/反序列化
			.registerTypeAdapter(Date.class,new JsonSerializer<Date>(){

				@Override
				public JsonElement serialize(Date src,Type typeOfSrc,
						JsonSerializationContext context){
					return context.serialize(src.getTime());
				}
				
			})
			.registerTypeAdapter(Date.class,new JsonDeserializer<Date>(){

				@Override
				public Date deserialize(JsonElement json,Type typeOfT,
						JsonDeserializationContext context)
						throws JsonParseException{
					try{
						return new Date(json.getAsLong());
					}
					catch(Exception e){
						return null;
					}
				}
			})
			//忽略@GsonHidden字段
			.setExclusionStrategies(new ExclusionStrategy(){
				
				@Override
				public boolean shouldSkipField(FieldAttributes f){
					return f.getAnnotation(GsonHidden.class)!=null;
				}
				
				@Override
				public boolean shouldSkipClass(Class<?> clazz){
					return false;
				}
			});
	}
	
	public static GsonBuilder defaultGsonBuilder(){
		return basicGsonBuilder()
				.setPrettyPrinting();
	}
	
	public static Gson gsonForLogging(){
		return new Gson();
	}
	
	public static Gson defaultGson(){
		return defaultGsonBuilder().create();
	}
	
	public static Gson defaultGsonNoPrettify(){
		return basicGsonBuilder().create();
	}
	
	/**
	 * deprecated：使用指定class的方法
	 * @param fields
	 * @return
	 */
	@Deprecated
	public static Gson filterByFields(final List<String> fields){
		return filterByFields(null,fields);
	}
	
	/**
	 * 对于clazz类型的数据，只序列号fields包含字段
	 * @param clazz
	 * @param fields
	 * @return
	 */
	public static Gson filterByFields(final Class<?> clazz,
			final List<String> fields){
		return filterByFields(clazz,fields,true);
	}
	
	public static Gson filterByFields(final Class<?> clazz,
			final List<String> fields,boolean prettify){
		GsonBuilder builder=basicGsonBuilder();
		if(fields!=null){
			builder.setExclusionStrategies(new ExclusionStrategy(){
				
				@Override
				public boolean shouldSkipField(FieldAttributes f){
					boolean contains=fields.contains(f.getName());
					if(clazz==null){
						return !contains;
					}
					else{
						return clazz.equals(f.getDeclaringClass()) && !contains;
					}
//					return clazz==null ? !contains :
//							clazz.equals(f.getClass()) && !contains;
				}
				
				@Override
				public boolean shouldSkipClass(Class<?> clazz){
					return false;
				}
			});
		}
		if(prettify){
			builder.setPrettyPrinting();
		}
		return builder.create();
	}
	
//	public static Gson expandFields(final List<String> fields){
//		GsonBuilder builder=basicGsonBuilder();
//		builder.setExclusionStrategies(new ExclusionStrategy(){
//			
//			@Override
//			public boolean shouldSkipField(FieldAttributes f){
//				if(f.getAnnotation(GsonOptional.class)!=null &&
//						fields.contains(f.getName())==false){
//					return true;
//				}
//				return false;
//			}
//			
//			@Override
//			public boolean shouldSkipClass(Class<?> clazz){
//				return false;
//			}
//		});
//		return builder.create();
//	}
}

