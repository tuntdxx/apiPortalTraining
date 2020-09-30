package com.vnpt.iot.portal.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.vnpt.iot.portal.entity.Customer;

/**
 * @author Trinh Dinh Tuan : Developer
 * @Email tuantdxyz@gmail.com
 * @Version 1.0.0 Sep 7, 2020
 */

public interface CustomerRepository extends JpaRepository<Customer, String> {

	@Query(value = "SELECT c FROM Customer c where (?1 IS NULL or c.contractType = ?1)")
	List<Customer> findByContractType(Integer contractType);

	@Query(value = "SELECT c FROM Customer c where (?1 is null or c.fullname=?1 or c.email=?1 or c.phone=?1) and (?2 is null or c.contractType = ?2)")
	List<Customer> findByCustomer(String params, Integer contractType, Pageable pageable);

	@Query(value = "SELECT count(*) FROM Customer c where (?1 is null or c.fullname=?1 or c.email=?1 or c.phone=?1) and (?2 is null or c.contractType = ?2)")
	int countByCustomer(String params, Integer contractType);

	Customer findByEmail(String email);
}
