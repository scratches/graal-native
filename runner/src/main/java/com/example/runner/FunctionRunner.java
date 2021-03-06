/*
 * Copyright 2019-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.runner;

import java.io.File;
import java.util.function.Function;

import com.example.TransferProtos.Transfer;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * @author Dave Syer
 *
 */
public class FunctionRunner {

	public static void string(Function<String, String> function) {
		transfer(strings(function));
	}

	public static void transfer(Function<Transfer, Transfer> function) {
		run0(isolate, proto(function));
		block();
	}

	public static void raw(Function<byte[], byte[]> function) {
		transfer(bytes(function));
	}

	public static void close() {
		close0(isolate);
	}

	private static Function<byte[], byte[]> proto(Function<Transfer, Transfer> function) {
		return bytes -> {
			try {
				return function.apply(Transfer.parseFrom(bytes)).toByteArray();
			}
			catch (InvalidProtocolBufferException e) {
				throw new IllegalStateException("Cannot deserialize", e);
			}
		};
	}

	private static long isolate;
	static {
		if (System.getProperty("function.library.location") != null) {
			System.load(new File(System.getProperty("function.library.location"))
					.getAbsolutePath());
		}
		else if (System.getProperty("function.library.path") != null) {
			System.load(new File(new File(System.getProperty("function.library.path")),
					"libnettylistener.so").getAbsolutePath());
		}
		else {
			System.loadLibrary("nettylistener");
		}
		isolate = createIsolate();
	}

	private static Function<Transfer, Transfer> bytes(Function<byte[], byte[]> function) {
		return body -> Transfer.newBuilder(body)
				.setBody(
						ByteString.copyFrom(function.apply(body.getBody().toByteArray())))
				.build();
	}

	private static Function<Transfer, Transfer> strings(
			Function<String, String> function) {
		return body -> Transfer.newBuilder(body)
				.setBody(ByteString.copyFrom(function
						.apply(new String(body.getBody().toByteArray())).getBytes()))
				.build();
	}

	private static void block() {
		Thread thread = new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(1000L);
				}
				catch (InterruptedException e) {
				}
			}
		});
		thread.setDaemon(false);
		thread.start();
	}

	private static native void run0(long isolate, Function<byte[], byte[]> function);

	private static native void close0(long isolate);

	private static native long createIsolate();

}
