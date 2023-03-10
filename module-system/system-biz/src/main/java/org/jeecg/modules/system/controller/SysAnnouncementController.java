package org.jeecg.modules.system.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.CommonSendStatus;
import org.jeecg.common.constant.WebsocketConst;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.message.websocket.WebSocket;
import org.jeecg.modules.system.entity.SysAnnouncement;
import org.jeecg.modules.system.entity.SysAnnouncementSend;
import org.jeecg.modules.system.service.ISysAnnouncementSendService;
import org.jeecg.modules.system.service.ISysAnnouncementService;
import org.jeecg.modules.system.service.impl.SysBaseApiImpl;

import org.jeecg.modules.system.util.XssUtils;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jeecg.common.constant.CommonConstant.ANNOUNCEMENT_SEND_STATUS_1;

/**
 * @Title: Controller
 * @Description: ???????????????
 * @Author: jeecg-boot
 * @Date: 2019-01-02
 * @Version: V1.0
 */
@RestController
@RequestMapping("/sys/annountCement")
@Slf4j
public class SysAnnouncementController {
	@Autowired
	private ISysAnnouncementService sysAnnouncementService;
	@Autowired
	private ISysAnnouncementSendService sysAnnouncementSendService;
	@Resource
  @Lazy
  private WebSocket webSocket;
//	@Autowired
//  private ThirdAppWechatEnterpriseServiceImpl wechatEnterpriseService;

//	@Autowired
//  private ThirdAppDingtalkServiceImpl dingtalkService;

	@Autowired
	private SysBaseApiImpl sysBaseApi;

	@Autowired
	@Lazy
	private RedisUtil redisUtil;

	/**
	  * ??????????????????
	 * @param sysAnnouncement
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public Result<IPage<SysAnnouncement>> queryPageList(SysAnnouncement sysAnnouncement,
									  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
									  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
									  HttpServletRequest req) {
		Result<IPage<SysAnnouncement>> result = new Result<>();
		sysAnnouncement.setDelFlag(CommonConstant.DEL_FLAG_0.toString());
		QueryWrapper<SysAnnouncement> queryWrapper = QueryGenerator.initQueryWrapper(sysAnnouncement, req.getParameterMap());
		Page<SysAnnouncement> page = new Page<>(pageNo, pageSize);

		//update-begin-author:lvdandan date:20211229 for: sqlserver mssql-jdbc 8.2.2.jre8??????????????????????????????????????? ??????SQL??????????????????create_time DESC????????????????????????
		//???????????? ??????
//		String column = req.getParameter("column");
//		String order = req.getParameter("order");
//		if(oConvertUtils.isNotEmpty(column) && oConvertUtils.isNotEmpty(order)) {
//			if("asc".equals(order)) {
//				queryWrapper.orderByAsc(oConvertUtils.camelToUnderline(column));
//			}else {
//				queryWrapper.orderByDesc(oConvertUtils.camelToUnderline(column));
//			}
//		}
		//update-end-author:lvdandan date:20211229 for: sqlserver mssql-jdbc 8.2.2.jre8??????????????????????????????????????? ??????SQL??????????????????create_time DESC????????????????????????
		IPage<SysAnnouncement> pageList = sysAnnouncementService.page(page, queryWrapper);
		result.setSuccess(true);
		result.setResult(pageList);
		return result;
	}

	/**
	  *   ??????
	 * @param sysAnnouncement
	 * @return
	 */
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public Result<SysAnnouncement> add(@RequestBody SysAnnouncement sysAnnouncement) {
		Result<SysAnnouncement> result = new Result<>();
		try {
			// update-begin-author:liusq date:20210804 for:????????????xss???????????????
			String title = XssUtils.scriptXss(sysAnnouncement.getTitile());
			sysAnnouncement.setTitile(title);
			// update-end-author:liusq date:20210804 for:????????????xss???????????????
			sysAnnouncement.setDelFlag(CommonConstant.DEL_FLAG_0.toString());
            //?????????
			sysAnnouncement.setSendStatus(CommonSendStatus.UNPUBLISHED_STATUS_0);
			sysAnnouncementService.saveAnnouncement(sysAnnouncement);
			result.success("???????????????");
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("????????????");
		}
		return result;
	}

	/**
	  *  ??????
	 * @param sysAnnouncement
	 * @return
	 */
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<SysAnnouncement> eidt(@RequestBody SysAnnouncement sysAnnouncement) {
		Result<SysAnnouncement> result = new Result<>();
		SysAnnouncement sysAnnouncementEntity = sysAnnouncementService.getById(sysAnnouncement.getId());
		if(sysAnnouncementEntity==null) {
			result.error500("?????????????????????");
		}else {
			// update-begin-author:liusq date:20210804 for:????????????xss???????????????
			String title = XssUtils.scriptXss(sysAnnouncement.getTitile());
			sysAnnouncement.setTitile(title);
			// update-end-author:liusq date:20210804 for:????????????xss???????????????
			boolean ok = sysAnnouncementService.upDateAnnouncement(sysAnnouncement);
			//TODO ??????false???????????????
			if(ok) {
				result.success("????????????!");
			}
		}

		return result;
	}

