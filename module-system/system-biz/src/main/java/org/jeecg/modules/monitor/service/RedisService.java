package org.jeecg.modules.monitor.service;

import com.alibaba.fastjson.JSONArray;
import org.jeecg.modules.monitor.domain.RedisInfo;

import java.util.List;
import java.util.Map;

/**
 * @Description: redis信息service接口
 * @author: jeecg-boot
 */
public interface RedisService {

	/**
	 * 获取 redis 的详细信息
	 *
	 * @return List
     */
	List<RedisInfo> getRedisInfo();

	/**
	 * 获取 redis key 数量
	 *
	 * @return Map
     */
	Map<String, Object> getKeysSize();

	/**
	 * 获取 redis 内存信息
	 *
	 * @return Map
     */
	Map<String, Object> getMemoryInfo();
	/**
	 * 获取 报表需要个redis信息
	 * @param type
	 * @return Map
     */
	Map<String, JSONArray> getMapForReport(String type);
}
