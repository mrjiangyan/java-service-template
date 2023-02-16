package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.system.entity.SysAnnouncement;

/**
 * {@code @Description:} 系统通告表
 * {@code @Author:} jeecg-boot
 * {@code @Date:}  2019-01-02
 * {@code @Version:} V1.0
 */
public interface ISysAnnouncementService extends IService<SysAnnouncement> {

    /**
     * 保存系统通告
     * @param sysAnnouncement
     */
    void saveAnnouncement(SysAnnouncement sysAnnouncement);

    /**
     * 修改系统通告
     * @param sysAnnouncement
     * @return
     */
    boolean upDateAnnouncement(SysAnnouncement sysAnnouncement);

    /**
     * 保存系统通告
     * @param title 标题
     * @param msgContent 信息内容
     */
    void saveSysAnnouncement(String title, String msgContent);

    /**
     * 分页查询系统通告
     * @param page 当前页数
     * @param userId 用户id
     * @param msgCategory 消息类型
     * @return Page<SysAnnouncement>
     */
    Page<SysAnnouncement> querySysCementPageByUserId(Page<SysAnnouncement> page, String userId, String msgCategory);


}
