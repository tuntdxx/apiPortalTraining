package com.vnpt.iot.portal.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vnpt.iot.portal.model.ResponseModel;
import com.vnpt.iot.portal.service.ProductService;
import com.vnpt.iot.portal.utils.ConstantDefine;
import com.vnpt.iot.portal.utils.EnumValues;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Trinh Dinh Tuan : Developer
 * @Email tuantdxyz@gmail.com
 * @Version 1.0.0 Sep 14, 2020
 */

@RestController
@RequestMapping("api/product")
@Slf4j
public class ProductController {

	@Autowired
	ProductService productService;

	@GetMapping
	@Secured("ROLE_ADMIN")
	public ResponseModel getProduct(String gte, String lte, boolean type, HttpServletRequest request,
			HttpServletResponse response) {
		ResponseModel responseModel = null;
		try {
			responseModel = productService.getAllProductions(gte, lte, type);
			if (responseModel.getResponseStatusCode().equals(EnumValues.StatusProtocolEnum.STATUS_200.code)) {
				response.setHeader(ConstantDefine.STATUS_CODE,
						EnumValues.StatusProtocolEnum.STATUS_200.code.toString());
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			response.setHeader(ConstantDefine.STATUS_CODE, EnumValues.StatusProtocolEnum.STATUS_611.code.toString());
			responseModel.setResponseStatusCode(EnumValues.StatusProtocolEnum.STATUS_611.code);
			responseModel.setResponseStatusMessage(EnumValues.StatusProtocolEnum.STATUS_611.message);
		}

		return responseModel;

	}

	@GetMapping("customer")
	@Secured("ROLE_ADMIN")
	public ResponseModel getProductForCustomer(String gte, String lte, String email, boolean type,
			HttpServletRequest request, HttpServletResponse response) {
		ResponseModel responseModel = null;
		try {
			responseModel = productService.getAllProductionForCustomer(gte, lte, email, type);
			if (responseModel.getResponseStatusCode().equals(EnumValues.StatusProtocolEnum.STATUS_200.code)) {
				response.setHeader(ConstantDefine.STATUS_CODE,
						EnumValues.StatusProtocolEnum.STATUS_200.code.toString());
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			response.setHeader(ConstantDefine.STATUS_CODE, EnumValues.StatusProtocolEnum.STATUS_611.code.toString());
			responseModel.setResponseStatusCode(EnumValues.StatusProtocolEnum.STATUS_611.code);
			responseModel.setResponseStatusMessage(EnumValues.StatusProtocolEnum.STATUS_611.message);
		}

		return responseModel;
	}

	@GetMapping("index")
	@Secured("ROLE_ADMIN")
	public ResponseModel getProductByEmailCustomer(String email, HttpServletRequest request,
			HttpServletResponse response) {
		ResponseModel responseModel = null;
		try {
			responseModel = productService.getProductByEmailCustomer(email);
			if (responseModel.getResponseStatusCode().equals(EnumValues.StatusProtocolEnum.STATUS_200.code)) {
				response.setHeader(ConstantDefine.STATUS_CODE,
						EnumValues.StatusProtocolEnum.STATUS_200.code.toString());
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			response.setHeader(ConstantDefine.STATUS_CODE, EnumValues.StatusProtocolEnum.STATUS_611.code.toString());
			responseModel.setResponseStatusCode(EnumValues.StatusProtocolEnum.STATUS_611.code);
			responseModel.setResponseStatusMessage(EnumValues.StatusProtocolEnum.STATUS_611.message);
		}

		return responseModel;

	}
}
