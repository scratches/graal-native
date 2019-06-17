package org.lib.apinative;

final class SVM {

	static final long ID;
	static {
		long id;
		try {
			System.loadLibrary("nativeimpl");
			id = svmInit();
		}
		catch (LinkageError e) {
			id = -1;
		}

		ID = id;
	}

	private static native long svmInit();

}
