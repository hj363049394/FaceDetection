package com.haojian.facedetection;

import android.graphics.Bitmap;
import android.util.Log;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

/**
 * Created by haojian12583 on 2016/10/12.
 */

public class FaceDetect {

    public static void detect(final Bitmap bitmap ,final CallBack callBack){

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // request 请求
                    HttpRequests httpRequests = new HttpRequests(Constant.FACE_DETACTION_APPKEY,Constant.FACE_DETACTION_APPKEY_SECRET,true,true);

                    Bitmap bitmapSmall = Bitmap.createBitmap(bitmap,0 , 0 ,bitmap.getWidth(),bitmap.getHeight());
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                    bitmapSmall.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream); // 将bitmap压缩到stram中

                    byte[] arrays = byteArrayOutputStream.toByteArray();

                    PostParameters postParameters = new PostParameters();
                    postParameters.setImg(arrays);

                    JSONObject jsonObject = httpRequests.detectionDetect(postParameters);

                    Log.d("TAG",jsonObject.toString());

                    if (callBack != null) {
                        callBack.success(jsonObject);
                    }

                } catch (FaceppParseException e) {
                    e.printStackTrace();
                    if (callBack != null) {
                        callBack.error(e);
                    }
                }


            }
        }).start();


    }

}
