package com.vnpt.iot.portal.utils;

import java.util.List;

import com.google.gson.Gson;
import com.vnpt.iot.portal.model.ResponseModel;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Trinh Dinh Tuan : Developer
 * @Email tuantdxyz@gmail.com
 * @Version 1.0.0 Sep 17, 2020
 */

@Slf4j
public class CommonResponseModel {

	/**
	 * Convert List<Object> to String Json
	 * 
	 * @param list
	 * @return
	 */
	public static ResponseModel getResponseModelfromList(List<?> list) {
		ResponseModel response = new ResponseModel();
		try {
			Gson gson = new Gson();
			String jsonStr = gson.toJson(list);
			Object obj = gson.fromJson(jsonStr, Object.class);
			response.setContent(obj);
			response.setResponseStatusCode(EnumValues.StatusProtocolEnum.STATUS_200.code);
			response.setResponseStatusMessage(EnumValues.StatusProtocolEnum.STATUS_200.message);
		} catch (Exception e) {
			log.error(e.getMessage());
			response.setResponseStatusCode(EnumValues.StatusProtocolEnum.STATUS_603.code);
		}

		return response;
	}

}
