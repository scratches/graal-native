package com.example.libnative;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.graalvm.nativeimage.c.CContext;

final class JNIHeaderDirectives implements CContext.Directives {
	@Override
	public List<String> getOptions() {
		File[] jnis = findJNIHeaders();
		return Arrays.asList("-I" + jnis[0].getParent(), "-I" + jnis[1].getParent());
	}

	@Override
	public List<String> getHeaderFiles() {
		File[] jnis = findJNIHeaders();
		return Arrays.asList("<" + jnis[0] + ">", "<" + jnis[1] + ">");
	}

	private static File[] findJNIHeaders() throws IllegalStateException {
		final File jreHome = new File(System.getProperty("java.home"));
		final File include = new File(jreHome.getParentFile(), "include");
		final File[] jnis = { new File(include, "jni.h"),
				new File(new File(include, "linux"), "jni_md.h"), };
		return jnis;
	}
}