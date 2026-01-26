package cn.tannn.lychnos.common.util;

import lombok.extern.slf4j.Slf4j;

/**
 * 压缩
 *
 * @author <a href="https://t.tannn.cn/">tan</a>
 * @version V1.0
 * @date 2026/1/17 03:05
 */
@Slf4j
public class ZipUtil {

    /**
     * ModelScope 提示词最大长度限制
     */
    private static final int MAX_PROMPT_LENGTH = 2000;


    /**
     * 智能压缩提示词（仅在超过指定长度时才压缩） - def 2000
     *
     * @param prompt 原始提示词
     * @return 压缩后的提示词
     */
    public static String smartCompressPromptLimit2K(String prompt){

        String finalPrompt = prompt;

        if (prompt.length() > MAX_PROMPT_LENGTH) {
            log.info("提示词长度 {} 超过限制 {}，尝试智能压缩", prompt.length(), MAX_PROMPT_LENGTH);

            // 第一步：尝试普通压缩
            finalPrompt = ZipUtil.smartCompressPrompt(prompt, MAX_PROMPT_LENGTH);

            // 第二步：如果还是超长，使用激进压缩
            if (finalPrompt.length() > MAX_PROMPT_LENGTH) {
                log.warn("普通压缩后长度 {} 仍超限，使用激进压缩", finalPrompt.length());
                finalPrompt = ZipUtil.compressPromptAggressive(prompt);

                // 第三步：最后的保险，如果还是超长才截断
                if (finalPrompt.length() > MAX_PROMPT_LENGTH) {
                    log.error("激进压缩后长度 {} 仍超限，被迫截断到 {}",
                            finalPrompt.length(), MAX_PROMPT_LENGTH);
                    finalPrompt = finalPrompt.substring(0, MAX_PROMPT_LENGTH);
                }
            }

            log.info("提示词压缩完成：{} -> {} 字符（节省 {}%）",
                    prompt.length(),
                    finalPrompt.length(),
                    String.format("%.1f", (1.0 - (double)finalPrompt.length() / prompt.length()) * 100));
        }
        return finalPrompt;
    }

    /**
     * 智能压缩提示词（仅在超过指定长度时才压缩）
     *
     * @param prompt 原始提示词
     * @param threshold 压缩阈值（超过此长度才进行压缩）
     * @return 压缩后的提示词
     */
    public static String smartCompressPrompt(String prompt, int threshold) {
        if (prompt == null || prompt.isEmpty()) {
            return prompt;
        }

        int originalLength = prompt.length();

        // 如果未超过阈值，直接返回原始提示词
        if (originalLength <= threshold) {
            log.debug("提示词长度 {} 未超过阈值 {}，跳过压缩", originalLength, threshold);
            return prompt;
        }

        // 超过阈值，执行压缩
        log.info("提示词长度 {} 超过阈值 {}，开始压缩", originalLength, threshold);
        return compressPrompt(prompt);
    }


