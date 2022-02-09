package org.lib.apinative;

import org.graalvm.nativeimage.StackValue;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CTypeConversion;

public final class NativeImpl {
	@CEntryPoint(name = "Java_org_pkg_apinative_Native_createIsolate", builtin = CEntryPoint.Builtin.CREATE_ISOLATE)
	public static native long createIsolate();

	@CEntryPoint(name = "Java_org_pkg_apinative_Native_print0")
	static void print(JNIEnvironment env, JClass clazz,
			@CEntryPoint.IsolateThreadContext long  isolateId, JObject object) {
		JNINativeInterface fn = env.getFunctions();
		System.err.println("Running: " + string(env, object));
	}

	private static String string(JNIEnvironment env, JObject object) {
		JNINativeInterface fn = env.getFunctions();
		try (CTypeConversion.CCharPointerHolder name = CTypeConversion
				.toCString("toString");
				CTypeConversion.CCharPointerHolder sig = CTypeConversion
						.toCString("()Ljava/lang/String;");) {
			JClass cls = fn.getGetObjectClass().find(env, object);
			JMethodID method = fn.getGetMethodID().find(env, cls, name.get(), sig.get());
			JObject call = fn.getCallObjectMethodA().call(env, object, method, null);
			String string = CTypeConversion
					.toJavaString(fn.getGetStringUTFChars().find(env, call));
			return string;
		}
	}

}