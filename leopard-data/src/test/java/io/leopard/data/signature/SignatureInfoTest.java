package io.leopard.data.signature;

import io.leopard.test4j.mock.BeanAssert;

import org.junit.Test;

public class SignatureInfoTest {

	@Test
	public void SignatureInfo() {
		BeanAssert.assertModel(SignatureInfo.class);
	}

}