	/**
	  *   ??????id??????
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	public Result<SysAnnouncement> delete(@RequestParam(name="id") String id) {
		Result<SysAnnouncement> result = new Result<>();
		SysAnnouncement sysAnnouncement = sysAnnouncementService.getById(id);
		if(sysAnnouncement==null) {
			result.error500("?????????????????????");
		}else {
			sysAnnouncement.setDelFlag(CommonConstant.DEL_FLAG_1.toString());
			boolean ok = sysAnnouncementService.updateById(sysAnnouncement);
			if(ok) {
				result.success("????????????!");
			}
		}

		return result;
	}

	/**
	  *  ????????????
	 * @param ids
	 * @return
	 */
	@RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
	public Result<SysAnnouncement> deleteBatch(@RequestParam(name="ids") String ids) {
		Result<SysAnnouncement> result = new Result<>();
		if(ids==null || "".equals(ids.trim())) {
			result.error500("??????????????????");
		}else {
			String[] id = ids.split(",");
			for (String s : id) {
				SysAnnouncement announcement = sysAnnouncementService.getById(s);
				announcement.setDelFlag(CommonConstant.DEL_FLAG_1.toString());
				sysAnnouncementService.updateById(announcement);
			}
			result.success("????????????!");
		}
		return result;
	}

