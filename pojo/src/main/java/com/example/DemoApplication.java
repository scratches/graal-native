package com.example;

import com.example.spring.SpringFunctionRunner;

public final class DemoApplication {

	public static void main(String[] args) {
		SpringFunctionRunner.plain(String::toUpperCase, String.class, String.class);
	}

}
