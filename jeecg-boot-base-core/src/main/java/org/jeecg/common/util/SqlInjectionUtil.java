package org.jeecg.common.util;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * sql注入处理工具类
 * 
 * @author zhoujf
 */
@Slf4j
public class SqlInjectionUtil {
	/**
	 * sign 用于表字典加签的盐值【SQL漏洞】
	 * （上线修改值 20200501，同步修改前端的盐值）
	 */
	private final static String TABLE_DICT_SIGN_SALT = "20200501";
	private final static String XSS_STR = "and |exec |insert |select |delete |update |drop |count |chr |mid |master |truncate |char |declare |;|or |+|user()";

	/**
	 * 正则 user() 匹配更严谨
	 */
	private final static String REGULAR_EXPRE_USER = "user[\\s]*\\([\\s]*\\)";
    /**正则 show tables*/
	private final static String SHOW_TABLES = "show\\s+tables";


	/**
	 * sql注入过滤处理，遇到注入关键字抛异常
	 * @param value
	 */
	public static void filterContent(String value) {
		filterContent(value, null);
	}

	/**
	 * sql注入过滤处理，遇到注入关键字抛异常
	 * 
	 * @param value
	 * @return
	 */
	public static void filterContent(String value, String customXssString) {
		if (value == null || "".equals(value)) {
			return;
		}
		// 统一转为小写
		value = value.toLowerCase();
		//SQL注入检测存在绕过风险 https://gitee.com/jeecg/jeecg-boot/issues/I4NZGE
		value = value.replaceAll("/\\*.*\\*/","");

		String[] xssArr = XSS_STR.split("\\|");
        for (String item : xssArr) {
            if (value.contains(item)) {
                log.error("请注意，存在SQL注入关键词---> {}", item);
                log.error("请注意，值可能存在SQL注入风险!---> {}", value);
                throw new RuntimeException("请注意，值可能存在SQL注入风险!--->" + value);
            }
        }
		//update-begin-author:taoyan date:2022-7-13 for: 除了XSS_STR这些提前设置好的，还需要额外的校验比如 单引号
		if (customXssString != null) {
			String[] xssArr2 = customXssString.split("\\|");
            for (String s : xssArr2) {
                if (value.contains(s)) {
                    log.error("请注意，存在SQL注入关键词---> {}", s);
                    log.error("请注意，值可能存在SQL注入风险!---> {}", value);
                    throw new RuntimeException("请注意，值可能存在SQL注入风险!--->" + value);
                }
            }
		}
		//update-end-author:taoyan date:2022-7-13 for: 除了XSS_STR这些提前设置好的，还需要额外的校验比如 单引号
		if(Pattern.matches(SHOW_TABLES, value) || Pattern.matches(REGULAR_EXPRE_USER, value)){
			throw new RuntimeException("请注意，值可能存在SQL注入风险!--->" + value);
		}
    }

	/**
	 * sql注入过滤处理，遇到注入关键字抛异常
	 * @param values
	 */
	public static void filterContent(String[] values) {
		filterContent(values, null);
	}

	/**
	 * sql注入过滤处理，遇到注入关键字抛异常
	 * 
	 * @param values
	 * @return
	 */
	public static void filterContent(String[] values, String customXssString) {
		String[] xssArr = XSS_STR.split("\\|");
		for (String value : values) {
			if (value == null || "".equals(value)) {
				return;
			}
			// 统一转为小写
			value = value.toLowerCase();
			//SQL注入检测存在绕过风险 https://gitee.com/jeecg/jeecg-boot/issues/I4NZGE
			value = value.replaceAll("/\\*.*\\*/","");

            for (String item : xssArr) {
                if (value.contains(item)) {
                    log.error("请注意，存在SQL注入关键词---> {}", item);
                    log.error("请注意，值可能存在SQL注入风险!---> {}", value);
                    throw new RuntimeException("请注意，值可能存在SQL注入风险!--->" + value);
                }
            }
			//update-begin-author:taoyan date:2022-7-13 for: 除了XSS_STR这些提前设置好的，还需要额外的校验比如 单引号
			if (customXssString != null) {
				String[] xssArr2 = customXssString.split("\\|");
                for (String s : xssArr2) {
                    if (value.contains(s)) {
                        log.error("请注意，存在SQL注入关键词---> {}", s);
                        log.error("请注意，值可能存在SQL注入风险!---> {}", value);
                        throw new RuntimeException("请注意，值可能存在SQL注入风险!--->" + value);
                    }
                }
			}
			//update-end-author:taoyan date:2022-7-13 for: 除了XSS_STR这些提前设置好的，还需要额外的校验比如 单引号
			if(Pattern.matches(SHOW_TABLES, value) || Pattern.matches(REGULAR_EXPRE_USER, value)){
				throw new RuntimeException("请注意，值可能存在SQL注入风险!--->" + value);
			}
		}
    }

