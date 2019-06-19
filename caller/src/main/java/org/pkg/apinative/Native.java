package org.pkg.apinative;

public final class Native {

	public static void main(String[] args) {
		FunctionRunner.run(foo -> foo.toUpperCase());
	}

}
