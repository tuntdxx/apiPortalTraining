package com.vnpt.iot.portal.model;

import java.util.Map;

/**
 * @author <a href="mailto:nguyenvanhiep@vnpt.vn">Hiep Nguyen Van</a>
 * @version 1.0.0
 * modified last on 2020-09-24
 */
public class RootProduct {
	public int size;
    public Map<?, ?> query;
    public Map<?, ?> aggs;
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public Map<?, ?> getQuery() {
		return query;
	}
	public void setQuery(Map<?, ?> query) {
		this.query = query;
	}
	public Map<?, ?> getAggs() {
		return aggs;
	}
	public void setAggs(Map<?, ?> aggs) {
		this.aggs = aggs;
	}
}
