package com.vnpt.iot.portal.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketCollector;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.filter.Filters;
import org.elasticsearch.search.aggregations.bucket.terms.InternalTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vnpt.iot.portal.model.BucketProduct;
import com.vnpt.iot.portal.model.DeviceActiveDTO;

/**
 * @author Trinh Dinh Tuan : Developer
 * @Email tuantdxyz@gmail.com
 * @Version 1.0.0 Sep 22, 2020
 */

@Service
@SuppressWarnings("unused")
public class TestService {

	@Autowired
	private RestHighLevelClient client;

	public String test01() {
		String condition = "";

		SearchRequest searchRequest = new SearchRequest();
		searchRequest.indices("requestprimitivemqtt");
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.size(0);
//		searchSourceBuilder.query(QueryBuilders.queryStringQuery(condition)); 
		// where ?
		// Query
		QueryBuilder queryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("resourceType", 4))
				.must(QueryBuilders.rangeQuery("year_month_day").gte("20200909").lte("20200930"));
		searchSourceBuilder.query(queryBuilder);
		// Aggs
		TermsAggregationBuilder aggregation = AggregationBuilders.terms("_group_by").field("year_month_day.keyword")
				.size(100).order(BucketOrder.key(false));
		searchSourceBuilder.aggregation(aggregation);

		// set to source
		searchRequest.source(searchSourceBuilder);
		SearchResponse searchResponse = null;
		try {
			searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
//			System.out.println(searchResponse.toString());
			Terms terms = searchResponse.getAggregations().get("_group_by");
			List<BucketProduct> buckets = new ArrayList<>();
			for (Bucket bucket : terms.getBuckets()) {
				BucketProduct b = new BucketProduct();
				b.setKey(bucket.getKeyAsString());
				b.setDoc_count(bucket.getDocCount());
				buckets.add(b);
			}
			System.out.println("YYY: " + buckets);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return searchResponse.toString();
	}
}
