package org.lib.apinative;

import org.graalvm.nativeimage.c.CContext;
import org.graalvm.nativeimage.c.function.CFunctionPointer;
import org.graalvm.nativeimage.c.function.InvokeCFunctionPointer;
import org.graalvm.nativeimage.c.struct.CField;
import org.graalvm.nativeimage.c.struct.CPointerTo;
import org.graalvm.nativeimage.c.struct.CStruct;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.word.PointerBase;

@CContext(JNIHeaderDirectives.class)
@CStruct(value = "JNIEnv_", addStructKeyword = true)
interface JNIEnvironment extends PointerBase {
	@CField("functions")
	JNINativeInterface getFunctions();
}

@CPointerTo(JNIEnvironment.class)
interface JNIEnvironmentPointer extends PointerBase {
	JNIEnvironment read();

	void write(JNIEnvironment value);
}

@CContext(JNIHeaderDirectives.class)
@CStruct(value = "JNINativeInterface_", addStructKeyword = true)
interface JNINativeInterface extends PointerBase {
	@CField
	GetStaticMethodId getGetStaticMethodID();

	@CField
	GetMethodId getGetMethodID();

	@CField
	GetObjectClass getGetObjectClass();

	@CField
	CallStaticVoidMethod getCallStaticVoidMethodA();

	@CField
	CallObjectMethod getCallObjectMethodA();

	@CField
	GetStringUTFChars getGetStringUTFChars();

	@CField
	GetStringUTFLength getGetStringUTFLength();

	@CField
	NewString getNewStringUTF();

}

interface NewString extends CFunctionPointer {
	@InvokeCFunctionPointer
	JString find(JNIEnvironment env, CCharPointer str);
}

interface GetStringUTFLength extends CFunctionPointer {
	@InvokeCFunctionPointer
	int find(JNIEnvironment env, JString object);
}

interface GetStringUTFChars extends CFunctionPointer {
	@InvokeCFunctionPointer
	CCharPointer find(JNIEnvironment env, JString object, boolean copy);
}

interface GetObjectClass extends CFunctionPointer {
	@InvokeCFunctionPointer
	JClass find(JNIEnvironment env, JObject object);
}

interface GetStaticMethodId extends CFunctionPointer {
	@InvokeCFunctionPointer
	JMethodID find(JNIEnvironment env, JClass clazz, CCharPointer name, CCharPointer sig);
}

interface GetMethodId extends CFunctionPointer {
	@InvokeCFunctionPointer
	JMethodID find(JNIEnvironment env, JClass clazz, CCharPointer name, CCharPointer sig);
}

interface JObject extends PointerBase {
}

interface JString extends JObject {
}

interface CallStaticVoidMethod extends CFunctionPointer {
	@InvokeCFunctionPointer
	void call(JNIEnvironment env, JClass cls, JMethodID methodid, JValue args);
}

interface CallObjectMethod extends CFunctionPointer {
	@InvokeCFunctionPointer
	JObject call(JNIEnvironment env, JObject obj, JMethodID methodid, JValue args);
}

interface JClass extends PointerBase {
}

interface JMethodID extends PointerBase {
}