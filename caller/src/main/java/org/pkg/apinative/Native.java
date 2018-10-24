package org.pkg.apinative;

import java.util.function.Function;

public final class Native {

	private static long isolate;
	static {
		System.loadLibrary("nativeimpl");
		isolate = createIsolate();
	}

	public static void main(String[] args) {
		run(foo -> foo.toUpperCase());
	}

	public static void run(Function<String, String> function) {
		run0(isolate, function);
	}

	private static native void run0(long isolate, Function<?, ?> function);

	private static native long createIsolate();

}
