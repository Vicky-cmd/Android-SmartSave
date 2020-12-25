package com.infotrends.in.smartsave;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.infotrends.in.smartsave.database.DBHelper;
import com.infotrends.in.smartsave.interaces.DrawerInterface;
import com.infotrends.in.smartsave.interaces.IOnBackPressed;
import com.infotrends.in.smartsave.models.FileModel;
import com.infotrends.in.smartsave.models.UserModel;
import com.infotrends.in.smartsave.orchestrator.LoginOrc;
import com.infotrends.in.smartsave.orchestrator.OTPValidator;
import com.infotrends.in.smartsave.orchestrator.SendOrc;
import com.infotrends.in.smartsave.services.DownloadFile;
import com.infotrends.in.smartsave.services.UploadFile;
import com.infotrends.in.smartsave.ui.home.HomeFragment;
import com.infotrends.in.smartsave.utils.AppProps;
import com.infotrends.in.smartsave.utils.CustomExceptionHandler;
import com.infotrends.in.smartsave.utils.FilePicker;
import com.infotrends.in.smartsave.utils.IntegProperties;
import com.infotrends.in.smartsave.utils.LoadingBox;
import com.infotrends.in.smartsave.utils.LoggerFile;
import com.infotrends.in.smartsave.utils.MySuggestionProvider;
import com.infotrends.in.smartsave.utils.NotificationMan;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity implements DrawerInterface {

    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;
    private NavHostFragment navHostFragment;
    private SQLiteDatabase mDatabase;
    private DrawerLayout drawer;
    private LoginOrc loginOrc = new LoginOrc();
    private TextView navEmail, navUsrname, navLoginTm;
    private UserModel userModel;
    private Handler handler = new Handler();
    SharedPreferences sharedPref;
    private OTPValidator otpValidator;
    private LoggerFile ologger = LoggerFile.createInstance(MainActivity.class);
    private static IntegProperties oInteg = IntegProperties.getInstance();
    private static String rootMediaDir = Environment.getExternalStorageDirectory()
            .getAbsolutePath()  +  oInteg.getString("app_base_dir") + oInteg.getString("app_download_media_dir");

    public static int PERMISSION_CODE_READ_FILES = 100;
    public static int PERMISSION_CODE_WRITE_FILES = 101;
    public static int PERMISSION_CODE_INTERNET = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        handleIntent(getIntent());


        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.send_fragment, R.id.nav_share)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        DBHelper dbHelper = DBHelper.ctreateInstance(this);
        mDatabase = dbHelper.getWritableDatabase();
        AppProps.setProperties(this, this, navController);
        loginOrc = new LoginOrc();
        otpValidator = new OTPValidator();

        sharedPref = this.getPreferences(Context.MODE_PRIVATE);

        View headerView = navigationView.getHeaderView(0);
        navUsrname = headerView.findViewById(R.id.nav_username);
        navEmail = headerView.findViewById(R.id.nav_email);
        navLoginTm = headerView.findViewById(R.id.nav_loginTime);

        if(savedInstanceState==null && !loginOrc.checkSignIn(this,mDatabase)) {

            String actionType = sharedPref.getString(getString(R.string.saved_ActionType), "0");
            if(actionType!=null && actionType.equalsIgnoreCase("2")) {
                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        userModel = new UserModel();
                        String phNo = sharedPref.getString(getString(R.string.saved_signup_phNo), "");
                        String usrnmae = sharedPref.getString(getString(R.string.saved_signup_usrname), "");
                        String pwd = sharedPref.getString(getString(R.string.saved_signup_password), "");
                        userModel.setPhone_no(phNo);
                        userModel.setPassword(pwd);
                        userModel.setUsername(usrnmae);
                        if(phNo!=null && !phNo.equalsIgnoreCase("") && pwd!=null && !pwd.equalsIgnoreCase("") && usrnmae!=null && !usrnmae.equalsIgnoreCase("")) {
                            otpValidator.otpCheckMethod("confirmNumber", userModel);
                        }
                    }
                });
            }
