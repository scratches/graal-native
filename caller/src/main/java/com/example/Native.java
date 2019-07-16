package com.example;

import org.pkg.apinative.FunctionRunner;

public final class Native {

	public static void main(String[] args) {
		FunctionRunner.string(foo -> foo.toUpperCase());
	}

}
