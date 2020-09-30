package com.vnpt.iot.portal.controller;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vnpt.iot.portal.service.TestService;

/**
 * @author Trinh Dinh Tuan : Developer
 * @Email tuantdxyz@gmail.com
 * @Version 1.0.0 Sep 22, 2020
 */

@RestController
@RequestMapping("api/testdb")
//@Slf4j
public class TestController {

	@Autowired
	TestService testRepo;

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@Secured("ROLE_ADMIN")
	public String checkTestDB2(HttpServletResponse response) {
//		response.setContentType("application/json");
//		response.setCharacterEncoding("UTF-8");
		return testRepo.test01();
	}

}
