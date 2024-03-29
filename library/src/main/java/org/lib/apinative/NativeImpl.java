package org.lib.apinative;

import org.graalvm.nativeimage.StackValue;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CTypeConversion;
import org.graalvm.nativeimage.c.type.CCharPointer;

public final class NativeImpl {
	@CEntryPoint(name = "Java_org_pkg_apinative_Native_createIsolate", builtin = CEntryPoint.Builtin.CREATE_ISOLATE)
	public static native long createIsolate();

	@CEntryPoint(name = "Java_org_pkg_apinative_Native_add0")
	static int add(JNIEnvironment env, JClass clazz,
			@CEntryPoint.IsolateThreadContext long isolateId, int a, int b) {
		JNINativeInterface fn = env.getFunctions();

		try (CTypeConversion.CCharPointerHolder name = CTypeConversion.toCString("hello");
				CTypeConversion.CCharPointerHolder sig = CTypeConversion
						.toCString("(ZCBSIJFD)V");) {
			System.err.println("Finding static method");
			JMethodID helloId = fn.getGetStaticMethodID().find(env, clazz, name.get(),
					sig.get());

			JValue args = StackValue.get(8, JValue.class);
			args.addressOf(0).z(false);
			args.addressOf(1).c('A');
			args.addressOf(2).b((byte) 22);
			args.addressOf(3).s((short) 33);
			args.addressOf(4).i(39);
			args.addressOf(5).j(Long.MAX_VALUE / 2l);
			args.addressOf(6).f((float) Math.PI);
			args.addressOf(7).d(Math.PI);
			System.err.println("Calling");
			fn.getCallStaticVoidMethodA().call(env, clazz, helloId, args);
		}

		return a + b;
	}

	@CEntryPoint(name = "Java_org_pkg_apinative_Native_run0")
	static JObject run(JNIEnvironment env, JClass clazz,
			@CEntryPoint.IsolateThreadContext long isolateId, JObject function,
			JObject object) {
		JNINativeInterface fn = env.getFunctions();
		try (CTypeConversion.CCharPointerHolder name = CTypeConversion.toCString("apply");
				CTypeConversion.CCharPointerHolder sig = CTypeConversion
						.toCString("(Ljava/lang/Object;)Ljava/lang/Object;");) {
			JClass cls = fn.getGetObjectClass().find(env, function);
			JMethodID apply = fn.getGetMethodID().find(env, cls, name.get(), sig.get());
			JValue args = StackValue.get(1, JValue.class);
			args.addressOf(0).l(object);
			System.err.println("Running: " + string(env, object));
			JObject result = fn.getCallObjectMethodA().call(env, function, apply, args);
			return result;
		}
	}

	@CEntryPoint(name = "Java_org_pkg_apinative_Native_print0")
	static void print(JNIEnvironment env, JClass clazz,
			@CEntryPoint.IsolateThreadContext long isolateId, JObject object) {
		JNINativeInterface fn = env.getFunctions();
		System.err.println("Printing: " + CTypeConversion
				.toJavaString(fn.getGetStringUTFChars().find(env, object, false)));
	}

	private static String string(JNIEnvironment env, JObject object) {
		JNINativeInterface fn = env.getFunctions();
		try (CTypeConversion.CCharPointerHolder name = CTypeConversion
				.toCString("toString");
				CTypeConversion.CCharPointerHolder sig = CTypeConversion
						.toCString("()Ljava/lang/String;");) {
			JClass cls = fn.getGetObjectClass().find(env, object);
			JMethodID method = fn.getGetMethodID().find(env, cls, name.get(), sig.get());
			JValue args = StackValue.get(0, JValue.class);
			JObject call = fn.getCallObjectMethodA().call(env, object, method, args);
			String string = CTypeConversion
					.toJavaString(fn.getGetStringUTFChars().find(env, call, false));
			return string;
		}
	}

	public static void main(String[] args) throws Exception {
		System.err.println(NativeImpl.class.getMethod("hello", boolean.class, char.class)
				.toGenericString());
	}

	public String getValue() {
		return "value";
	}

	public static void hello(boolean z, char c) {
	}

}