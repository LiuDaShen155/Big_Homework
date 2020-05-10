package com.damage0413.big_homework;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

import tools.MyHelper;

//搜索签到记录
public class searcher extends AppCompatActivity implements View.OnClickListener {

    TextView search_sum;
    EditText IDS;
    Button btn_search,btn_search_TI,btn_choosetime,btn_confirm;
    int flag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searcher);
        btn_search=(Button)findViewById(R.id.search_NI);
        btn_search.setOnClickListener(this);
        btn_search_TI=(Button)findViewById(R.id.search_ka);
        btn_search_TI.setOnClickListener(this);
        btn_choosetime = (Button) findViewById(R.id.time);
        btn_choosetime.setOnClickListener(this);
        btn_confirm=(Button)findViewById(R.id.FINALLY);
        btn_confirm.setOnClickListener(this);

        IDS=(EditText)findViewById(R.id.ids);
        search_sum=(TextView)findViewById(R.id.tv_sum);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.search_ka) {
            StringBuffer sum = new StringBuffer();
            SQLiteDatabase db;
            MyHelper myHelper = new MyHelper(searcher.this);
            db = myHelper.getWritableDatabase();
            Cursor cursor = db.query("time_id", null,
                    null, null, null, null, null);
            if (cursor.getCount() != 0) {
                cursor.moveToFirst();
                String id = cursor.getString(0);
                String name_time = cursor.getString(1);

                System.out.println("查询结果：\n");
                System.out.println(id + ":" + name_time);
                sum.append("查询结果：" + "\n    ID:" + id + "        time:" + name_time + "\n");

                while (cursor.moveToNext()) {
                    String id1 = cursor.getString(0);
                    String name_time1 = cursor.getString(1);
                    System.out.println("查询结果：");
                    System.out.println(id1 + ":" + name_time1);
                    sum.append("    ID:" + id1 + "        time:" + name_time1 + "\n");
                }
            }
            cursor.close();
            db.close();
            search_sum.setText(sum.toString());

        } else if (v.getId() == R.id.search_NI) {

            StringBuffer sum = new StringBuffer();
            SQLiteDatabase db;
            MyHelper ggg = new MyHelper(searcher.this);
            db = ggg.getWritableDatabase();
            Cursor cursor = db.query("name_id", null,
                    null, null, null, null, null);
            if (cursor.getCount() != 0) {
                cursor.moveToFirst();
                String id = cursor.getString(0);
                String name_time = cursor.getString(1);
                System.out.println("查询结果：");
                System.out.println(id + ":" + name_time);
                sum.append("查询结果：" + "\n    ID:" + id + "        name:" + name_time + "\n");

                while (cursor.moveToNext()) {
                    String id1 = cursor.getString(0);
                    String name_time1 = cursor.getString(1);
                    System.out.println("查询结果：");
                    System.out.println(id1 + ":" + name_time1);
                    sum.append("    ID:" + id1 + "        name:" + name_time1 + "\n");
                }
            }
            cursor.close();
            db.close();
            search_sum.setText(sum.toString());
        } else if (v.getId() == R.id.time) {
            final Calendar c = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(searcher.this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    c.set(year, monthOfYear, dayOfMonth);
                    btn_choosetime.setText(DateFormat.format("yyy-MM-dd", c));
                }
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            dialog.show();
            flag = 1;

        } else if (v.getId() == R.id.FINALLY) {
            if (flag == 1) {    //选了日期
                if (IDS.getText().toString().trim().equals("")) {      //只选了日期
                    String TIMES = btn_choosetime.getText().toString().trim();   //日期在此.
                    System.out.println("截取时间在此：" + TIMES);

                    StringBuffer sum = new StringBuffer();
                    SQLiteDatabase db;
                    MyHelper ggg = new MyHelper(searcher.this);
                    db = ggg.getWritableDatabase();
                    Cursor cursor = db.query("time_id", null,
                            null, null, null, null, null);

                    sum.append("结果如下：");
                    if (cursor.getCount() != 0) {
                        cursor.moveToFirst();
                        String id = cursor.getString(0);
                        String name_time = cursor.getString(1);

                        if (TIMES.substring(0, TIMES.length()).equals
                                (name_time.substring(0, TIMES.length()))) {    //截取前面的部分比较。如2018-12-31
                            System.out.println("查询结果：\n");
                            System.out.println(id + ":" + name_time);
                            sum.append("\n    ID:" + id + "        time:" + name_time + "\n");
                        }

                        while (cursor.moveToNext()) {
                            String id1 = cursor.getString(0);
                            String name_time1 = cursor.getString(1);

                            if (TIMES.substring(0, TIMES.length()).equals
                                    (name_time1.substring(0, TIMES.length()))) {
                                System.out.println(id + ":" + name_time);
                                sum.append("    ID:" + id1 + "        time:" + name_time1 + "\n");
                            }
                        }
                    }
                    cursor.close();
                    db.close();
                    search_sum.setText(sum.toString());
                } else {    //选了日期，选了ID

                    String id = IDS.getText().toString().trim();
                    String TIMES = btn_choosetime.getText().toString().trim();   //日期在此.
                    SQLiteDatabase db;
                    MyHelper ggg = new MyHelper(searcher.this);
                    db = ggg.getWritableDatabase();

                    StringBuffer sum = new StringBuffer();

                    String sql = "select * from time_id where id=\"" + id + "\"";

                    System.out.println("差部分：" + sql);

                    Cursor cursor = db.rawQuery(sql, null);
                    while (cursor.moveToNext()) {
                        String ID = cursor.getString(0); //获取第一列的值,第一列的索引从0开始
                        String TIME = cursor.getString(1);//获取第二列的值

                        if(TIMES.equals(TIME.substring(0,TIMES.length()))){
                            sum.append("    ID:" + ID + "        time:" + TIME + "\n");
                        }
                    }
                    cursor.close();
                    db.close();
                    search_sum.setText(sum.toString());



                }
            } else {     //flag=0；  //没选日期
                if (IDS.getText().toString().trim().equals("")) {    //啥都没选
                    Toast.makeText(this, "什么都没有选择！", Toast.LENGTH_SHORT).show();
                } else {       //只选了ID
                    String id = IDS.getText().toString().trim();    //获取id

                    SQLiteDatabase db;
                    MyHelper ggg = new MyHelper(searcher.this);
                    db = ggg.getWritableDatabase();

                    StringBuffer sum = new StringBuffer();

                    String sql = "select * from time_id where id=\"" + id + "\"";
                    // System.out.println("差部分：" + sql);
                    Cursor cursor = db.rawQuery(sql, null);
                    while (cursor.moveToNext()) {
                        String ID = cursor.getString(0); //获取第一列的值,第一列的索引从0开始
                        String TIME = cursor.getString(1);//获取第二列的值
                        sum.append("    ID:" + ID + "        time:" + TIME + "\n");
                    }
                    cursor.close();
                    db.close();
                    search_sum.setText(sum.toString());
                }
            }
        }
    }
}
