package common.util;


import common.enumeration.ErrorType;
import common.exception.RpcException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lcf
 */
public class PlaceHolderUtil {
    /**
     * 我们要匹配"messge : {}dwqdwq {}"中的多个{}
     * (.*?)表示匹配任意字符段
     *
     */
    private static final Pattern REGEX = Pattern.compile("\\{(.*?)}");

    /**
     * 替换占位符
     * @param template 模板
     * @param args 多个参数
     * @return 替换后字符串
     */
    public static String replace(String template, Object... args) {
        Matcher matcher = REGEX.matcher(template);
        StringBuffer sb = new StringBuffer();

        int idx = 0;
        while (matcher.find()) {
            String placeHolder = matcher.group(1);
            // 如果参数个数小于占位符个数
            if (idx >= args.length) {
                throw new RpcException(ErrorType.PLACEHOLDER_ERROR, "占位符替换出错: {}", placeHolder);
            }

            Object obj = args[idx++];
            if (obj == null) {
                matcher.appendReplacement(sb, "");
            } else {
                matcher.appendReplacement(sb, obj.toString());
            }
        }

        matcher.appendTail(sb);
        return sb.toString();
    }
}
