package org.pkg.apinative;

public final class Native {
	public static void main(String[] args) {
		System.loadLibrary("nativeimpl");

		long isolate = createIsolate();

		System.out.println("2 + 40 = " + add(isolate, 2, 40));
		System.out.println("12 + 30 = " + add(isolate, 12, 30));
		System.out.println("20 + 22 = " + add(isolate, 20, 22));
	}

	private static native int add(long isolate, int a, int b);

	private static native long createIsolate();

	public static void hello(boolean z, char c, byte b, short s, int i, long j, float f,
			double d) {
		System.err.println("Hi, I have just been called back!");
		System.err.print("With: " + z + " " + c + " " + b + " " + s);
		System.err.println(" and: " + i + " " + j + " " + f + " " + d);
	}
}