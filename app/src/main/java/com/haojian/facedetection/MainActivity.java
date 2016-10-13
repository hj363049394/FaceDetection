package com.haojian.facedetection;

import android.app.VoiceInteractor;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facepp.error.FaceppParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    @InjectView(R.id.detect_btn)
    Button mDetectBtn;
    @InjectView(R.id.get_image_btn)
    Button mGetImgeBtn;
    @InjectView(R.id.lading_pb)
    ProgressBar mProgressBar;
    @InjectView(R.id.photo_iv)
    ImageView mPhotoIv;
    @InjectView(R.id.result_fl)
    FrameLayout mFrameLayout;
    @InjectView(R.id.tip_tv)
    TextView mTipTv;
    @InjectView(R.id.age_gender_tv)
    TextView mAgeGenderTv;
// requestCode
    private static final int PICK_CODE = 0X110;
    private static  final int CALLBACK_SUSS = 1;
    private static final int CALLBACK_ERROR = 2;
// 图片的uri路径
    private String mCurrentPhotoStr ;
    private Bitmap mPhote;
    private Paint mPaint = new Paint();

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){
                case CALLBACK_SUSS:
                    mProgressBar.setVisibility(View.GONE);
                    JSONObject jsonObject = (JSONObject) msg.obj;

                    prepareBitmap(jsonObject);

                    mPhotoIv.setImageBitmap(mPhote);

                    break;
                case CALLBACK_ERROR:
                    mProgressBar.setVisibility(View.GONE);
                    String errorMsg = (String) msg.obj;
                    if (TextUtils.isEmpty(errorMsg)) {
                        mTipTv.setText("Error。。。。");
                    }else {
                        mTipTv.setText(errorMsg.toString());
                    }

                    break;
            }

            super.handleMessage(msg);
        }
    };

    private void prepareBitmap(JSONObject jsonObject) {

        /**
         * {"face":[{"position":{"mouth_right":{"y":84.074583,"x":63.096562},
         * "mouth_left":{"y":85.01,"x":44.56625},"center":{"y":69.583333,"x":53.4375},
         * "height":47.5,"width":35.625,"nose":{"y":72.4475,"x":52.360312},"eye_left":{"y":59.568333,"x":40.924687},
         * "eye_right":{"y":59.24125,"x":64.953125}},"attribute":{"race":{"value":"White","confidence":44.8239},
         * "gender":{"value":"Male","confidence":98.7927},"smiling":{"value":35.4085},"age":{"value":21,"range":5}},
         * "tag":"","face_id":"6430f6d4600e93913a4483c4cc37e08e"}],"session_id":"11c1dc3c77994195bd35b74676ccdbe7",
         * "img_height":240,"img_width":320,"img_id":"5df7cf30677cc1d529491b8354708ab9","url":null,"response_code":200}
         */

        Bitmap bitmap = Bitmap.createBitmap(mPhote.getWidth(),mPhote.getHeight(),mPhote.getConfig());
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(mPhote,0,0,mPaint);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(3);

        try {
            JSONArray faces = jsonObject.getJSONArray("face");
            int facesCount = faces.length();
            mTipTv.setText("find " + facesCount);
            for (int i = 0 ;i < facesCount ;i++){
                JSONObject face = faces.getJSONObject(i);
                JSONObject position = face.getJSONObject("position");
                // 取得center中的x、y
                float x = (float) position.getJSONObject("center").getDouble("x"); // 脸部中心点x点相对图片的 x%
                float y = (float) position.getJSONObject("center").getDouble("y");

                float w = (float) position.getDouble("width"); // 相对百分比
                float h = (float) position.getDouble("height");

                x = x / 100 * bitmap.getWidth();
                y = y / 100 * bitmap.getHeight();

                w = w / 100 * bitmap.getWidth();
                h = h / 100 * bitmap.getHeight();

                // 绘制box
                canvas.drawLine(x - w / 2,y - h / 2,x - w / 2,y + h / 2,mPaint);
                canvas.drawLine(x - w / 2,y - h / 2,x + w / 2,y - h / 2,mPaint);
                canvas.drawLine(x + w / 2,y - h / 2,x + w / 2,y + h / 2,mPaint);
                canvas.drawLine(x - w / 2,y + h / 2,x + w / 2,y + h / 2,mPaint);

                // age gender
                int age = face.getJSONObject("attribute").getJSONObject("age").getInt("value");
                String gender = face.getJSONObject("attribute").getJSONObject("gender").getString("value");

                // 使用TextView方式添加age、gender信息到bitmap上面
                Bitmap ageBitmap = buildAgeBitmap(age,"male".equals(gender));
                // 缩放age的bitmap 否则图片大小不同age的尺寸不变
                if (bitmap.getWidth() < mPhotoIv.getWidth() && bitmap.getHeight() <mPhotoIv.getHeight()){
                    float ratio = Math.max(bitmap.getWidth() * 1.0f / mPhotoIv.getWidth(),bitmap.getHeight() * 1.0f / mPhotoIv.getHeight());
                    ageBitmap = Bitmap.createScaledBitmap(ageBitmap,(int)(ageBitmap.getWidth() * ratio ),(int)(ageBitmap.getHeight() * ratio),true);
                }

                canvas.drawBitmap(ageBitmap,x - ageBitmap.getWidth() / 2 ,y - h / 2 - ageBitmap.getHeight(),null);

                mPhote = bitmap;


            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
// 将textview转换bitmap
    private Bitmap buildAgeBitmap(int age, boolean isMale) {

        mAgeGenderTv.setText(age + "");
        if (isMale) {
            mAgeGenderTv.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.male),null,null,null);
        }else{
            mAgeGenderTv.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.female),null,null,null);
        }
        mAgeGenderTv.setDrawingCacheEnabled(true);

        mAgeGenderTv.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        mAgeGenderTv.layout(0, 0, mAgeGenderTv.getMeasuredWidth(), mAgeGenderTv.getMeasuredHeight());
        mAgeGenderTv.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(mAgeGenderTv.getDrawingCache());
        mAgeGenderTv.destroyDrawingCache();

        return bitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(MainActivity.this);// 表示要对哪个Activity起作用；参数也可以直接为this;
        initEvents();
    }

    private void initEvents() {
        mGetImgeBtn.setOnClickListener(this);
        mDetectBtn.setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == PICK_CODE) {
            if ( intent != null){
                Uri uri = intent.getData();
                Cursor cursor =  getContentResolver().query(uri,null,null,null,null);
                cursor.moveToFirst();

                int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                mCurrentPhotoStr = cursor.getString(index);
                cursor.close();

                reSizePhoto();

                mPhotoIv.setImageBitmap(mPhote);
                mTipTv.setText("Click Detect---->");


            }
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }
//压缩照片
    private void reSizePhoto() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
