package org.lib.apinative;

import org.graalvm.nativeimage.StackValue;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CTypeConversion;

import org.springframework.context.ConfigurableApplicationContext;

public final class NativeImpl {

	private static JObject function;

	private static JNIEnvironment env;

	private static ConfigurableApplicationContext context;

	public static void main(String[] args) {
		FunctionalSpringApplication application = new FunctionalSpringApplication(Object.class);
		application.addInitializers(new FunctionEndpointInitializer(value -> value.toUpperCase()));
		context = application.run(args);
	}

	@CEntryPoint(name = "Java_org_pkg_apinative_FunctionRunner_createIsolate",
			builtin = CEntryPoint.Builtin.CREATE_ISOLATE)
	public static native long createIsolate();

	@CEntryPoint(name = "Java_org_pkg_apinative_FunctionRunner_run0")
	static void run(JNIEnvironment env, JClass clazz, @CEntryPoint.IsolateThreadContext long threadId,
			JObject function) {
		NativeImpl.env = env;
		NativeImpl.function = env.getFunctions().getNewGlobalRef().find(env, function);
		FunctionalSpringApplication application = new FunctionalSpringApplication(Object.class);
		application.addInitializers(new FunctionEndpointInitializer(NativeImpl::process));
		context = application.run();
	}

	@CEntryPoint(name = "Java_org_pkg_apinative_FunctionRunner_close0")
	static void close(JNIEnvironment env, JClass clazz, @CEntryPoint.IsolateThreadContext long threadId) {
		if (context != null) {
			context.close();
			context = null;
		}
	}

	public static String process(String body) {
		JavaVM jvm = javaVM(env);
		System.err.println("Processing: " + body);
		JNIEnvironment fromEnv = fromEnv(jvm);
		try (CTypeConversion.CCharPointerHolder name = CTypeConversion.toCString("apply");
				CTypeConversion.CCharPointerHolder sig = CTypeConversion
						.toCString("(Ljava/lang/Object;)Ljava/lang/Object;");
				CTypeConversion.CCharPointerHolder input = CTypeConversion.toCString(body);) {
			JNINativeInterface fn = fromEnv.getFunctions();
			JClass cls = fn.getGetObjectClass().find(fromEnv, function);
			JMethodID apply = fn.getGetMethodID().find(fromEnv, cls, name.get(), sig.get());
			JValue args = StackValue.get(1, JValue.class);
			JString string = fn.getNewStringUTF().find(fromEnv, input.get());
			args.addressOf(0).l(string);
			JObject result = fn.getCallObjectMethodA().call(fromEnv, function, apply, args);
			return CTypeConversion.toJavaString(fn.getGetStringUTFChars().find(fromEnv, (JString) result, false));
		}
		finally {
			jvm.getFunctions().detachCurrentThread().call(jvm);
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

}