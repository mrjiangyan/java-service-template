//package org.jeecg.modules.system.controller;
//
//import com.alibaba.fastjson.JSONObject;
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import com.xkcoding.justauth.AuthRequestFactory;
//import io.swagger.annotations.ApiOperation;
//import lombok.extern.slf4j.Slf4j;
//import me.zhyd.oauth.model.AuthCallback;
//import me.zhyd.oauth.model.AuthResponse;
//import me.zhyd.oauth.request.AuthRequest;
//import me.zhyd.oauth.utils.AuthStateUtils;
//import org.jeecg.common.api.vo.Result;
//import org.jeecg.common.constant.CommonConstant;
//import org.jeecg.common.system.util.JwtUtil;
//import org.jeecg.common.util.PasswordUtil;
//import org.jeecg.common.util.RedisUtil;
//import org.jeecg.common.util.RestUtil;
//import org.jeecg.common.util.oConvertUtils;
//import org.jeecg.config.thirdapp.ThirdAppConfig;
//import org.jeecg.config.thirdapp.ThirdAppTypeItemVo;
//import org.jeecg.modules.base.service.BaseCommonService;
//import org.jeecg.modules.system.entity.SysThirdAccount;
//import org.jeecg.modules.system.entity.SysUser;
//import org.jeecg.modules.system.model.ThirdLoginModel;
//import org.jeecg.modules.system.service.ISysThirdAccountService;
//import org.jeecg.modules.system.service.ISysUserService;
//import org.jeecg.modules.system.service.impl.ThirdAppDingtalkServiceImpl;
//import org.jeecg.modules.system.service.impl.ThirdAppWechatEnterpriseServiceImpl;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.ModelMap;
//import org.springframework.web.bind.annotation.*;
//
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.io.UnsupportedEncodingException;
//import java.net.URLEncoder;
//import java.util.List;
//
//import static org.jeecg.common.constant.CommonConstant.TOKEN;
//
///**
// * @Author scott
// * @since 2018-12-17
// */
//@Controller
//@RequestMapping("/sys/thirdLogin")
//@Slf4j
//public class ThirdLoginController {
//	@Autowired
//	private ISysUserService sysUserService;
//	@Autowired
//	private ISysThirdAccountService sysThirdAccountService;
//
//	@Autowired
//	private BaseCommonService baseCommonService;
//	@Autowired
//    private RedisUtil redisUtil;
//	@Autowired
//	private AuthRequestFactory factory;
//
//	@Autowired
//	ThirdAppConfig thirdAppConfig;
//	@Autowired
//	private ThirdAppWechatEnterpriseServiceImpl thirdAppWechatEnterpriseService;
//	@Autowired
//	private ThirdAppDingtalkServiceImpl thirdAppDingtalkService;
//
//	@RequestMapping("/render/{source}")
//    public void render(@PathVariable("source") String source, HttpServletResponse response) throws IOException {
//        log.info("?????????????????????render???" + source);
//        AuthRequest authRequest = factory.get(source);
//        String authorizeUrl = authRequest.authorize(AuthStateUtils.createState());
//        log.info("??????????????????????????????" + authorizeUrl);
//        response.sendRedirect(authorizeUrl);
//    }
//
//	@RequestMapping("/{source}/callback")
//    public String loginThird(@PathVariable("source") String source, AuthCallback callback,ModelMap modelMap) {
//		log.info("?????????????????????callback???" + source + " params???" + JSONObject.toJSONString(callback));
//        AuthRequest authRequest = factory.get(source);
//        AuthResponse response = authRequest.login(callback);
//        log.info(JSONObject.toJSONString(response));
//        Result<JSONObject> result = new Result<>();
//        if(response.getCode()==2000) {
//
//        	JSONObject data = JSONObject.parseObject(JSONObject.toJSONString(response.getData()));
//        	String username = data.getString("username");
//        	String avatar = data.getString("avatar");
//        	String uuid = data.getString("uuid");
//        	//???????????????????????????????????????
//			ThirdLoginModel tlm = new ThirdLoginModel(source, uuid, username, avatar);
//        	//????????????????????????
//			//update-begin-author:wangshuai date:20201118 for:?????????????????????????????????
//        	LambdaQueryWrapper<SysThirdAccount> query = new LambdaQueryWrapper<>();
//        	query.eq(SysThirdAccount::getThirdUserUuid, uuid);
//        	query.eq(SysThirdAccount::getThirdType, source);
//        	List<SysThirdAccount> thridList = sysThirdAccountService.list(query);
//			SysThirdAccount user;
//        	if(thridList==null || thridList.size()==0) {
//				//???????????????????????????
//				user = sysThirdAccountService.saveThirdUser(tlm);
//        	}else {
//        		//????????? ?????????????????? ???????????????
//        		user = thridList.get(0);
//        	}
//        	// ??????token
//			//update-begin-author:wangshuai date:20201118 for:??????????????????????????????????????????id???????????????????????????
//			if(oConvertUtils.isNotEmpty(user.getSysUserId())) {
//				String sysUserId = user.getSysUserId();
//				SysUser sysUser = sysUserService.getById(sysUserId);
//				String token = saveToken(sysUser);
//    			modelMap.addAttribute(TOKEN, token);
//			}else{
//				modelMap.addAttribute(TOKEN, "???????????????,"+""+uuid);
//			}
//			//update-end-author:wangshuai date:20201118 for:??????????????????????????????????????????id???????????????????????????
//		//update-begin--Author:wangshuai  Date:20200729 for????????????????????????????????????????????????????????? issues#1441--------------------
//        }else{
//			modelMap.addAttribute(TOKEN, "????????????");
//		}
//		//update-end--Author:wangshuai  Date:20200729 for????????????????????????????????????????????????????????? issues#1441--------------------
//        result.setSuccess(false);
//        result.setMessage("?????????????????????,??????????????????");
//        return "thirdLogin";
//    }
//
//	/**
//	 * ???????????????
//	 * @param model
//	 * @return
//	 */
//	@PostMapping("/user/create")
//	@ResponseBody
//	public Result<String> thirdUserCreate(@RequestBody ThirdLoginModel model) {
//		log.info("?????????????????????????????????" );
//		Result<String> res = new Result<>();
//		Object operateCode = redisUtil.get(CommonConstant.THIRD_LOGIN_CODE);
//		if(operateCode==null || !operateCode.toString().equals(model.getOperateCode())){
//			res.setSuccess(false);
//			res.setMessage("????????????");
//			return res;
//		}
//		//???????????????
//		//update-begin-author:wangshuai date:20201118 for:???????????????????????????????????????user_id???????????????????????????token
//		SysThirdAccount user = sysThirdAccountService.saveThirdUser(model);
//		if(oConvertUtils.isNotEmpty(user.getSysUserId())){
//			String sysUserId = user.getSysUserId();
//			SysUser sysUser = sysUserService.getById(sysUserId);
//			// ??????token
//			String token = saveToken(sysUser);
//			//update-end-author:wangshuai date:20201118 for:???????????????????????????????????????user_id???????????????????????????token
//			res.setResult(token);
//			res.setSuccess(true);
//		}
//		return res;
//	}
//
//	/**
//	 * ???????????? ?????????????????? ?????????????????????
//	 * @param json
//	 * @return
//	 */
//	@PostMapping("/user/checkPassword")
//	@ResponseBody
//	public Result<String> checkPassword(@RequestBody JSONObject json) {
//		Result<String> result = new Result<>();
//		Object operateCode = redisUtil.get(CommonConstant.THIRD_LOGIN_CODE);
//		if(operateCode==null || !operateCode.toString().equals(json.getString("operateCode"))){
//			result.setSuccess(false);
//			result.setMessage("????????????");
//			return result;
//		}
//		String username = json.getString("uuid");
//		SysUser user = this.sysUserService.getUserByName(username);
//		if(user==null){
//			result.setMessage("???????????????");
//			result.setSuccess(false);
//			return result;
//		}
//		String password = json.getString("password");
//		String salt = user.getSalt();
//		String passwordEncode = PasswordUtil.encrypt(user.getUsername(), password, salt);
//		if(!passwordEncode.equals(user.getPassword())){
//			result.setMessage("???????????????");
//			result.setSuccess(false);
//			return result;
//		}
//
//		sysUserService.updateById(user);
//		result.setSuccess(true);
//		// ??????token
//		String token = saveToken(user);
//		result.setResult(token);
//		return result;
//	}
//
//	private String saveToken(SysUser user) {
//		// ??????token
//		String token = JwtUtil.sign(user.getUsername(), user.getPassword());
//		redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, token);
//		// ??????????????????
//		redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, JwtUtil.EXPIRE_TIME * 2 / 1000);
//		return token;
//	}
//
//	/**
//	 * ???????????????????????????
//	 * @param token
//	 * @param thirdType
//	 * @return
//     */
//	@SuppressWarnings("unchecked")
//	@RequestMapping(value = "/getLoginUser/{token}/{thirdType}", method = RequestMethod.GET)
//	@ResponseBody
//	public Result<JSONObject> getThirdLoginUser(@PathVariable(TOKEN) String token,@PathVariable("thirdType") String thirdType) {
//		String username = JwtUtil.getUsername(token);
//
//		//1. ????????????????????????
//		SysUser sysUser = sysUserService.getUserByName(username);
//		Result<JSONObject>  result = sysUserService.checkUserIsEffective(sysUser);
//		if(!result.isSuccess()) {
//			return result;
//		}
//		//update-begin-author:wangshuai date:20201118 for:????????????????????????????????????????????????????????????
//		LambdaQueryWrapper<SysThirdAccount> query = new LambdaQueryWrapper<>();
//		query.eq(SysThirdAccount::getSysUserId,sysUser.getId());
//		query.eq(SysThirdAccount::getThirdType,thirdType);
//		SysThirdAccount account = sysThirdAccountService.getOne(query);
//		if(oConvertUtils.isEmpty(sysUser.getRealname())){
//			sysUser.setRealname(account.getRealname());
//		}
//		if(oConvertUtils.isEmpty(sysUser.getAvatar())){
//			sysUser.setAvatar(account.getAvatar());
//		}
//		//update-end-author:wangshuai date:20201118 for:????????????????????????????????????????????????????????????
//		JSONObject obj = new JSONObject();
//		//??????????????????
//		obj.put("userInfo", sysUser);
//		//token ??????
//		obj.put(TOKEN, token);
//		result.setResult(obj);
//		result.setSuccess(true);
//		result.setCode(200);
//		baseCommonService.addLog("?????????: " + username + ",????????????[???????????????]???", CommonConstant.LOG_TYPE_1, null);
//		return result;
//	}
//	/**
//	 * ??????????????????????????????token
//	 *
//	 * @param jsonObject
//	 * @return
//	 */
//	@ApiOperation("?????????????????????")
//	@PostMapping("/bindingThirdPhone")
//	@ResponseBody
//	public Result<String> bindingThirdPhone(@RequestBody JSONObject jsonObject) {
//		Result<String> result = new Result<>();
//		String phone = jsonObject.getString("mobile");
//		String thirdUserUuid = jsonObject.getString("thirdUserUuid");
//		// ???????????????
//		String captcha = jsonObject.getString("captcha");
//		Object captchaCache = redisUtil.get(phone);
//		if (oConvertUtils.isEmpty(captcha) || !captcha.equals(captchaCache)) {
//			result.setMessage("???????????????");
//			result.setSuccess(false);
//			return result;
//		}
//		//?????????????????????
//		SysUser sysUser = sysUserService.getUserByPhone(phone);
//		if(sysUser != null){
//			// ???????????????????????????
//			sysThirdAccountService.updateThirdUserId(sysUser,thirdUserUuid);
//		}else{
//			// ?????????????????????????????????
//			sysUser = sysThirdAccountService.createUser(phone,thirdUserUuid);
//		}
//		String token = saveToken(sysUser);
//		result.setSuccess(true);
//		result.setResult(token);
//		return result;
//	}
//
//	/**
//	 * ????????????/?????? OAuth2??????
//	 *
//	 * @param source
//	 * @param state
//	 * @return
//	 */
//	@ResponseBody
//	@GetMapping("/oauth2/{source}/login")
//	public String oauth2LoginCallback(@PathVariable("source") String source, @RequestParam("state") String state, HttpServletResponse response) throws Exception {
//		String url;
//		if (ThirdAppConfig.WECHAT_ENTERPRISE.equalsIgnoreCase(source)) {
//			ThirdAppTypeItemVo config = thirdAppConfig.getWechatEnterprise();
//			StringBuilder builder = new StringBuilder();
//			// ??????????????????OAuth2??????????????????
//			builder.append("https://open.weixin.qq.com/connect/oauth2/authorize");
//			// ?????????CorpID
//			builder.append("?appid=").append(config.getClientId());
//			// ???????????????????????????????????????????????????urlencode?????????????????????
//			String redirectUri = RestUtil.getBaseUrl() + "/sys/thirdLogin/oauth2/wechat_enterprise/callback";
//			builder.append("&redirect_uri=").append(URLEncoder.encode(redirectUri, "UTF-8"));
//			// ?????????????????????????????????code
//			builder.append("&response_type=code");
//			// ????????????????????????
//			// snsapi_base??????????????????????????????????????????????????????UserId???DeviceId??????
//			builder.append("&scope=snsapi_base");
//			// ?????????????????????state???????????????????????????128?????????
//			builder.append("&state=").append(state);
//			// ?????????????????????????????????????????????????????????
//			builder.append("#wechat_redirect");
//			url = builder.toString();
//		} else if (ThirdAppConfig.DINGTALK.equalsIgnoreCase(source)) {
//			ThirdAppTypeItemVo config = thirdAppConfig.getDingtalk();
//			StringBuilder builder = new StringBuilder();
//			// ????????????OAuth2??????????????????
//			builder.append("https://login.dingtalk.com/oauth2/auth");
//			// ????????????/????????????????????????
//			// ?????? ??????????????????????????????????????????????????????
//			String redirectUri = RestUtil.getBaseUrl() + "/sys/thirdLogin/oauth2/dingtalk/callback";
//			builder.append("?redirect_uri=").append(URLEncoder.encode(redirectUri, "UTF-8"));
//			// ????????????code???
//			// ?????????????????????authCode???
//			builder.append("&response_type=code");
//			// ?????????????????????????????????????????????
//			// ?????????????????????client_id????????????AppKey???
//			builder.append("&client_id=").append(config.getClientId());
//			// ????????????????????????????????????????????????????????????????????????????????????
//			// openid???????????????????????????userid
//			builder.append("&scope=openid");
//			// ??????authCode???????????????
//			builder.append("&state=").append(state);
//            //update-begin---author:wangshuai ---date:20220613  for???[issues/I5BOUF]oauth2 ??????????????????------------
//            builder.append("&prompt=").append("consent");
//            //update-end---author:wangshuai ---date:20220613  for???[issues/I5BOUF]oauth2 ??????????????????--------------
//            url = builder.toString();
//		} else {
//			return "????????????source";
//		}
//		log.info("oauth2 login url:" + url);
//		response.sendRedirect(url);
//		return "login???";
//	}
//
//    /**
//     * ????????????/?????? OAuth2????????????
//     *
//     * @param code
//     * @param state
//     * @param response
//     * @return
//     */
//	@ResponseBody
//	@GetMapping("/oauth2/{source}/callback")
//	public String oauth2LoginCallback(
//			@PathVariable("source") String source,
//			// ?????????????????????code
//			@RequestParam(value = "code", required = false) String code,
//			// ???????????????code
//			@RequestParam(value = "authCode", required = false) String authCode,
//			@RequestParam("state") String state,
//			HttpServletResponse response
//	) {
//        SysUser loginUser;
//        if (ThirdAppConfig.WECHAT_ENTERPRISE.equalsIgnoreCase(source)) {
//            log.info("??????????????????OAuth2????????????callback???code=" + code + ", state=" + state);
//            loginUser = thirdAppWechatEnterpriseService.oauth2Login(code);
//            if (loginUser == null) {
//                return "????????????";
//            }
//        } else if (ThirdAppConfig.DINGTALK.equalsIgnoreCase(source)) {
//			log.info("????????????OAuth2????????????callback???authCode=" + authCode + ", state=" + state);
//			loginUser = thirdAppDingtalkService.oauth2Login(authCode);
//			if (loginUser == null) {
//				return "????????????";
//			}
//        } else {
//            return "????????????source";
//        }
//        try {
//
//			//update-begin-author:taoyan date:2022-6-30 for: ????????????????????? ????????????????????????????????????
//			String redirect = "";
//			if (state.indexOf("?") > 0) {
//				String[] arr = state.split("\\?");
//				state = arr[0];
//				if(arr.length>1){
//					redirect = arr[1];
//				}
//			}
//
//			String token = saveToken(loginUser);
//			state += "/oauth2-app/login?oauth2LoginToken=" + URLEncoder.encode(token, "UTF-8");
//			//update-begin---author:wangshuai ---date:20220613  for???[issues/I5BOUF]oauth2 ??????????????????------------
//			state += "&thirdType=" + source;
//			//state += "&thirdType=" + "wechat_enterprise";
//			if (redirect != null && redirect.length() > 0) {
//				state += "&" + redirect;
//			}
//			//update-end-author:taoyan date:2022-6-30 for: ????????????????????? ????????????????????????????????????
//
//            //update-end---author:wangshuai ---date:20220613  for???[issues/I5BOUF]oauth2 ??????????????????------------
//			log.info("OAuth2?????????????????????: " + state);
//            try {
//                response.sendRedirect(state);
//                return "ok";
//            } catch (IOException e) {
//                e.printStackTrace();
//                return "???????????????";
//            }
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//            return "????????????";
//        }
//    }
//
//}