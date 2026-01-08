package cn.tannn.lychnos.common.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2024/10/30 上午11:17
 */
public class CodeUtil {
    private static final int MAX_NUMERIC = 999;

    /**
     * 计算 字典code 层级
     * @param code 字典code
     * @return 层级 [1~n]
     */
    public static int dictCodeLevel(String code) {
        if(code == null){
            throw new IllegalArgumentException("非法参数");
        }
        int level = code.length() / 3;
        if(level == 0){
            throw new IllegalArgumentException("code.length() 必须能被 3 整除");
        }
        return level;
    }


    /**
     * 组织机构层级
     * @param parentCode 父级code
     * @param nextNum 下一个序号
     * @return
     */
    public static String formatOrgCode(String parentCode, int nextNum) {
        if ("0".equals(parentCode)) {
            return "A";
        }

        // 验证编码格式
        if (!parentCode.matches("^A(([0-9]{3})|([A-Z]01))*$")) {
            throw new IllegalArgumentException("无效的组织机构编码格式");
        }

        // 检查层级限制
        int currentLevel = orgCodeLevel(parentCode);
        if (currentLevel >= 9) {
            throw new IllegalArgumentException("组织层级不能超过9层");
        }

        if (nextNum <= MAX_NUMERIC) {
            return String.format("%s%03d", parentCode, nextNum);
        } else {
            // 超过999时使用大写字母替换
            int letterIndex = (nextNum - MAX_NUMERIC - 1) % 26;
            char letter = (char) ('A' + letterIndex);
            return String.format("%s%c01", parentCode, letter);
        }
    }


    /**
     * 计算 org code 层级
     * @param code org_code
     * @return 层级  [1~n]
     */
    public static int orgCodeLevel(String code) {
        if (code == null || "0".equals(code)) {
            return 0;
        }
        if (code.length() == 1) { // 顶级 A
            return 1;
        }
        // 除去顶级A，每3位数字代表一层
        return 1 + (code.length() - 1) / 3;
    }

    /**
     * 获取组织机构所有父级编码（包含自己）
     * @param code 当前编码
     * @param self ture包含自己，false不含
     * @return 所有父级编码列表，从顶级到当前编码排序
     */
    public static List<String> gainOrgParentNos(String code,boolean self) {
        if (code == null || "0".equals(code)) {
            return List.of();
        }

        List<String> parents = new ArrayList<>();

        if(self){
            // 添加自身
            parents.add(code);
        }

        if (!code.matches("^A(([0-9]{3})|([A-Z]01))*$")) {
            throw new IllegalArgumentException("无效的组织机构编码格式");
        }

        // 如果是顶级节点A，直接返回
        if (code.length() == 1) {
            return parents;
        }

        // 从完整编码中依次截取父级编码
        String parent = code;
        while (parent.length() > 1) {
            parent = parent.substring(0, parent.length() - 3);
            parents.add(0, parent);
        }

        return parents;
    }
}
