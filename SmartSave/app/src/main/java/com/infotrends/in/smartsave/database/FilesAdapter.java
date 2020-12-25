package com.infotrends.in.smartsave.database;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.infotrends.in.smartsave.BuildConfig;
import com.infotrends.in.smartsave.MainActivity;
import com.infotrends.in.smartsave.R;
import com.infotrends.in.smartsave.models.FileModel;
import com.infotrends.in.smartsave.orchestrator.FilesModOrc;
import com.infotrends.in.smartsave.services.DownloadFile;
import com.infotrends.in.smartsave.utils.AppProps;
import com.infotrends.in.smartsave.utils.IntegProperties;
import com.infotrends.in.smartsave.utils.LoadingBox;
import com.infotrends.in.smartsave.utils.NotificationMan;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FilesListHolder> {


    private Context mContext;
    private List<FileModel> mCursor;
    private int fLen = 0;
    private SimpleDateFormat sf = new SimpleDateFormat("MM-dd-yyyy, H:mm:ss");
    private FilesModOrc oFilesModOrc = new FilesModOrc();
    private AppProps oAppProps = AppProps.getInstance();
    private IntegProperties oInteg = IntegProperties.getInstance();
    private String rootMediaDir = Environment.getExternalStorageDirectory()
            .getAbsolutePath()  +  oInteg.getString("app_base_dir") + oInteg.getString("app_download_media_dir");
    private SharableContactsAdapter mSharableAdapter;
    private RecyclerView sharableRecyclerView;
    private String contsactsStr = "";

    public FilesAdapter(Context context, List<FileModel> cursor) {
        mContext = context;
        mCursor = cursor;
        if(cursor!=null)
            fLen = cursor.size();
    }

    public class FilesListHolder extends RecyclerView.ViewHolder {

        public TextView fileName;
        public TextView fileType;
        public TextView fileTimestamp;
        public ImageView fileTypeImg, imgView;
        public FilesListHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.file_Name);
            fileType = itemView.findViewById(R.id.file_type);
            fileTimestamp = itemView.findViewById(R.id.file_timestamp);
            fileTypeImg = itemView.findViewById(R.id.file_type_img);

            imgView = itemView.findViewById(R.id.recycle_view_optBtn);


            imgView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FileModel oMod = mCursor.get(getAdapterPosition());
                    if(!oMod.getFileType().equalsIgnoreCase("Folder")) {
                        createOptionsDlg(getAdapterPosition());
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    createOptionsDlg(getAdapterPosition());
                    return true;
                }
            });

        }
    }

    @NonNull
    @Override
    public FilesListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        final int devWidth = metrics.widthPixels;

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.file_list_recycle_view, parent, false);

        return new FilesListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilesListHolder holder, int position) {

        if(position>=fLen) {
            return;
        }

        final FileModel  oFileModel = mCursor.get(position);
        String fType = oFileModel.getFileType();
//        String tmpFname = oFileModel.getFileName();
//        String[] fpaths = oFileModel.getFileName().split("\\|");
//        String tmpFname = fpaths[fpaths.length - 1];
        final String fName = FilesModOrc.getFileName(oFileModel.getFileName()); //fpaths[fpaths.length - 1];
        String fTstamp = sf.format(oFileModel.getFileTimestamp());

        String ftmp = "";
        if(fName.length()>13) {
            ftmp += fName.substring(0, 13) + "...";
//            while(fName.length()!=15) {
//                if(fName.length()>15) {
//                    ftmp += fName.substring(0, 15) + "\n";
//                    fName = fName.substring(15, fName.length());
//                } else {
//                    ftmp += fName.substring(0, fName.length());
//                }
//            }
        } else {
            ftmp = fName;
        }

        File cacheDir = new File(oFilesModOrc.rootPath + "/.thumbnails/");//context.getCacheDir();

        File imgFile = new File(cacheDir + "/" + oFileModel.getFileName());
        //                        if(imgFile.exists())
        {
            try {

                if(oFileModel.getIsImage().equalsIgnoreCase("Y") && imgFile.exists()) {
                    Bitmap imgBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath()); //decodeStream(fis);

                    holder.fileTypeImg.setMaxWidth(holder.fileType.getWidth());
                    holder.fileTypeImg.setMaxHeight(holder.fileType.getHeight());
                    holder.fileTypeImg.setMinimumWidth(holder.fileType.getWidth());
                    holder.fileTypeImg.setMinimumHeight(holder.fileType.getHeight());
                    holder.fileTypeImg.setImageBitmap(imgBitmap);
                    holder.fileTypeImg.setVisibility(View.VISIBLE);
                    holder.fileType.setVisibility(View.VISIBLE);
                    holder.fileType.setBackgroundColor(Color.BLACK);
                    holder.fileType.setText(".jpeg");
                    holder.imgView.setVisibility(View.VISIBLE);
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    });
                } else {

                    if(oFileModel.getFileType().equalsIgnoreCase("Folder")) {
                        fType = "Fold";
                        holder.imgView.setVisibility(View.GONE);
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                try {
                                    Thread.sleep(500);
                                    Bundle dirBundles = new Bundle();
                                    dirBundles.putString("curDir", oFileModel.getFileName());
                                    dirBundles.putString("curSharedFileStatus", oFileModel.getIsSharedStatus());
                                    oAppProps.getNavController().navigate(R.id.nav_home, dirBundles);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else {
                        holder.imgView.setVisibility(View.VISIBLE);
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            }
                        });
                    }
                    holder.fileType.setVisibility(View.VISIBLE);
                    holder.fileTypeImg.setVisibility(View.GONE);
                    holder.fileType.setText(fType);
                    holder.fileType.setBackgroundColor(Color.TRANSPARENT);
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        holder.fileName.setText(ftmp);
        holder.fileTimestamp.setText(fTstamp);

        holder.itemView.setTag(R.string.file_name, "fName");
        holder.itemView.setTag(R.string.file_Type, "fType");
        holder.itemView.setTag(R.string.file_TimeStamp, "fTstamp");


    }

    @Override
    public int getItemCount() {
        return fLen;
    }

    private void createOptionsDlg(final int pos) {
        Activity act = oAppProps.getActivity();
        final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(act);
        final View optionsDlg = act.getLayoutInflater().inflate(R.layout.files_options_popup, null);
        mBottomSheetDialog.setContentView(optionsDlg);

        final Button btnOpen = (Button) optionsDlg.findViewById(R.id.foption_open);
        final Button btnDownload = (Button) optionsDlg.findViewById(R.id.foption_download);
        final Button btnDelete = (Button) optionsDlg.findViewById(R.id.foption_delete);
        final Switch btSwitch = (Switch) optionsDlg.findViewById(R.id.foption_make_public);
        final Button btnShare = (Button) optionsDlg.findViewById(R.id.foption_shareFile);
        final Button btnGetURL = (Button) optionsDlg.findViewById(R.id.foption_getPublicUrl);
//        optionsDlg.setTitle("Options: ");
        TextView fOption_head = optionsDlg.findViewById(R.id.foption_heading);
//        int pos = getAdapterPosition();
        FileModel oModel = mCursor.get(pos);
        String fileName = FilesModOrc.getFileName(oModel.getFileName());
        fOption_head.setText(fileName);
        if(oModel.getFileType().equalsIgnoreCase("Folder")) {
            btnOpen.setText("Open Folder");
            btnDownload.setVisibility(View.GONE);
        }

        if(oModel.getIsPublic().equalsIgnoreCase("Y")) {
            btSwitch.setChecked(true);
        } else {
//            if(oModel.getSharedCont().trim().equalsIgnoreCase("")) {
//                btnGetURL.setEnabled(false);
//            }
            btSwitch.setChecked(false);
        }

        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                int pos = getAdapterPosition();

                MainActivity.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, MainActivity.PERMISSION_CODE_READ_FILES);
                MainActivity.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, MainActivity.PERMISSION_CODE_WRITE_FILES);
                MainActivity.checkPermission(Manifest.permission.INTERNET, MainActivity.PERMISSION_CODE_INTERNET);
                final FileModel oModel = mCursor.get(pos);
                Intent intentFOpen = new Intent();
                intentFOpen.setAction(Intent.ACTION_VIEW);
                intentFOpen.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intentFOpen.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if(oModel.getFileType().equalsIgnoreCase("Folder")) {

                    oAppProps.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(500);
                                Bundle dirBundles = new Bundle();
                                dirBundles.putString("curDir", oModel.getFileName());
                                dirBundles.putString("curSharedFileStatus", oModel.getIsSharedStatus());
                                oAppProps.getNavController().navigate(R.id.nav_home, dirBundles);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    mBottomSheetDialog.dismiss();
                    return;
                }
                String pth = FilesModOrc.getDirectory(oModel.getFileName());
                if(oModel.getIsSharedStatus()!=null &&
                        (oModel.getIsSharedStatus().equalsIgnoreCase("P") || oModel.getIsSharedStatus().equalsIgnoreCase("S"))) {
                    pth = "Shared Files/" + pth;
                }
                File inpFile = new File(rootMediaDir + pth, oModel.getFileName());
                if(inpFile.exists()) {
                    Uri path = FileProvider.getUriForFile(oAppProps.getContext(), BuildConfig.APPLICATION_ID + ".provider", inpFile);//Uri.fromFile(inpFile);
                    String type = "";
                    String extension = MimeTypeMap.getFileExtensionFromUrl(path.toString());
                    if (extension != null) {
                        type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                    }
                    intentFOpen.setDataAndType(path, type);
                    oAppProps.getContext().startActivity(intentFOpen);
                } else {
                    final AlertDialog alertD = new AlertDialog.Builder(oAppProps.getContext()).create();
                    alertD.setCancelable(true);
                    alertD.setCanceledOnTouchOutside(false);
                    alertD.setMessage("File Not Found!\nDo You Want To Download the File?");
                    alertD.setButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            FileModel oModel = mCursor.get(pos);
                            NotificationMan.createNotificationForUploadDownloads("d", oModel.getFileName(), oModel.getFileType());
                            NotificationMan.startDisplayNotifForUploadDownload();
                            Intent intent = new Intent(oAppProps.getActivity(), DownloadFile.class);
                            intent.putExtra("fModelName", oModel.getFileName());
                            intent.putExtra("fModelType", oModel.getFileType());
                            oAppProps.getActivity().startService(intent);

                            alertD.dismiss();
                        }
                    });
                    alertD.setButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            alertD.dismiss();
                        }
                    });
                    alertD.show();
                }
            }
        });


        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view1) {

                MainActivity.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, MainActivity.PERMISSION_CODE_READ_FILES);
                MainActivity.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, MainActivity.PERMISSION_CODE_WRITE_FILES);
                MainActivity.checkPermission(Manifest.permission.INTERNET, MainActivity.PERMISSION_CODE_INTERNET);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
