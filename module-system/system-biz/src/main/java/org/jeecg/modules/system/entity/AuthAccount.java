package org.jeecg.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Timestamp;

/**
 * <p>Description : {todo} </p>
 *
 * @author : xyf
 * @version : v1.0.0
 * @since : 2022/12/9 13:08
 **/
@Data
@TableName("auth_account")
public class AuthAccount {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * appid
     */
    private String appid;

    /**
     * name
     */
    private String name;
    /**
     * secretKey
     */
    private String secretKey;
    /**
     * status 状态 1 正常 2 异常
     */
    private Integer status;
    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp gmtCreate;
    /**
     * 修改时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp gmtModify;

    private Integer deleted;
}
