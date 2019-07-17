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
package com.example.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.example.TransferProtos.Headers;
import com.example.TransferProtos.Headers.Header;
import com.example.TransferProtos.Transfer;
import com.example.TransferProtos.Transfer.Builder;
import com.google.protobuf.ByteString;
import com.google.protobuf.ProtocolStringList;

import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

/**
 * @author Dave Syer
 *
 */
public class TransferUtils {

	public static Message<byte[]> toMessage(Transfer transfer) {
		Map<String, Object> headers = new HashMap<>();
		for (Header header : transfer.getHeaders().getHeaderList()) {
			ProtocolStringList list = header.getValueList();
			String name = header.getName();
			if (HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(name)) {
				name = MessageHeaders.CONTENT_TYPE;
			}
			if (list.size() == 1) {
				headers.put(name, list.get(0));
			}
			else {
				headers.put(name, list);
			}
		}
		return MessageBuilder.withPayload(transfer.getBody().toByteArray())
				.copyHeaders(headers).build();
	}

	public static Transfer fromMessage(Message<byte[]> message) {
		Headers.Builder headers = Headers.newBuilder();
		for (Entry<String, Object> entry : message.getHeaders().entrySet()) {
			Object value = entry.getValue();
			String key = entry.getKey();
			if (MessageHeaders.CONTENT_TYPE.equalsIgnoreCase(key)) {
				key = HttpHeaders.CONTENT_TYPE;
			}
			if (value != null) {
				if (value instanceof Collection) {
					Iterable<String> values = ((Collection<?>) value).stream()
							.map(v -> v.toString()).collect(Collectors.toList());
					headers.addHeader(
							Header.newBuilder().setName(key).addAllValue(values).build());
				}
				else {
					headers.addHeader(
							Header.newBuilder().setName(key).addValue(value.toString()));
				}
			}
			else {
				headers.addHeader(Header.newBuilder().setName(key));
			}
		}
		Builder builder = Transfer.newBuilder();
		if (message.getPayload() != null) {
			builder = builder.setBody(ByteString.copyFrom(message.getPayload()));
		}
		return builder.setHeaders(headers).build();
	}

}
