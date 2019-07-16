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
package com.example.message;

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
import org.springframework.messaging.simp.config.AbstractMessageBrokerConfiguration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.ClassUtils;
import org.springframework.util.MimeTypeUtils;

/**
 * @author Dave Syer
 *
 */
public class MessageFunctionRunner {

	public static <I, O> void message(Function<Message<I>, Message<O>> function) {
		FunctionRunner.transfer(transfer(function));
	}

	private static <I, O> Function<Transfer, Transfer> transfer(
			Function<Message<I>, Message<O>> function) {
		MessageConverter converter = new ConverterFactory().brokerMessageConverter();
		return transfer -> {
			Message<byte[]> input = TransferUtils.toMessage(transfer);
			@SuppressWarnings("unchecked")
			Message<I> message = (Message<I>) converter.toMessage(input.getPayload(),
					input.getHeaders());
			Message<O> output = function.apply(message);
			byte[] payload = (byte[]) converter.fromMessage(output, byte[].class);
			Message<byte[]> result = MessageBuilder.withPayload(payload)
					.copyHeaders(output.getHeaders()).build();
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