//                                holder.itemView.setTag(R.string.file_name, "fName");
//                                holder.itemView.setTag(R.string.file_Type, "fType");
//                                holder.itemView.setTag(R.string.file_TimeStamp, "fTstamp");
//                                    Snackbar.make(view1, "Replace with your own action" , Snackbar.LENGTH_LONG)
//                                            .setAction("Action", null).show();

//                        int pos = getAdapterPosition();
                        FileModel oModel = mCursor.get(pos);
                        NotificationMan.createNotificationForUploadDownloads("d", oModel.getFileName(), oModel.getFileType());
                        NotificationMan.startDisplayNotifForUploadDownload();
                        //FilesListHolder oFilesListHolder = ;
//                                    new Thread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            int pos = getAdapterPosition();
//                                            FileModel oModel = mCursor.get(pos);
//                                            oFilesModOrc.downloadSelectedFile(oModel);
//                                        }
//                                    }).start();
                        Intent intent = new Intent(oAppProps.getActivity(), DownloadFile.class);
                        intent.putExtra("fModelName", oModel.getFileName());
                        intent.putExtra("fModelType", oModel.getFileType());
                        intent.putExtra("curSharedFileStatus", oModel.getIsSharedStatus());
                        oAppProps.getActivity().startService(intent);
                    }
                }).start();
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoadingBox.createInstance(oAppProps.getActivity()).show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
//                        int pos = getAdapterPosition();
                        FileModel oModel = mCursor.get(pos);
                        oFilesModOrc.deleteFile(oModel);
                        LoadingBox.getInstance().dismiss();
                        Bundle bdl = new Bundle();
                        oAppProps.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                oAppProps.getActivity().recreate();
                            }
                        });

                    }
                }).start();
