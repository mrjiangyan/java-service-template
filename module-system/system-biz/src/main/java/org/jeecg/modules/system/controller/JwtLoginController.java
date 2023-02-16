package org.jeecg.modules.system.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysDictService;
import org.jeecg.modules.system.service.ISysUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.security.Key;
import java.util.LinkedHashMap;

import static org.jeecg.common.constant.CommonConstant.TOKEN;
import static org.jeecg.common.constant.SymbolConstant.COLON;
import static org.jeecg.common.system.util.JwtUtil.EXPIRE_TIME;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * IDaaS身份登录JWT
 */
@Slf4j
@RestController
@RequestMapping("/sys/")
public class JwtLoginController {

  @Value("${redirect:0}")
  private String redirect;

  @Autowired
  private RedisUtil redisUtil;

  @Autowired
  private ISysUserService sysUserService;

  @Autowired
  private ISysDictService sysDictService;

  @Resource
  private BaseCommonService baseCommonService;

  /**
   * 必需功能：JWT登录接⼝，推荐⽀持redirect参数，可以⾃定义登录后的跳转⻚
   */
  @SneakyThrows
  @RequestMapping(value = "/login/baidu", method = { POST, GET})
  public void jwtLogin(HttpServletRequest request, HttpServletResponse response, @RequestParam("Authorization") String authorization) {
    // 1.准备从Authorization中解析出JWT

    // 设置验签的密钥，签名⽅式可以是HS256或者RS256，其中RS256安全性较⾼，HS256较为简易
    String sigingAlg = "HS256";
    JwtParser parser =  Jwts.parserBuilder().setSigningKey(getSigningKey(sigingAlg))
      // 设置解码器
      .base64UrlDecodeWith(Decoders.BASE64URL)
      .build();
    // Authorization格式为Bearer eyJhbGci...<snip>...yu5CSpyHI，⽤空格分割后取出后⾯的JWT字符串，然后解析
    // 此⽅法会校验token的有效期和签名，请⾃⾏捕获相关异常并处理
    Jws<Claims> jwt;
    try {
      jwt = parser.parseClaimsJws(authorization.split(" ")[1]);
    } catch (Exception ex) {
      throw new RuntimeException("解析JWT失败:" + ex.getMessage());
    }
    // 3.校验通过，⽤户id为sub，对应的⽤户设置为登录态，⼀般是创建⽤户session和cookie等⼯作
    String sub = jwt.getBody().getSubject();
    redirect(sub,response, request);
  }

  @SneakyThrows
  private void redirect(String sub, HttpServletResponse response,HttpServletRequest request){
    log.info( "{} use jwt to login success", sub);
    // 4. 创建会话
    //    sessionService.createSession(sub, request, response);
    // 登录成功，如果指定了redirect就跳转到redirect
    //1. 校验用户是否有效
    //update-begin-author:wangshuai date:20200601 for: 登录代码验证用户是否注销bug，if条件永远为false
    LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(SysUser::getId, sub);
    SysUser sysUser = sysUserService.getOne(queryWrapper);
    //update-end-author:wangshuai date:20200601 for: 登录代码验证用户是否注销bug，if条件永远为false
    Result result = sysUserService.checkUserIsEffective(sysUser);
    if(!result.isSuccess()) {
      response.getWriter().write("非法授权，无法登录");
      return;
    }
    //用户登录信息
    userInfo(sysUser, result);
    //update-begin--Author:liusq  Date:20210126  for：登录成功，删除redis中的验证码
    LoginUser loginUser = new LoginUser();
    BeanUtils.copyProperties(sysUser, loginUser);
    baseCommonService.addLog("用户id: " + sub + ",登录成功！", CommonConstant.LOG_TYPE_1, null,loginUser);
    //update-end--Author:wangshuai  Date:20200714  for：登录日志没有记录人员

    String host = request.getHeader(HttpHeaders.HOST);
    if(!ObjectUtils.isEmpty(host) && host.contains(COLON)){
    }
    String token = ((JSONObject)result.getResult()).getString(TOKEN);
//    log.info("token:{}", token);
//    Cookie cookie = new Cookie(profile + TOKEN, token);
//    cookie.setPath("/");
//    cookie.setMaxAge(-1);
//
//    cookie.setDomain("iot.cscec8b.com.cn");
//    response.addCookie(cookie);
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(redirect + "/user/login?token=" + token);
    if (StringUtils.isNotBlank(sysUser.getAvatar())) {
      stringBuilder.append("&avatar=" + URLEncoder.encode(sysUser.getAvatar()));
    }
    if (StringUtils.isNotBlank(sysUser.getUsername())) {
      stringBuilder.append("&username=" + URLEncoder.encode(sysUser.getUsername()));
    }
    if (StringUtils.isNotBlank(sysUser.getRealname())) {
      stringBuilder.append("&realname=" + URLEncoder.encode(sysUser.getRealname()));
    }
    log.info("redirect:{}", stringBuilder);
    response.sendRedirect(stringBuilder.toString());
  }
  /**
   * 获取验签密钥，根据签名⽅式决定实现，签名⽅式可在IDaaS的应⽤配置⻚配置，当前⽀持HS256和RS256
   *
   * @param sigingAlg 签名算法名称 HS256/RS256
   * @return
   */
  private Key getSigningKey(String sigingAlg) {
    // HS256使⽤对称密钥验签，密钥配置在IDaaS的JWT应⽤配置⻚，推荐使⽤32位以上字⺟+数字，下⾯是示例
    String hs256SignKey = "12345abcde12345abcde12345abcde12";
    return new SecretKeySpec(hs256SignKey.getBytes(), SignatureAlgorithm.HS256.getJcaName());
  }

  /**
   * 用户信息
   *
   * @param sysUser
   * @param result
   * @return
   */
  private Result<JSONObject> userInfo(SysUser sysUser, Result<JSONObject> result) {
    String username = sysUser.getUsername();
    String syspassword = sysUser.getPassword();
    // 获取用户部门信息
    JSONObject obj = new JSONObject(new LinkedHashMap<>());

    // 生成token
    String token = JwtUtil.sign(username, syspassword);
    // 设置token缓存有效时间
    redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, token);
    redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, EXPIRE_TIME * 2 / 1000);
    obj.put(TOKEN, token);

    obj.put("userInfo", sysUser);
    obj.put("multi_depart", 0);
    obj.put("sysAllDictItems", sysDictService.queryAllDictItems());
    result.setResult(obj);
    result.success("登录成功");
    return result;
  }

}