package com.vnpt.iot.portal.utils;

/**
 * @author Trinh Dinh Tuan : Developer
 * @Email tuantdxyz@gmail.com
 * @Version 1.0.0 Sep 22, 2020
 */

public class CommonUtils {

	public static String getSecret(String server) {
		String secret = null;
		if (ConstantDefine.LOCALHOST_VALUE.equals(server)) {
			// set secret with localhost
			secret = ConstantDefine.JWT_SECRET_LOCAL;
		} else {
			// set secret with server
			secret = ConstantDefine.JWT_SECRET;
		}
		return secret;
	}

}
