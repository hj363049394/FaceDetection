package com.haojian.facedetection;

import com.facepp.error.FaceppParseException;

import org.json.JSONObject;

/**
 * Created by haojian12583 on 2016/10/12.
 */

public interface CallBack {

    void success (JSONObject result);

    void error(FaceppParseException exception);

}
