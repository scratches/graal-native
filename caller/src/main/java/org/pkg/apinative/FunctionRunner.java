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
package org.pkg.apinative;

import java.util.function.Function;

/**
 * @author Dave Syer
 *
 */
public class FunctionRunner {

	private static long isolate;
	static {
		System.loadLibrary("nativeimpl");
		isolate = createIsolate();
	}

	private static Function<byte[], byte[]> bytes(Function<String, String> function) {
		return body -> function.apply(new String(body)).getBytes();
	}

	public static void run(Function<String, String> function) {
		run0(isolate, bytes(function));
		block();
	}

	public static void raw(Function<byte[], byte[]> function) {
		run0(isolate, function);
		block();
	}

	private static void block() {
		Thread thread = new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(100L);
				}
				catch (InterruptedException e) {
				}
			}
		});
		thread.setDaemon(false);
		thread.start();
	}

	private static native void run0(long isolate, Function<?, ?> function);

	private static native long createIsolate();

}
