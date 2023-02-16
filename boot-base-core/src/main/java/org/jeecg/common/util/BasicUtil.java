//package org.jeecg.common.util;
//
//import cn.hutool.core.codec.Base64;
//import cn.hutool.core.util.StrUtil;
//import org.springframework.util.CollectionUtils;
//
//import javax.servlet.http.HttpServletRequest;
//import java.util.List;
//
///**
// * @Description: 加密工具
// *
// * update: 【类名改了大小写】 date: 2022-04-18
// * @author: jeecg-boot
// */
//public class BasicUtil {
//
//	private static final String BASIC_USERNAME = "admin";
//
//	private static final String BASIC_PASSWORD = "!@#QWE123";
//
//	public static void checkAuthorization(HttpServletRequest request) {
//		String authorization = request.getHeader("Authorization");
//		if (authorization == null || authorization.equals("")) {
//			throw new RuntimeException("basic认证失败,参数为空");
//		}
//		boolean authStatusFlag = false;
//    List<String> authorizationInfos = StrUtil.splitTrim(authorization, StrUtil.SPACE);
//		if(!CollectionUtils.isEmpty(authorizationInfos) && authorizationInfos.size() == 2) {
//			String authType = authorizationInfos.get(0);
//			String authInfoCredentials = authorizationInfos.get(1);
//			if (StrUtil.isNotBlank(authInfoCredentials)) {
//				if (StrUtil.equalsAnyIgnoreCase(authType, "BASIC")) {
//					String authInfo = Base64.decodeStr(authInfoCredentials);
//					String userName = StrUtil.splitTrim(authInfo, StrUtil.COLON).get(0);
//					String password = StrUtil.splitTrim(authInfo, StrUtil.COLON).get(1);
//					if(BASIC_USERNAME.equals(userName) && BASIC_PASSWORD.equals(password)) {
//						authStatusFlag = true;
//					}
//				}
//			}
//		}
//		if (!authStatusFlag) {
//			throw new RuntimeException("basic认证失败,请检查参数是否正确");
//		}
//	}
//
//}
