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
package com.example;

import com.example.codec.CodecFunctionRunner;

class FooApplication {

	public static void main(String[] args) {
		IntegrationTests.setUp();
		CodecFunctionRunner.plain(FooApplication::foos, Foo.class, Foo.class);
	}

	static Foo foos(Foo foo) {
		System.err.println("Receiving: " + foo);
		String value = foo.getValue();
		return new Foo(value == null ? null : value.toUpperCase());
	}

}