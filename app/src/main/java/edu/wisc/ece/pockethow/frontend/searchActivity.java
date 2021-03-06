package edu.wisc.ece.pockethow.frontend;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import edu.wisc.ece.pockethow.R;
import edu.wisc.ece.pockethow.dbOperations.DbOperations;
import edu.wisc.ece.pockethow.entity.PHCategory;
import edu.wisc.ece.pockethow.httpRequests.PHWikihowFetches;

public class searchActivity extends AppCompatActivity {
    Button button;
    EditText searchEditText;
    TextView loadingTextView;
    ImageButton imageButton;
    Button deleteButton;
    static final String codeword = "catagory";
    static final String categoryIntIdCodeword = "categoryIntId";
    static final String filenameCodeword = "filename";
    static final String downloadedParentPath = "/storage/emulated/0/Download/";
    static final String downloadDatabase = "downloadDatabase";
    static final String databaseFromServer = "from server";
    //int categoryIdGlobal;
    ArrayList<Integer> categoryIdList = new ArrayList<>();
    ArrayList<String> categoryArrayList = new ArrayList<>();
    ArrayList<String> downloadedFilePathList = new ArrayList<>();
    final DbOperations dbOperations = new DbOperations(this);
    private int requestCode;
    private int grantResults[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.action_bar)));

        dbOperations.open();
        dbOperations.close();
        button = (Button) findViewById(R.id.main_search_btn);
        searchEditText = (EditText) findViewById(R.id.main_search_bar);
        loadingTextView = (TextView) findViewById(R.id.textViewLoading);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingTextView.setVisibility(View.VISIBLE);
                Context context = searchActivity.this;
                File dbFile = context.getDatabasePath("PocketHow.db");
                if (dbFile.exists()) {
                    Intent intent = new Intent(searchActivity.this, PageListActivity.class);
                    Log.d("searchActivity", searchEditText.getText().toString());
                /*
                TODO: searching door should allow for "Indoor" and "Door", but it only returns "Indoor"
                high priority
                 */
                /*
                TODO: "heal" turns into "deal" instead of "health"
                high priority
                 */
                /*
                TODO: prioritize articles that have more matched items
                low priority
                For example, "nut health" should have the "nut health" article on top instead of "health..." and then "nut health"
                 */
                    String inputString = "";
                    //intent.putExtra("message", dbOperations.getClosestSearchWord(searchEditText.getText().toString()));
                    String originalString = searchEditText.getText().toString();
                    /*
                    String[] tokenArray = originalString.split(" ");
                    for (int i = 0; i < tokenArray.length; i++) {

                    //check for 's and delete them
                    //for example: nut's becomes nut

                        String tempInput = tokenArray[i];
                        for (int j = 0; j < tempInput.length(); j++) {
                            if (tempInput.charAt(j) == '\'' && (j + 1) < tempInput.length() && tempInput.charAt(j + 1) == 's') {
                                tempInput = tempInput.substring(0, j);
                                j = tempInput.length();
                            }
                        }
                        if(!inputString.equals(""))
                        {
                            inputString += dbOperations.getClosestSearchWord(tempInput) + " ";
                        }

                    }
                    Log.d("searchActivity", "input string = " + inputString);
                    if(inputString.equals(""))
                    {
                        intent.putExtra("message", "");

                    }
                    else
                    {
                        intent.putExtra("message", dbOperations.getClosestSearchWord(inputString));

                    }
                    */
                    intent.putExtra("message", dbOperations.getClosestSearchWord(originalString));
                    dbOperations.open();
                    dbOperations.close();
                    if (dbOperations.isOpen()) {
                        Toast.makeText(searchActivity.this, "Please wait, the database is loading",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Log.d("searchActivity", "Done loading DB");
                        startActivity(intent);
                    }
                    //startActivity(new Intent(searchActivity.this, PageDetailActivity.class));
                } else {
                    Toast.makeText(searchActivity.this, "Database does not exist. Please download a category",
                            Toast.LENGTH_LONG).show();
                }

            }
        });

        imageButton = (ImageButton) findViewById(R.id.imageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               button.performClick();
                                           }
                                       }
        );

        deleteButton = (Button) findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dbOperations.isOpen()) {
                    Toast.makeText(searchActivity.this, "Please wait after downloading the databases or the search is completed", Toast.LENGTH_SHORT).show();
                } else {
                    deleteDatabase("PocketHow.db");
                    CategorySelectionActivity.deleteButtonPressed = true;
                }

            }
        });
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        try {
            if (bundle.getBoolean(databaseFromServer)) {
                //used for downloading from server
                downloadedFilePathList = bundle.getStringArrayList(downloadDatabase);
                populateDB();
            } else {
                //used for scraping WikiHow
                categoryArrayList = bundle.getStringArrayList(codeword);
                categoryIdList = bundle.getIntegerArrayList(categoryIntIdCodeword);
                downloadedFilePathList = bundle.getStringArrayList(filenameCodeword);

                //deleteDatabase("PocketHow.db");
                populateDB();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //categoryIdGlobal = bundle.getInt(categoryIntIdCodeword);
        //deleteDatabase("PocketHow.db");
        //populateDB();
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);


        onRequestPermissionsResult(requestCode, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, grantResults);


        /*
        temporary for sending databases
         */
        Button writeButton = (Button) findViewById(R.id.writeButton);
        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File root = android.os.Environment.getExternalStorageDirectory();
                //File dir = new File (root.getAbsolutePath() + "/download");
                File dir = new File(root.getAbsoluteFile() + "/pockethowdatabases");
                dir.mkdirs();
                File file = new File(dir, "myData.txt");

                try {

                    file.createNewFile();
                    FileOutputStream f = new FileOutputStream(file);
                    PrintWriter pw = new PrintWriter(f);
                    pw.println("Hi , How are you");
                    pw.println("Hello");
                    pw.flush();
                    pw.close();
                    f.close();
                    File dest = new File(dir, "database.db");
                    File source = new File(dbOperations.getDatabase().getPath());
                    InputStream is = null;
                    OutputStream os = null;
                    try {
                        is = new FileInputStream(source);
                        os = new FileOutputStream(dest);
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = is.read(buffer)) > 0) {
                            os.write(buffer, 0, length);
                        }
                    } finally {
                        is.close();
                        os.close();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Log.i("file", "******* File not found. Did you" +
                            " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        try {
            String command = "ping -c 1 google.com";
            boolean isInternetAvailable = (Runtime.getRuntime().exec(command).waitFor() == 0);
            if (!isInternetAvailable) {
                return;
            } else {
                super.onBackPressed();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override // android recommended class to handle permissions
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d("permission", "granted");
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.uujm
                    Toast.makeText(searchActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();

                    //app cannot function without this permission for now so close it...
                    onDestroy();
                }
                return;
            }

            // other 'case' line to check fosr other
            // permissions this app might request
        }
    }

    public void populateDB() {
        //deleteDatabase("PocketHow.db");
        dbOperations.searchWordList.clear();
        new Thread(new Runnable() {
            public void run() {
                if (downloadedFilePathList == null || downloadedFilePathList.size() == 0) {
                    //fetch and parse WikiHow pages
                    final PHWikihowFetches phWikihowFetches = new PHWikihowFetches();

                    dbOperations.open();
                        /*
                        List<String> testIDs = phWikihowFetches.fetchPagesFromCategory(categoryStr, 100);
                        dbOperations.addCategoryToPageID(new PHCategory(2, categoryStr
                                , phWikihowFetches.categoryListToDelimString(testIDs),
                                null));
                        dbOperations.parsePagesAndPopulateDB(phWikihowFetches.getJSONFromURL
                                (phWikihowFetches.getFetchURLFromPageIds
                                        (testIDs)));
                                        */
                    //for (String categoryStr : categoryArrayList) {
                    for (int i = 0; i < categoryArrayList.size(); i++) {
                        String categoryStr = categoryArrayList.get(i);
                        int categoryIdGlobal = categoryIdList.get(i);
                        List<String> testIDs = phWikihowFetches.fetchPagesFromCategory(categoryStr, 100);
                        dbOperations.addCategoryToPageID(new PHCategory(categoryIdGlobal, categoryStr
                                , phWikihowFetches.categoryListToDelimString(testIDs),
                                null));

                        //// supports upto 100 article requests ////
                        if (testIDs.size() > 50) {
                            List<String> temp = new ArrayList<>(testIDs.subList(0, 50));
                            dbOperations.parsePagesAndPopulateDB(phWikihowFetches.getJSONFromURL
                                    (phWikihowFetches.getFetchURLFromPageIds
                                            (temp)));
                            List<String> temp1 = new ArrayList<>(testIDs.subList(50, testIDs.size()));
                            dbOperations.parsePagesAndPopulateDB(phWikihowFetches.getJSONFromURL
                                    (phWikihowFetches.getFetchURLFromPageIds
                                            (temp1)));

                        } else {
                            dbOperations.parsePagesAndPopulateDB(phWikihowFetches.getJSONFromURL
                                    (phWikihowFetches.getFetchURLFromPageIds
                                            (testIDs)));
                        }
                    }
                    dbOperations.populateSearchWordTable();
                    dbOperations.addArticlesToDb();
                    dbOperations.close();
                    categoryArrayList.clear();
                }
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //dbOperations.open();
        loadingTextView.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //dbOperations.close();
    }
}
