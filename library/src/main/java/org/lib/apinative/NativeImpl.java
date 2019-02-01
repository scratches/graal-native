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
		FunctionalSpringApplication application = new FunctionalSpringApplication(
				Object.class);
		application.addInitializers(
				new FunctionEndpointInitializer(value -> value.toUpperCase()));
		context = application.run(args);
	}

	@CEntryPoint(name = "Java_org_pkg_apinative_Native_createIsolate", builtin = CEntryPoint.Builtin.CREATE_ISOLATE)
	public static native long createIsolate();

	@CEntryPoint(name = "Java_org_pkg_apinative_Native_run0")
	static void run(JNIEnvironment env, JClass clazz,
			@CEntryPoint.IsolateThreadContext long threadId, JObject function) {
		NativeImpl.env = env;
		NativeImpl.function = function;
		FunctionalSpringApplication application = new FunctionalSpringApplication(
				Object.class);
		application.addInitializers(new FunctionEndpointInitializer(NativeImpl::process));
		context = application.run();
	}

	@CEntryPoint(name = "Java_org_pkg_apinative_Native_close0")
	static void close(JNIEnvironment env, JClass clazz,
			@CEntryPoint.IsolateContext long isolateId,
			@CEntryPoint.IsolateThreadContext long thread) {
		if (context != null) {
			context.close();
			context = null;
		}
	}

	public static String process(String body) {
		JNINativeInterface fn = env.getFunctions();
		try (CTypeConversion.CCharPointerHolder name = CTypeConversion.toCString("apply");
				CTypeConversion.CCharPointerHolder sig = CTypeConversion
						.toCString("(Ljava/lang/Object;)Ljava/lang/Object;");
				CTypeConversion.CCharPointerHolder input = CTypeConversion
						.toCString(body);) {
			JClass cls = fn.getGetObjectClass().find(env, function);
			JMethodID apply = fn.getGetMethodID().find(env, cls, name.get(), sig.get());
			JValue args = StackValue.get(1, JValue.class);
			JString string = fn.getNewStringUTF().find(env, input.get());
			args.addressOf(0).l(string);
			JObject result = fn.getCallObjectMethodA().call(env, function, apply, args);
			return CTypeConversion.toJavaString(
					fn.getGetStringUTFChars().find(env, (JString) result, false));
		}
	}

}