    /**
     * 极致压缩提示词（支持中英文混合，牺牲可读性换取最大信息密度）
     *
     * @param prompt 原始提示词
     * @return 压缩后的提示词
     */
    public static String compressPrompt(String prompt) {
        if (prompt == null || prompt.isEmpty()) {
            return prompt;
        }

        String compressed = prompt;

        // ============ 第一层：去除英文冗余词汇 ============
        // 去除常见的冗余词（对图片生成无实际意义）
        compressed = compressed.replaceAll("(?i)\\b(please|kindly|very|really|actually|basically|essentially|literally)\\b", "");

        // 去除冗余的礼貌用语
        compressed = compressed.replaceAll("(?i)\\b(could you|can you|would you|please help|thank you|thanks)\\b", "");

        // 去除"a", "an", "the"（图片生成模型对冠词不敏感）
        compressed = compressed.replaceAll("(?i)\\b(a|an|the)\\s+", "");


        // ============ 第二层：英文同义词压缩 ============
        compressed = compressed.replaceAll("(?i)\\bhorizontal\\b", "horiz");
        compressed = compressed.replaceAll("(?i)\\bvertical\\b", "vert");
        compressed = compressed.replaceAll("(?i)\\bbackground\\b", "bg");
        compressed = compressed.replaceAll("(?i)\\binformation\\b", "info");
        compressed = compressed.replaceAll("(?i)\\bprofessional\\b", "pro");
        compressed = compressed.replaceAll("(?i)\\beducational\\b", "edu");
        compressed = compressed.replaceAll("(?i)\\bpresentation\\b", "present");
        compressed = compressed.replaceAll("(?i)\\billustration\\b", "illust");
        compressed = compressed.replaceAll("(?i)\\bphotograph\\b", "photo");
        compressed = compressed.replaceAll("(?i)\\blandscape\\b", "lands");
        compressed = compressed.replaceAll("(?i)\\bportrait\\b", "port");
        compressed = compressed.replaceAll("(?i)\\bcomposition\\b", "comp");
        compressed = compressed.replaceAll("(?i)\\batmosphere\\b", "atmos");
        compressed = compressed.replaceAll("(?i)\\benvironment\\b", "env");
        compressed = compressed.replaceAll("(?i)\\bcharacter\\b", "char");
        compressed = compressed.replaceAll("(?i)\\barchitecture\\b", "arch");
        compressed = compressed.replaceAll("(?i)\\btechnology\\b", "tech");
        compressed = compressed.replaceAll("(?i)\\bdocument\\b", "doc");
        compressed = compressed.replaceAll("(?i)\\bapplication\\b", "app");
        compressed = compressed.replaceAll("(?i)\\bdevelopment\\b", "dev");
        compressed = compressed.replaceAll("(?i)\\bmanagement\\b", "mgmt");
        compressed = compressed.replaceAll("(?i)\\bconfiguration\\b", "config");
        compressed = compressed.replaceAll("(?i)\\bmaximum\\b", "max");
        compressed = compressed.replaceAll("(?i)\\bminimum\\b", "min");
        compressed = compressed.replaceAll("(?i)\\baverage\\b", "avg");
        compressed = compressed.replaceAll("(?i)\\btemperature\\b", "temp");


        // ============ 第三层：中文冗余词去除 ============
        // 去除中文常见的冗余词
        compressed = compressed.replaceAll("请|麻烦|谢谢|感谢", "");
        compressed = compressed.replaceAll("非常|很|十分|特别|尤其|格外", "");
        compressed = compressed.replaceAll("基本上|实际上|事实上|其实", "");
        compressed = compressed.replaceAll("可以|能够|应该|需要|必须", "");
        compressed = compressed.replaceAll("进行|实现|完成", "");
        compressed = compressed.replaceAll("的话|的时候|的情况下", "");

        // 去除中文助词（在不影响语义的情况下）
        compressed = compressed.replaceAll("了|吗|呢|啊|呀|吧", "");


        // ============ 第四层：中英文同义词替换 ============
        // 中文词汇缩写
        compressed = compressed.replaceAll("背景", "bg");
        compressed = compressed.replaceAll("信息", "info");
        compressed = compressed.replaceAll("专业", "pro");
        compressed = compressed.replaceAll("教育", "edu");
        compressed = compressed.replaceAll("技术", "tech");
        compressed = compressed.replaceAll("水平", "horiz");
        compressed = compressed.replaceAll("垂直", "vert");
        compressed = compressed.replaceAll("横向", "horiz");
        compressed = compressed.replaceAll("竖向", "vert");
        compressed = compressed.replaceAll("风景", "lands");
        compressed = compressed.replaceAll("人物", "char");
        compressed = compressed.replaceAll("照片", "photo");
        compressed = compressed.replaceAll("图片", "img");
        compressed = compressed.replaceAll("图像", "img");
        compressed = compressed.replaceAll("文档", "doc");
        compressed = compressed.replaceAll("应用", "app");
        compressed = compressed.replaceAll("开发", "dev");
        compressed = compressed.replaceAll("管理", "mgmt");
        compressed = compressed.replaceAll("配置", "config");
        compressed = compressed.replaceAll("最大", "max");
        compressed = compressed.replaceAll("最小", "min");
        compressed = compressed.replaceAll("平均", "avg");
        compressed = compressed.replaceAll("温度", "temp");
        compressed = compressed.replaceAll("环境", "env");
        compressed = compressed.replaceAll("氛围", "atmos");


        // ============ 第五层：数字和单位缩写 ============
        compressed = compressed.replaceAll("(?i)\\bpixels?\\b", "px");
        compressed = compressed.replaceAll("像素", "px");
        compressed = compressed.replaceAll("(?i)\\bpercent\\b", "%");
        compressed = compressed.replaceAll("百分之", "%");
        compressed = compressed.replaceAll("(?i)\\bdegrees?\\b", "deg");
        compressed = compressed.replaceAll("(?i)\\bkilometers?\\b", "km");
        compressed = compressed.replaceAll("公里", "km");
        compressed = compressed.replaceAll("(?i)\\bcentimeters?\\b", "cm");
        compressed = compressed.replaceAll("厘米", "cm");
        compressed = compressed.replaceAll("(?i)\\bmillimeters?\\b", "mm");
        compressed = compressed.replaceAll("毫米", "mm");
        compressed = compressed.replaceAll("(?i)\\bmeters?\\b", "m");
        compressed = compressed.replaceAll("米", "m");


        // ============ 第六层：标点符号优化 ============
        // 中英文标点统一
        compressed = compressed.replaceAll("[;；]", ",");  // 分号改逗号
        compressed = compressed.replaceAll("[!！?？]+", ".");  // 感叹号/问号改句号
        compressed = compressed.replaceAll("[。]+", ".");  // 中文句号改英文句号
        compressed = compressed.replaceAll("\\.{2,}", ".");  // 多个句号合并
        compressed = compressed.replaceAll("[,，]{2,}", ",");  // 多个逗号合并
        compressed = compressed.replaceAll("[、，]", ",");  // 顿号、中文逗号改英文逗号

        // 去除引号（对图片生成无影响）
        compressed = compressed.replaceAll("[\"'`《》【】]+", "");

        // 去除括号内容（通常是补充说明，优先级低）
        compressed = compressed.replaceAll("[(（][^)）]{0,30}[)）]", "");  // 只删除短括号内容

        // 去除破折号、省略号
        compressed = compressed.replaceAll("[—–-]{2,}|…+|\\.{3,}", "");


        // ============ 第七层：空白符压缩 ============
        // 移除所有换行和制表符
        compressed = compressed.replaceAll("[\\r\\n\\t]+", " ");

        // 标点前后空格优化
        compressed = compressed.replaceAll("\\s*([,.;:])\\s*", "$1 ");  // 标点后保留1个空格
        compressed = compressed.replaceAll("\\s+([,.;:])", "$1");  // 标点前无空格

        // 合并多余空格
        compressed = compressed.replaceAll("\\s{2,}", " ");

        // 去除首尾空格
        compressed = compressed.trim();


        // ============ 第八层：重复内容去重 ============
        // 去除连续重复的短语（例如 "modern modern" -> "modern"）
        compressed = compressed.replaceAll("\\b(\\w+)\\s+\\1\\b", "$1");


        // ============ 日志输出（修正格式） ============
        int originalLength = prompt.length();
        int compressedLength = compressed.length();
        int savedChars = originalLength - compressedLength;
        double compressionRatio = (double) savedChars / originalLength * 100;

        log.debug("提示词压缩完成 | 原始: {}字符 -> 压缩: {}字符 | 节省: {}字符 ({}%)",
                originalLength, compressedLength, savedChars, String.format("%.1f", compressionRatio));

        return compressed;
    }


