package com.vnpt.iot.portal.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vnpt.iot.portal.entity.Customer;
import com.vnpt.iot.portal.exceptions.PortalException;
import com.vnpt.iot.portal.model.BucketDTO;
import com.vnpt.iot.portal.model.BucketProduct;
import com.vnpt.iot.portal.model.DeviceActiveDTO;
import com.vnpt.iot.portal.model.QueryESProduct;
import com.vnpt.iot.portal.model.ResponseModel;
import com.vnpt.iot.portal.service.CustomerService;
import com.vnpt.iot.portal.service.DeviceActiveService;
import com.vnpt.iot.portal.service.ElasticsearchService;
import com.vnpt.iot.portal.service.ProductService;
import com.vnpt.iot.portal.utils.CommonResponseModel;
import com.vnpt.iot.portal.utils.ConstantDefine;
import com.vnpt.iot.portal.utils.EnumValues;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Trinh Dinh Tuan : Developer
 * @Email tuantdxyz@gmail.com
 * @Version 1.0.0 Sep 16, 2020
 */

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

	@Value("${spring.post.call.elastic.search.uri.connectivity}")
	private String CONNECTIVITY;

	@Autowired
	private DeviceActiveService deviceActiveService;

	@Autowired
	private CustomerService customerService;

	@Autowired
	private ElasticsearchService elasticsearchService;

	@Override
	public ResponseModel getAllProductions(String gte, String lte, boolean type) {
		log.info("get AllProductions with from: " + gte + " to: " + lte);
		// set value header
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		// main processes
		ResponseModel productResponse = getProductions(gte, lte, null, type, headers);
		return productResponse;
	}

	@Override
	public ResponseModel getAllProductionForCustomer(String gte, String lte, String email, boolean type) {
		log.info("get AllProductionForCustomer with from: " + gte + " to: " + lte + " email customer: " + email);
		// set value header
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		// main processes
		ResponseModel productResponse = getProductions(gte, lte, email, type, headers);
		return productResponse;
	}

	@Override
	public ResponseModel getProductByEmailCustomer(String email) {
		Customer customer = customerService.findByEmail(email);
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMM");
		String gte = dtf.format(customer.getCreatedDate());
		LocalDateTime now = LocalDateTime.now();
		String lte = dtf.format(now);
		ResponseModel responseModel = getAllProductionForCustomer(gte, lte, email, false);
		return responseModel;
	}

	/**
	 * get output body
	 * 
	 * @param entity
	 * @param uri
	 * @param protocol
	 * @return List<Bucket>
	 */
	private List<BucketProduct> getBuckets(HttpEntity<?> entity, String uri, String protocol) {
		RestTemplate restTemplate = new RestTemplate();
		List<BucketProduct> buckets = null;
		log.info("URL: " + uri);
		try {
			ResponseEntity<String> result = restTemplate.postForEntity(uri, entity, String.class);
			log.info("BODY: " + result.getBody());
			JSONObject jsonObject = new JSONObject(result.getBody().toString()).getJSONObject("aggregations");
			if (ConstantDefine.CONNECTIVITY.equalsIgnoreCase(protocol)) {
				String jsonBucket = jsonObject.getJSONObject("group_by").getJSONArray("buckets").toString();
				TypeToken<List<BucketProduct>> token = new TypeToken<List<BucketProduct>>() {
				};
				buckets = new Gson().fromJson(jsonBucket, token.getType());
			}
			log.info("Buckets size of " + protocol + ": " + buckets.size());
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return buckets;
	}

	/**
	 * get list BucketDTO
	 * 
	 * @param buckets
	 * @param protocol
	 * @return List<BucketDTO>
	 */
	private List<BucketDTO> getBucketDTO(List<BucketProduct> buckets, String protocol) {
		List<BucketDTO> bucketDtos = new ArrayList<BucketDTO>();
		for (BucketProduct b : buckets) {
			BucketDTO bucketDTO = new BucketDTO();
			bucketDTO.setKey(b.getKey());
			if (ConstantDefine.HTTP_REQUEST.equals(protocol)) {
				bucketDTO.setHttpCount(b.getDoc_count());
			} else if (ConstantDefine.MQTT_REQUEST.equals(protocol)) {
				bucketDTO.setMqttCount(b.getDoc_count());
			} else if (ConstantDefine.CONNECTIVITY.equals(protocol)) {
				bucketDTO.setConnectivityTime(b.getSumNode().get("value"));
			} else {
				// other request
				bucketDTO.setOtherCount(b.getDoc_count());
			}
			bucketDtos.add(bucketDTO);
		}
		log.info("list of " + protocol + ": " + bucketDtos);
		return bucketDtos;
	}

	/**
	 * getProductions: main processes with type = true: YMD, type = false = YM
	 * 
	 * @param gte
	 * @param lte
	 * @param email
	 * @param type
	 * @param headers
	 * @return ResponseModel
	 */
	private ResponseModel getProductions(String gte, String lte, String email, boolean type, HttpHeaders headers) {
		ResponseModel productResponse = null;
		List<BucketDTO> bucketDtos = null;
		String apikey = null;
		String aaiPattern = null;

		if (null != email) {
			// get customer by email
			Customer customer = customerService.findByEmail(email);
			// get apikey
			apikey = customer.getCustomerApikeys().get(0).getApci();
			// get aaiPattern
			aaiPattern = customer.getCustomerApikeys().get(0).getAaiPattern();
			log.info("Email of customer: " + email + " with Apikey: " + apikey + " with AaiPattern: " + aaiPattern);
		}

		List<BucketProduct> mqttBuckets2 = elasticsearchService.createRequestElasticsearch(gte, lte, apikey,
				ConstantDefine.ELASTICSEARCH_INDEX_MQTT_REQUEST, ConstantDefine.MQTT_REQUEST, type);
		// get list mqtt
		List<BucketDTO> mqttBucketDtos = getBucketDTO(mqttBuckets2, ConstantDefine.MQTT_REQUEST);

		List<BucketProduct> httpBuckets2 = elasticsearchService.createRequestElasticsearch(gte, lte, apikey,
				ConstantDefine.ELASTICSEARCH_INDEX_HTTP_REQUEST, ConstantDefine.HTTP_REQUEST, type);
		// get list http
		List<BucketDTO> httpBucketDtos = getBucketDTO(httpBuckets2, ConstantDefine.HTTP_REQUEST);

		// set value to map from MQTT_BucketDtos
		Map<String, BucketDTO> map = mqttBucketDtos.stream().collect(Collectors.toMap(BucketDTO::getKey, item -> item));

		// merge value HTTP_BucketDtos in map
		for (BucketDTO bucket : httpBucketDtos) {
			if (map.containsKey(bucket.getKey()) && bucket.getKey().equals(map.get(bucket.getKey()).getKey())) {
				// duplicate key when create BucketDTO (merge value)
				BucketDTO newB = new BucketDTO();
				newB.setMqttCount(map.get(bucket.getKey()).getMqttCount());
				newB.setHttpCount(bucket.getHttpCount());
				newB.setKey(bucket.getKey());
				map.put(bucket.getKey(), newB);
			} else {
				map.put(bucket.getKey(), bucket);
			}
		}

		List<BucketProduct> otherMqttBuckets2 = elasticsearchService.createRequestElasticsearch(gte, lte, apikey,
				ConstantDefine.ELASTICSEARCH_INDEX_MQTT_REQUEST, ConstantDefine.OTHER_MQTT_REQUEST, type);
		// get list otherMqtt
		List<BucketDTO> otherMqttBucketDtos = getBucketDTO(otherMqttBuckets2, ConstantDefine.OTHER_MQTT_REQUEST);

		List<BucketProduct> otherHttpBuckets2 = elasticsearchService.createRequestElasticsearch(gte, lte, apikey,
				ConstantDefine.ELASTICSEARCH_INDEX_HTTP_REQUEST, ConstantDefine.OTHER_HTTP_REQUEST, type);
		// get list otherHttp
		List<BucketDTO> otherHttpBucketDtos = getBucketDTO(otherHttpBuckets2, ConstantDefine.OTHER_HTTP_REQUEST);

		// set value to mapOther from otherMqttBuckets
		Map<String, BucketDTO> mapOther = otherMqttBucketDtos.stream()
				.collect(Collectors.toMap(BucketDTO::getKey, item -> item));

		// merge value otherHttpBucketDtos in mapOther
		for (BucketDTO bucket : otherHttpBucketDtos) {
			if (mapOther.containsKey(bucket.getKey())
					&& bucket.getKey().equals(mapOther.get(bucket.getKey()).getKey())) {
				// duplicate key when create BucketDTO (sum value OtherCount)
				BucketDTO newB = new BucketDTO();
				newB.setOtherCount(mapOther.get(bucket.getKey()).getOtherCount() + bucket.getOtherCount());
				newB.setKey(bucket.getKey());
				mapOther.put(bucket.getKey(), newB);
			} else {
				mapOther.put(bucket.getKey(), bucket);
			}
		}

		// conver mapOther to list BucketDTO and sort by key
		List<BucketDTO> otherBucketDtos = mapOther.values().stream().sorted(Comparator.comparing(BucketDTO::getKey))
				.collect(Collectors.toList());
		log.info("List of OTHER: " + otherBucketDtos);

		// merge value other_HTTP_BucketDtos in map
		for (BucketDTO bucket : otherBucketDtos) {
			if (map.containsKey(bucket.getKey()) && bucket.getKey().equals(map.get(bucket.getKey()).getKey())) {
				// duplicate key when create BucketDTO (merge value)
				BucketDTO newB = new BucketDTO();
				newB.setMqttCount(map.get(bucket.getKey()).getMqttCount());
				newB.setHttpCount(map.get(bucket.getKey()).getHttpCount());
				newB.setOtherCount(bucket.getOtherCount());
				newB.setKey(bucket.getKey());
				map.put(bucket.getKey(), newB);
			} else {
				map.put(bucket.getKey(), bucket);
			}
		}

		// conver map to list BucketDTO and sort by key
		bucketDtos = map.values().stream().sorted(Comparator.comparing(BucketDTO::getKey)).collect(Collectors.toList());
		log.info("List<BucketDTO> after merge http, mqtt and other: " + bucketDtos);

		List<DeviceActiveDTO> deviceDtos = new ArrayList<DeviceActiveDTO>();
		// get Device Active
		if (aaiPattern == null) {
			deviceDtos = deviceActiveService.getDeviceActive(gte, lte, type);
		} else {
			deviceDtos = deviceActiveService.getDeviceActiveForCustomer(gte, lte, type, aaiPattern);
		}
		// set DeviceActive from list BucketDTO
		mergeDeviceToBucketDTO(bucketDtos, deviceDtos, type);
		log.info("List<BucketDTO> after merge device active: " + bucketDtos);

		// create request connectivity
		String connectivityQuery = genConnectivityQueryRequestEs(gte, lte, type, apikey);
		HttpEntity<?> httpEntity = new HttpEntity<>(connectivityQuery, headers);
		// Rest = POST ONNECTIVITY
		List<BucketProduct> connectivityResult = getBuckets(httpEntity, CONNECTIVITY, ConstantDefine.CONNECTIVITY);

		// get list connectivity
		List<BucketDTO> connectivityResultDtos = getBucketDTO(connectivityResult, ConstantDefine.CONNECTIVITY);
		log.info("List<BucketDTO> connectivity: " + connectivityResultDtos.toString());

		mergeConnectivity(bucketDtos, connectivityResultDtos);

		log.info("List<BucketDTO> after merge connectivity (list all): " + bucketDtos);

		// set value
		productResponse = CommonResponseModel.getResponseModelfromList(bucketDtos);
		return productResponse;
	}

	/**
	 * Create connectivity query request elasticsearch
	 * 
	 * @param gte
	 * @param lte
	 * @param type
	 * @param apiKey
	 * @return string json request
	 */
	private String genConnectivityQueryRequestEs(String gte, String lte, boolean type, String apiKey) {
		QueryESProduct queryBuilderES = new QueryESProduct();
		if (StringUtils.isNotBlank(apiKey)) {
			queryBuilderES.buildWildCards("clientID", apiKey + "*");
		}
		queryBuilderES.buildMatchs("state", 0);
		if (type) {
			// group by Year_Month_day
			queryBuilderES.buildRanges("year_month_day", gte, lte);
		} else {
			// group by Year_Month
			queryBuilderES.buildRanges("year_month", gte, lte);
		}
		queryBuilderES.buildMust();
		queryBuilderES.buildBool();
		queryBuilderES.buildSum("duration");
		queryBuilderES.buildGroupBy("year_month_day");
		return queryBuilderES.generateQueryEs(0);
	}

	/**
	 * Merge list connectivity to List<BucketDTO> bucketDTOs
	 * 
	 * @param bucketDTOs
	 * @param connectivityResultDtos
	 */
	private void mergeConnectivity(List<BucketDTO> bucketDTOs, List<BucketDTO> connectivityResultDtos) {
		bucketDTOs.forEach(brucket -> {
			List<BucketDTO> bucketDTOList = connectivityResultDtos.stream()
					.filter(connectivity -> connectivity.getKey().contains(brucket.getKey()))
					.collect(Collectors.toList());
			if (!bucketDTOList.isEmpty()) {
				Long minutesConnectivity = TimeUnit.MILLISECONDS.toMinutes(bucketDTOList.get(0).getConnectivityTime());
				brucket.setConnectivityTime(minutesConnectivity);
			}
		});
	}

	/**
	 * Merge device active to list BucketDTO
	 * 
	 * @param bucketDto
	 * @param deviceDto
	 * @param type
	 */
	private void mergeDeviceToBucketDTO(List<BucketDTO> bucketDto, List<DeviceActiveDTO> deviceDto, boolean type) {
		switch (type + "") {
		case "true":
			// YMD: 20200909
			bucketDto.forEach(bucket -> {
				List<DeviceActiveDTO> deviceActiveDTOList = deviceDto.stream()
						.filter(device -> device.getYearMonthDay().equals(bucket.getKey()))
						.collect(Collectors.toList());
				if (!deviceActiveDTOList.isEmpty()) {
					bucket.setDeviceActiveCount(deviceActiveDTOList.get(0).getCountDate());
				}
			});
			break;
		case "false":
			// YM: 202009
			bucketDto.forEach(bucket -> {
				List<DeviceActiveDTO> deviceActiveDTOList = deviceDto.stream()
						.filter(device -> device.getYearMonth().equals(bucket.getKey())).collect(Collectors.toList());
				if (!deviceActiveDTOList.isEmpty()) {
					bucket.setDeviceActiveCount(deviceActiveDTOList.get(0).getCountDate());
				}
			});
			break;
		default:
			throw new PortalException(EnumValues.StatusProtocolEnum.STATUS_400.code,
					EnumValues.StatusProtocolEnum.STATUS_400.message);
		}
	}

}
