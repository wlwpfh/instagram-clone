package com.godwpfh.instagram;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class Base64Util {
    public static String encode(String text) throws UnsupportedEncodingException{
        byte[] data=text.getBytes("UTF-8");
        return Base64.encodeToString(data,Base64.DEFAULT);
    }

    public static  String encode(byte[] digest){
        return Base64.encodeToString(digest,Base64.DEFAULT);
    }

    public static String decode(String text) throws UnsupportedEncodingException{
        return new String(Base64.decode(text,Base64.DEFAULT),"UTF-8");
    }

    public static String getURLEncode(String content){
        try{
            return URLEncoder.encode(content,"UTF-8");
        }catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }
        return null;
    }

    public static String getURLDecode(String content){
        try{
            return URLDecoder.decode(content,"UTF-8");
        }catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }
        return null;
    }
}
