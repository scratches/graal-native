package org.lib.apinative;

import org.graalvm.nativeimage.Isolate;
import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.Isolates;
import org.graalvm.nativeimage.StackValue;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CTypeConversion;

public final class NativeImpl {

	@CEntryPoint(name = "Java_org_pkg_apinative_Native_createIsolate", builtin = CEntryPoint.Builtin.CREATE_ISOLATE)
	public static native IsolateThread createIsolate();

	@CEntryPoint(name = "Java_org_pkg_apinative_Native_run0")
	static void run(JNIEnvironment env, JClass clazz, @CEntryPoint.IsolateThreadContext IsolateThread threadId,
			JObject function) {
		System.err.println(threadId.rawValue());
		Isolate isolate = Isolates.getIsolate(threadId);
		System.err.println(isolate.rawValue());
		System.err.println(process(env, function, "foo"));
		System.err.println(env.rawValue());
		JavaVMPointer pjvm = StackValue.get(1, JavaVMPointer.class);
		env.getFunctions().getJavaVM().find(env, pjvm);
		System.err.println("JVM: " + pjvm.rawValue());
		long id = Isolates.getIsolate(threadId).rawValue();
		System.err.println("SVM: " + id);
		new Thread(() -> {
			System.err.println("Thread, SVM: " + id);
			JNIEnvironmentPointer pfromEnv = StackValue.get(1, JNIEnvironmentPointer.class);
			JValue args = StackValue.get(1, JValue.class);
			JavaVM jvm = pjvm.read();
			jvm.getFunctions().attachCurrentThread().call(jvm, pfromEnv, args);
			JNIEnvironment fromEnv = pfromEnv.read();
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
				System.err.println("Thread, detached");
				System.err.println("Thread, done");
			}
		}).start();
		try {
			Thread.sleep(1000L);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
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