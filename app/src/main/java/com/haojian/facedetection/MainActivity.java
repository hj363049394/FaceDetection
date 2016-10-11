package com.haojian.facedetection;

import android.app.VoiceInteractor;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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

    private static final int PICK_CODE = 0X110;

    private String mCurrentPhotoStr ;
    private Bitmap mPhote;

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
                intent.setType("iamge/*");
                startActivityForResult(intent,PICK_CODE);

                break;
            case R.id.detect_btn:

                break;
        }
    }
}
