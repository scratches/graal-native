package com.example;

import java.nio.charset.Charset;

import com.example.TransferProtos.Transfer;
import com.google.protobuf.ByteString;
import org.pkg.apinative.FunctionRunner;

public final class Native {

	public static void main(String[] args) {
		FunctionRunner.transfer(Native::process);
	}

	private static Transfer process(Transfer transfer) {
		System.err.println("Receiving: " + transfer);
		return Transfer.newBuilder(transfer)
				.setBody(ByteString.copyFrom(transfer.getBody()
						.toString(Charset.defaultCharset()).toUpperCase().getBytes()))
				.build();
	}

}