//
            }
        });

        btSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean currentStateOn = btSwitch.isChecked();

                String state = "N";
                if(currentStateOn) {
                    state = "Y";
                }

                final String currState = state;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        FileModel oModel = mCursor.get(pos);
                        oModel.setIsPublic(currState);
                        boolean bool = oFilesModOrc.makeFilePublic(oModel);
                        if(bool) {
                            mCursor.get(pos).setIsPublic(currState);
//                            oAppProps.getActivity().runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    if(currState.equalsIgnoreCase("Y")) {
//                                        btnGetURL.setEnabled(true);
//                                    } else {
//                                        btnGetURL.setEnabled(false);
//                                    }
//                                }
//                            });

                        } else {
                            oAppProps.getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(btSwitch.isChecked()) {
                                        btSwitch.setChecked(false);
                                    } else {
                                        btSwitch.setChecked(true);
                                    }
                                }
                            });
                        }
                    }
                }).start();



            }
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Activity act = oAppProps.getActivity();
                final BottomSheetDialog sharedContactsView = new BottomSheetDialog(act);
                final View sharedCView = act.getLayoutInflater().inflate(R.layout.sharable_nos_list, null);
                Button addSharedNo = sharedCView.findViewById(R.id.shared_add_num);
                final EditText sharedNoEdit = sharedCView.findViewById(R.id.shared_ph_no_edit_text);
                Button sharedApplyChanges =  sharedCView.findViewById(R.id.shared_apply);
                sharedContactsView.setContentView(sharedCView);

                sharableRecyclerView = sharedCView.findViewById(R.id.shared_recycleview);
                sharableRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
                sharableRecyclerView.setItemAnimator(new DefaultItemAnimator());
                sharableRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));

