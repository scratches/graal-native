package org.lib.apinative;

import org.graalvm.nativeimage.StackValue;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CTypeConversion;

public final class NativeImpl {

	private static JObject function;

	private static JNIEnvironment env;

	@CEntryPoint(name = "Java_org_pkg_apinative_Native_createIsolate", builtin = CEntryPoint.Builtin.CREATE_ISOLATE)
	public static native long createIsolate();

	@CEntryPoint(name = "Java_org_pkg_apinative_Native_run0")
	static void run(JNIEnvironment env, JClass clazz,
			@CEntryPoint.IsolateThreadContext long threadId, JObject function) {
		NativeImpl.env = env;
		NativeImpl.function = function;
		System.err.println(process("foo"));
		new Thread(() -> System.err.println(process("bar"))).start();
		try {
			Thread.sleep(1000L);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public static String process(String body) {
		JNINativeInterface fn = env.getFunctions();
		try (CTypeConversion.CCharPointerHolder name = CTypeConversion.toCString("apply");
				CTypeConversion.CCharPointerHolder sig = CTypeConversion
						.toCString("(Ljava/lang/Object;)Ljava/lang/Object;");
				CTypeConversion.CCharPointerHolder input = CTypeConversion
						.toCString(body);) {
			System.err.println("6");
			JClass cls = fn.getGetObjectClass().find(env, function);
			System.err.println("5");
			JMethodID apply = fn.getGetMethodID().find(env, cls, name.get(), sig.get());
			System.err.println("4");
			JValue args = StackValue.get(1, JValue.class);
			System.err.println("3");
			JString string = fn.getNewStringUTF().find(env, input.get());
			System.err.println("2");
			args.addressOf(0).l(string);
			System.err.println("1");
			JObject result = fn.getCallObjectMethodA().call(env, function, apply, args);
			System.err.println("0");
			return CTypeConversion.toJavaString(
					fn.getGetStringUTFChars().find(env, (JString) result, false));
		}
	}

}