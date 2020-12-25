package com.infotrends.in.smartsave.orchestrator;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.infotrends.in.smartsave.database.DBHelper;
import com.infotrends.in.smartsave.models.FileModel;
import com.infotrends.in.smartsave.models.UserModel;
import com.infotrends.in.smartsave.utils.AppProps;
import com.infotrends.in.smartsave.utils.IntegProperties;
import com.infotrends.in.smartsave.utils.LoggerFile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import androidx.annotation.RequiresApi;
import androidx.navigation.NavController;

public class FilesModOrc {
    private ConnectionOrchestrator connOrc = new ConnectionOrchestrator();
    private IntegProperties oInteg = IntegProperties.getInstance();
    DBHelper dbHelper = DBHelper.getInstance();
    private SQLiteDatabase mDatabase = dbHelper==null?null:dbHelper.getWritableDatabase();
    private AppProps oAppProps = AppProps.getInstance();
    private String  ErrorString = "";
    private NavController navController = oAppProps.getNavController();
    private LoginOrc oLoginOrc = new LoginOrc();
    private UserModel userModel = new UserModel();
    private Context context = oAppProps.getContext();
    private FileModel fileModel = new FileModel();
    private SimpleDateFormat sf = new SimpleDateFormat("MM-dd-yyyy, H:mm:ss");
    private LoggerFile ologger = LoggerFile.createInstance(FilesModOrc.class);
    private Activity mActivity = oAppProps.getActivity();
    private OTPValidator otpValidator = new OTPValidator();
    public String rootPath = Environment.getExternalStorageDirectory()
            .getAbsolutePath()  +  oInteg.getString("app_base_dir") + oInteg.getString("app_download_media_dir");

    public FilesModOrc() {
        userModel = oLoginOrc.validateLogin(context, mDatabase);
    }

    public List<FileModel> getFilesListInServer(String curDir, String currFSharedStatus) {
//        userModel = oLoginOrc.validateLogin(context, mDatabase);
        List<FileModel> fileModelLst = new ArrayList<FileModel>();
        if(userModel!=null) {
            userModel.setCurrDir(curDir);
            userModel.setIsSharedOrPubFile(currFSharedStatus);
            JSONObject jsonReq = oLoginOrc.jsonReqFormation("getFilesList", userModel);
            HashMap<String, Object> respHMap = connOrc.executeMethod(oInteg.getString("getFilesList_url"), jsonReq);
            fileModelLst = responseToUsrModMapping(fileModelLst, respHMap);
        }
        if(fileModelLst!=null && fileModelLst.size()>0) {
            oAppProps.setFilesList(fileModelLst);
        }
        return fileModelLst;
    }

    public List<FileModel> getSharedFileFromServer(String sharedUrl) {
//        userModel = oLoginOrc.validateLogin(context, mDatabase);
        List<FileModel> fileModelLst = new ArrayList<FileModel>();
        if(userModel!=null) {
            userModel.setCurrDir("");
            JSONObject jsonReq = oLoginOrc.jsonReqFormation("getFilesList", userModel);
            HashMap<String, Object> respHMap = connOrc.executeMethod(sharedUrl, jsonReq);
            fileModelLst = responseToUsrModMapping(fileModelLst, respHMap);
        }
        if(fileModelLst!=null && fileModelLst.size()>0) {
            oAppProps.setFilesList(fileModelLst);
        }
        return fileModelLst;
    }


