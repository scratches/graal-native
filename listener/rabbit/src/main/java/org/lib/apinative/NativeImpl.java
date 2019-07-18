package org.lib.apinative;

import com.example.TransferProtos.Transfer;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.graalvm.nativeimage.StackValue;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CTypeConversion;
import org.graalvm.word.Pointer;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public final class NativeImpl {

	private static JObject function;

	private static JNIEnvironment env;

	private static ConfigurableApplicationContext context;

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(
				FunctionConfiguration.class);
		application.addInitializers(new FunctionEndpointInitializer(NativeImpl::uppercase));
		context = application.run(args);
	}

	/**
	 * Simple function for testing with main method.
	 */
	private static Transfer uppercase(Transfer value) {
		byte[] body = value.getBody().toByteArray();
		System.err.println("Processing: " + value + "\n" + body.length + " bytes");
		return Transfer.newBuilder(value).setBody(ByteString.copyFrom(
				new String(value.getBody().toByteArray()).toUpperCase().getBytes()))
				.build();
	}

	@CEntryPoint(name = "Java_com_example_runner_FunctionRunner_createIsolate", builtin = CEntryPoint.Builtin.CREATE_ISOLATE)
	public static native long createIsolate();

	@CEntryPoint(name = "Java_com_example_runner_FunctionRunner_run0")
	static void run(JNIEnvironment env, JClass clazz,
			@CEntryPoint.IsolateThreadContext long threadId, JObject function) {
		NativeImpl.env = env;
		NativeImpl.function = env.getFunctions().getNewGlobalRef().find(env, function);
		SpringApplication application = new SpringApplication(
				FunctionConfiguration.class);
		application.addInitializers(new FunctionEndpointInitializer(NativeImpl::process));
		context = application.run("--spring.config.location=file:application.properties,classpath:application.properties");
	}

	@CEntryPoint(name = "Java_com_example_runner_FunctionRunner_close0")
	static void close(JNIEnvironment env, JClass clazz,
			@CEntryPoint.IsolateThreadContext long threadId) {
		if (context != null) {
			context.close();
			context = null;
		}
	}

	public static Transfer process(Transfer transfer) {
		JavaVM jvm = javaVM(env);
		System.err.println("Processing: " + transfer.getBody().size() + " bytes");
		JNIEnvironment fromEnv = fromEnv(jvm);
		JNINativeInterface fn = fromEnv.getFunctions();
		try (CTypeConversion.CCharPointerHolder name = CTypeConversion.toCString("apply");
				CTypeConversion.CCharPointerHolder sig = CTypeConversion
						.toCString("(Ljava/lang/Object;)Ljava/lang/Object;")) {
			JClass cls = fn.getGetObjectClass().find(fromEnv, function);
			JMethodID apply = fn.getGetMethodID().find(fromEnv, cls, name.get(),
					sig.get());
			JValue args = StackValue.get(1, JValue.class);
			JByteArray bytes = copy(fromEnv, transfer.toByteArray());
			args.addressOf(0).l(bytes);
			JObject result = fn.getCallObjectMethodA().call(fromEnv, function, apply,
					args);
			if (!result.isNonNull()) {
				throw new IllegalStateException("Function call failed");
			}
			try {
				// Leaks memory because bytes is not released?
				return Transfer.parseFrom(bytes(fromEnv, (JByteArray) result));
			}
			catch (InvalidProtocolBufferException e) {
				throw new IllegalStateException("Cannot serialize", e);
			}
		}
		finally {
			jvm.getFunctions().detachCurrentThread().call(jvm);
		}
	}

	private static byte[] bytes(JNIEnvironment env, JByteArray result) {
		JNINativeInterface fn = env.getFunctions();
		int size = fn.getGetArrayLength().find(env, result);
		byte[] bytes = new byte[size];
		Pointer buffer = fn.getGetByteArrayElements().find(env, result, false);
		for (int i = 0; i < size; i++) {
			bytes[i] = buffer.readByte(i);
		}
		return bytes;
	}

	private static JByteArray copy(JNIEnvironment env, byte[] body) {
		JNINativeInterface fn = env.getFunctions();
		JByteArray bytes = fn.getNewByteArray().find(env, body.length);
		Pointer buffer = fn.getGetByteArrayElements().find(env, bytes, false);
		for (int i = 0; i < body.length; i++) {
			buffer.writeByte(i, body[i]);
		}
		fn.getSetByteArrayRegion().find(env, bytes, 0, body.length, buffer);
		return bytes;
	}

	private static JNIEnvironment fromEnv(JavaVM jvm) {
		JNIEnvironmentPointer pfromEnv = StackValue.get(1, JNIEnvironmentPointer.class);
		JValue args = StackValue.get(0, JValue.class);
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