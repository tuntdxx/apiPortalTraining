package com.vnpt.iot.portal.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.vnpt.iot.portal.entity.Customer;
import com.vnpt.iot.portal.model.RequestModel;
import com.vnpt.iot.portal.model.ResponseModel;

/**
 * @author Trinh Dinh Tuan : Developer
 * @Email tuantdxyz@gmail.com
 * @Version 1.0.0 Sep 7, 2020
 */

public interface CustomerService {
	public ResponseModel createCustomer(RequestModel customerRequest, HttpServletRequest request,
			HttpServletResponse response);

	public ResponseModel viewCustomers(Integer page, Integer limit, boolean sortDesc, String sortBy, String params,
			Integer contractType);

	public Customer findById(String id);

	public ResponseModel showCustomer(String id);

	public void editCustomer(RequestModel customerRequest);

	public void deleteCustomer(String id);

	public ResponseModel findByCustomer(Integer contractType);
	
	public Customer findByEmail(String email);
}
