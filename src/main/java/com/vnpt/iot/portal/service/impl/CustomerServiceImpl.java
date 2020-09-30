package com.vnpt.iot.portal.service.impl;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.RandomStringUtils;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.onem2m.common.constants.Operation;
import com.onem2m.common.constants.ResponseStatusCode;
import com.onem2m.common.model.RequestPrimitive;
import com.onem2m.common.model.ResponsePrimitive;
import com.onem2m.common.model.shortname.Asar;
import com.onem2m.protocol.protocol.RestClient;
import com.vnpt.iot.portal.entity.AuthUser;
import com.vnpt.iot.portal.entity.Customer;
import com.vnpt.iot.portal.entity.CustomerApikey;
import com.vnpt.iot.portal.exceptions.PortalException;
import com.vnpt.iot.portal.jwt.JwtAuthenticationFilter;
import com.vnpt.iot.portal.jwt.JwtTokenProvider;
import com.vnpt.iot.portal.model.CustomerDTO;
import com.vnpt.iot.portal.model.RequestModel;
import com.vnpt.iot.portal.model.ResponseModel;
import com.vnpt.iot.portal.repository.CustomerRepository;
import com.vnpt.iot.portal.service.CustomerService;
import com.vnpt.iot.portal.utils.CommonResponseModel;
import com.vnpt.iot.portal.utils.CommonUtils;
import com.vnpt.iot.portal.utils.ConstantDefine;
import com.vnpt.iot.portal.utils.EnumValues;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Trinh Dinh Tuan : Developer
 * @Email tuantdxyz@gmail.com
 * @Version 1.0.0 Sep 7, 2020
 */

