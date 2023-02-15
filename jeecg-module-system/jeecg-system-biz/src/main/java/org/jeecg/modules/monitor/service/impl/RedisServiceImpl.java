package org.jeecg.modules.monitor.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.monitor.domain.RedisInfo;
import org.jeecg.modules.monitor.service.RedisService;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * Redis 监控信息获取
 *
 * @Author MrBird
 */
@Service("redisService")
@Slf4j
public class RedisServiceImpl implements RedisService {

	@Resource
	private RedisConnectionFactory redisConnectionFactory;

    /**
     * redis信息
     */
    private static final String REDIS_MESSAGE = "3";

	/**
	 * Redis详细信息
	 */
	@Override
	public List<RedisInfo> getRedisInfo() {
		Properties info = redisConnectionFactory.getConnection().info();
		List<RedisInfo> infoList = new ArrayList<>();
		RedisInfo redisInfo;
		for (Map.Entry<Object, Object> entry : info.entrySet()) {
			redisInfo = new RedisInfo();
			redisInfo.setKey(oConvertUtils.getString(entry.getKey()));
			redisInfo.setValue(oConvertUtils.getString(entry.getValue()));
			infoList.add(redisInfo);
		}
		return infoList;
	}

	@Override
	public Map<String, Object> getKeysSize() {
		Long dbSize = redisConnectionFactory.getConnection().dbSize();
		Map<String, Object> map = new HashMap(5);
		map.put("create_time", System.currentTimeMillis());
		map.put("dbSize", dbSize);

		log.debug("--getKeysSize--: " + map);
		return map;
	}

	@Override
	public Map<String, Object> getMemoryInfo() {
		Map<String, Object> map = null;
		Properties info = redisConnectionFactory.getConnection().info();
		for (Map.Entry<Object, Object> entry : info.entrySet()) {
			String key = oConvertUtils.getString(entry.getKey());
			if ("used_memory".equals(key)) {
				map = new HashMap(5);
				map.put("used_memory", entry.getValue());
				map.put("create_time", System.currentTimeMillis());
			}
		}
		log.debug("--getMemoryInfo--: " + map.toString());
		return map;
	}

    /**
     * 查询redis信息for报表
     * @param type 1redis key数量 2 占用内存 3redis信息
     * @return
     */
	@Override
	public Map<String, JSONArray> getMapForReport(String type) {
		Map<String,JSONArray> mapJson=new HashMap(5);
		JSONArray json = new JSONArray();
		if(REDIS_MESSAGE.equals(type)){
			List<RedisInfo> redisInfo = getRedisInfo();
			for(RedisInfo info:redisInfo){
				Map<String, Object> map= Maps.newHashMap();
				BeanMap beanMap = BeanMap.create(info);
				for (Object key : beanMap.keySet()) {
					map.put(key+"", beanMap.get(key));
				}
				json.add(map);
			}
			mapJson.put("data",json);
			return mapJson;
		}
		int length = 5;
		for(int i = 0; i < length; i++){
			JSONObject jo = new JSONObject();
			Map<String, Object> map;
			if("1".equals(type)){
				map= getKeysSize();
				jo.put("value",map.get("dbSize"));
			}else{
				map = getMemoryInfo();
				int usedMemory = Integer.parseInt(map.get("used_memory").toString());
				jo.put("value",usedMemory/1000);
			}
			String createTime = DateUtil.formatTime(DateUtil.date((Long) map.get("create_time")-(4-i)*1000));
			jo.put("name",createTime);
			json.add(jo);
		}
		mapJson.put("data",json);
		return mapJson;
	}
}
