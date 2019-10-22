package com.changgou.filter;

/**
 * @Author Ayden
 * @Date 2019/9/2 22:53
 * @Description
 * @Version
 **/
public class UrlFilter {
    public static final String urls = "/api/user/login,/api/user/add";

    public static boolean hasAuthority(String requestUrl) {
        String[] url = urls.split(",");
        for (String s : url) {
            if (s.equals(requestUrl)) {
                return true;
            }
        }
        return false;
    }
}
