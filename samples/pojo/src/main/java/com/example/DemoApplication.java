package com.example;

import com.example.codec.CodecFunctionRunner;

public final class DemoApplication {

	public static void main(String[] args) {
		CodecFunctionRunner.plain(String::toUpperCase, String.class, String.class);
	}

}