//                sharedApplyChanges
                sharedApplyChanges.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        
                        List<String> sharedContLst = mSharableAdapter.getFilesList();
                        contsactsStr = "";
                        if((sharedContLst!=null && sharedContLst.size()>0)) {
                            for(String contact:sharedContLst) {
                                contsactsStr += contact + ",";
                            }
                        }
                        final FileModel oModel = mCursor.get(pos);
                        
                        if((contsactsStr==null || contsactsStr.isEmpty()) && (oModel.getSharedCont()==null || oModel.getSharedCont().isEmpty())) {
                            return;
                        }
                        oModel.setSharedCont(contsactsStr);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                boolean bool = oFilesModOrc.updateSharable(oModel);
                                if(bool) {
                                    oAppProps.getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            sharedContactsView.dismiss();
                                            mCursor.get(pos).setSharedCont(contsactsStr);
                                        }
                                    });


                                }
                            }
                        }).start();

                    }
                });
                sharedNoEdit.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View view, int i, KeyEvent keyEvent) {
                        if(keyEvent.getKeyCode() == keyEvent.KEYCODE_ENTER) {
                            hideKeyboard();
                            addPhoneNumbertoSharedList(sharedNoEdit);

                            return true;
                        }
                        return false;
                    }
                });

                addSharedNo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        addPhoneNumbertoSharedList(sharedNoEdit);

                    }
                });

                new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT| ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        int selectedPos = Integer.decode(viewHolder.itemView.getTag(R.string.shared_no).toString());
                        mSharableAdapter.removeSelectedItem(selectedPos);
                        oAppProps.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sharableRecyclerView.setAdapter(mSharableAdapter);
                            }
                        });
                    }
                }).attachToRecyclerView(sharableRecyclerView);

                FileModel oModel = mCursor.get(pos);
                List<String> contacts = new LinkedList<String>(Arrays.asList(oModel.getSharedCont().split(",")));
                mSharableAdapter = new SharableContactsAdapter(mContext, contacts);

                oAppProps.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sharableRecyclerView.setAdapter(mSharableAdapter);
                    }
                });

                sharedContactsView.show();

            }
        });
        btnGetURL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final FileModel oModel = mCursor.get(pos);
                if(oModel.getIsPublic().equalsIgnoreCase("N")
                        && oModel.getSharedCont().trim().equalsIgnoreCase("")) {
                    Toast.makeText(mContext, "Sharable Urls Can Only be Generated For Shared Or Public Files",
                            Toast.LENGTH_SHORT).show();
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean bool = oFilesModOrc.getSharableUrl(oModel);
                    }
                }).start();
            }
        });

        if(oModel.getIsSharedStatus()!=null &&
                (oModel.getIsSharedStatus().equalsIgnoreCase("P") || oModel.getIsSharedStatus().equalsIgnoreCase("S"))) {
            btSwitch.setChecked(true);
            btSwitch.setClickable(false);
            btSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btSwitch.setChecked(true);
                }
            });
            btnShare.setVisibility(View.GONE);
            btnGetURL.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
        }
        mBottomSheetDialog.show();
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) oAppProps.getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = oAppProps.getActivity().getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(oAppProps.getActivity());
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void addPhoneNumbertoSharedList(final EditText sharedNoEdit) {
        String sharedNumber = sharedNoEdit.getText().toString();
        if(sharedNumber==null || sharedNumber.length()==0) {
            //Toast.makeText(mContext, "Please Enter A Phone Number", Toast.LENGTH_SHORT).show();
            return;
        }
        Pattern noPattern = Pattern.compile("^\\d{10}$");
        Matcher matcher = noPattern.matcher(sharedNumber);
        if(sharedNumber.length()!=10 || !matcher.matches()) {
            Toast.makeText(mContext, "Please Enter A Valid Phone Number", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean addedSuccssfully = mSharableAdapter.addItem(sharedNumber);
        if(addedSuccssfully) {
            oAppProps.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    sharableRecyclerView.setAdapter(mSharableAdapter);
                    sharedNoEdit.setText("");
                }
            });
        } else {
            Toast.makeText(mContext, "Number Already added", Toast.LENGTH_SHORT).show();
        }
    }

}
