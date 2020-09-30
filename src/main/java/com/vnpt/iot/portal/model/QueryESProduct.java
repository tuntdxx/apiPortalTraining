package com.vnpt.iot.portal.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

/**
 * @author <a href="mailto:nguyenvanhiep@vnpt.vn">Hiep Nguyen Van</a>
 * @version 1.0.0
 * modified last on 2020-09-24
 */
public class QueryESProduct {
	public static final String WILDCARD = "wildcard";
	public static final String MATCH = "match";
	public static final String RANGE = "range";
	public static final String GROUP_BY = "group_by";
	public static final String SUM = "sum";
	
	public static Map<String, Map<String, Object>> wildcardMap;
	public static Map<String, Map<String, Object>> matchMap;
	public static Map<String, Map<String, ?>> rangeMap;
	public static Map<String, Map<String, ?>> groupByMap;
	public static Map<String, Map<String, Map<String, Object>>> sumMap;
	public static Map<String, Map<String, ?>> aggsSubMap;
	public static Map<String, List<?>> mustMap;
	public static Map<String, Map<String, List<?>>> boolMap;
	
	public QueryESProduct() {
		wildcardMap = new HashMap<>();
		matchMap = new HashMap<>();
		rangeMap = new HashMap<>();
		groupByMap = new HashMap<>();
		sumMap = new HashMap<>();
		aggsSubMap = new HashMap<>();
		mustMap = new HashMap<>();
		boolMap = new HashMap<>();
	}

	public void buildWildCards(String field, String fieldValue) {
		Map<String, Object> wildCardConditions = new HashMap<>();
		wildCardConditions.put(field, fieldValue);
		wildcardMap.put(WILDCARD, wildCardConditions);
	}
	
	public void buildMatchs(String field, Object fieldValue) {
		Map<String, Object> matchConditions = new HashMap<>();
		matchConditions.put(field, fieldValue);
		matchMap.put(MATCH, matchConditions);
	}
	
	public void buildRanges(String field, String from, String to) {
		Map<String, Map<String, Object>> rangeField = new HashMap<>();
		Map<String, Object> rangeCondition = new HashMap<>();
		rangeCondition.put("from", from);
		rangeCondition.put("to", to);
		rangeField.put(field,rangeCondition);
		rangeMap.put(RANGE, rangeField);
	}
	
	public void buildGroupBy(String field) {
		Map<String, Object> groupByTerms = new HashMap<>();
		Map<String, Object> groupByField = new HashMap<>();
		Map<String, Object> orderField = new HashMap<>();
		orderField.put("_term", "asc");
		groupByField.put("field", field + ".keyword");
		groupByField.put("order", orderField);
		
		groupByTerms.put("terms", groupByField);
		groupByTerms.put("aggs", sumMap);
		groupByMap.put(GROUP_BY, groupByTerms);
	}
	
	public void buildSubAggregation(Map<String, ?> aggs) {
		aggsSubMap.put("aggs", aggs);
	}
	
	
	public void buildSum(String field) {
		Map<String, Map<String, Object>> sumField = new HashMap<>();
		Map<String, Object> sumFieldChidrent = new HashMap<>();
		sumFieldChidrent.put("field", field);
		sumField.put(SUM, sumFieldChidrent);
		sumMap.put("sumNode", sumField);
	}
	
	public void buildMust() {
		List<Map<String, ?>> arrayMust = new ArrayList<>();
		if(!matchMap.isEmpty()) {
			arrayMust.add(matchMap);
		}
		if(!wildcardMap.isEmpty()) {
			arrayMust.add(wildcardMap);
		}
		if(!rangeMap.isEmpty()) {
			arrayMust.add(rangeMap);
		}
		mustMap.put("must", arrayMust);
	}
	
	public void buildBool() {
		boolMap.put("bool", mustMap);
	}
	
	public String generateQueryEs(Integer size) {
		RootProduct root = new RootProduct();
		root.setAggs(groupByMap);
		root.setQuery(boolMap);
		root.setSize(size);
		return new Gson().toJson(root);
	}
}