    public boolean updateSharable(final FileModel fileModel) {
        boolean bool = false;

        if(userModel!=null) {

            userModel.setFileModel(fileModel);
            JSONObject jsonReq = oLoginOrc.jsonReqFormation("updateSharedFileDtls", userModel);
            HashMap<String, Object> respHMap = connOrc.executeMethod(oInteg.getString("updateSharedFileDtls_Url"), jsonReq);
            if(respHMap!=null) {
                if (respHMap.get("body") != null) {
                    HashMap respB = (HashMap) respHMap.get("body");
                    if (respB.get("status") != null && respB.get("status").toString().trim().equalsIgnoreCase("200")) {
//                        Toast.makeText(context, respHMap.get("status").toString(), Toast.LENGTH_SHORT).show();
                        userModel.setStatusCode("200");
                        bool = true;
                    } else {
                        if (respB.get("status") != null && !respB.get("status").toString().isEmpty()) {
                            userModel.setStatusCode(respB.get("status").toString());
                        } else {
                            userModel.setStatusCode("100");
                        }
                    }
                }
//                if(ErrorString==null || ErrorString.isEmpty()) {
//                    ErrorString="ERROR!";
//                }
                if (userModel.getStatusCode() != null) {
                    if ((userModel.getStatusCode().equalsIgnoreCase("710")
                            || userModel.getStatusCode().equalsIgnoreCase("755"))) {
                        UserModel userModel1 = oLoginOrc.genLoginOTP(userModel);
                        if (userModel1.getStatusCode().equalsIgnoreCase("200")) {
                            AppProps.getInstance().getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    otpValidator.otpCheckMethod("loginAction", userModel);
                                }
                            });
                        }
                    }
                }
            }
        }

        return bool;
    }

    public boolean getSharableUrl(final FileModel fileModel) {
        boolean bool = false;

        if(userModel!=null) {

            userModel.setFileModel(fileModel);
            JSONObject jsonReq = oLoginOrc.jsonReqFormation("getSharableUrl", userModel);
            HashMap<String, Object> respHMap = connOrc.executeMethod(oInteg.getString("getSharable_Url"), jsonReq);
            if(respHMap!=null) {
                if(respHMap.get("body")!= null) {
                    HashMap respB = (HashMap) respHMap.get("body");
                    if (respB.get("status") != null && respB.get("status").toString().trim().equalsIgnoreCase("200")) {
//                        Toast.makeText(context, respHMap.get("status").toString(), Toast.LENGTH_SHORT).show();
                        if(respB.get("sharableLink")!=null && !respB.get("sharableLink").toString().trim().equals("")) {
                            userModel.setStatusMessage(respB.get("sharableLink").toString());
                        }
                        userModel.setStatusCode("200");
                        bool = true;
                    } else {
                        if(respB.get("status")!=null && !respB.get("status").toString().isEmpty()) {
                            userModel.setStatusCode(respB.get("status").toString());
                        } else {
                            userModel.setStatusCode("100");
                        }
                    }
                }
//                if(ErrorString==null || ErrorString.isEmpty()) {
//                    ErrorString="ERROR!";
//                }
                if(userModel.getStatusCode()!=null) {
                    if((userModel.getStatusCode().equalsIgnoreCase("710")
                        || userModel.getStatusCode().equalsIgnoreCase("755"))) {
                        UserModel userModel1 = oLoginOrc.genLoginOTP(userModel);
                        if (userModel1.getStatusCode().equalsIgnoreCase("200")) {
                            AppProps.getInstance().getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    otpValidator.otpCheckMethod("loginAction", userModel);
                                }
                            });
                        }
                    } else if(userModel.getStatusCode().equalsIgnoreCase("200")) {
                        AppProps.getInstance().getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ClipboardManager clipboard = (ClipboardManager) AppProps.getInstance().getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("SharableUrl", userModel.getStatusMessage());
                                clipboard.setPrimaryClip(clip);
//                                FileModel oModel = mCursor.get(pos);
                                Toast.makeText(oAppProps.getActivity(),"Url Copied to Clipboard!", Toast.LENGTH_LONG).show();
                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                shareIntent.setType("text/plain");
                                shareIntent.putExtra(Intent.EXTRA_TEXT,"Hi! You can access the File "
                                        + FilesModOrc.getDirectory(fileModel.getFileName())
                                        + FilesModOrc.getFileName(fileModel.getFileName()) + " using the Link : " + userModel.getStatusMessage());
                                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Share File Link...");
                                oAppProps.getActivity().startActivity(Intent.createChooser(shareIntent, "Share..."));
                            }
                        });
                    }
                }
