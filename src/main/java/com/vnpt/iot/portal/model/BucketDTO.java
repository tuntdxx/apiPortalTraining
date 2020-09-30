package com.vnpt.iot.portal.model;

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
public class BucketDTO {
	private String key;
	private Long httpCount = 0l;
	private Long mqttCount = 0l;
	private Long otherCount = 0l;
	private Long deviceActiveCount = 0l;
	private Long connectivityTime = 0l; // minute
}
