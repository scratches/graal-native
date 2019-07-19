package com.example.rabbit;

import java.util.function.Function;

import com.example.TransferProtos.Transfer;
import com.example.libnative.FunctionListenerAdapter;
import com.google.protobuf.ByteString;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public final class RabbitFunctionListenerAdapter implements FunctionListenerAdapter {

	private ConfigurableApplicationContext context;

	@Override

	public void run(Function<Transfer, Transfer> function) {
		SpringApplication application = new SpringApplication(
				FunctionConfiguration.class);
		application.addInitializers(new FunctionEndpointInitializer(function));
		context = application.run("--spring.config.location=file:application.properties,classpath:application.properties");
	}

	@Override
	public void close() {
		if (context != null) {
			context.close();
			context = null;
		}
	}

	/**
	 * A simple main method for testing purposes.
	 */
	public static void main(String[] args) {
		new RabbitFunctionListenerAdapter().run(RabbitFunctionListenerAdapter::uppercase);
	}

	private static Transfer uppercase(Transfer value) {
		byte[] body = value.getBody().toByteArray();
		System.err.println("Processing: " + value + "\n" + body.length + " bytes");
		return Transfer.newBuilder(value).setBody(ByteString.copyFrom(
				new String(value.getBody().toByteArray()).toUpperCase().getBytes()))
				.build();
	}

}