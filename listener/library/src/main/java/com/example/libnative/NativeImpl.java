/*
 * Copyright 2019-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.libnative;

import java.util.ServiceLoader;
import java.util.function.Function;

import com.example.TransferProtos.Transfer;
import com.google.protobuf.InvalidProtocolBufferException;
import org.graalvm.nativeimage.StackValue;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CTypeConversion;
import org.graalvm.word.Pointer;

/**
 * Entry points for native library that connects to a source and sink of messages, and
 * passes them to a user-defined {@link Function} to be handled. Library authors need to
 * provide a single implementation of {@link FunctionListenerAdapter} and declare it in
 * the standard service loader (META-INF/services).
 * 
 * @author Dave Syer
 *
 */
public final class NativeImpl {

	private static JObject function;

	private static JNIEnvironment env;

	private static FunctionListenerAdapter adapter;

	@CEntryPoint(name = "Java_com_example_runner_FunctionRunner_createIsolate", builtin = CEntryPoint.Builtin.CREATE_ISOLATE)
	public static native long createIsolate();

	@CEntryPoint(name = "Java_com_example_runner_FunctionRunner_run0")
	static void run(JNIEnvironment env, JClass clazz,
			@CEntryPoint.IsolateThreadContext long threadId, JObject function) {
		NativeImpl.env = env;
		NativeImpl.function = env.getFunctions().getNewGlobalRef().find(env, function);
		// TODO: look for conflicts, missing adapter etc.
		adapter = ServiceLoader.load(FunctionListenerAdapter.class).iterator().next();
		System.err.println("Found adapter: " + adapter);
		adapter.run(NativeImpl::process);
	}

	@CEntryPoint(name = "Java_com_example_runner_FunctionRunner_close0")
	static void close(JNIEnvironment env, JClass clazz,
			@CEntryPoint.IsolateThreadContext long threadId) {
		if (adapter != null) {
			adapter.close();
			adapter = null;
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