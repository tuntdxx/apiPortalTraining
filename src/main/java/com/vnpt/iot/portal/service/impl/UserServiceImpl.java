package com.vnpt.iot.portal.service.impl;

import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.vnpt.iot.portal.config.CustomUserDetails;
import com.vnpt.iot.portal.entity.AuthUser;
import com.vnpt.iot.portal.model.RequestModel;
import com.vnpt.iot.portal.model.ResponseModel;
import com.vnpt.iot.portal.repository.UserRepository;
import com.vnpt.iot.portal.utils.ConstantDefine;
import com.vnpt.iot.portal.utils.EnumValues;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Trinh Dinh Tuan : Developer
 * @Email tuantdxyz@gmail.com
 * @Version 1.0.0 Sep 1, 2020
 */

@Service
@Slf4j
public class UserServiceImpl implements UserDetailsService {

	@Autowired
	UserRepository userRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// check User exist with username = email
		Optional<AuthUser> user = userRepository.findByEmail(username);
		if (!user.isPresent()) {
			throw new UsernameNotFoundException("API authen token not found user with username: " + username);
		}
		// set password with passwordEncoder
//		user.get().setPassword(passwordEncoder.encode(user.get().getPassword()));
		log.info("login with User : " + username + " PasswordEncoder: " + user.get().getPassword());
		return new CustomUserDetails(user.get());
	}

	public UserDetails loadUserById(String id) {
		AuthUser user = userRepository.findById(id)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with id : " + id));
		return new CustomUserDetails(user);
	}

	@Transactional
	public AuthUser addUser(AuthUser user) {
		return userRepository.save(user);
	}

	public AuthUser getUser(String id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with id : " + id));
	}

	public ResponseModel doCreateUser(RequestModel userRequest, HttpServletResponse response) {
		ResponseModel userResponse = new ResponseModel();
		Gson gson = new Gson();
		String json = gson.toJson(userRequest.getContent());
		AuthUser user = gson.fromJson(json, AuthUser.class);
		// check User exist with username = email
		Optional<AuthUser> userCheck = userRepository.findByEmail(user.getEmail());
		if (userCheck.isPresent()) {
			log.error("An email already exists in the system: " + user.getEmail());
			throw new UsernameNotFoundException("An email already exists in the system: " + user.getEmail());
		} else {
			// save password encode
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			addUser(user);
			response.setHeader(ConstantDefine.STATUS_CODE, EnumValues.StatusProtocolEnum.STATUS_200.code.toString());
			userResponse.setResponseStatusCode(EnumValues.StatusProtocolEnum.STATUS_200.code);
			userResponse.setResponseStatusMessage(EnumValues.StatusProtocolEnum.STATUS_200.message);
//			response.setStatus(EnumValues.StatusProtocolEnum.STATUS_200.code);
		}
		return userResponse;
	}
}
