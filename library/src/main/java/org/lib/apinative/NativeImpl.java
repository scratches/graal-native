package org.lib.apinative;

import com.oracle.svm.core.c.function.CEntryPointActions;
import com.oracle.svm.core.c.function.CEntryPointOptions;
import org.graalvm.nativeimage.Isolate;
import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.Isolates;
import org.graalvm.nativeimage.StackValue;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CTypeConversion;
import org.graalvm.word.WordFactory;

public final class NativeImpl {

	@CEntryPoint(name = "Java_org_pkg_apinative_Native_createIsolate", builtin = CEntryPoint.Builtin.CREATE_ISOLATE)
	public static native IsolateThread createIsolate();

	@CEntryPoint(name = "Java_org_pkg_apinative_Native_run0")
	static void run(JNIEnvironment env, JClass clazz, @CEntryPoint.IsolateThreadContext IsolateThread threadId,
			JObject function) {
		System.err.println(threadId.rawValue());
		Isolate isolate = Isolates.getIsolate(threadId);
		System.err.println(isolate.rawValue());
		System.err.println(process(env, threadId, function, "foo"));
		System.err.println(env.rawValue());
		// Barf...
		System.loadLibrary("nativeimpl");
		long id = Isolates.getIsolate(threadId).rawValue();
		System.err.println("SVM: " + id);
		System.err.println(ownJNIEnv(id));
		new Thread(() -> {
			long raw = Isolates.getIsolate(threadId).rawValue();
			System.err.println("SVM: " + id);
			System.err.println("SVM: " + raw);
			System.err.println(ownJNIEnv(id));
			final JNIEnvironment fromEnv = WordFactory.pointer(ownJNIEnv(id));
			IsolateThread current = Isolates.attachCurrentThread(isolate);
			System.err.println(current.rawValue());
			try {
				System.err.println(process(fromEnv, threadId, function, "bar"));
			}
			catch (Throwable t) {
				t.printStackTrace();
			}
			finally {
				Isolates.detachThread(current);
			}
		}).start();
		try {
			Thread.sleep(1000L);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public static String process(JNIEnvironment env, IsolateThread threadId, JObject function, String body) {
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

	@CEntryPoint(name = "Java_org_lib_apinative_SVM_svmInit", builtin = CEntryPoint.Builtin.CREATE_ISOLATE)
	public static native long svmInit();

	public static final class AttachThreadPrologue {

		static void enter(@CEntryPoint.IsolateThreadContext long id) {
			Isolate isolate = WordFactory.pointer(id);
			int code = CEntryPointActions.enterAttachThread(isolate);
			if (code != 0) {
				CEntryPointActions.bailoutInPrologue();
			}
		}

	}

	@CEntryPointOptions(prologue = AttachThreadPrologue.class)
	@CEntryPoint(name = "Java_org_lib_apinative_NativeImpl_ownJNIEnv")
	static long ownJNIEnvImpl(JNIEnvironment env, JClass clazz, @CEntryPoint.IsolateThreadContext long isolateId) {
		return env.rawValue();
	}

	private static native long ownJNIEnv(long isolateId);

	@CEntryPoint(name = "Java_org_lib_apinative_NativeImpl_objPointer")
	static long objPointerImpl(JNIEnvironment env, JClass clazz, @CEntryPoint.IsolateThreadContext long isolateId,
			JObject obj) {
		JObject global = env.getFunctions().getNewGlobalRef().find(env, obj);
		return global.rawValue();
	}

	private static native long objPointer(long isolateId, Object obj);

}