// 获取路径的照片
        BitmapFactory.decodeFile(mCurrentPhotoStr, options);
//宽高不超过1024  每张图片不能超过3M
        double radio = Math.max(options.outWidth * 1.0d /1024,options.outHeight * 0.1d / 1024);

        options.inSampleSize = (int) Math.ceil(radio);
        options.inJustDecodeBounds = false;
//得到压缩后的图片
        mPhote = BitmapFactory.decodeFile(mCurrentPhotoStr,options);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.get_image_btn:

                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,PICK_CODE);

                break;
            case R.id.detect_btn:

                if (mCurrentPhotoStr != null && mCurrentPhotoStr.trim().equals("")){

// 该回掉是在Thread的子线程中执行，该回调的success（绘制解析后的信息）、error（在TextView上显示）方法执行需要在UI线程，
// 故需要使用Handler发送消息到UI线程执行操作
                    FaceDetect.detect(mPhote, new CallBack() {
                        @Override
                        public void success(JSONObject result) {
                            Message message = new Message();
                            message.what = CALLBACK_SUSS;
                            message.obj = result;
                            mHandler.sendMessage(message);
                        }

                        @Override
                        public void error(FaceppParseException exception) {
                            Message message = new Message();
                            message.what = CALLBACK_ERROR;
                            message.obj = exception;
                            mHandler.sendMessage(message);
                        }
                    });
                }else {
                    Toast.makeText(MainActivity.this,"Please click 'Get Image' button to choose a photo!",Toast.LENGTH_LONG).show();
                }



                break;
        }
    }
}
