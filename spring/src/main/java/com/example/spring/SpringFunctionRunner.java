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
package com.example.spring;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.example.TransferProtos.Transfer;
import com.example.runner.FunctionRunner;
import com.example.util.TransferUtils;

import org.springframework.messaging.Message;
import org.springframework.messaging.converter.ByteArrayMessageConverter;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.DefaultContentTypeResolver;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.ClassUtils;
import org.springframework.util.MimeTypeUtils;

/**
 * @author Dave Syer
 *
 */
public class SpringFunctionRunner {

	public static <I, O> void message(Function<Message<I>, Message<O>> function, Class<I> input, Class<O> output) {
		FunctionRunner.transfer(transfer(function, input, output));
	}

	public static <I, O> void plain(Function<I, O> function, Class<I> input, Class<O> output) {
		FunctionRunner.transfer(plains(function, input, output));
	}

	private static <I, O> Function<Transfer, Transfer> plains(
			Function<I, O> function, Class<I> inputType, Class<O> outputType) {
		return transfer(input -> wrap(input, function), inputType, outputType);
	}
	
	private static <I,O> Message<O> wrap(Message<I> input, Function<I, O> function) {
		O payload = function.apply(input.getPayload());
		return MessageBuilder.withPayload(payload).copyHeaders(input.getHeaders()).build();
	}
	
	private static <I, O> Function<Transfer, Transfer> transfer(
			Function<Message<I>, Message<O>> function, Class<I> inputType, Class<O> outputType) {
		MessageConverter converter = new ConverterFactory().brokerMessageConverter();
		return transfer -> {
			Message<byte[]> input = TransferUtils.toMessage(transfer);
			@SuppressWarnings("unchecked")
			I payload = (I)converter.fromMessage(input, inputType);
			if (payload==null) {
				throw new IllegalStateException("Cannot convert to: " + inputType);
			}
			Message<I> message = MessageBuilder.withPayload(payload).copyHeaders(input.getHeaders()).build();
			Message<O> output = function.apply(message);
			@SuppressWarnings("unchecked")
			Message<byte[]> result = (Message<byte[]>) converter.toMessage(output.getPayload(), output.getHeaders());
			if (result == null) {
				throw new IllegalStateException("Cannot convert from: " + outputType);
			}
			return TransferUtils.fromMessage(result);
		};
	}

	static class ConverterFactory {
		private static final boolean jackson2Present = ClassUtils.isPresent(
				"com.fasterxml.jackson.databind.ObjectMapper",
				ConverterFactory.class.getClassLoader());

		public CompositeMessageConverter brokerMessageConverter() {
			List<MessageConverter> converters = new ArrayList<>();
			converters.add(new StringMessageConverter());
			converters.add(new ByteArrayMessageConverter());
			if (jackson2Present) {
				converters.add(createJacksonConverter());
			}
			return new CompositeMessageConverter(converters);
		}

		protected MappingJackson2MessageConverter createJacksonConverter() {
			DefaultContentTypeResolver resolver = new DefaultContentTypeResolver();
			resolver.setDefaultMimeType(MimeTypeUtils.APPLICATION_JSON);
			MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
			converter.setContentTypeResolver(resolver);
			return converter;
		}
	}
}
