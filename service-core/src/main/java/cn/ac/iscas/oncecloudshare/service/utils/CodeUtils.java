package cn.ac.iscas.oncecloudshare.service.utils;

import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 邀请码生成工具类
 * 
 * @author One
 * 
 */
public class CodeUtils {

	/**
	 * 自增
	 */
	private static AtomicInteger _nextInc = new AtomicInteger((new java.util.Random()).nextInt());

	/**
	 * 机器码
	 */
	private static final int _genmachine;
	static {
		try {
			int machinePiece;
			{
				try {
					StringBuilder sb = new StringBuilder();
					Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
					while (e.hasMoreElements()) {
						NetworkInterface ni = e.nextElement();
						sb.append(ni.toString());
					}
					machinePiece = sb.toString().hashCode() << 12;
				} catch (Throwable e) {
					machinePiece = (new Random().nextInt()) << 12;
				}
			}

			final int processPiece;
			{
				int processId = new java.util.Random().nextInt();
				try {
					processId = java.lang.management.ManagementFactory.getRuntimeMXBean().getName().hashCode();
				} catch (Throwable t) {
				}

				ClassLoader loader = CodeUtils.class.getClassLoader();
				int loaderId = loader != null ? System.identityHashCode(loader) : 0;
				StringBuilder sb = new StringBuilder();
				sb.append(Integer.toHexString(processId));
				sb.append(Integer.toHexString(loaderId));
				processPiece = sb.toString().hashCode() & 0xFFFF;
			}
			_genmachine = machinePiece | processPiece;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * 时间戳
	 */
	final int time;

	/**
	 * 机器码
	 */
	final int machine;

	/**
	 * 自增变量
	 */
	final int inc;

	private CodeUtils() {

		this.time = (int) System.currentTimeMillis() / new Random().nextInt(1000);

		this.machine = _genmachine;

		this.inc = _nextInc.getAndIncrement();

	}

	private byte[] toByteArray() {
		byte[] b = new byte[12];

		ByteBuffer buffer = ByteBuffer.wrap(b);

		buffer.putInt(time);
		buffer.putInt(machine);
		buffer.putInt(inc);
		return b;
	}

	private String toHexString() {
		final StringBuilder buf = new StringBuilder(24);

		for (final byte b : toByteArray()) {

			buf.append(String.format("%02x", b & 0xff));
		}

		return buf.toString();
	}

	/**
	 * 获取邀请码 长度为24的字符串
	 * 
	 * @return
	 */
	public static String getCodeBody() {
		return new CodeUtils().toHexString();
	}

}