@Service
@Slf4j
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepo;

	@Value("${spring.post.call.asar.url}")
	private String POSTURL;

	@Value("${spring.post.call.asar.aai.value}")
	private String AAI_VALUE;

	@Value("${spring.post.call.role.iot.core.value}")
	private String ROLE_VALUE;

	@Autowired
	private JavaMailSender javaMailSender;

	@Autowired
	private JwtTokenProvider tokenProvider;

	@Autowired
	private JwtAuthenticationFilter jwtAuthenticationFilter;

	@Autowired
	private UserServiceImpl userServiceImpl;

	@Override
	@Transactional
	public ResponseModel createCustomer(RequestModel customerRequest, HttpServletRequest request,
			HttpServletResponse response) throws JsonSyntaxException, DataAccessException {
		// get infor customer
		Gson gson = new Gson();
		String json = gson.toJson(customerRequest.getContent());
		Customer customer = gson.fromJson(json, Customer.class);
		// set value, not input NullPointerException
		String emailRn = customer.getEmail().replace("@", "_");
		// added by HA
		String webSite = customer.getWebsite();
		if (webSite != null) {
			// using website to create the appRule on IoT Core
			if (webSite.startsWith("https://")) {
				webSite = webSite.replace("https://", "");
			}
			if (webSite.startsWith("http://")) {
				webSite = webSite.replace("http://", "");
			}
			if (webSite.startsWith("www.")) {
				webSite = webSite.replace("www.", "");
			}
		}
		String appRule = webSite;

//		String role = "/SuperTester";
		String role = ROLE_VALUE;

		ResponseModel customerResponse = new ResponseModel();
		// call API
		log.info("call API core: POST_ServiceSubscribedAppRuleEntity CREATE");
		// get ASAR
		Asar asar = getAsar(POSTURL, emailRn, role, appRule);
		// get token from jwt then set
		String jwt = jwtAuthenticationFilter.getJwtFromRequest(request);
		String server = request.getServerName();
		String secret = CommonUtils.getSecret(server);
		String userId = null;
		if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt, server)) {
			// get user
			userId = tokenProvider.getUserIdFromJWT(jwt, secret);
		}
		AuthUser user = userServiceImpl.getUser(userId);
		log.info("Created by: " + user.getFullname());
		if (null != asar.getApci() && !asar.getApci().isEmpty()) {
			String apci = asar.getApci().toString();
//			String aai = asar.getAai().toString();
			// get aai pattern
//			aai = getValueAaiPattern(aai);
			// get list apci
			List<CustomerApikey> apikeys = new ArrayList<CustomerApikey>();
			for (int i = 0; i < asar.getApci().size(); i++) {
				CustomerApikey apikey = new CustomerApikey();
				apikey.setApci(asar.getApci().get(i));
				apikeys.add(apikey);
				apikey.setCustomer(customer);
				apikey.setAaiPattern(appRule);
			}
			customer.setCustomerApikeys(apikeys);
			customer.setCreated(user.getFullname());
			// set password random 12 characters
			customer.setPassword(RandomStringUtils.randomAlphanumeric(12));
			customer = customerRepo.save(customer);

			// TODO phai check not null tren giao dien, ko se ko save dc ma van sendEmail
			// TODO luu y khi send Email bi server chan
			log.warn("TODO::SERVER FIREWALL:: Cannot Send to Email!!!");
//			customerResponse.setResponseStatusCode(EnumValues.StatusProtocolEnum.STATUS_200.code);
//			customerResponse.setResponseStatusMessage(EnumValues.StatusProtocolEnum.STATUS_200.message);
			if (null != customer) {
				log.info("Send email to customer: " + customer.getEmail());
				String checkEmail = sendEmail(customer.getEmail(), customer.getPassword(), apci, appRule);
				if (ConstantDefine.SUCCESS.equals(checkEmail)) {
					response.setHeader(ConstantDefine.STATUS_CODE,
							EnumValues.StatusProtocolEnum.STATUS_200.code.toString());
					customerResponse.setResponseStatusCode(EnumValues.StatusProtocolEnum.STATUS_200.code);
					customerResponse.setResponseStatusMessage(EnumValues.StatusProtocolEnum.STATUS_200.message);
				} else {
					response.setHeader(ConstantDefine.STATUS_CODE,
							EnumValues.StatusProtocolEnum.STATUS_600.code.toString());
					throw new PortalException(EnumValues.StatusProtocolEnum.STATUS_600.code,
							EnumValues.StatusProtocolEnum.STATUS_600.message);
				}
			}

		} else {
			log.error("Cannot create APCI from email because RN(of Iot_Core) already exist!");
			response.setHeader(ConstantDefine.STATUS_CODE, EnumValues.StatusProtocolEnum.STATUS_613.code.toString());
			throw new PortalException(EnumValues.StatusProtocolEnum.STATUS_613.code,
					EnumValues.StatusProtocolEnum.STATUS_613.message);
		}

		return customerResponse;

	}

	/**
	 * call API IOT_CORE get infor Asar, method = post
	 * 
	 * @param url
	 * @param emailRn
	 * @param role
	 * @param appRule
	 * @return
	 */
	private Asar getAsar(String url, String emailRn, String role, String appRule) {
		// init rest mqtt,http client
		log.info("getAsar: init rest mqtt,http client");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String times = String.valueOf(timestamp.getTime());
		log.info("POST_URL: " + url);
		// Create AE
		RequestPrimitive requestPrimitive = new RequestPrimitive();
		requestPrimitive.setFrom(role);
		requestPrimitive.setTo(url);
		requestPrimitive.setOperation(Operation.CREATE);
		requestPrimitive.setRequestContentType("application/json");
		requestPrimitive.setResourceType(BigInteger.valueOf(19));

		Map<String, String> httpHeaders = new HashMap<String, String>();
		httpHeaders.put("Accept", "application/json");
		requestPrimitive.setHttpHeaders(httpHeaders);
		requestPrimitive.setRequestIdentifier(times);

		JSONObject jsonObjectActutor = new JSONObject();
		Gson gsonActutor = new Gson();
		Asar asar = new Asar();
		asar.setRn(emailRn);
		log.info("getRN: " + asar.getRn());
		// modified by HA
		String ruleForApp = appRule == null ? AAI_VALUE : AAI_VALUE.replace("company", appRule);
		log.info("ruleForApp : " + ruleForApp);
		asar.getAai().add(ruleForApp);

		String jsonActutor = gsonActutor.toJson(asar);
		Object obj = gsonActutor.fromJson(jsonActutor, Object.class);
		jsonObjectActutor.put("m2m:asar", obj);
		requestPrimitive.setContent(jsonObjectActutor.toString());
		log.info("jsonObjectActutor.toString() : " + jsonObjectActutor.toString());
		ResponsePrimitive responsePrimitive = RestClient.sendRequest(requestPrimitive);
		String apci = null;
		if (responsePrimitive != null && responsePrimitive.getResponseStatusCode() != null
				&& responsePrimitive.getResponseStatusCode().intValue() == ResponseStatusCode.CREATED.intValue()) {
			JSONObject jsonObjectCsr = new JSONObject(responsePrimitive.getContent().toString())
					.getJSONObject("m2m:asar");
			asar = gsonActutor.fromJson(jsonObjectCsr.toString(), Asar.class);
			apci = asar.getApci().toString();
			log.info("getAPCI: " + apci);
			// TODO truong hop loi:
			// saved AE thi da tao RN roi ma loi save customer thi ko revert lai RN dc
			// HA: them ham xoa
		}
		return asar;
	}

	/**
	 * un provided for customer on IoT Core
	 * 
	 * @param email
	 * @return ResponsePrimitive
	 */
	private ResponsePrimitive doUnProvided(String email) {
		log.info("do un provisioning the email " + email);

		try {
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			String times = String.valueOf(timestamp.getTime());
			String url = POSTURL + "/" + email.replace("@", "_");
//			String role = "/SuperTester";
			log.info("POST_URL: " + url);
			// Create AE
			RequestPrimitive requestPrimitive = new RequestPrimitive();
			requestPrimitive.setFrom(ROLE_VALUE);
			requestPrimitive.setTo(url);
			requestPrimitive.setOperation(Operation.DELETE);
			requestPrimitive.setRequestContentType("application/json");
			Map<String, String> httpHeaders = new HashMap<String, String>();
			httpHeaders.put("Accept", "application/json");
			requestPrimitive.setHttpHeaders(httpHeaders);
			requestPrimitive.setRequestIdentifier(times);
			return RestClient.sendRequest(requestPrimitive);
		} catch (Exception e) {
			log.error(e.getMessage());

		}
		return null;
	}

	/**
	 * Send Email to Customer
	 * 
	 * @param emailTo
	 * @param password
	 * @param appRule
	 * @param apci
	 * @return String
	 */
	private String sendEmail(String emailTo, String password, String apci, String appRule) {
		try {
			SimpleMailMessage msg = new SimpleMailMessage();
			msg.setTo(emailTo);
			msg.setSubject("Cấp phát thành công");
			String txt = "API KEY : " + apci.replace("[", "").replace("]", "");

			String ruleForApp = appRule == null ? AAI_VALUE : AAI_VALUE.replace("company", appRule);
			txt += "\nQuy tắc đăng ký AE cho ứng dụng IoT,thiết bị IoT : " + ruleForApp;
			txt += "\nTài khoản sử dụng của quý khách : " + emailTo;
			txt += "\nPassword sử dụng của quý khách : " + password;
			msg.setText(txt);
			javaMailSender.send(msg);
			log.info("Mail sent to :" + emailTo + " succesfully!");
			return ConstantDefine.SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
			log.error("Failed to send email to :" + emailTo);
			return ConstantDefine.ERROR;
		}
	}

	/**
	 * search and view Customer
	 * 
	 * @param page
	 * @param limit
	 * @param sortDesc
	 * @param sortBy
	 * @param params
	 * @param contractType
	 * @return ResponseModel
	 * 
	 */
	@Override
	public ResponseModel viewCustomers(Integer page, Integer limit, boolean sortDesc, String sortBy, String params,
			Integer contractType) {
		ResponseModel customerResponse = null;
		List<Customer> customers = null;
		// get value param when empty or null
		String paramCheck = Strings.emptyToNull(params);
//		System.out.println("paramCheck: " + paramCheck);

		Sort sortable = null;
		// sort by createdDate
		if (StringUtils.isEmpty(sortBy)) {
			sortBy = "createdDate";
		}
		// sort by element
		if (sortDesc) {
			sortable = Sort.by(sortBy).descending();
		} else {
			sortable = Sort.by(sortBy).ascending();
		}

		Pageable pageable = PageRequest.of(page - 1, limit, sortable);
		log.info("Customer paging: " + "params: " + paramCheck + " contractType: " + contractType);
		// search Customer
		customers = customerRepo.findByCustomer(paramCheck, contractType, pageable);
		// set value
		List<CustomerDTO> customerDtos = getCustomer(customers);
		customerResponse = CommonResponseModel.getResponseModelfromList(customerDtos);
		customerResponse.setPage(page);
		int checkTotalRecord = 0;
		// count Customer
		checkTotalRecord = customerRepo.countByCustomer(paramCheck, contractType);
		int checkTotalPage = checkTotalRecord / limit;
		if (checkTotalPage == 0) {
			customerResponse.setTotalPage(1);
		} else {
			customerResponse.setTotalPage(checkTotalPage + 1);
		}
		customerResponse.setTotalRecord(checkTotalRecord);

		return customerResponse;
	}

	/**
	 * mapper data from List<Customer> to List<CustomerDTO>
	 * 
	 * @param customers
	 * @return List<CustomerDTO>
	 */
	private List<CustomerDTO> getCustomer(List<Customer> customers) {
		List<CustomerDTO> customerDtos = new ArrayList<CustomerDTO>();
		ModelMapper modelMapper = new ModelMapper();
		customerDtos = modelMapper.map(customers, new TypeToken<List<CustomerDTO>>() {
		}.getType());
		log.info("total CustomerDTO: " + customerDtos.size());
		return customerDtos;
	}

	@Override
	public Customer findById(String id) {
		return customerRepo.findById(id).get();
	}

	/**
	 * Show Customer
	 * 
	 * @param id
	 * @return ResponseModel
	 */
	@Override
	public ResponseModel showCustomer(String id) {
		// get infor customer
		Customer customer = findById(id);

		ResponseModel customerResponse = new ResponseModel();
		customerResponse.setContent(customer);
		return customerResponse;

	}

	/**
	 * Edit Customer
	 * 
	 * @param customerRequest
	 * @return
	 */
	@Override
	@Transactional
	public void editCustomer(RequestModel customerRequest) {

		// get infor customer
		Gson gson = new Gson();
		String json = gson.toJson(customerRequest.getContent());
		Customer customer = gson.fromJson(json, Customer.class);

		// update customer
		customerRepo.save(customer);
	}

	/**
	 * Delete Customer
	 * 
	 * @param id
	 * @return
	 */
	@Override
	@Transactional
	public void deleteCustomer(String id) {
		Customer customer = findById(id);
		// delete RN (iot_core)
		ResponsePrimitive responsePrimitive = doUnProvided(customer.getEmail());
		Gson gson = new Gson();
		log.info(gson.toJson(responsePrimitive));
		if (responsePrimitive != null
				&& responsePrimitive.getResponseStatusCode().intValue() == ResponseStatusCode.DELETED.intValue()) {
			// delete customer
			customerRepo.deleteById(id);
		}

	}

	/**
	 * Find Customer by contractType
	 * 
	 * @param id
	 * @return ResponseModel
	 */
	@Override
	public ResponseModel findByCustomer(Integer contractType) {
		List<Customer> customers = customerRepo.findByContractType(contractType);
		ResponseModel customerResponse = new ResponseModel();
		customerResponse.setContent(customers);
		return customerResponse;
	}

	@Override
	public Customer findByEmail(String email) {
		return customerRepo.findByEmail(email);
	}

	/**
	 * get aai pattern from ServiceSubscribedAppRuleEntity with apikey and
	 * website(company)
	 * 
	 * @param aai
	 * @return
	 */
	public String getValueAaiPattern(String aai) {
		aai = aai.replace("\\.", "");
		String[] result = aai.split("\\*");
		String resultCheck = result[1];
//		int start = x.indexOf("(");
		int end = resultCheck.indexOf(")");
		aai = resultCheck.substring(1, end).trim();
		return aai;
	}
}