//                AppProps.getInstance().getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(AppProps.getInstance().getContext(), ErrorString, Toast.LENGTH_SHORT).show();
//                    }
//                });

            }

        }
        return bool;
    }

    public boolean makeFilePublic(FileModel fileModel) {
        boolean bool = false;

        if(userModel!=null) {

            userModel.setFileModel(fileModel);
            JSONObject jsonReq = oLoginOrc.jsonReqFormation("makePublic", userModel);
            HashMap<String, Object> respHMap = connOrc.executeMethod(oInteg.getString("makePublic_url"), jsonReq);
            if(respHMap!=null) {
                if(respHMap.get("body")!= null) {
                    HashMap respB = (HashMap) respHMap.get("body");
                    if (respB.get("status") != null && respB.get("status").toString().trim().equalsIgnoreCase("200")) {
//                        Toast.makeText(context, respHMap.get("status").toString(), Toast.LENGTH_SHORT).show();
                        userModel.setStatusCode("200");
                        bool = true;
                    } else {
                        if(respB.get("error_msg")!=null && !respB.get("error_msg").toString().isEmpty()) {
                            userModel.setStatusMessage(respB.get("error_msg").toString());
                        }
                        if(respB.get("status")!=null && !respB.get("status").toString().isEmpty()) {
                            userModel.setStatusCode(respB.get("status").toString());
                        } else {
                            userModel.setStatusCode("100");
                            userModel.setStatusMessage("Unable To connect to Server!");
                        }
                    }
                }
//                if(ErrorString==null || ErrorString.isEmpty()) {
//                    ErrorString="ERROR!";
//                }
                if(userModel.getStatusCode()!=null && (userModel.getStatusCode().equalsIgnoreCase("710")
                        || userModel.getStatusCode().equalsIgnoreCase("755"))) {
                    UserModel userModel1 = oLoginOrc.genLoginOTP(userModel);
                    if(userModel1.getStatusCode().equalsIgnoreCase("200")) {
                        AppProps.getInstance().getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                otpValidator.otpCheckMethod("loginAction", userModel);
                            }
                        });
                    }
                } else if(userModel.getStatusCode()!=null && (!userModel.getStatusCode().equalsIgnoreCase("200") && userModel.getStatusMessage()!=null)) {
                    AppProps.getInstance().getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(oAppProps.getContext(), userModel.getStatusMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
//                AppProps.getInstance().getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(AppProps.getInstance().getContext(), ErrorString, Toast.LENGTH_SHORT).show();
//                    }
//                });

            }

        }

        return bool;
    }

    public boolean createNewFolder(String folderName) {
        boolean bool = false;
        if(userModel!=null) {
            FileModel fMod = new FileModel();
            fMod.setFileName(folderName);
            userModel.setFileModel(fMod);
            JSONObject jsonReq = oLoginOrc.jsonReqFormation("createFolder", userModel);
            HashMap<String, Object> respHMap = connOrc.executeMethod(oInteg.getString("createFolder_url"), jsonReq);
            if(respHMap!=null) {
                if (respHMap.get("body") != null) {
                    HashMap respB = (HashMap) respHMap.get("body");
                    if (respB.get("status") != null && respB.get("status").toString().trim().equalsIgnoreCase("200")) {

                        return true;
                    }
                }
            }
        }
            return bool;
    }


    public boolean deleteFile(FileModel fileModel) {
        ErrorString = "";
//        userModel = oLoginOrc.validateLogin(context, mDatabase);
        if(userModel!=null) {
            userModel.setFileModel(fileModel);
            JSONObject jsonReq = oLoginOrc.jsonReqFormation("deleteFile", userModel);
            HashMap<String, Object> respHMap = connOrc.executeMethod(oInteg.getString("deleteFile_url"), jsonReq);
            if(respHMap!=null) {
                if(respHMap.get("body")!= null) {
                    HashMap respB = (HashMap) respHMap.get("body");
                    if (respB.get("status") != null && respB.get("status").toString().trim().equalsIgnoreCase("200")) {
//                        Toast.makeText(context, respHMap.get("status").toString(), Toast.LENGTH_SHORT).show();
                        ErrorString = "SuccessFully Deleted the File!";
                        userModel.setStatusCode("200");
                    } else {
                        if(respB.get("status")!=null && !respB.get("status").toString().isEmpty()) {
                            userModel.setStatusCode(respB.get("status").toString());
                        } else {
                            userModel.setStatusCode("100");
                        }
                        if (respB.get("error_msg") != null && !respB.get("error_msg").toString().isEmpty()) {
                            ErrorString = respB.get("error_msg").toString();
                        }
                    }
                }
                if(ErrorString==null || ErrorString.isEmpty()) {
                    ErrorString="ERROR!";
                }
                if(userModel.getStatusCode()!=null && (userModel.getStatusCode().equalsIgnoreCase("710")
                        || userModel.getStatusCode().equalsIgnoreCase("755"))) {
                    UserModel userModel1 = oLoginOrc.genLoginOTP(userModel);
                    if(userModel1.getStatusCode().equalsIgnoreCase("200")) {
                        AppProps.getInstance().getActivity().runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               otpValidator.otpCheckMethod("loginAction", userModel);
                           }
                        });
                    }
                }
                AppProps.getInstance().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AppProps.getInstance().getContext(), ErrorString, Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }
        return true;
    }

    public boolean uploadLargeFile(FileModel fileModel) {
        ErrorString = "";

        boolean bool = false;
        if (userModel != null) {
            ologger.info("Users exists!");

            String type = "";
            String fName = fileModel.getFileName();
            String extension = fName.substring(fName.lastIndexOf(".")+1, fName.length());//MimeTypeMap.getFileExtensionFromUrl(fileModel.getFileURI().toString());
            if (extension != null) {
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            }
            if(type!=null)
                type = type.split("/")[0];
            if(type!=null && type.equalsIgnoreCase("image")) {
                fileModel.setIsImage("Y");
            } else {
                fileModel.setIsImage("N");
            }
            userModel.setFileModel(fileModel);
            ologger.info("File Data ---> " + fileModel.getFileName() + "  ---  " + fileModel.getFileType());
            String url = oInteg.getString("uploadLargeFile_url");
            JSONObject jsonReq = oLoginOrc.jsonReqFormation("uploadFile", userModel);
            ologger.info("JSON Request is Formed");
            HashMap<String, Object> respHMap = connOrc.executeMethod(url, jsonReq);

            if(respHMap!=null) {

                if(respHMap.get("body")!= null) {
                    HashMap respB = (HashMap) respHMap.get("body");
                    if (respB.get("status") != null && respB.get("status").toString().trim().equalsIgnoreCase("200")) {
                        url = respB.get("upload_url").toString();
                        respHMap = connOrc.executes(url, fileModel);

                        if(respHMap!= null) {
                            System.out.println(respHMap.get("code"));
                            System.out.println(respHMap.get("reponse"));
                            ologger.info(respHMap.get("code").toString());
                            ologger.info(respHMap.get("reponse").toString());
                            if (respHMap.get("code") != null && respHMap.get("code").toString().trim().equalsIgnoreCase("200")) {

                                url = oInteg.getString("uploadFileDtls_url");
                                jsonReq = oLoginOrc.jsonReqFormation("uploadFDtls", userModel);
                                ologger.info("JSON Request is Formed");
                                HashMap<String, Object> resp = connOrc.executeMethod(url, jsonReq);

                                if(resp!=null && resp.get("body")!=null
                                        && ((HashMap) resp.get("body")).get("status")!=null
                                        && ((HashMap) resp.get("body")).get("status").toString().equalsIgnoreCase("200")) {
                                    AppProps.getInstance().getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(context, "File Uploaded Successfully!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    return true;
                                }
                            }
                        }
                    }
                }
            }

        }
        return bool;
    }
    public boolean uploadFile(FileModel fileModel) {
        ErrorString = "";

        boolean bool = false;
        if(userModel!=null) {
            ologger.info("Users exists!");
            userModel.setFileModel(fileModel);
            userModel.setCurrDir(AppProps.getInstance().getCurrDir());
            ologger.info("File Data ---> " + fileModel.getFileName() + "  ---  " + fileModel.getFileType());
            String url = oInteg.getString("uploadFile_url");
            JSONObject jsonReq = readFileData(fileModel);
//            JSONObject jsonReq = oLoginOrc.jsonReqFormation("s3Connect", userModel);
//            ologger.info("JSON Request is Formed");

            AppProps.getInstance().getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "Created JSON Object!", Toast.LENGTH_SHORT).show();
                }
            });

            HashMap<String, Object> respHMap = connOrc.executeMethod(url, jsonReq);
            if(respHMap!=null) {
                if(respHMap.get("body")!= null) {
                    HashMap respB = (HashMap) respHMap.get("body");
                    if (respB.get("status") != null && respB.get("status").toString().trim().equalsIgnoreCase("200")) {
//                        Toast.makeText(context, respHMap.get("status").toString(), Toast.LENGTH_SHORT).show();
                        ErrorString = "SuccessFully Uploaded the File!";
                        bool = true;
                    } else {
                        if (respB.get("error_msg") != null && !respB.get("error_msg").toString().isEmpty())
                            ErrorString = respHMap.get("error_msg").toString();
                    }
                }
                if(ErrorString==null || ErrorString.isEmpty()) {
                    ErrorString="ERROR!";
                }
                AppProps.getInstance().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AppProps.getInstance().getContext(), ErrorString, Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }
        return bool;
    }

    JSONObject readFileData(FileModel fileModel) {
        ErrorString = "";

        JSONObject jsonReq = new JSONObject();

        String fName = fileModel.getFileName();

        File file = new File(fName);

        FileInputStream fis = null;
        try {

            String type = "";
            String extension = fName.substring(fName.lastIndexOf(".")+1, fName.length());//MimeTypeMap.getFileExtensionFromUrl(fileModel.getFileURI().toString());
            if (extension != null) {
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            }
            if(type!=null)
                type = type.split("/")[0];
            if(type!=null && type.equalsIgnoreCase("image")) {
                fileModel.setFileType("Y");
            } else {
                fileModel.setFileType("N");
            }

            String encContent = "";


//                fis = new FileInputStream(file);
                fis = (FileInputStream) mActivity.getContentResolver().openInputStream(fileModel.getFileURI());
//                StringBuilder builder = new StringBuilder();

//                try (BufferedReader br = new BufferedReader(new InputStreamReader(fis, "utf-8"))) {
//                    StringBuilder encFileContent = new StringBuilder();
//                    String responseLine = null;
//                    while ((responseLine = br.readLine()) != null) {
//                        encFileContent.append(responseLine.trim());
//                    }
//                FileInputStream inp = (FileInputStream) getContentResolver().openInputStream(uri);
                byte[] dataB = new byte[fis.available()];
//                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//                int nRead;
//                while ((nRead = fis.read(dataB, 0, dataB.length)) != -1) {
//                    buffer.write(dataB, 0, nRead);
//                }
//                    byte[] byteInp = encFileContent.toString().getBytes();
                fis.read(dataB);
                encContent = Base64.getEncoder().encodeToString(dataB);

//                }

//                byte[] input_file = Files.readAllBytes(file.toPath());
//                byte[] encodedBytes = Base64.getEncoder().encode(input_file);
//                encContent =  new String(encodedBytes);
//
//                InputStream in = null;
//                in = mActivity.getContentResolver().openInputStream(fileModel.getFileURI());



            jsonReq.put("phone_no", userModel.getPhone_no());
            jsonReq.put("otp", userModel.getAuthCode());
            jsonReq.put("file_name", fName);
            jsonReq.put("isencode", "Y");
            jsonReq.put("username", userModel.getUsername());
            jsonReq.put("content", encContent);
            jsonReq.put("emailid", userModel.getEmailId());
            jsonReq.put("is_Image", fileModel.getFileType());
            jsonReq.put("target_Dir", AppProps.getInstance().getCurrDir());
//            fileModel.setFileContent(encContent);

        } catch (FileNotFoundException e) {
            ErrorString = "Internal Error Occured!";
            ologger.info(e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            ologger.info(e.toString());
            ErrorString = "Internal Error Occured!";
            e.printStackTrace();
        } catch (Exception e) {
            ologger.info(e.toString());
            ErrorString = "Internal Error Occured!";

            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }



        return jsonReq;
    }

        public boolean downloadSelectedFile(FileModel fileModel) {
        ErrorString = "";
        boolean bool = false;
        ologger.info("Inside downloadSelectedFile Method");
        if(userModel!=null) {
            ologger.info("Users exists!");
            userModel.setFileModel(fileModel);
            userModel.setIsSharedOrPubFile(fileModel.getIsSharedStatus());
            ologger.info("File Data ---> " + fileModel.getFileName() + "  ---  " + fileModel.getFileType());
            JSONObject jsonReq = oLoginOrc.jsonReqFormation("getFile", userModel);
            ologger.info("JSON Request Formed is: " + jsonReq.toString());
            HashMap<String, Object> respHMap = connOrc.executeMethod(oInteg.getString("downloadFile_url"), jsonReq);
//            ologger.info("Response From the server ---> " + respHMap);
//            parseResponseToGetFile(respHMap);
            if(respHMap!=null) {
                HashMap resp = (HashMap) respHMap.get("body");
                if (resp.get("status")!=null && resp.get("status").toString().equalsIgnoreCase("200")) {

                    String url = resp.get("download_url").toString();
                    respHMap = connOrc.executeDownload(url, fileModel);
                    if(respHMap!=null && respHMap.get("code")!=null && respHMap.get("code").toString().equalsIgnoreCase("200")) {
                        ErrorString = "Succesfully Downloaded the File!";
                        bool = true;
                    } else {
                        ErrorString = "Error Connecting To the Server!";
                        ologger.info("Error Connecting To The Server!");
                    }
                }
            }
            if(ErrorString==null || ErrorString.trim().equalsIgnoreCase("")) {
                userModel.setStatusMessage("Error Connecting To the Server!");
                ologger.info("Error Connecting To The Server!");
            }
            AppProps.getInstance().getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AppProps.getInstance().getContext(), ErrorString, Toast.LENGTH_SHORT).show();
                }
            });
        }
        return bool;
    }

    public void parseResponseToGetFile(HashMap<String, Object> json) {
        try {

            if (json != null && json.size() > 0) {
                HashMap<String, Object> jsonResponse = (HashMap<String, Object>) json.get("body");
                if (jsonResponse != null && jsonResponse.size() > 0) {

                    if(jsonResponse.get("status")!=null && !jsonResponse.get("status").toString().isEmpty()) {
                        userModel.setStatusCode(jsonResponse.get("status").toString());

                        if(jsonResponse.get("status").toString().equalsIgnoreCase("200")) {

                            String file_content = jsonResponse.get("file_content").toString();
                            String fileName = jsonResponse.get("fileName").toString();
                            ologger.info("Received File  --->  " + fileName);
                            String is_Image = jsonResponse.get("is_Image").toString();

                            ologger.info("Saving File!");
                            saveFile(file_content, fileName, is_Image);
                            ologger.info("File Save Successfully!");

                        }
//                        ologger.info("Response From the server ---> " + json);
                    } else {
                        ologger.info("Response From the server ---> " + json);
                        userModel.setStatusCode("100");
                    }

                    if(jsonResponse.get("error_msg")!=null && !jsonResponse.get("error_msg").toString().isEmpty()) {
                        userModel.setStatusMessage(jsonResponse.get("error_msg").toString());
                        ologger.info("Response From the server ---> " + json);
                    } else if(jsonResponse.get("statusMsg")!=null && !jsonResponse.get("statusMsg").toString().isEmpty()) {
                        userModel.setStatusMessage(jsonResponse.get("statusMsg").toString());
                        ologger.info("Response From the server ---> " + json);
                    }

                } else {
                    ologger.info("Response From the server ---> " + json);
                    userModel.setStatusCode("100");
                }
            } else {
                ologger.info("Response From the server ---> " + json);
                userModel.setStatusCode("100");
            }

        } catch (Exception e) {
            e.printStackTrace();
            ologger.info(e.toString());
        }
    }

    private void saveFile(String encFileContent, String fileName, String isImg) {

        FileOutputStream fos = null;
        try {
            File rootFile = new File(rootPath);
            if(!rootFile.exists()) {
                rootFile.mkdirs();
            }

            String currFPath = rootPath + fileName;

            if(isImg.equalsIgnoreCase("Y")) {


    //            ByteArrayInputStream io = new ByteArrayInputStream(android.util.Base64.decode(encFileContent, android.util.Base64.DEFAULT));
    //            Bitmap img = Bitmap.read(io);
    //                io.close();
    //            } catch (IOException e) {
    //                e.printStackTrace();
    //            }
    //
    //            File opFile = new File(fileName);
    //            ImageIO.write(img, fileName.substring(fileName.lastIndexOf(".")+1), opFile);

                fos =  new FileOutputStream(new File(rootPath, fileName));//context.openFileOutput(currFPath, Context.MODE_PRIVATE);
                byte[] decodedString = android.util.Base64.decode(encFileContent, android.util.Base64.DEFAULT);
                fos.write(decodedString);
                fos.flush();
                fos.close();


            } else{

                byte[] decodedBytes = android.util.Base64.decode(encFileContent, android.util.Base64.DEFAULT);

                fos = new FileOutputStream(currFPath);
                fos.write(decodedBytes);
                fos.flush();
                fos.close();

            }
        } catch (FileNotFoundException e) {
            ErrorString = "Internal Error Occured!";
            ologger.info(e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            ologger.info(e.toString());
            ErrorString = "Internal Error Occured!";
            e.printStackTrace();
        } catch (Exception e) {
            ologger.info(e.toString());
            ErrorString = "Internal Error Occured!";

            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch(Exception e) {
                    fos = null;
                }
            }
        }

    }

    public List<FileModel> responseToUsrModMapping(List<FileModel> fileModelLst,HashMap<String, Object> json) {
        try {
            fileModelLst = new ArrayList<FileModel>();
            if (json != null && json.size() > 0) {
                HashMap<String, Object> jsonResponse = (HashMap<String, Object>) json.get("body");
                if (jsonResponse != null && jsonResponse.size() > 0) {

                    List jsonFileLst = (List) jsonResponse.get("files");
                    if (jsonFileLst != null && jsonFileLst.size() > 0) {
                        for (int i = 0; i < jsonFileLst.size(); i++) {
                            List fileDtlLst = (List) jsonFileLst.get(i);
                            FileModel fileModel = new FileModel();
                            if (fileDtlLst != null) {
                                fileModel.setFileName(fileDtlLst.get(0).toString());
                                fileModel.setFileType(fileDtlLst.get(1).toString());
                                Date fDate = sf.parse(fileDtlLst.get(2).toString());
                                fileModel.setFileTimestamp(fDate);
                                fileModel.setIsImage(fileDtlLst.get(3).toString());
                                fileModel.setIsPublic(fileDtlLst.get(4).toString());
                                fileModel.setSharedCont(fileDtlLst.get(5).toString());
                                fileModel.setIsSharedStatus(fileDtlLst.get(6).toString());
                                fileModelLst.add(fileModel);
                            }
                        }
                    }
                    if (jsonResponse.get("status") != null) {
                        userModel.setStatusCode(jsonResponse.get("status").toString());
                    } if(jsonResponse.get("error_msg")!=null) {
                        userModel.setStatusMessage(jsonResponse.get("error_msg").toString());
                    } if(jsonResponse.get("statusMsg")!=null) {
                        userModel.setStatusMessage(jsonResponse.get("statusMsg").toString());
                    }
                } else {
                    userModel.setStatusCode("100");
                }
            }


            new Thread(new Runnable() {
                @Override
                public void run() {
//                    if(userModel.getStatusMessage()!=null && !userModel.getStatusMessage().isEmpty()) {
//                        ErrorString = userModel.getStatusMessage();
//                    } else if(userModel.getStatusCode()!=null && userModel.getStatusCode().equalsIgnoreCase("200")) {
//                        ErrorString = "Successfully Uploaded the Files!";
//                    } else  {
//                        ErrorString = "Error Connecting to the Server!";
//                    }
//
//                    AppProps.getInstance().getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(oAppProps.getContext(), ErrorString, Toast.LENGTH_SHORT).show();
//                        }
//                    });
                    if(userModel.getStatusCode()!=null && (userModel.getStatusCode().equalsIgnoreCase("710")
                            || userModel.getStatusCode().equalsIgnoreCase("755"))) {
                        UserModel userModel1 = oLoginOrc.genLoginOTP(userModel);
                        AppProps.getInstance().getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(oAppProps.getContext(), "Need OTP Authentication!", Toast.LENGTH_SHORT).show();
                                otpValidator.otpCheckMethod("loginAction", userModel);
                            }
                        });
                    }
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileModelLst;
    }

    public boolean getThumbnailsData(List<Integer> posLst) {
//        userModel = oLoginOrc.validateLogin(context, mDatabase);
        List<FileModel> fileModelLst = new ArrayList<FileModel>();
        if(userModel!=null) {
            JSONObject jsonReq = jsonReqFormation("getThumbnailsData", posLst);
            HashMap<String, Object> respHMap = connOrc.executeMethod(oInteg.getString("getThumbFiles_url"), jsonReq);
            return parseRespToCreateCacheFile(respHMap);
        }
        return false;
    }

    private boolean parseRespToCreateCacheFile(HashMap<String, Object> respHMap) {

        try {
            if(respHMap!=null && respHMap.size()>0) {

                HashMap<String, Object> json = (HashMap<String, Object>) respHMap.get("body");
                if (json != null && json.size() > 0) {
                    if (json.get("status") != null && json.get("status").toString().equalsIgnoreCase("200")) {
                        List jsonLst = (List) json.get("filesLst");
                        if (jsonLst != null && jsonLst.size() > 0) {
                            for (int i = 0; i < jsonLst.size(); i++) {
                                try {
                                    HashMap fileData = (HashMap) jsonLst.get(i);
                                    String encFileContent = fileData.get("FileContent").toString();
                                    String fileName = fileData.get("Name").toString();

                                    byte[] decodedFileCont = android.util.Base64.decode(encFileContent, android.util.Base64.DEFAULT);
                                    File cacheDir = new File(rootPath + "/.thumbnails/");
                                    if(!cacheDir.exists()) {
                                        cacheDir.mkdirs();
                                    }
                                    String inpFile = cacheDir + "/" + fileName;
//                                    if(!inpFile.exists()) {
//                                        inpFile.mkdir();
//                                    }
                                    FileOutputStream fos = new FileOutputStream(inpFile);
                                    fos.write(decodedFileCont);
                                    fos.flush();
                                    fos.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        return true;
                    }

                }
            }

        } catch (Exception e) {
            ologger.error(e.toString());
            e.printStackTrace();
        }

        return false;
    }

    private JSONObject jsonReqFormation(String reqType, List<Integer> posLst) {
        JSONObject jsonReq = new JSONObject();
        switch (reqType) {
            case "getThumbnailsData":
                try {
                    List<FileModel> oFModel = oAppProps.getFilesList();
                    jsonReq.put("phone_no", userModel.getPhone_no());
                    jsonReq.put("otp", userModel.getAuthCode());
                    jsonReq.put("username", userModel.getUsername());
                    jsonReq.put("isencode", "Y");
                    for(int i=0; i<posLst.size(); i++) {
                        FileModel oFMod = oFModel.get(posLst.get(i));
//                        List<String> fListObj = new ArrayList<String>();
//                        fListObj.add(String.valueOf(posLst.get(i)));
//                        fListObj.add(oFMod.getFileName());
                        jsonReq.put("fListE" + String.valueOf(i),oFMod.getFileName());
                    }
//                    JSONArray resArr = new JSONArray(fList);
                    jsonReq.put("fListNo", posLst.size());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
        return jsonReq;
    }


    public static String getDirectory(String dir) {
        String pth = "";
        String [] paths = dir.split("\\|");
        for(int idx =0; idx<paths.length-1; idx++) {
            pth  += paths[idx] + "/";
        }
        return pth;
    }

    public static String getFileName(String file) {
        String[] fpaths = file.split("\\|");
//        String tmpFname = fpaths[fpaths.length - 1];
        return fpaths[fpaths.length - 1];
    }
}
