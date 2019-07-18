package org.lib.apinative;

import org.graalvm.nativeimage.c.CContext;
import org.graalvm.nativeimage.c.function.CFunctionPointer;
import org.graalvm.nativeimage.c.function.InvokeCFunctionPointer;
import org.graalvm.nativeimage.c.struct.CField;
import org.graalvm.nativeimage.c.struct.CPointerTo;
import org.graalvm.nativeimage.c.struct.CStruct;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.word.Pointer;
import org.graalvm.word.PointerBase;

@CContext(JNIHeaderDirectives.class)
@CStruct(value = "JNIEnv_", addStructKeyword = true)
interface JNIEnvironment extends PointerBase {

	@CField("functions")
	JNINativeInterface getFunctions();

}

@CContext(JNIHeaderDirectives.class)
@CStruct(value = "JavaVM_", addStructKeyword = true)
interface JavaVM extends PointerBase {

	@CField("functions")
	JNIInvokeInterface getFunctions();

}

@CContext(JNIHeaderDirectives.class)
@CStruct(value = "JNIInvokeInterface_", addStructKeyword = true)
interface JNIInvokeInterface extends PointerBase {

	@CField("AttachCurrentThread")
	AttachCurrentThread attachCurrentThread();

	@CField("DetachCurrentThread")
	DetachCurrentThread detachCurrentThread();

}

@CPointerTo(JNIEnvironment.class)
interface JNIEnvironmentPointer extends PointerBase {

	JNIEnvironment read();

	void write(JNIEnvironment value);

}

@CPointerTo(JavaVM.class)
interface JavaVMPointer extends PointerBase {

	JavaVM read();

	void write(JavaVM value);

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

	@CField
	NewGlobalRef getNewGlobalRef();

	@CField("GetJavaVM")
	GetJavaVM getJavaVM();

	@CField
	NewByteArray getNewByteArray();

	@CField
	GetByteArrayElements getGetByteArrayElements();

	@CField
	GetArrayLength getGetArrayLength();

	@CField
	SetByteArrayRegion getSetByteArrayRegion();

	@CField
	ReleaseByteArrayElements getReleaseByteArrayElements();
}

interface ReleaseByteArrayElements extends CFunctionPointer {
	@InvokeCFunctionPointer
	void find(JNIEnvironment env, JByteArray array, int mode);
}

interface NewByteArray extends CFunctionPointer {
	@InvokeCFunctionPointer
	JByteArray find(JNIEnvironment env, int length);
}

interface GetArrayLength extends CFunctionPointer {
	@InvokeCFunctionPointer
	int find(JNIEnvironment env, JArray array);
}

interface GetByteArrayElements extends CFunctionPointer {
	@InvokeCFunctionPointer
	Pointer find(JNIEnvironment env, JByteArray array, boolean isCopy);
}

interface SetByteArrayRegion extends CFunctionPointer {
	@InvokeCFunctionPointer
	void find(JNIEnvironment env, JByteArray array, int start, int length,
			Pointer buffer);
}

interface NewGlobalRef extends CFunctionPointer {

	@InvokeCFunctionPointer
	JObject find(JNIEnvironment env, JObject object);

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

interface GetJavaVM extends CFunctionPointer {

	@InvokeCFunctionPointer
	int find(JNIEnvironment env, JavaVMPointer jvm);

}

interface JObject extends PointerBase {

}

interface JString extends JObject {

}

interface JArray extends JObject {
}

interface JByteArray extends JArray {
}

interface CallStaticVoidMethod extends CFunctionPointer {

	@InvokeCFunctionPointer
	void call(JNIEnvironment env, JClass cls, JMethodID methodid, JValue args);

}

interface CallObjectMethod extends CFunctionPointer {

	@InvokeCFunctionPointer
	JObject call(JNIEnvironment env, JObject obj, JMethodID methodid, JValue args);

}

interface AttachCurrentThread extends CFunctionPointer {

	@InvokeCFunctionPointer
	int call(JavaVM vm, PointerBase penv, JValue args);

}

interface DetachCurrentThread extends CFunctionPointer {

	@InvokeCFunctionPointer
	int call(JavaVM vm);

}

interface JClass extends PointerBase {

}

interface JMethodID extends PointerBase {

}
