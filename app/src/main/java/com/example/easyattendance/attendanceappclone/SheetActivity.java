package com.example.easyattendance.attendanceappclone;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import com.example.android.EasyAttendance.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.Calendar;
public class SheetActivity extends AppCompatActivity {
    StorageReference reference;
    File filePath;
    String fileName;
    private Uri uriFile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sheet);


        long[] idArray  = getIntent().getLongArrayExtra("idArray");
        int[] rollArray = getIntent().getIntArrayExtra("rollArray");
        String[] nameArray = getIntent().getStringArrayExtra("nameArray");

        reference = FirebaseStorage.getInstance().getReference();

        setToolbar(idArray,rollArray, nameArray);
        showTable();

    }

    private void setToolbar(long[] idArray, int[] rollArray, String[] nameArray) {

        ImageButton back=findViewById(R.id.back);
        back.setOnClickListener(v->onBackPressed());
        TextView title=findViewById(R.id.title_toolbar);
        TextView subtitle=findViewById(R.id.subtitle_toolbar);
        String month=getIntent().getStringExtra("month");

        ImageButton save = findViewById(R.id.save);
        save.setImageResource(R.drawable.icon_save_att);
        Intent ii  = getIntent();
        String className = ii.getStringExtra("className");
        String subjectName = ii.getStringExtra("subjectName");
        title.setText(className+" | "+subjectName);
        subtitle.setText(month);
        back.setOnClickListener(view -> onBackPressed());
        save.setOnClickListener(v ->  createExcelSheet(idArray,rollArray, nameArray,month));
    }



    private void showTable() {
        DBHelper dbHelper=new DBHelper(this);
        TableLayout tableLayout=findViewById(R.id.table_layout);
        long[] idArray=getIntent().getLongArrayExtra("idArray");
        int[] rollArray=getIntent().getIntArrayExtra("rollArray");
        String[] nameArray=getIntent().getStringArrayExtra("nameArray");
        String month=getIntent().getStringExtra("month");
        int DAY_IN_MONTH=getDayInMonth(month);

        //row setup
        int rowSize=idArray.length + 1;
        TableRow[] rows=new TableRow[rowSize];
        TextView[] roll_tvs=new TextView[rowSize];
        TextView[] name_tvs=new TextView[rowSize];
        TextView[][] status_tvs=new TextView[rowSize][DAY_IN_MONTH +1];

        for (int i =0;i<rowSize;i++){
            roll_tvs[i]=new TextView(this);
            name_tvs[i]=new TextView(this);
            for (int j=1;j<=DAY_IN_MONTH;j++){
                status_tvs[i][j]=new TextView(this);
            }
        }

        //header
        roll_tvs[0].setText("Roll");
        roll_tvs[0].setTypeface(roll_tvs[0].getTypeface(), Typeface.BOLD);
        roll_tvs[0].setTextColor(Color.parseColor("#000000"));
        name_tvs[0].setText("Name");
        name_tvs[0].setTypeface(name_tvs[0].getTypeface(), Typeface.BOLD);
        name_tvs[0].setTextColor(Color.parseColor("#000000"));
        for (int i=1;i<=DAY_IN_MONTH;i++){
            status_tvs[0][i].setText(String.valueOf(i));
            status_tvs[0][i].setTypeface(status_tvs[0][i].getTypeface(), Typeface.BOLD);
           status_tvs[0][i].setTextColor(Color.parseColor("#000000"));
        }

        for (int i=1;i<rowSize;i++){
            roll_tvs[i].setText(String.valueOf(rollArray[i-1]));
            name_tvs[i].setText(nameArray[i-1]);
            roll_tvs[i].setTextColor(Color.parseColor("#000000"));
            name_tvs[i].setTextColor(Color.parseColor("#000000"));
            for (int j=1;j<=DAY_IN_MONTH;j++){
                String day=String .valueOf(j);
                if (day.length()==1){
                    day= "0"+day;
                }
                String date=day+"."+month;
                String status=dbHelper.getStatus(idArray[i-1],date);
                status_tvs[i][j].setText(status);
                status_tvs[i][j].setTextColor(Color.parseColor("#000000"));
            }
        }

        for (int i=0;i<rowSize;i++){
            rows[i]=new TableRow(this);

            if (i%2==0)
                rows[i].setBackgroundColor(Color.parseColor("#EEEEEE"));

            else rows[i].setBackgroundColor(Color.parseColor("#E4E4E4"));

            roll_tvs[i].setPadding(16,16,16,16);
            name_tvs[i].setPadding(16,16,16,16);

            rows[i].addView(roll_tvs[i]);
            rows[i].addView(name_tvs[i]);

            for (int j=1;j<=DAY_IN_MONTH;j++){

                status_tvs[i][j].setPadding(16,16,16,16);
                rows[i].addView(status_tvs[i][j]);
            }

            tableLayout.addView(rows[i]);
        }
        tableLayout.setShowDividers(TableLayout.SHOW_DIVIDER_MIDDLE);

    }

    private int getDayInMonth(String month) {
        int monthIndex=Integer.parseInt(month.substring(0,2))-1;
        int year= Integer.parseInt(month.substring(3));

        Calendar calendar=Calendar.getInstance();
        calendar.set(Calendar.MONTH,monthIndex);
        calendar.set(Calendar.YEAR,year);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }
    private void createExcelSheet(long[] idArray, int[] rollArray, String[] nameArray, String month) {


        DBHelper dbHelper = new DBHelper(this);

        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        HSSFSheet hssfSheet = hssfWorkbook.createSheet("Custom Sheet");
        HSSFRow rollRow = hssfSheet.createRow(0);
        HSSFCell rollCell = rollRow.createCell(0);
        rollCell.setCellValue("ROLL");
        HSSFCell nameCell = rollRow.createCell(1);
        nameCell.setCellValue("NAME");

        int DAY_IN_MONTH = getDayInMonth(month);

        int rowSize = idArray.length + 1;

        for(int i = 1; i <= DAY_IN_MONTH; i++){
            HSSFCell dayCell = rollRow.createCell(i+1);
            dayCell.setCellValue(i);

        }
        for(int i =1; i<rowSize; i++){

            HSSFRow rollCol = hssfSheet.createRow(i);
            HSSFCell rollsCell = rollCol.createCell(0);
            HSSFCell namesCell= rollCol.createCell(1);
            rollsCell.setCellValue(rollArray[i-1]);
            namesCell.setCellValue(nameArray[i-1]);
            for(int s =1; s<=DAY_IN_MONTH;s++){
                String day = String.valueOf(s);
                if(day.length()==1) day ="0"+day;

                String date = day+"."+month;
                String status = dbHelper.getStatus(idArray[i-1],date);
                HSSFCell statusCell = rollCol.createCell(s+1);
                statusCell.setCellValue(status);
            }
        }



        String className = getIntent().getStringExtra("className");
        String subjectName = getIntent().getStringExtra("subjectName");

        filePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) , "/" + className + "_" + subjectName +"_"+ month + ".xls");

        uriFile  = Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) , "/" + className + "_" + subjectName +"_"+ month + ".xls"));
        fileName = className + "_" + subjectName +"_"+ month + ".xls";


        FileOutputStream fileOutputStream=null;
        try {
            if (!filePath.exists()){
                filePath.createNewFile();
            }

            fileOutputStream= new FileOutputStream(filePath);
            hssfWorkbook.write(fileOutputStream);

            if (fileOutputStream!=null){
                fileOutputStream.flush();
                fileOutputStream.close();
            }
            Toast.makeText(this, "Downloading File...", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error occurred", Toast.LENGTH_SHORT).show();
        }


    }
//    private void uploadExcelFile(){
//        if(uriFile != null){
//            ProgressDialog progressDialog = new ProgressDialog(this);
//            progressDialog.setMessage("Uploading...");
//            StorageReference fireBaseStr = reference.child("Attendance/" + fileName);
//            UploadTask uploadTask = fireBaseStr.putFile(uriFile);
//
//            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    progressDialog.dismiss();
//                    Toast.makeText(SheetActivity.this, "File Uploaded", Toast.LENGTH_SHORT).show();
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    progressDialog.dismiss();
//                    Toast.makeText(SheetActivity.this, "Operation Failed", Toast.LENGTH_SHORT).show();
//
//                }
//            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
//                    double progress = 100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount();
//                    progressDialog.setMessage((int) progress+"% uploaded");
//
//                }
//            });
//        }else{
//            Toast.makeText(this, "File Name Error", Toast.LENGTH_SHORT).show();
//        }
//
//    }
}