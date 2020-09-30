package com.vnpt.iot.portal.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * @author Trinh Dinh Tuan : Developer
 * @Email tuantdxyz@gmail.com
 * @Version 1.0.0 Sep 23, 2020
 */

@Configuration
public class DatabaseJdbcConfig {

	@Value("${spring.datasource.url}")
	private String URL;

	@Value("${spring.datasource.username}")
	private String USERNAME;

	@Value("${spring.datasource.password}")
	private String PASSWORD;

	@Value("${spring.datasource.driver-class-name}")
	private String ClASSNAME;

	@Bean(name = "dsJdbc")
	public DataSource firstDataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(ClASSNAME);
		dataSource.setUrl(URL);
		dataSource.setUsername(USERNAME);
		dataSource.setPassword(PASSWORD);

		return dataSource;
	}

	@Bean(name = "jdbcDataSource")
	public JdbcTemplate jdbcTemplate(DataSource dsJdbc) {
		return new JdbcTemplate(dsJdbc);
	}

}
