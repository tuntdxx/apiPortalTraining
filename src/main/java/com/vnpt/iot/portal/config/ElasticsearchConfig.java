package com.vnpt.iot.portal.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Trinh Dinh Tuan : Developer
 * @Email tuantdxyz@gmail.com
 * @Version 1.0.0 Sep 25, 2020
 */

@Configuration
public class ElasticsearchConfig {

	@Value("${elasticsearch.host}")
	private String ELASTICSEARCH_HOST;

	@Value("${elasticsearch.port}")
	private int ELASTICSEARCH_PORT;

	@Bean(destroyMethod = "close")
	public RestHighLevelClient client() {

		RestHighLevelClient restHighLevelClient = new RestHighLevelClient(
				RestClient.builder(new HttpHost(ELASTICSEARCH_HOST, ELASTICSEARCH_PORT)));

		return restHighLevelClient;

	}

}
