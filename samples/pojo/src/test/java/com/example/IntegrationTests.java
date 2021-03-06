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
import com.example.runner.FunctionRunner;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import reactor.core.publisher.Mono;

import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Dave Syer
 *
 */
public class IntegrationTests {

	private WebClient client = WebClient.builder().baseUrl("http://localhost:9000")
			.build();

	@BeforeClass
	public static void setUp() {
		if (System.getProperty("function.library.location") == null
				&& System.getProperty("function.library.path") == null) {
			System.setProperty("function.library.path", "../../target");
		}
	}

	@After
	public void close() {
		FunctionRunner.close();
	}

	@Test
	public void plainText() {
		CodecFunctionRunner.plain(String::toUpperCase, String.class, String.class);
		Mono<ClientResponse> result = client.post().body(Mono.just("foo"), String.class)
				.headers(headers -> headers.setContentType(MediaType.TEXT_PLAIN))
				.exchange();
		assertThat(result.block().bodyToMono(String.class).block()).isEqualTo("FOO");
	}

	@Test
	public void pojo() {
		CodecFunctionRunner.plain(IntegrationTests::foos, Foo.class, Foo.class);
		Mono<ClientResponse> result = client.post()
				.body(Mono.just("{\"value\":\"foo\"}"), String.class)
				.headers(headers -> headers.setContentType(MediaType.APPLICATION_JSON))
				.exchange();
		assertThat(result.block().bodyToMono(String.class).block())
				.isEqualTo("{\"value\":\"FOO\"}");
	}

	@Test
	public void message() {
		CodecFunctionRunner.message(IntegrationTests::process, Foo.class, Foo.class);
		Mono<ClientResponse> result = client.post()
				.body(Mono.just("{\"value\":\"foo\"}"), String.class)
				.headers(headers -> headers.setContentType(MediaType.APPLICATION_JSON))
				.exchange();
		assertThat(result.block().bodyToMono(String.class).block())
				.isEqualTo("{\"value\":\"FOO\"}");
	}

	static Message<Foo> process(Message<Foo> message) {
		System.err.println("Receiving: " + message);
		String value = message.getPayload().getValue();
		return MessageBuilder
				.withPayload(new Foo(value == null ? null : value.toUpperCase()))
				.copyHeaders(message.getHeaders()).build();
	}

	static Foo foos(Foo foo) {
		System.err.println("Receiving: " + foo);
		String value = foo.getValue();
		return new Foo(value == null ? null : value.toUpperCase());
	}

}

class Foo {

	private String value;

	public Foo() {
	}

	public Foo(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "Foo [value=" + this.value + "]";
	}

}