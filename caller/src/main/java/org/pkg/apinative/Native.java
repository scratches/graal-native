package org.pkg.apinative;

public final class Native {

	private static long isolate;
	static {
		System.loadLibrary("nativeimpl");
		isolate = createIsolate();
	}

	public static void main(String[] args) {
		System.out.println("2 + 40 = " + add(2, 40));
		System.out.println("12 + 30 = " + add(12, 30));
		System.out.println("20 + 22 = " + add(20, 22));
		run(new Foo("foo"));
	}

	public static int add(int a, int b) {
		return add0(isolate, a, b);
	}

	public static void run(Foo foo) {
		run0(isolate, foo);
	}

	private static native int add0(long isolate, int a, int b);

	private static native int run0(long isolate, Foo foo);

	private static native long createIsolate();

	public static void hello(boolean z, char c, byte b, short s, int i, long j, float f,
			double d) {
		System.err.println("Hi, I have just been called back!");
		System.err.print("With: " + z + " " + c + " " + b + " " + s);
		System.err.println(" and: " + i + " " + j + " " + f + " " + d);
	}
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