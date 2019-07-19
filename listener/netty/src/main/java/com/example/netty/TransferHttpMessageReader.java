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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.example.TransferProtos.Headers;
import com.example.TransferProtos.Headers.Header;
import com.example.TransferProtos.Transfer;
import com.example.TransferProtos.Transfer.Builder;
import com.google.protobuf.ByteString;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpInputMessage;
import org.springframework.http.codec.HttpMessageReader;

/**
 * @author Dave Syer
 *
 */
public class TransferHttpMessageReader implements HttpMessageReader<Transfer> {

	@Override
	public List<MediaType> getReadableMediaTypes() {
		return Collections.singletonList(MediaType.ALL);
	}

	@Override
	public boolean canRead(ResolvableType elementType, MediaType mediaType) {
		return elementType.isAssignableFrom(Transfer.class);
	}

	@Override
	public Flux<Transfer> read(ResolvableType elementType,
			ReactiveHttpInputMessage message, Map<String, Object> hints) {
		return Flux.from(readMono(elementType, message, hints));
	}

	@Override
	public Mono<Transfer> readMono(ResolvableType elementType,
			ReactiveHttpInputMessage message, Map<String, Object> hints) {
		Builder builder = Transfer.newBuilder();
		Headers.Builder headers = Headers.newBuilder();
		for (Entry<String, List<String>> header : message.getHeaders().entrySet()) {
			headers.addHeader(Header.newBuilder().setName(header.getKey())
					.addAllValue(header.getValue()).build());
		}
		builder.setHeaders(headers);
		ByteString body = ByteString.EMPTY;
		return message.getBody().map(buffer -> map(buffer, body)).last()
				.map(buffer -> builder.setBody(buffer))
				.map(b -> b.build());
	}

	private ByteString map(DataBuffer buffer, ByteString value) {
		ByteString body = ByteString.copyFrom(buffer.asByteBuffer());
		return value.concat(body);
	}

}
