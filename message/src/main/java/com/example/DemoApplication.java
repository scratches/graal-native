package com.example;

import com.example.spring.SpringFunctionRunner;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

public final class DemoApplication {

	public static void main(String[] args) {
		// SpringFunctionRunner.message(DemoApplication::process, Foo.class, Foo.class);
		SpringFunctionRunner.plain(DemoApplication::foos, Foo.class, Foo.class);
		// SpringFunctionRunner.plain(String::toUpperCase, String.class, String.class);
	}

	static Message<Foo> process(Message<Foo> transfer) {
		System.err.println("Receiving: " + transfer);
		String value = transfer.getPayload().getValue();
		return MessageBuilder
				.withPayload(new Foo(value == null ? null : value.toUpperCase()))
				.copyHeaders(transfer.getHeaders()).build();
	}

	static Foo foos(Foo transfer) {
		System.err.println("Receiving: " + transfer);
		String value = transfer.getValue();
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