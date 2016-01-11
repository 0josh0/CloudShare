package cn.ac.iscas.oncecloudshare.service.utils;

import java.text.ParseException;
import java.util.Date;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

public class DateUtils extends org.apache.commons.lang3.time.DateUtils {
	// 永不过期
	public static final long NEVER_EXPIRE_MILLIS;
	static {
		try {
			NEVER_EXPIRE_MILLIS = parseDate("4000-01-01", "yyyy-MM-dd").getTime();
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
	
	// 生成 0-9 a-z A-Z的字符数组
	private static char[] chars = new char[62];	
	static{
		for (int i = 0; i < chars.length; i++){
			if (i < 10){
				chars[i] = (char) ('0' + i);
			}else if (i < 36){
				chars[i] = (char) ('a' + i - 10);
			} else{
				chars[i] = (char) ('A' + i -36);
			}
		}
	}
	
	/**
	 * 通过时间生成unique key
	 * 
	 * @return
	 */
	public static String uniqueKeyFromDate(Date date){
		Random random = new Random();
		long time = date.getTime() * 10 + random.nextInt(10);
		time = Long.valueOf(StringUtils.reverse(String.valueOf(time)));		
		long tmp = time;
		StringBuilder buffer = new StringBuilder();
		while (tmp > chars.length){
			buffer.append(chars[(int) (tmp % chars.length)]);
			tmp = tmp / chars.length;
		}
		String result = buffer.toString();
		return StringUtils.rightPad(result, 8, chars[random.nextInt(chars.length)]);
	}
}
