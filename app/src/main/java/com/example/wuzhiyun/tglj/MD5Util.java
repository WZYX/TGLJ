package com.example.wuzhiyun.tglj;

import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5加密类
 * 
 */
public class MD5Util
{

    /**
     * md5加密不必变字符编码
     * 
     * @param inStr
     * @return
     * @throws Exception
     */
    public static String MD5(String inStr)
    {
        StringBuffer sb = new StringBuffer();
        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(inStr.getBytes());
            byte b[] = md.digest();
            int i;
            for (int offset = 0; offset < b.length; offset++)
            {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    sb.append("0");
                sb.append(Integer.toHexString(i));
            }
        }
        catch (Exception e)
        {
            return null;
            // e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * md5加密不必变字符编码
     * 
     * @param inStr
     * @return
     * @throws Exception
     */
    public static byte[] getByteMD5(String inStr)
    {
        try
        {
            if (TextUtils.isEmpty(inStr))
            {
                return null;
            }
            MessageDigest md = MessageDigest.getInstance("MD5");
            if (md == null)
            {
                return null;
            }
            md.update(inStr.getBytes("UTF-8"));
            byte b[] = md.digest();
            return b;
        }
        catch (Exception e)
        {
            return null;
        }
    }
    
    /**
     * 对带有中文字符的字符串进行MD5处理
     * 
     * @param source
     * @return
     */
    public final static String md5(String source)
    {
        String dest = null;
        try
        {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            try
            {
                md5.update(source.getBytes("UTF-8"));
            }
            catch (UnsupportedEncodingException e)
            {
                md5.update(source.getBytes());
            }

            byte[] md5Bytes = md5.digest();
            StringBuffer hexValue = new StringBuffer();
            for (int i = 0; i < md5Bytes.length; i++)
            {
                int val = (md5Bytes[i]) & 0xff;
                if (val < 16)
                    hexValue.append("0");
                hexValue.append(Integer.toHexString(val));
            }
            dest = hexValue.toString();

        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        return dest;
    }

    /**
     * 获取文件的MD5值
     * 
     * @param file
     * @return
     */
    public static String getFileMD5(File file)
    {
        if (!file.isFile() || !file.exists())
        {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try
        {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1)
            {
                digest.update(buffer, 0, len);
            }
            in.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

        return byteArrayToHex(digest.digest());
        // BigInteger bigInt = new BigInteger(1, digest.digest());
        // return bigInt.toString(16);
    }

    /**
     * 获取文件的MD5值
     * 
     * @param file
     * @return
     */
    public static byte[] getByteArrayFromFileMD5(File file)
    {
        if (!file.isFile() || !file.exists())
        {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try
        {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1)
            {
                digest.update(buffer, 0, len);
            }
            in.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

        return digest.digest();
    }

    public static String byteArrayToHex(byte[] byteArray)
    {
        char[] hexDigits =
        { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        char[] resultCharArray = new char[byteArray.length * 2];
        int index = 0;
        for (byte b : byteArray)
        {
            resultCharArray[index++] = hexDigits[b >>> 4 & 0xf];
            resultCharArray[index++] = hexDigits[b & 0xf];
        }
        return new String(resultCharArray);

    }

    public static String getByteArrayMd5(byte[] buffer)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(buffer, 0, buffer.length);
            return byteArrayToHex(digest.digest());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

}