//            {
//                navController.navigateUp();
//                navController.navigate(R.id.action_HomeFragment_to_SignUpFragment);
//            }
            else {
                navController.navigateUp();
                navController.navigate(R.id.action_HomeFragment_to_LoginFragment);
            }
        } else {

            userModel = LoginOrc.validateLogin(this, mDatabase);
            if(userModel!=null && (userModel.getUsername()!=null && !userModel.getUsername().equalsIgnoreCase(""))) {
                navUsrname.setText(userModel.getUsername());
                if(userModel.getEmailId()!=null && !userModel.getEmailId().equalsIgnoreCase("")) {
                    navEmail.setText(userModel.getEmailId());
                }
                if(userModel.getLoginTime()!=null && !userModel.getLoginTime().equalsIgnoreCase("")) {
                    String Tm = "Login Time : " + userModel.getLoginTime();
                    navLoginTm.setText(Tm);
                }
            } else {
                navController.navigateUp();
                navController.navigate(R.id.action_HomeFragment_to_LoginFragment);
            }
        }

        if(!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());
        }

        MainActivity.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, MainActivity.PERMISSION_CODE_READ_FILES);
        MainActivity.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, MainActivity.PERMISSION_CODE_WRITE_FILES);
        MainActivity.checkPermission(Manifest.permission.INTERNET, MainActivity.PERMISSION_CODE_INTERNET);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FilePicker.FILE_SELECT_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = data.getData();
                    ologger.info("File Uri: " + uri.toString());
                    String path = uri.getPath();

                    ologger.info("File Path: " + path);
                    String filename = getFileName(uri);//uri.getLastPathSegment();
                    Intent intent = new Intent(this, UploadFile.class);
                    intent.putExtra("fpath", filename);
                    intent.putExtra("currDir", AppProps.getInstance().getCurrDir());
                    intent.putExtra("furi", uri.toString());
                    int fsize = getFileSize(uri);
                    if(fsize >= (5.5*1024*1024)) {
                        Snackbar.make(findViewById(android.R.id.content), "The Selected File Size is " + (fsize/(1024*1024)) + "MB which exceeds the currently available limit of 6MB" , Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();

                        intent.putExtra("fType", "2");
                    } else {
                        intent.putExtra("fType", "1");
                    }
                    NotificationMan.createNotificationForUploadDownloads("u", filename, filename.substring(filename.lastIndexOf(".")+1, filename.length()));
                    NotificationMan.startDisplayNotifForUploadDownload();
                    this.startService(intent);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    public int getFileSize(Uri uri) {
        int result = 0;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getInt(cursor.getColumnIndex(OpenableColumns.SIZE));
                }
            } finally {
                cursor.close();
            }
        }
        return result;
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        String action = intent.getAction();

        if (Intent.ACTION_SEARCH.equals(action)) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);
            Toast.makeText(this, query, Toast.LENGTH_LONG).show();

            Bundle searchB = new Bundle();
            searchB.putBoolean("searchQuery",true);
            searchB.putString("queryString", query);
