package com.springboot.demo.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class is used to make interaction with cookies simple and easier to use. This class provides the following benefits. <br>
 * <br>
 * <ul>
 * <li>Decodes the cookie names. e.g., user_id
 * <li>Simplified access to cookies by name, instead of looping through an array.
 * <li>Better preformance by saving an instance of the cookie within the request attribute.
 * <li>elper methods for creating, detectnig if a cookie is present and removing cookies (Needs to be improved upon)
 * </ul>
 *
 * User: rbur Date: Dec 10, 2003
 *
 */
public class CookieUtils {
    /**
     * Returns an array containing all of the <code>Cookie</code> object contain in the request, or <code>null</code> if no cookies are available.
     *
     * @param request
     *            Request to retrieve cookies from.
     * @return an array of all <code>Cookie</code>s contained in the request, or <code>null</code> if no cookies are available
     */
    public static Cookie[] getCookies(HttpServletRequest request) {
        return request.getCookies();
    }

    /**
     * Returns value of specified key.
     *
     * @param request
     *            request to retrieve cookie from
     * @param key
     *            key of value to return
     * @return value of specified key
     */
    public static String getCookie(HttpServletRequest request, String key) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return "";
        }

        for (int i = 0; i < cookies.length; i++) {
            try {
                if (URLDecoder.decode(cookies[i].getName(), "UTF-8").equals(key)) {
                    return URLDecoder.decode(cookies[i].getValue(), "UTF-8");
                }
            } catch (UnsupportedEncodingException e) {
            }
        }

        return null;
    }

    /**
     * This method will create a cookie on the client side, with the proper domain.
     *
     * @param request
     *            request used for information used to write cookie
     * @param response
     *            respose to write cookie to
     * @param key
     *            cookie key
     * @param value
     *            cookie value
     * @param expires
     *            expiration in seconds, or -1 for no expiration
     */
    public static void setCookie(HttpServletRequest request, HttpServletResponse response, String key, String value, boolean secureInd, int expires) {
        Cookie cookie;
        String domain;
        int dot;

        try {
            if (value != null) {
                cookie = new Cookie(URLEncoder.encode(key, "UTF-8"), URLEncoder.encode(value, "UTF-8"));
            } else {
                cookie = new Cookie(URLEncoder.encode(key, "UTF-8"), "");
            }
        } catch (UnsupportedEncodingException e) {
            return;
        }

        domain = request.getServerName();
        dot = domain.indexOf(".");
        if (domain.indexOf(".") != -1) {
            cookie.setDomain(domain.substring(dot + 1));
        }

        cookie.setMaxAge(expires);
        cookie.setPath("/");
        cookie.setSecure(secureInd);
        response.addCookie(cookie);
    }

    /**
     * Delete a cookie.
     *
     * @param request
     *            request to use
     * @param response
     *            response to use
     * @param key
     *            key of cookie to delete
     */
    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String key) {
        CookieUtils.setCookie(request, response, key, null, false, 0);
    }

    /**
     * This method checks to see if a cookie value is stored.
     *
     * @param request
     *            request to use
     * @param key
     *            key of cookie to test
     * @return <code>true</code> if there is a value for this field, <code>false</code> otherwise.
     */
    public static boolean exists(HttpServletRequest request, String key) {
        if (getCookie(request, key) != null) {
            return true;
        }

        return false;
    }

    /**
     * Return a <code>String</code> of all existing cookie values.
     *
     * @param request
     *            request to use
     * @return <code>String</code> value of all cookies
     */
    public static String getCookiesString(HttpServletRequest request) {
        StringBuffer output = new StringBuffer();
        Cookie[] cookies;

        if (request == null) {
            return null;
        }

        cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        if (cookies.length == 0) {
            return "";
        }

        for (int i = 0; i < cookies.length; i++) {
            try {
                if (cookies[i].getName() == null) {
                    continue;
                }
                output.append(URLDecoder.decode(cookies[i].getName(), "UTF-8"));
                output.append(cookies[i].getName());
                output.append(": (");

                if (cookies[i].getValue() == null) {
                    output.append("[null]");
                } else {
                    output.append(URLDecoder.decode(cookies[i].getValue(), "UTF-8"));
                    output.append(cookies[i].getValue());
                }
                output.append(")\n");
            } catch (UnsupportedEncodingException uee) {
                continue;
            }
        }

        return output.toString();
    }
}