    /**
     * 超激进压缩（仅保留核心关键词，支持中英文）
     */
    public static String compressPromptAggressive(String prompt) {
        if (prompt == null || prompt.isEmpty()) {
            return prompt;
        }

        String compressed = prompt;

        // 执行基础压缩
        compressed = compressPrompt(compressed);

        // ============ 激进策略 ============

        // 1. 去除所有英文冠词、介词、连词
        compressed = compressed.replaceAll("(?i)\\b(in|on|at|to|for|of|with|from|by|and|or|but|if|as|so)\\b", "");

        // 2. 去除中文介词、连词
        compressed = compressed.replaceAll("在|和|与|或|但|如果|因为|所以|而且|并且|以及", "");
        compressed = compressed.replaceAll("对于|关于|通过|根据|按照|依据", "");

        // 3. 去除"must", "should", "need"等情态动词
        compressed = compressed.replaceAll("(?i)\\b(must|should|need|can|will|shall)\\b", "");

        // 4. 去除"的"、"地"、"得"（在不影响理解的情况下）
        compressed = compressed.replaceAll("[的地得]", "");

        // 5. 用逗号替代所有句号（减少分隔符）
        compressed = compressed.replaceAll("[.。]", ",");

        // 6. 清理多余逗号
        compressed = compressed.replaceAll(",{2,}", ",");
        compressed = compressed.replaceAll("^,+|,+$", "");

        // 7. 最终空格压缩
        compressed = compressed.replaceAll("\\s+", " ").trim();

        int originalLength = prompt.length();
        int compressedLength = compressed.length();
        int savedChars = originalLength - compressedLength;
        double compressionRatio = (double) savedChars / originalLength * 100;

        log.debug("激进压缩完成 | 原始: {}字符 -> 压缩: {}字符 | 节省: {}字符 ({}%)",
                originalLength, compressedLength, savedChars, String.format("%.1f", compressionRatio));

        return compressed;
    }
}
