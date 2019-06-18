package org.lib.apinative;

import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.StackValue;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CTypeConversion;

public final class NativeImpl {

	@CEntryPoint(name = "Java_org_pkg_apinative_Native_createIsolate", builtin = CEntryPoint.Builtin.CREATE_ISOLATE)
	public static native IsolateThread createIsolate();

	@CEntryPoint(name = "Java_org_pkg_apinative_Native_run0")
	static void run(JNIEnvironment env, JClass clazz, @CEntryPoint.IsolateThreadContext IsolateThread threadId,
			JObject function) {
		System.err.println(process(env, function, "foo"));
		System.err.println(env.rawValue());
		JavaVM jvm = javaVM(env);
		System.err.println("JVM: " + jvm.rawValue());
		new Thread(() -> {
			JNIEnvironment fromEnv = fromEnv(jvm);
			System.err.println("Thread, Env: " + fromEnv.rawValue());
			try {
				System.err.println("Result: " + process(fromEnv, function, "bar"));
			}
			catch (Throwable t) {
				t.printStackTrace();
			}
			finally {
				System.err.println("Thread, clean");
				jvm.getFunctions().detachCurrentThread().call(jvm);
				System.err.println("Thread, done");
			}
		}).start();
		try {
			System.err.println("Sleeping");
			Thread.sleep(1000L);
			System.err.println("Done");
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private static JNIEnvironment fromEnv(JavaVM jvm) {
		JNIEnvironmentPointer pfromEnv = StackValue.get(1, JNIEnvironmentPointer.class);
		JValue args = StackValue.get(1, JValue.class);
		jvm.getFunctions().attachCurrentThread().call(jvm, pfromEnv, args);
		JNIEnvironment fromEnv = pfromEnv.read();
		return fromEnv;
	}

	private static JavaVM javaVM(JNIEnvironment env) {
		JavaVMPointer pjvm = StackValue.get(1, JavaVMPointer.class);
		env.getFunctions().getJavaVM().find(env, pjvm);
		JavaVM jvm = pjvm.read();
		return jvm;
	}

	public static String process(JNIEnvironment env, JObject function, String body) {
		if (env.isNull()) {
			System.err.println("Null JNIEnvironment");
			return body;
		}
		if (function.isNull()) {
			System.err.println("Null Class");
			return body;
		}
		JNINativeInterface fn = env.getFunctions();
		if (fn.isNull()) {
			System.err.println("Null functions");
			return body;
		}
		try (CTypeConversion.CCharPointerHolder name = CTypeConversion.toCString("apply");
				CTypeConversion.CCharPointerHolder sig = CTypeConversion
						.toCString("(Ljava/lang/Object;)Ljava/lang/Object;");
				CTypeConversion.CCharPointerHolder input = CTypeConversion.toCString(body);) {
			System.err.println("6");
			JClass cls = fn.getGetObjectClass().find(env, function);
			System.err.println("5");
			if (cls.isNull()) {
				System.err.println("Null Class");
				return body;
			}
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
			return CTypeConversion.toJavaString(fn.getGetStringUTFChars().find(env, (JString) result, false));
		}
	}

}