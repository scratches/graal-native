package org.pkg.apinative;

public final class Native {

	private static long isolate;
	static {
		System.loadLibrary("nativeimpl");
		isolate = createIsolate();
	}

	public static void main(String[] args) {
		print(new Foo("foo"));
	}

	public static <T> T print(T input) {
		return print0(isolate, input);
	}

	private static native <T> T print0(long isolate, T foo);

	private static native long createIsolate();

}

class Foo {

	private String value;

	public Foo() {
	}

	public Foo(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "Foo [value=" + this.value + "]";
	}

}