package com.example;

import com.example.message.MessageFunctionRunner;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

public final class MessageApplication {

	public static void main(String[] args) {
		MessageFunctionRunner.message(MessageApplication::process);
	}

	private static Message<Foo> process(Message<Foo> transfer) {
		System.err.println("Receiving: " + transfer);
		return MessageBuilder
				.withPayload(new Foo(transfer.getPayload().getValue().toUpperCase()))
				.copyHeaders(transfer.getHeaders()).build();
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