	/**
	  * ??????id??????
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/queryById", method = RequestMethod.GET)
	public Result<SysAnnouncement> queryById(@RequestParam(name="id") String id) {
		Result<SysAnnouncement> result = new Result<>();
		SysAnnouncement sysAnnouncement = sysAnnouncementService.getById(id);
		if(sysAnnouncement==null) {
			result.error500("?????????????????????");
		}else {
			result.setResult(sysAnnouncement);
			result.setSuccess(true);
		}
		return result;
	}

	/**
	 *	 ??????????????????
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/doReleaseData", method = RequestMethod.GET)
	public Result<SysAnnouncement> doReleaseData(@RequestParam(name="id") String id, HttpServletRequest request) {
		Result<SysAnnouncement> result = new Result<>();
		SysAnnouncement sysAnnouncement = sysAnnouncementService.getById(id);
		if(sysAnnouncement==null) {
			result.error500("?????????????????????");
		}else {
            //?????????
			sysAnnouncement.setSendStatus(CommonSendStatus.PUBLISHED_STATUS_1);
			sysAnnouncement.setSendTime(new Date());
			String currentUserName = JwtUtil.getUserNameByToken(request);
			sysAnnouncement.setSender(currentUserName);
			boolean ok = sysAnnouncementService.updateById(sysAnnouncement);
			if(ok) {
				result.success("???????????????????????????");
				if(sysAnnouncement.getMsgType().equals(CommonConstant.MSG_TYPE_ALL)) {
					JSONObject obj = new JSONObject();
			    	obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_TOPIC);
					obj.put(WebsocketConst.MSG_ID, sysAnnouncement.getId());
					obj.put(WebsocketConst.MSG_TXT, sysAnnouncement.getTitile());
			    	webSocket.sendMessage(obj.toJSONString());
				}else {
					// 2.???????????????????????????????????????
					String userId = sysAnnouncement.getUserIds();
					String[] userIds = userId.substring(0, (userId.length()-1)).split(",");
					String anntId = sysAnnouncement.getId();
					Date refDate = new Date();
					JSONObject obj = new JSONObject();
			    	obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_USER);
					obj.put(WebsocketConst.MSG_ID, sysAnnouncement.getId());
					obj.put(WebsocketConst.MSG_TXT, sysAnnouncement.getTitile());
			    	webSocket.sendMessage(userIds, obj.toJSONString());
				}
				try {
					// ??????????????????????????????????????????
//					Response<String> dtResponse = dingtalkService.sendActionCardMessage(sysAnnouncement, true);
//					wechatEnterpriseService.sendTextCardMessage(sysAnnouncement, true);
//
//					if (dtResponse != null && dtResponse.isSuccess()) {
//						String taskId = dtResponse.getResult();
//						sysAnnouncement.setDtTaskId(taskId);
//						sysAnnouncementService.updateById(sysAnnouncement);
//					}
				} catch (Exception e) {
					log.error("?????????????????????APP???????????????", e);
				}
			}
		}

		return result;
	}

	/**
	 *	 ??????????????????
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/doReovkeData", method = RequestMethod.GET)
	public Result<SysAnnouncement> doReovkeData(@RequestParam(name="id") String id, HttpServletRequest request) {
		Result<SysAnnouncement> result = new Result<>();
		SysAnnouncement sysAnnouncement = sysAnnouncementService.getById(id);
		if(sysAnnouncement==null) {
			result.error500("?????????????????????");
		}else {
            //????????????
			sysAnnouncement.setSendStatus(CommonSendStatus.REVOKE_STATUS_2);
			sysAnnouncement.setCancelTime(new Date());
			boolean ok = sysAnnouncementService.updateById(sysAnnouncement);
			if(ok) {
				result.success("???????????????????????????");
				if (oConvertUtils.isNotEmpty(sysAnnouncement.getDtTaskId())) {
					try {
						//dingtalkService.recallMessage(sysAnnouncement.getDtTaskId());
					} catch (Exception e) {
						log.error("?????????APP?????????????????????", e);
					}
				}
			}
		}

		return result;
	}

	/**
	 * @???????????????????????????????????????????????????
	 * @return
	 */
	@RequestMapping(value = "/listByUser", method = RequestMethod.GET)
	public Result<Map<String, Object>> listByUser(@RequestParam(required = false, defaultValue = "5") Integer pageSize) {
		Result<Map<String,Object>> result = new Result<>();
		LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();
		String userId = sysUser.getId();
		// 1.??????????????????????????????????????????????????????
		LambdaQueryWrapper<SysAnnouncement> querySaWrapper = new LambdaQueryWrapper<>();
        //????????????
		querySaWrapper.eq(SysAnnouncement::getMsgType,CommonConstant.MSG_TYPE_ALL);
        //?????????
		querySaWrapper.eq(SysAnnouncement::getDelFlag,CommonConstant.DEL_FLAG_0.toString());
        //?????????
		querySaWrapper.eq(SysAnnouncement::getSendStatus, CommonConstant.HAS_SEND);
        //?????????????????????????????????
		querySaWrapper.ge(SysAnnouncement::getEndTime, sysUser.getCreateTime());
		//update-begin--Author:liusq  Date:20210108 for???[JT-424] ?????????issue???bug??????--------------------
		querySaWrapper.notInSql(SysAnnouncement::getId,"select annt_id from sys_announcement_send where user_id='"+userId+"'");
		//update-begin--Author:liusq  Date:20210108  for??? [JT-424] ?????????issue???bug??????--------------------
		List<SysAnnouncement> announcements = sysAnnouncementService.list(querySaWrapper);
		if(announcements.size()>0) {
			for (SysAnnouncement announcement : announcements) {
				//update-begin--Author:wangshuai  Date:20200803  for??? ????????????????????????LOWCOD-759--------------------
				//??????websocket??????????????????????????????????????????????????????????????????????????????????????????
				LambdaQueryWrapper<SysAnnouncementSend> query = new LambdaQueryWrapper<>();
				query.eq(SysAnnouncementSend::getAnntId, announcement.getId());
				query.eq(SysAnnouncementSend::getUserId, userId);
				SysAnnouncementSend one = sysAnnouncementSendService.getOne(query);
				if (null == one) {
					log.info("listByUser???????????????SysAnnouncementSend???pageSize{}???" + pageSize);
					SysAnnouncementSend announcementSend = new SysAnnouncementSend();
					announcementSend.setAnntId(announcement.getId());
					announcementSend.setUserId(userId);
					announcementSend.setReadFlag(CommonConstant.NO_READ_FLAG);
					sysAnnouncementSendService.save(announcementSend);
					log.info("announcementSend:{}", announcementSend);
				}
				//update-end--Author:wangshuai  Date:20200803  for??? ????????????????????????LOWCOD-759------------
			}
		}
		// 2.?????????????????????????????????
		Page<SysAnnouncement> anntMsgList = new Page<>(0, pageSize);
        //??????????????????
		anntMsgList = sysAnnouncementService.querySysCementPageByUserId(anntMsgList,userId,"1");
		Page<SysAnnouncement> sysMsgList = new Page<>(0, pageSize);
        //????????????
		sysMsgList = sysAnnouncementService.querySysCementPageByUserId(sysMsgList,userId,"2");
		Map<String,Object> sysMsgMap = new HashMap(5);
		sysMsgMap.put("sysMsgList", sysMsgList.getRecords());
		sysMsgMap.put("sysMsgTotal", sysMsgList.getTotal());
		sysMsgMap.put("anntMsgList", anntMsgList.getRecords());
		sysMsgMap.put("anntMsgTotal", anntMsgList.getTotal());
		result.setSuccess(true);
		result.setResult(sysMsgMap);
		return result;
	}


