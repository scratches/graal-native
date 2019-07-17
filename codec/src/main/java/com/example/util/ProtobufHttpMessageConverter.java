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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.example.TransferProtos.Headers;
import com.example.TransferProtos.Headers.Header;
import com.example.TransferProtos.Transfer;
import com.google.protobuf.ByteString;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

/**
 * @author Dave Syer
 *
 */
public class ProtobufHttpMessageConverter extends AbstractHttpMessageConverter<Transfer> {

	private static Set<String> INVALID_REPONSE_HEADERS = new HashSet<>(Arrays
			.asList(HttpHeaders.ACCEPT, HttpHeaders.CONTENT_LENGTH, HttpHeaders.HOST));

	@Override
	protected boolean supports(Class<?> clazz) {
		return Transfer.class.isAssignableFrom(clazz);
	}

	@Override
	protected Transfer readInternal(Class<? extends Transfer> clazz,
			HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {
		ByteString body = ByteString.readFrom(inputMessage.getBody());
		Headers.Builder headers = Headers.newBuilder();
		for (Entry<String, List<String>> header : inputMessage.getHeaders().entrySet()) {
			headers.addHeader(Header.newBuilder().setName(header.getKey())
					.addAllValue(header.getValue()).build());
		}
		return Transfer.newBuilder().setBody(body).setHeaders(headers).build();
	}

	@Override
	protected void writeInternal(Transfer transfer, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		HttpHeaders headers = outputMessage.getHeaders();
		for (Header header : transfer.getHeaders().getHeaderList()) {
			if (!validResponse(header)) {
				continue;
			}
			headers.addAll(header.getName(), header.getValueList());
		}
		outputMessage.getBody().write(transfer.getBody().toByteArray());
	}

	private boolean validResponse(Header header) {
		if (INVALID_REPONSE_HEADERS.contains(header.getName())) {
			return false;
		}
		return true;
	}

}
