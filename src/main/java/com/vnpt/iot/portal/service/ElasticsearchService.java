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
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vnpt.iot.portal.model.BucketProduct;
import com.vnpt.iot.portal.utils.ConstantDefine;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Trinh Dinh Tuan : Developer
 * @Email tuantdxyz@gmail.com
 * @Version 1.0.0 Sep 22, 2020
 */

@Service
@Slf4j
public class ElasticsearchService {

//	public static final String RESOURCE_TYPE = "year_month_day.keyword";

	@Autowired
	private RestHighLevelClient restHighLevelClient;

	public List<BucketProduct> createRequestElasticsearch(String gte, String lte, String apikey, String index,
			String request, boolean type) {
		List<BucketProduct> buckets = new ArrayList<>();
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.indices(index);
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.size(0);
		String aggsFieldKey = null;
		String rangeField = null;

		// check type
		if (type) {
			aggsFieldKey = ConstantDefine.FIELD_YEAR_MONTH_DAY_KEYWORD;
			rangeField = ConstantDefine.FIELD_YEAR_MONTH_DAY;
		} else {
			aggsFieldKey = ConstantDefine.FIELD_YEAR_MONTH_KEYWORD;
			rangeField = ConstantDefine.FIELD_YEAR_MONTH;
		}

		// check apikey
		if (apikey == null) {
			// where
			if (ConstantDefine.HTTP_REQUEST.equals(request) || ConstantDefine.MQTT_REQUEST.equals(request)) {
				// Query
				QueryBuilder queryBuilder = QueryBuilders.boolQuery()
						.must(QueryBuilders.matchQuery(ConstantDefine.RESOURCE_TYPE, ConstantDefine.INT_NUMBER_FOUR))
						.must(QueryBuilders.rangeQuery(rangeField).gte(gte).lte(lte));
				searchSourceBuilder.query(queryBuilder);
			}
			if (ConstantDefine.OTHER_HTTP_REQUEST.equals(request)
					|| ConstantDefine.OTHER_MQTT_REQUEST.equals(request)) {
				// Query
				QueryBuilder queryBuilder = QueryBuilders.boolQuery()
						.mustNot(QueryBuilders.matchQuery(ConstantDefine.RESOURCE_TYPE, ConstantDefine.INT_NUMBER_FOUR))
						.must(QueryBuilders.rangeQuery(rangeField).gte(gte).lte(lte));
				searchSourceBuilder.query(queryBuilder);
			}
		} else {
			// where
			if (ConstantDefine.HTTP_REQUEST.equals(request) || ConstantDefine.MQTT_REQUEST.equals(request)) {
				// Query
				QueryBuilder queryBuilder = QueryBuilders.boolQuery()
						.must(QueryBuilders.matchQuery(ConstantDefine.RESOURCE_TYPE, ConstantDefine.INT_NUMBER_FOUR))
						.must(QueryBuilders.matchQuery(ConstantDefine.FIELD_FROM, apikey))
						.must(QueryBuilders.rangeQuery(rangeField).gte(gte).lte(lte));
				searchSourceBuilder.query(queryBuilder);
			}
			if (ConstantDefine.OTHER_HTTP_REQUEST.equals(request)
					|| ConstantDefine.OTHER_MQTT_REQUEST.equals(request)) {
				// Query
				QueryBuilder queryBuilder = QueryBuilders.boolQuery()
						.mustNot(QueryBuilders.matchQuery(ConstantDefine.RESOURCE_TYPE, ConstantDefine.INT_NUMBER_FOUR))
						.must(QueryBuilders.matchQuery(ConstantDefine.FIELD_FROM, apikey))
						.must(QueryBuilders.rangeQuery(rangeField).gte(gte).lte(lte));
				searchSourceBuilder.query(queryBuilder);
			}
		}

		// Aggs
		TermsAggregationBuilder aggregation = AggregationBuilders.terms(ConstantDefine.TERMS_AGGREGATION)
				.field(aggsFieldKey).size(ConstantDefine.INT_NUMBER_ONE_THOUSAND).order(BucketOrder.key(false));
		searchSourceBuilder.aggregation(aggregation);

		// set to source
		searchRequest.source(searchSourceBuilder);
		SearchResponse searchResponse = null;
		try {
			// to search
			searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
//			log.info("Index: " + index + " Response: " + searchResponse.toString());
			Terms terms = searchResponse.getAggregations().get(ConstantDefine.TERMS_AGGREGATION);
			// set value
			for (Bucket bucket : terms.getBuckets()) {
				BucketProduct b = new BucketProduct();
				b.setKey(bucket.getKeyAsString());
				b.setDoc_count(bucket.getDocCount());
				buckets.add(b);
			}
			log.info("Index: " + index + " Response List<BucketRetrieve>: " + buckets);
		} catch (IOException e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
		return buckets;
	}
}
