package com.vnpt.iot.portal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.vnpt.iot.portal.model.DeviceActiveDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Trinh Dinh Tuan : Developer
 * @Email tuantdxyz@gmail.com
 * @Version 1.0.0 Sep 23, 2020
 */

@Service
@Slf4j
@SuppressWarnings("rawtypes")
public class DeviceActiveService {

	@Autowired
	@Qualifier("jdbcDataSource")
	protected JdbcTemplate jdbc;

	/**
	 * get DeviceActive genaral
	 * 
	 * @param gte
	 * @param lte
	 * @param type
	 * @return List<DeviceActiveDTO>
	 */
	public List<DeviceActiveDTO> getDeviceActive(String gte, String lte, boolean type) {
		String sql = null;
		if (type) {
			// YMD = 20200909 --> 2020-09-09
			gte = gte.substring(0, 4) + "-" + gte.substring(4, 6) + "-" + gte.substring(6, 8);
			lte = lte.substring(0, 4) + "-" + lte.substring(4, 6) + "-" + lte.substring(6, 8);
			sql = "SELECT COUNT(*) AS countDate, DATE_FORMAT(a.ct, '%Y%m%d') AS yearMonthDay FROM onem2m.AE a WHERE ? <= DATE(a.ct) AND DATE(a.ct) <= ? GROUP BY yearMonthDay";
		} else {
			// YM = 202009
			sql = "SELECT COUNT(*) as countDate, DATE_FORMAT(ct, '%Y%m') as yearMonth FROM onem2m.AE a WHERE ? <= DATE_FORMAT(a.ct, '%Y%m') and DATE_FORMAT(a.ct, '%Y%m') <= ? GROUP BY yearMonth";
		}

		List<Map<String, Object>> rows = jdbc.queryForList(sql, new Object[] { gte, lte });
//		System.out.println(rows.size());
		List<DeviceActiveDTO> devices = new ArrayList<DeviceActiveDTO>();
		for (Map row : rows) {
			DeviceActiveDTO device = new DeviceActiveDTO();
			device.setCountDate(Long.parseLong(String.valueOf(row.get("countDate"))));
			device.setYearMonthDay(String.valueOf(row.get("yearMonthDay")));
			device.setYearMonth(String.valueOf(row.get("yearMonth")));
			devices.add(device);
		}
		log.info("List DeviceActiveDTO: " + devices);
		return devices;
	}

	/**
	 * get DeviceActive for Customer
	 * 
	 * @param gte
	 * @param lte
	 * @param type
	 * @param apikey
	 * @return List<DeviceActiveDTO>
	 */
	public List<DeviceActiveDTO> getDeviceActiveForCustomer(String gte, String lte, boolean type, String aaiPattern) {
		String sql = null;
		if (type) {
			// YMD = 20200909 --> 2020-09-09
			gte = gte.substring(0, 4) + "-" + gte.substring(4, 6) + "-" + gte.substring(6, 8);
			lte = lte.substring(0, 4) + "-" + lte.substring(4, 6) + "-" + lte.substring(6, 8);
			sql = "SELECT COUNT(*) AS countDate, DATE_FORMAT(a.ct, '%Y%m%d') AS yearMonthDay FROM onem2m.AE a WHERE ? <= DATE(a.ct) AND DATE(a.ct) <= ? AND a.api like ? GROUP BY yearMonthDay";
		} else {
			// YM = 202009
			sql = "SELECT COUNT(*) as countDate, DATE_FORMAT(ct, '%Y%m') as yearMonth FROM onem2m.AE a WHERE ? <= DATE_FORMAT(a.ct, '%Y%m') and DATE_FORMAT(a.ct, '%Y%m') <= ? AND a.api like ? GROUP BY yearMonth";
		}

		List<Map<String, Object>> rows = jdbc.queryForList(sql, new Object[] { gte, lte, "%" + aaiPattern + "%" });
//		System.out.println(rows.size());
		List<DeviceActiveDTO> devices = new ArrayList<DeviceActiveDTO>();
		for (Map row : rows) {
			DeviceActiveDTO device = new DeviceActiveDTO();
			device.setCountDate(Long.parseLong(String.valueOf(row.get("countDate"))));
			device.setYearMonthDay(String.valueOf(row.get("yearMonthDay")));
			device.setYearMonth(String.valueOf(row.get("yearMonth")));
			devices.add(device);
		}
		log.info("List DeviceActiveDTO: " + devices);
		return devices;
	}

}
