package cn.ac.iscas.oncecloudshare.service.utils.wrapper;

/**
 * 使用 MutableInt
 *
 */
@Deprecated
public class IntWrapper {
	private int value;

	public IntWrapper() {
		this(0);
	}

	public IntWrapper(int value) {
		this.value = value;
	}

	public int get() {
		return value;
	}

	public IntWrapper set(int value) {
		this.value = value;
		return this;
	}

	public int add(int delta) {
		value = value + delta;
		return value;
	}
}
