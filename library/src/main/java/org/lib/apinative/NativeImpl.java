package org.lib.apinative;

import org.graalvm.nativeimage.StackValue;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CTypeConversion;

public final class NativeImpl {
	@CEntryPoint(name = "Java_org_pkg_apinative_Native_createIsolate", builtin = CEntryPoint.Builtin.CreateIsolate)
	public static native long createIsolate();

	@CEntryPoint(name = "Java_org_pkg_apinative_Native_add0")
	static int add(JNIEnvironment env, JClass clazz,
			@CEntryPoint.IsolateContext long isolateId, int a, int b) {
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
	static void run(JNIEnvironment env, JClass clazz,
			@CEntryPoint.IsolateContext long isolateId, JObject object) {
		JNINativeInterface fn = env.getFunctions();
		try (CTypeConversion.CCharPointerHolder name = CTypeConversion
				.toCString("toString");
				CTypeConversion.CCharPointerHolder sig = CTypeConversion
						.toCString("()Ljava/lang/String;");) {
			JClass cls = fn.getGetObjectClass().find(env, object);
			System.err.println("Finding method");
			JMethodID method = fn.getGetMethodID().find(env, cls, name.get(), sig.get());
			System.err.println("Running");
			JObject call = fn.getCallObjectMethodA().call(env, object, method, null);
			String string = CTypeConversion
					.toJavaString(fn.getGetStringUTFChars().find(env, call));
			System.err.println("Result: " + string);
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