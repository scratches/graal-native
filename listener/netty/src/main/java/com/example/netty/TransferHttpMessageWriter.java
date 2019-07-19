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
package com.example.netty;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.TransferProtos.Headers.Header;
import com.example.TransferProtos.Transfer;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import org.springframework.core.ResolvableType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.codec.HttpMessageWriter;

/**
 * @author Dave Syer
 *
 */
public class TransferHttpMessageWriter implements HttpMessageWriter<Transfer> {

	private static Set<String> INVALID_REPONSE_HEADERS = new HashSet<>(Arrays
			.asList(HttpHeaders.ACCEPT, HttpHeaders.CONTENT_LENGTH, HttpHeaders.HOST));

	private boolean validResponse(Header header) {
		if (INVALID_REPONSE_HEADERS.contains(header.getName())) {
			return false;
		}
		return true;
	}

	@Override
	public List<MediaType> getWritableMediaTypes() {
		return Collections.singletonList(MediaType.ALL);
	}

	@Override
	public boolean canWrite(ResolvableType elementType, MediaType mediaType) {
		return elementType.isAssignableFrom(Transfer.class);
	}

	@Override
	public Mono<Void> write(Publisher<? extends Transfer> input,
			ResolvableType elementType, MediaType mediaType,
			ReactiveHttpOutputMessage message, Map<String, Object> hints) {
		return Mono.from(input).flatMap(transfer -> {
			HttpHeaders headers = message.getHeaders();
			for (Header header : transfer.getHeaders().getHeaderList()) {
				if (!validResponse(header)) {
					continue;
				}
				headers.addAll(header.getName(), header.getValueList());
			}
			return message.writeAndFlushWith(Mono.just(Mono.just(message.bufferFactory()
					.wrap(transfer.getBody().asReadOnlyByteBuffer()))));
		}).then(Mono.empty());
	}

}
