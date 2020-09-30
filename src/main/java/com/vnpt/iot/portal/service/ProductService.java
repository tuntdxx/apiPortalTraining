package com.vnpt.iot.portal.service;

import com.vnpt.iot.portal.model.ResponseModel;

/**
 * @author Trinh Dinh Tuan : Developer
 * @Email tuantdxyz@gmail.com
 * @Version 1.0.0 Sep 16, 2020
 */

public interface ProductService {

	public ResponseModel getAllProductions(String gte, String lte, boolean type);

	public ResponseModel getAllProductionForCustomer(String gte, String lte, String email, boolean type);

	public ResponseModel getProductByEmailCustomer(String email);

}
