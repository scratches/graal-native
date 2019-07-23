package com.example.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Function;

import com.example.TransferProtos.Headers;
import com.example.TransferProtos.Headers.Header;
import com.example.TransferProtos.Transfer;
import com.example.libnative.FunctionListenerAdapter;
import com.google.protobuf.ByteString;

public final class HttpFunctionListenerAdapter implements FunctionListenerAdapter {

	@Override

	public void run(Function<Transfer, Transfer> function) {
		int port = 8080;
		try {

			@SuppressWarnings("resource")
			ServerSocket socket = new ServerSocket(port);
			System.out.println("Listening...");
			while (true) {
				Socket clientSocket = socket.accept();
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				Transfer input = input(clientSocket.getInputStream());
				System.err.println(input);
				String contentType = contentType(input);
				StringBuilder response = new StringBuilder(
						"HTTP/1.1 200 OK\nContent-Type: " + contentType + "\n\n");
				response.append(function.apply(input).getBody().toStringUtf8());
				System.err.println(response);
				out.println(response);
				clientSocket.close();
			}
			// socket.close();
		}
		catch (IOException ex) {
			System.out.println("I/O error: " + ex.getMessage());
		}
	}

	private String contentType(Transfer input) {
		for (Header header : input.getHeaders().getHeaderList()) {
			if ("Content-Type".toLowerCase().equals(header.getName())) {
				if (header.getValueCount() > 0) {
					return header.getValue(0);
				}
			}
		}
		return "text/plain";
	}

	private Transfer input(InputStream inputStream) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
		StringBuilder body = new StringBuilder();
		Headers.Builder headers = Headers.newBuilder();
		String inputLine = in.readLine();
		int contentLength = 0;
		while (inputLine != null) {
			if (inputLine.contains(":")) {
				String name = inputLine.substring(0, inputLine.indexOf(":"));
				String value = inputLine.substring(name.length() + 1).trim();
				if (name.toLowerCase().equals("content-length")) {
					contentLength = Integer.parseInt(value);
				}
				headers.addHeader(
						Header.newBuilder().setName(name).addValue(value).build());
			}
			if (inputLine.isEmpty()) {
				body.append(copyToString(in, contentLength));
				break;
			}
			else {
				inputLine = in.readLine();
			}
		}
		return Transfer.newBuilder()
				.setBody(ByteString.copyFrom(body.toString().getBytes()))
				.setHeaders(headers.build()).build();
	}

	private String copyToString(Reader reader, int contentLength) throws IOException {
		StringBuilder out = new StringBuilder();
		char[] buffer = new char[contentLength];
		int bytesRead = -1;
		if ((bytesRead = reader.read(buffer)) != -1) {
			out.append(buffer, 0, bytesRead);
		}
		return out.toString();
	}

	@Override
	public void close() {
	}

	/**
	 * A simple main method for testing purposes.
	 */
	public static void main(String[] args) {
		new HttpFunctionListenerAdapter().run(HttpFunctionListenerAdapter::uppercase);
	}

	private static Transfer uppercase(Transfer value) {
		byte[] body = value.getBody().toByteArray();
		System.err.println("Processing: " + value + "\n" + body.length + " bytes");
		return Transfer.newBuilder(value).setBody(ByteString.copyFrom(
				new String(value.getBody().toByteArray()).toUpperCase().getBytes()))
				.build();
	}

}