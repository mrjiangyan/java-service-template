package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.system.entity.SysPermission;
import org.jeecg.modules.system.model.TreeModel;

import java.util.List;

/**
 * <p>
 * 菜单权限表 服务类
 * </p>
 *
 * @Author scott
 * @since 2018-12-21
 */
public interface ISysPermissionService extends IService<SysPermission> {
	/**
	 * 切换vue3菜单
	 */
	void switchVue3Menu();
	
    /**
     * 通过父id查询菜单
     * @param parentId 父id
     * @return
     */
	List<TreeModel> queryListByParentId(String parentId);
	
	/**
     * 真实删除
     * @param id 菜单id
     * @throws JeecgBootException
     */
	void deletePermission(String id) throws JeecgBootException;
	/**
     * 逻辑删除
     * @param id 菜单id
     * @throws JeecgBootException
     */
	void deletePermissionLogical(String id) throws JeecgBootException;

    /**
     * 添加菜单
     * @param sysPermission SysPermission对象
     * @throws JeecgBootException
     */
	void addPermission(SysPermission sysPermission) throws JeecgBootException;

    /**
     * 编辑菜单
     * @param sysPermission SysPermission对象
     * @throws JeecgBootException
     */
	void editPermission(SysPermission sysPermission) throws JeecgBootException;

    /**
     * 获取登录用户拥有的权限
     * @param username 用户名
     * @return
     */
	List<SysPermission> queryByUser(String username);
	
	/**
	 * 根据permissionId删除其关联的SysPermissionDataRule表中的数据
	 * 
	 * @param id
	 * @return
	 */
	void deletePermRuleByPermId(String id);
	
	/**
	  * 查询出带有特殊符号的菜单地址的集合
	 * @return
	 */
	List<String> queryPermissionUrlWithStar();

	/**
	 * 判断用户否拥有权限
	 * @param username
	 * @param sysPermission
	 * @return
	 */
	boolean hasPermission(String username, SysPermission sysPermission);

	/**
	 * 根据用户和请求地址判断是否有此权限
	 * @param username
	 * @param url
	 * @return
	 */
	boolean hasPermission(String username, String url);
}
