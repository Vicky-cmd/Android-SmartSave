package com.infotrends.in.smartsave.ui.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.infotrends.in.smartsave.R;
import com.infotrends.in.smartsave.database.DBContract;
import com.infotrends.in.smartsave.database.DBHelper;
import com.infotrends.in.smartsave.orchestrator.LoginOrc;
import com.infotrends.in.smartsave.utils.AppProps;
import com.infotrends.in.smartsave.utils.IntegProperties;
import com.infotrends.in.smartsave.utils.LoadingBox;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

public class SettingsFragment extends Fragment implements View.OnClickListener  {

    View root;
    private NavHostFragment navHostFragment;
    private AppProps oAppProps = AppProps.getInstance();
    private LoginOrc loginOrc = new LoginOrc();
    DBHelper dbHelper = DBHelper.getInstance();
    private Context context = oAppProps.getContext();
    private NavController navController = oAppProps.getNavController();
    private SQLiteDatabase mDatabase = dbHelper==null?null:dbHelper.getWritableDatabase();;
    private Activity mActivity = oAppProps.getActivity();

    private Switch resetSwitch, resetAllSwitch;
    private Button delAllData;
    private IntegProperties oInteg = IntegProperties.getInstance();

    private String rootPath = Environment.getExternalStorageDirectory()
            .getAbsolutePath()  +  oInteg.getString("app_base_dir") + oInteg.getString("app_download_media_dir");

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        loginOrc = new LoginOrc();
        root = inflater.inflate(R.layout.fragment_settings, container, false);
        context = getActivity();

        navHostFragment = (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();
        delAllData =  root.findViewById(R.id.delete_all_account_data);
        DBHelper dbHelper = new DBHelper(context);
        mDatabase = dbHelper.getWritableDatabase();
        resetSwitch = root.findViewById(R.id.res_switch);
        resetAllSwitch = root.findViewById(R.id.res_all_switch);
        resetSwitch.setChecked(false);
        resetSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setCancelable(true);
                    builder.setTitle("Reset");
                    builder.setMessage("Do You really want to reset the scores? (Can't be undone)");
                    builder.setPositiveButton("Confirm",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    File cacheDir = new File(rootPath + "/.thumbnails/");
                                    for(File chileFile : cacheDir.listFiles()) {

                                        chileFile.delete();

                                    }
                                    dialog.dismiss();
                                    resetSwitch.setChecked(false);
                                }
                            });
                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

        resetAllSwitch.setChecked(false);
        resetAllSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setCancelable(true);
                    builder.setTitle("Reset");
                    builder.setMessage("Do You really want to reset the scores? (Can't be undone)");
                    builder.setPositiveButton("Confirm",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    File appRootDir = new File(Environment.getExternalStorageDirectory()
                                            .getAbsolutePath()  +  oInteg.getString("app_base_dir"));
                                    appRootDir.delete();
                                    resetAllSwitch.setChecked(false);
                                    LoadingBox.createInstance(mActivity).show();
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            loginOrc.signOutAction();
                                            mActivity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        mActivity.recreate();
                                                        LoadingBox.getInstance().dismiss();
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                        }
                                    }).start();
                                }
                            });
                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

        delAllData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "Feature Currently Not Available! Will be added in the next Release.", Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onStop() {
        super.onStop();
    }


}