	/**
	 * 【提醒：不通用】
	 * 仅用于字典条件SQL参数，注入过滤
	 *
	 * @param value
	 * @return
	 */
	//@Deprecated
	public static void specialFilterContentForDictSql(String value) {
		String specialXssStr = " exec | insert | select | delete | update | drop | count | chr | mid | master | truncate | char | declare |;|+|user()";
		String[] xssArr = specialXssStr.split("\\|");
		if (value == null || "".equals(value)) {
			return;
		}
		// 统一转为小写
		value = value.toLowerCase();
		//SQL注入检测存在绕过风险 https://gitee.com/jeecg/jeecg-boot/issues/I4NZGE
		value = value.replaceAll("/\\*.*\\*/","");

        for (String s : xssArr) {
            if (value.contains(s) || value.startsWith(s.trim())) {
                log.error("请注意，存在SQL注入关键词---> {}", s);
                log.error("请注意，值可能存在SQL注入风险!---> {}", value);
                throw new RuntimeException("请注意，值可能存在SQL注入风险!--->" + value);
            }
        }
		if(Pattern.matches(SHOW_TABLES, value) || Pattern.matches(REGULAR_EXPRE_USER, value)){
			throw new RuntimeException("请注意，值可能存在SQL注入风险!--->" + value);
		}
    }


    /**
	 * 【提醒：不通用】
     *  仅用于Online报表SQL解析，注入过滤
     * @param value
     * @return
     */
	//@Deprecated
	public static void specialFilterContentForOnlineReport(String value) {
		String specialXssStr = " exec | insert | delete | update | drop | chr | mid | master | truncate | char | declare |user()";
		String[] xssArr = specialXssStr.split("\\|");
		if (value == null || "".equals(value)) {
			return;
		}
		// 统一转为小写
		value = value.toLowerCase();
		//SQL注入检测存在绕过风险 https://gitee.com/jeecg/jeecg-boot/issues/I4NZGE
		value = value.replaceAll("/\\*.*\\*/","");

        for (String s : xssArr) {
            if (value.contains(s) || value.startsWith(s.trim())) {
                log.error("请注意，存在SQL注入关键词---> {}", s);
                log.error("请注意，值可能存在SQL注入风险!---> {}", value);
                throw new RuntimeException("请注意，值可能存在SQL注入风险!--->" + value);
            }
        }

		if(Pattern.matches(SHOW_TABLES, value) || Pattern.matches(REGULAR_EXPRE_USER, value)){
			throw new RuntimeException("请注意，值可能存在SQL注入风险!--->" + value);
		}
    }


	/**
	 * 判断给定的字段是不是类中的属性
	 * @param field 字段名
	 * @param clazz 类对象
	 * @return
	 */
	public static boolean isClassField(String field, Class clazz){
		Field[] fields = clazz.getDeclaredFields();
        for (Field value : fields) {
            String fieldName = value.getName();
            String tableColumnName = oConvertUtils.camelToUnderline(fieldName);
            if (fieldName.equalsIgnoreCase(field) || tableColumnName.equalsIgnoreCase(field)) {
                return true;
            }
        }
		return false;
	}

	/**
	 * 判断给定的多个字段是不是类中的属性
	 * @param fieldSet 字段名set
	 * @param clazz 类对象
	 * @return
	 */
	public static boolean isClassField(Set<String> fieldSet, Class clazz){
		Field[] fields = clazz.getDeclaredFields();
		for(String field: fieldSet){
			boolean exist = false;
            for (Field value : fields) {
                String fieldName = value.getName();
                String tableColumnName = oConvertUtils.camelToUnderline(fieldName);
                if (fieldName.equalsIgnoreCase(field) || tableColumnName.equalsIgnoreCase(field)) {
                    exist = true;
                    break;
                }
            }
			if(!exist){
				return false;
			}
		}
		return true;
	}
}
