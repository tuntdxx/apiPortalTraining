package com.vnpt.iot.portal.model;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Trinh Dinh Tuan : Developer
 * @Email tuantdxyz@gmail.com
 * @Version 1.0.0 Sep 16, 2020
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BucketProduct {
	private String key;
	private Long doc_count;
	private Map<String, Long> sumNode;
}
