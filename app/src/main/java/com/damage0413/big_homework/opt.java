package com.damage0413.big_homework;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.aip.util.Base64Util;
import com.google.gson.Gson;

import java.awt.font.TextAttribute;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import entivity.Search_result_bean;
import tools.GsonUtils;
import tools.HttpUtil;
import tools.MyHelper;
import tools.toolsUnit;

import static android.media.MediaRecorder.VideoSource.CAMERA;

//签到
public class opt extends AppCompatActivity implements View.OnClickListener {

    private String ImagePath=null;
    private Uri imageUri,imageUri_display;
    private Bitmap bp=null;
    String resule;
    Button btn_pai,btn_xuan;
    TextView tv_sum;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opt);
        Button btn_pai=(Button)findViewById(R.id.take_a_picture);
        Button btn_xuan=(Button)findViewById(R.id.xuan);
        btn_pai.setOnClickListener(this);
        btn_xuan.setOnClickListener(this);
        ImageView iv_picture=(ImageView)findViewById(R.id.picture);
    }

    @RequiresApi(api= Build.VERSION_CODES.M)
    @SuppressLint("NewApi")
    @Override
    public void onClick(View v) {          //点击拍照或者从相册选取，返回值为带地址的intent
        if (v.getId() == R.id.take_a_picture) {   ///拍照
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            builder.detectFileUriExposure();            //7.0拍照必加
            File outputImage = new File(Environment.getExternalStorageDirectory() + File.separator + "face.jpg");     //临时照片存储地
            try {                                                                                   //文件分割符
                if (outputImage.exists()) {   //如果临时地址有照片，先清除
                    outputImage.delete();
                }
                outputImage.createNewFile();    ///创建临时地址
            } catch (IOException e) {
                e.printStackTrace();
            }
            imageUri = Uri.fromFile(outputImage);              //获取Uri

            //   imageUri_display= FileProvider.getUriForFile(opt.this,"com.example.a11630.face_new.fileprovider",outputImage);

            ImagePath = outputImage.getAbsolutePath();
            Log.i("拍照图片路径", ImagePath);         //，是传递你要保存的图片的路径，打开相机后，点击拍照按钮，系统就会根据你提供的地址进行保存图片
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);    //跳转相机
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);                          //相片输出路径
            startActivityForResult(intent, 2);                        //返回照片路径

        } else {
            Intent in = new Intent(Intent.ACTION_PICK);      //选择数据
            in.setType("image/*");                     //选择的数据为图片
            startActivityForResult(in, 1);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 相册选择图片
        if (requestCode == 1) {
            if (data != null) {       //开启了相册，但是没有选照片
                Uri uri = data.getData();
                //从uri获取内容的cursor
                Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                cursor.moveToNext();
                ImagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));   //获得图片的绝对路径
                cursor.close();
                Log.i("图片路径", ImagePath);
                bp = toolsUnit.getimage(ImagePath);
                //  iv_picture.setImageBitmap(bp);
                runthreaad();      //开启线程，传入图片
            }
        } else if (requestCode == 2) {

            bp = toolsUnit.getimage(ImagePath);
            //  iv_picture.setImageBitmap(bp);
            runthreaad();  //开启线程，传入图片
        }
    }

    void runthreaad() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "https://aip.baidubce.com/rest/2.0/face/v3/search";
                try {
                    byte[] bytes1 = toolsUnit.getBytesByBitmap(bp);

                    String image1 = Base64Util.encode(bytes1);

                    Map<String, Object> map = new HashMap<>();
                    map.put("image", image1);
                    map.put("liveness_control", "NORMAL");
                    map.put("group_id_list", "face");
                    map.put("image_type", "BASE64");
                    map.put("quality_control", "LOW");

                    String param = GsonUtils.toJson(map);


                    String clientId = "HuTNjUbYeHuOBibawwwDHnwN";
                    String clientSecret = "c56Xi5qXmzmNcWxiBH6WdObZsbAGoPVp";
                    String accessToken = toolsUnit.getAuth(clientId, clientSecret);

                    String result = HttpUtil.post(url, accessToken, "application/json", param);
                    System.out.println("完成:" + result);


                    Gson gson = new Gson();                      //新建GSON
                    Search_result_bean Result_bean = gson.fromJson(result, Search_result_bean.class); //GSON与我的工具类绑定

                    int Error_code = Result_bean.getError_code();
                    if (Error_code == 0) {                     //返回值为零，就是打卡识别成功

                        double score = Result_bean.getResult().getUser_list().get(0).getScore();   //一层层进入，获取到score

                        String user = Result_bean.getResult().getUser_list().get(0).getUser_id();   //获取用户名

                        if (score >= 78.0) {                                  //分数大于78.0分，判断为同一个人，提示打卡成功

                            SQLiteDatabase db;
                            MyHelper myHelper = new MyHelper(opt.this);
                            db = myHelper.getWritableDatabase();
                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式

                            myHelper.Insert_two(db, "time_id", df.format(new Date()), user);


                            Looper.prepare();
                            Toast.makeText(opt.this, "打卡成功！", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        } else {
                            Looper.prepare();
                            Toast.makeText(opt.this, "打卡失败！照片不在人脸库", Toast.LENGTH_LONG).show();
                            Looper.loop();
                        }
                    } else {
                        String error_message = "打卡失败：" + Result_bean.getError_msg();
                        System.out.println("打卡失败：" + error_message);

                        Looper.prepare();
                        Toast.makeText(opt.this, error_message, Toast.LENGTH_LONG).show();
                        Looper.loop();
                    }

                } catch (Exception e) {
                    Log.i("错误", "错误");
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
