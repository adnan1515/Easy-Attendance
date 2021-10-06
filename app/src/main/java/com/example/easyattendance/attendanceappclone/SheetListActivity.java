package com.example.easyattendance.attendanceappclone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.EasyAttendance.R;

import java.util.ArrayList;

public class SheetListActivity extends AppCompatActivity {
    private ListView sheetList;
    private ArrayAdapter adapter;
    private ArrayList<String> listItems=new ArrayList();
    private long cid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sheet_list);
        setToolbar();
        cid=getIntent().getLongExtra("cid",-1);
        loadListItems();
        sheetList=findViewById(R.id.sheetList);
        adapter=new ArrayAdapter(this,R.layout.sheet_list,R.id.date_list_item,listItems);
        sheetList.setAdapter(adapter);

        sheetList.setOnItemClickListener((parent, view, position, id) -> openSheetActivity(position));
    }

    private void setToolbar() {
        ImageButton back=findViewById(R.id.back);
        ImageButton save=findViewById(R.id.save);
        TextView title=findViewById(R.id.title_toolbar);
        back.setOnClickListener(view -> onBackPressed());
        save.setVisibility(View.GONE);
        title.setText("Month Divided Attendance");
        TextView subtitle = findViewById(R.id.subtitle_toolbar);
        subtitle.setVisibility(View.GONE);
    }

    private void openSheetActivity(int position) {
        long[] idArray=getIntent().getLongArrayExtra("idArray");
        int[] rollArray=getIntent().getIntArrayExtra("rollArray");
        String[] nameArray=getIntent().getStringArrayExtra("nameArray");
        Intent ii =new Intent(this,SheetActivity.class);
        ii.putExtra("idArray",idArray);
        ii.putExtra("rollArray",rollArray);
        ii.putExtra("nameArray",nameArray);
        ii.putExtra("month",listItems.get(position));
        Intent iii=getIntent();
        String className = iii.getStringExtra("className");
        String subjectName = iii.getStringExtra("subjectName");

        ii.putExtra("className",className);
        ii.putExtra("subjectName",subjectName);

        startActivity(ii);
    }

    private void loadListItems() {
        Cursor cursor=new DBHelper(this).getDistinctMonths(cid);

        while (cursor.moveToNext()){
            String date=cursor.getString(cursor.getColumnIndex(DBHelper.DATE_KEY));//01.04.2021
            listItems.add(date.substring(3));
        }
    }
}