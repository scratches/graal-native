package org.lib.apinative;

import org.graalvm.nativeimage.StackValue;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CTypeConversion;

public final class NativeImpl {
	@CEntryPoint(name = "Java_org_pkg_apinative_Native_createIsolate", builtin = CEntryPoint.Builtin.CreateIsolate)
	public static native long createIsolate();

	@CEntryPoint(name = "Java_org_pkg_apinative_Native_add0")
	static int add(JNIEnvironment env, JClass clazz,
			@CEntryPoint.IsolateContext long isolateId, int a, int b) {
		JNINativeInterface fn = env.getFunctions();

		try (CTypeConversion.CCharPointerHolder name = CTypeConversion.toCString("hello");
				CTypeConversion.CCharPointerHolder sig = CTypeConversion
						.toCString("(ZCBSIJFD)V");) {
			JMethodID helloId = fn.getGetStaticMethodID().find(env, clazz, name.get(),
					sig.get());

			JValue args = StackValue.get(8, JValue.class);
			args.addressOf(0).z(false);
			args.addressOf(1).c('A');
			args.addressOf(2).b((byte) 22);
			args.addressOf(3).s((short) 33);
			args.addressOf(4).i(39);
			args.addressOf(5).j(Long.MAX_VALUE / 2l);
			args.addressOf(6).f((float) Math.PI);
			args.addressOf(7).d(Math.PI);
			fn.getCallStaticVoidMethodA().call(env, clazz, helloId, args);
		}

		return a + b;
	}
}