//            HomeFragment homeFragment = new HomeFragment();
//            homeFragment.setArguments(searchB);
            navController.navigate(R.id.nav_home, searchB);
        }else if (Intent.ACTION_VIEW.equals(action)) {
            Uri data = intent.getData();

            String option = "";
            String fileID = "";
            try {
                URL url = new URL(data.toString());
                option = url.getPath();
                String query = url.getQuery();
                String[] queryString = query.split("&");
                Map<String, String> queryMap = new HashMap<>();
                for(String qPair: queryString) {
                    String[] splitPair = qPair.split("=");
                    queryMap.put(splitPair[0], splitPair[1]);
                }
                fileID = queryMap.get("fileId");
            } catch (MalformedURLException e) {



                Snackbar.make(findViewById(android.R.id.content), "Internal Error Occured while procesing the url " +data.toString(), BaseTransientBottomBar.LENGTH_SHORT).show();
                e.printStackTrace();
                return;
            }

            if(option!=null && !option.isEmpty()) {

//                if(option.equalsIgnoreCase("/loadSharedFile")) {
                Snackbar.make(findViewById(android.R.id.content), data.toString(), BaseTransientBottomBar.LENGTH_SHORT).show();
                Bundle arg = new Bundle();
                String urlStr = data.toString().replace("http://www.m.smartsave.com", "https://oai78ldl1g.execute-api.ap-southeast-1.amazonaws.com/v1/upload");
                arg.putString("openedSharedDir", urlStr);
                Log.i("Tag", " URL -----------------------------------------------------> "+urlStr);
                navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
                navController = navHostFragment.getNavController();
                AppProps.getInstance().setCurrSharedFileID(fileID);
                navController.navigate(R.id.nav_home, arg);
//                } else {
//                    Snackbar.make(findViewById(android.R.id.content), "Unsupported Operation! ->" + option, BaseTransientBottomBar.LENGTH_LONG).setBackgroundTint(Color.RED).show();
//                }
            }
        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if(item.getItemId() == R.id.play_game || item.getItemId() == R.id.game_reset) {
//            resetGame.setVisible(true);
//        }
//        else {
//            resetGame.setVisible(false);
//        }
        switch (item.getItemId()) {
            case R.id.action_settings:
                navController.navigate(R.id.settingsview);
                return true;
            case R.id.search_menu_item:
                return false;
            case R.id.share_app:
                SendOrc sOrc = new SendOrc();
                sOrc.shareApp(this);
                return true;
            case R.id.action_logout:
                LoadingBox.createInstance(this).show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        loginOrc.signOutAction();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    recreate();
                                    LoadingBox.getInstance().dismiss();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }).start();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        //this is only needed if you have specific things
        //that you want to do when the user presses the back button.
        /* your specific things...*/
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_login);
            if (navController.getCurrentDestination().getId()==R.id.fragment_login) {
                AlertDialog.Builder alertDb = new AlertDialog.Builder(this);
                alertDb.setCancelable(false)
                        .setMessage("Exit App?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                endActivity();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                AlertDialog alert = alertDb.create();
                alert.show();
            } else {
                super.onBackPressed();
            }
    }

    public void endActivity() {
        this.finish();
    }

    @Override
    public void lockDrawer() {
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    public void unlockDrawer() {
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    public static void responseProcessingOfServies(final Bundle inpBundle) {
        final AppProps oAppProp = AppProps.getInstance();
        final String opType = inpBundle.getString("opType");
        if (oAppProp.getActivity() != null) {
            oAppProp.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (opType) {
                        case "upload":
                            oAppProp.getActivity().recreate();
                            break;
                        case "download":
                            String fileName = inpBundle.getString("fileName");
                            Intent intentFOpen = new Intent();
                            intentFOpen.setAction(Intent.ACTION_VIEW);
                            intentFOpen.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intentFOpen.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            File inpFile = new File(rootMediaDir, fileName);
                            if(!inpFile.exists()) {
                                break;
                            }
                            Uri path = FileProvider.getUriForFile(AppProps.getInstance().getContext(), BuildConfig.APPLICATION_ID + ".provider", inpFile);//Uri.fromFile(inpFile);
                            String type = "";
                            String extension = MimeTypeMap.getFileExtensionFromUrl(path.toString());
                            if (extension != null) {
                                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                            }
                            intentFOpen.setDataAndType(path, type);
                            AppProps.getInstance().getContext().startActivity(intentFOpen);
                            break;
                    }
                }
            });
        }
    }


    public static void checkPermission(String permission, int requestCode)
    {
        if(AppProps.getInstance().getContext()!=null) {
            if (ContextCompat.checkSelfPermission(AppProps.getInstance().getContext(), permission)
                    == PackageManager.PERMISSION_DENIED) {

                // Requesting the permission
                ActivityCompat.requestPermissions(AppProps.getInstance().getActivity(),
                        new String[]{permission},
                        requestCode);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_CODE_READ_FILES
                || requestCode == PERMISSION_CODE_WRITE_FILES) {

            if (grantResults.length<=0 || (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_DENIED)) {

                // Showing the toast message
                Snackbar.make(findViewById(android.R.id.content),
                        "Need Storage Permission to upload and download files!",
                        BaseTransientBottomBar.LENGTH_SHORT).setBackgroundTint(Color.RED).show();
            }
        }
    }

}
