package com.example.badasaza.gohaesungsamodel;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by Badasaza on 2015-12-01.
 */
public class ServerTask{

    private static String lineEnd ="\r\n";
    private static String twoHyphens = "--";
    private static String boundary =  "*****";

    public static DataOutputStream sendImage(String fieldName, String fileDir, DataOutputStream stream) throws IOException{
        File f = new File(fileDir);
        FileInputStream inputStream = new FileInputStream(f);
        byte[] bytes = new byte[(int) f.length()];
        inputStream.read(bytes);
        inputStream.close();
        stream.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\";filename=\"" + f.getName() + "\"" + lineEnd);
        stream.writeBytes("Content-Type: image/jpeg" + lineEnd);
        stream.writeBytes("Content-Length: " + f.length() + lineEnd);
        stream.writeBytes(lineEnd);
        int bufferLength = 1024;
        for (int i = 0; i < bytes.length; i += bufferLength) {
            if (bytes.length - i >= bufferLength) {
                stream.write(bytes, i, bufferLength);
            } else {
                stream.write(bytes, i, bytes.length - i);
            }
        }
        stream.writeBytes(lineEnd);
        stream.writeBytes(twoHyphens + boundary + lineEnd);
        return stream;
    }

    public static DataOutputStream sendData(String fieldName, String value, DataOutputStream stream) throws IOException{
        stream.writeBytes("Content-Disposition: form-data; name=\""+ fieldName +"\"" + lineEnd);
        stream.writeBytes(lineEnd);
        stream.write(value.getBytes("euc-kr"));
        stream.writeBytes(lineEnd);
        stream.writeBytes(twoHyphens + boundary + lineEnd);
        return stream;
    }

}
