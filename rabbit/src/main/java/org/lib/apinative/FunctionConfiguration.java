/*
 * Copyright 2018 the original author or authors.
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

package org.lib.apinative;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.example.TransferProtos.Transfer;
import com.example.util.TransferUtils;

import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.amqp.support.AmqpHeaderMapper;
import org.springframework.integration.amqp.support.DefaultAmqpHeaderMapper;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * @author Dave Syer
 * @since 2.0
 *
 */
@SpringBootApplication(proxyBeanMethods = false)
class FunctionConfiguration {

	private volatile AmqpHeaderMapper inboundHeaderMapper = DefaultAmqpHeaderMapper
			.inboundMapper();

	private volatile AmqpHeaderMapper outboundHeaderMapper = DefaultAmqpHeaderMapper
			.outboundMapper();

	@Bean
	Queue queue() {
		return new AnonymousQueue();
	}

	@Bean
	TopicExchange input() {
		return new TopicExchange("input");
	}

	@Bean
	TopicExchange output() {
		return new TopicExchange("output");
	}

	@Bean
	Binding binding(Queue queue, @Qualifier("input") TopicExchange exchange) {
		return BindingBuilder.bind(queue).to(exchange).with("#");
	}

	@Bean
	MessageConverter transferMessageConverter() {
		// TODO: use MessagingMessageConverter?
		return new MessageConverter() {

			@Override
			public Message toMessage(Object object, MessageProperties messageProperties)
					throws MessageConversionException {
				Transfer transfer = (Transfer) object;
				Map<String, Object> headers = new HashMap<>();
				headers.putAll(
						outboundHeaderMapper.toHeadersFromReply(messageProperties));
				headers.putAll(TransferUtils.toMessage(transfer).getHeaders());
				outboundHeaderMapper.fromHeadersToReply(new MessageHeaders(headers),
						messageProperties);
				return new Message(transfer.getBody().toByteArray(), messageProperties);
			}

			@Override
			public Object fromMessage(Message message) throws MessageConversionException {
				Map<String, Object> map = inboundHeaderMapper
						.toHeadersFromRequest(message.getMessageProperties());
				return TransferUtils.fromMessage(MessageBuilder
						.withPayload(message.getBody()).copyHeaders(map).build());
			}

		};
	}

}

@Component
class Receiver {
	private RabbitTemplate template;
	private Function<Transfer, Transfer> function;

	public Receiver(RabbitTemplate template, Function<Transfer, Transfer> function) {
		this.template = template;
		this.function = function;
	}

	/**
	 * Send a message with empty routing key, JSON content and
	 * <code>content_type=application/json</code> in the properties.
	 */
	@RabbitListener(queues = "#{queue.name}")
	public void receive(Transfer message) {
		System.out.println("Received <" + message + ">");
		template.convertAndSend(function.apply(message));
	}

}