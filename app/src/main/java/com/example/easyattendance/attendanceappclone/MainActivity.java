package com.example.easyattendance.attendanceappclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import com.example.android.EasyAttendance.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ClassAdapter classAdapter;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<ClassItem> classItems=new ArrayList<>();
    Toolbar toolbar;
    FloatingActionButton fab;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
        dbHelper=new DBHelper(this);
        fab=findViewById(R.id.fab_main);
        fab.setOnClickListener(v-> showDialog());
        loadData();
        recyclerView = findViewById(R.id.recycleView);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        classAdapter=new ClassAdapter(this,classItems);
        recyclerView.setAdapter(classAdapter);
        classAdapter.setOnItemClickListener(position -> gotoItemActivity(position));
        setToolbar();
    }

    private void loadData() {
        Cursor cursor= dbHelper.getClassTable();
        classItems.clear();
        while(cursor.moveToNext()){
            int id=cursor.getInt(cursor.getColumnIndex(DBHelper.C_ID));
            String className=cursor.getString(cursor.getColumnIndex(DBHelper.CLASS_NAME_KEY));
            String subjectName=cursor.getString(cursor.getColumnIndex(DBHelper.SUBJECT_NAME_KEY));

            classItems.add(new ClassItem(id,className,subjectName));
        }
    }
    private void setToolbar() {
        toolbar=findViewById(R.id.toolbar);
        TextView title=toolbar.findViewById(R.id.title_toolbar);
        TextView subtitle=toolbar.findViewById(R.id.subtitle_toolbar);
        ImageButton back=toolbar.findViewById(R.id.back);
        ImageButton save=toolbar.findViewById(R.id.save);

        title.setText("Attendance App");
        subtitle.setVisibility(View.GONE);
        back.setVisibility(View.INVISIBLE);
        save.setVisibility(View.INVISIBLE);
    }

    private void gotoItemActivity(int position) {

        Intent ii=new Intent(getApplicationContext(),StudentActivity.class);

        ii.putExtra("className",classItems.get(position).getClassName());
        ii.putExtra("subjectName",classItems.get(position).getSubjectName());
        ii.putExtra("position",position);
        ii.putExtra("cid",classItems.get(position).getCid());
        startActivity(ii);
    }

    private void showDialog(){

        MyDialog dialog=new MyDialog();
        dialog.show(getSupportFragmentManager(),MyDialog.CLASS_ADD_DIALOG);

        dialog.setListener((className,subjectName)->addClass(className,subjectName));


    }

    private void addClass(String className,String subjectName) {

        long cid = dbHelper.addClass(className,subjectName);

        ClassItem classItem= new ClassItem(cid,className,subjectName);
        classItems.add(classItem);
        classAdapter.notifyDataSetChanged();

    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case 0:
                deleteClass(item.getGroupId());
                break;
            case 1:
                showUpdateDialog(item.getGroupId());

        }
        return super.onContextItemSelected(item);
    }

    private void showUpdateDialog(int position) {
        MyDialog dialog=new MyDialog();
        dialog.show(getSupportFragmentManager(),MyDialog.CLASS_UPDATE_DIALOG);
        dialog.setListener((className,subjectName)->updateClass(position,className,subjectName));
    }

    private void updateClass(int position, String className, String subjectName) {
        dbHelper.updateClass(classItems.get(position).getCid(),className,subjectName);
        classItems.get(position).setClassName(className);
        classItems.get(position).setSubjectName(subjectName);
        classAdapter.notifyItemChanged(position);
    }

    private void deleteClass(int position) {
        dbHelper.deleteClass(classItems.get(position).getCid());
        classItems.remove(position);
        classAdapter.notifyItemRemoved(position);
    }
}