    /**
     * ??????excel
     *
     * @param request
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(SysAnnouncement sysAnnouncement,HttpServletRequest request) {
        // Step.1 ??????????????????
        LambdaQueryWrapper<SysAnnouncement> queryWrapper = new LambdaQueryWrapper<>(sysAnnouncement);
        //Step.2 AutoPoi ??????Excel
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
		queryWrapper.eq(SysAnnouncement::getDelFlag,CommonConstant.DEL_FLAG_0.toString());
        List<SysAnnouncement> pageList = sysAnnouncementService.list(queryWrapper);
        //??????????????????
        mv.addObject(NormalExcelConstants.FILE_NAME, "??????????????????");
        mv.addObject(NormalExcelConstants.CLASS, SysAnnouncement.class);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("????????????????????????", "?????????:"+user.getRealname(), "????????????"));
        mv.addObject(NormalExcelConstants.DATA_LIST, pageList);
        return mv;
    }

    /**
     * ??????excel????????????
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            // ????????????????????????
            MultipartFile file = entity.getValue();
            ImportParams params = new ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(1);
            params.setNeedSave(true);
            try {
                List<SysAnnouncement> listSysAnnouncements = ExcelImportUtil.importExcel(file.getInputStream(), SysAnnouncement.class, params);
                for (SysAnnouncement sysAnnouncementExcel : listSysAnnouncements) {
                	if(sysAnnouncementExcel.getDelFlag()==null){
                		sysAnnouncementExcel.setDelFlag(CommonConstant.DEL_FLAG_0.toString());
					}
                    sysAnnouncementService.save(sysAnnouncementExcel);
                }
                return Result.ok("????????????????????????????????????" + listSysAnnouncements.size());
            } catch (Exception e) {
                log.error(e.getMessage(),e);
                return Result.error("?????????????????????");
            } finally {
                try {
                    file.getInputStream().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return Result.error("?????????????????????");
    }
	/**
	 *????????????
	 * @param anntId
	 * @return
	 */
	@RequestMapping(value = "/syncNotic", method = RequestMethod.GET)
	public Result<SysAnnouncement> syncNotic(@RequestParam(name="anntId",required=false) String anntId, HttpServletRequest request) {
		Result<SysAnnouncement> result = new Result<>();
		JSONObject obj = new JSONObject();
		if(StringUtils.isNotBlank(anntId)){
			SysAnnouncement sysAnnouncement = sysAnnouncementService.getById(anntId);
			if(sysAnnouncement==null) {
				result.error500("?????????????????????");
			}else {
				if(sysAnnouncement.getMsgType().equals(CommonConstant.MSG_TYPE_ALL)) {
					obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_TOPIC);
					obj.put(WebsocketConst.MSG_ID, sysAnnouncement.getId());
					obj.put(WebsocketConst.MSG_TXT, sysAnnouncement.getTitile());
					webSocket.sendMessage(obj.toJSONString());
				}else {
					// 2.???????????????????????????????????????
					String userId = sysAnnouncement.getUserIds();
					if(oConvertUtils.isNotEmpty(userId)){
						String[] userIds = userId.substring(0, (userId.length()-1)).split(",");
						obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_USER);
						obj.put(WebsocketConst.MSG_ID, sysAnnouncement.getId());
						obj.put(WebsocketConst.MSG_TXT, sysAnnouncement.getTitile());
						webSocket.sendMessage(userIds, obj.toJSONString());
					}
				}
			}
		}else{
			obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_TOPIC);
			obj.put(WebsocketConst.MSG_TXT, "??????????????????");
			webSocket.sendMessage(obj.toJSONString());
		}
		return result;
	}

	/**
	 * ??????????????????????????????????????????APP???
	 * @param modelAndView
	 * @param id
	 * @return
	 */
    @GetMapping("/show/{id}")
    public ModelAndView showContent(ModelAndView modelAndView, @PathVariable("id") String id, HttpServletRequest request) {
        SysAnnouncement announcement = sysAnnouncementService.getById(id);
        if (announcement != null) {
            boolean tokenOk = false;
            try {
                // ??????Token?????????
                tokenOk = TokenUtils.verifyToken(request, sysBaseApi, redisUtil);
            } catch (Exception ignored) {
            }
            // ?????????????????????Token?????????Token?????????????????????????????????????????????????????????
            // ??????Token??????????????????????????????????????????????????????
            if (tokenOk || ANNOUNCEMENT_SEND_STATUS_1.equals(announcement.getSendStatus())) {
                modelAndView.addObject("data", announcement);
                modelAndView.setViewName("announcement/showContent");
                return modelAndView;
            }
        }
        modelAndView.setStatus(HttpStatus.NOT_FOUND);
        return modelAndView;
    }

}
