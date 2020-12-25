package com.infotrends.in.smartsave.orchestrator;

import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.OpenableColumns;

import com.infotrends.in.smartsave.models.FileModel;
import com.infotrends.in.smartsave.utils.AppProps;
import com.infotrends.in.smartsave.utils.IntegProperties;
import com.infotrends.in.smartsave.utils.LoggerFile;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class ConnectionOrchestrator {

    private LoggerFile ologger = LoggerFile.createInstance(ConnectionOrchestrator.class);
    private IntegProperties oInteg = IntegProperties.getInstance();
    public String rootPath = Environment.getExternalStorageDirectory()
            .getAbsolutePath()  +  oInteg.getString("app_base_dir") + oInteg.getString("app_download_media_dir");

    public HashMap<String, Object> executeMethod(String urlStr,JSONObject jsonRequest) {
        HashMap<String, Object> ResponseMap = new HashMap<String, Object>();
        try {
            System.out.println(jsonRequest);
            URL url = new URL(urlStr);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "*/*");
            conn.setDoOutput(true);
            conn.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
//			OutputStreamWriter req = new OutputStreamWriter(conn.getOutputStream());
//			req.write(jsonRequest.toString());
//			conn.getOutputStream().write(jsonRequest.toString());
            String jsonInputString = jsonRequest.toString();
            try(OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int status = conn.getResponseCode();

            InputStream inputStream;
            if (status != HttpURLConnection.HTTP_OK)  {
                inputStream = conn.getErrorStream();
            }
            else  {
                inputStream = conn.getInputStream();
            }

            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(inputStream, "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println(response.toString());
                ResponseMap = toHashMap(new JSONObject(response.toString()));
            }

        }catch (JSONException e) {
            ResponseMap.put("status", 101);
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MalformedURLException e) {
            ResponseMap.put("status", "100");
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            ResponseMap.put("status", "100");
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            ResponseMap.put("status", "100");
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return ResponseMap;
    }

    public HashMap<String, Object> executeDownload(String urlStr, FileModel fileModel) {
        HashMap<String, Object> ResponseMap = new HashMap<String, Object>();
        try {
            URL url = new URL(urlStr);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
//            conn.setRequestProperty("Content-Type", "application/json");
//            conn.setRequestProperty("Accept", "*/*");
            conn.setDoOutput(false);
//            conn.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
//			OutputStreamWriter req = new OutputStreamWriter(conn.getOutputStream());
//			req.write(jsonRequest.toString());
//			conn.getOutputStream().write(jsonRequest.toString());
//            String jsonInputString = "";
//            try(OutputStream os = conn.getOutputStream()) {
//                byte[] input = jsonInputString.getBytes("utf-8");
//                os.write(input, 0, input.length);
//            }

            int status = conn.getResponseCode();

            InputStream inputStream;
            if (status != HttpURLConnection.HTTP_OK)  {
                inputStream = conn.getErrorStream();
            }
            else  {
                inputStream = conn.getInputStream();
            }

//            try(BufferedReader br = new BufferedReader(
//                    new InputStreamReader(inputStream, "utf-8"))) {
//                StringBuilder response = new StringBuilder();
//                String responseLine = null;
//                while ((responseLine = br.readLine()) != null) {
//                    response.append(responseLine.trim());
//                }
//
//            }

            if (status == HttpURLConnection.HTTP_OK) {
                ResponseMap.put("code", String.valueOf(status));
                String fName = fileModel.getFileName();
                FileOutputStream fos = new FileOutputStream(new File(rootPath, fName));
                try {
//                    String [] paths = fName.split("\\|");
                    String pth = FilesModOrc.getDirectory(fName);
//                    for(int idx =0; idx<paths.length-1; idx++) {
//                        pth  += paths[idx] + "/";
//                    }
                    String fileName = FilesModOrc.getFileName(fName);
                    if(fileModel.getIsSharedStatus()!=null &&
                            (fileModel.getIsSharedStatus().equalsIgnoreCase("P") || fileModel.getIsSharedStatus().equalsIgnoreCase("S"))) {
                        pth = "Shared Files/" + pth;
                    }
                    File newDir = new File(rootPath + pth);

                    if(!newDir.exists()) {
                        newDir.mkdirs();
                    }
                    File downFile = new File(rootPath + pth, fileName);
                    if(downFile.exists()) {
                        Files.copy(inputStream, Paths.get(rootPath + pth, fileName), StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        Files.copy(inputStream, Paths.get(rootPath + pth, fileName));
                    }
//                        fis = inputStream;
//                    byte[] fContents = new byte[inputStream.available()];
//                    fos = new FileOutputStream(new File(rootPath, fName));
//                    int rd;
//                    while((rd = inputStream.read(fContents))!= -1) {
//                        fos.write(fContents, 0, rd);
//                    }
                    fos.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                    ResponseMap.put("code", "100");
                }finally {
                    fos.close();
                }

            }
            else {
//                System.out.println(response.toString());
//                    ResponseMap = toHashMap(new JSONObject(response.toString()));
            }

        }catch (MalformedURLException e) {
            ResponseMap.put("status", "100");
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            ResponseMap.put("status", "100");
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            ResponseMap.put("status", "100");
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return ResponseMap;
    }


    public HashMap executes(String urlString , FileModel oFileModel) {
        HashMap<String, Object> resp = new HashMap<String, Object>();

        String fileName = oFileModel.getFileName();
        Uri fUri = oFileModel.getFileURI();

        try {

            HttpClient httpclient = new DefaultHttpClient();
            HttpPut httpput = new HttpPut(urlString);
            InputStream inputStream = (FileInputStream) AppProps.getInstance().getActivity().getContentResolver().openInputStream(fUri);
            InputStreamEntity entity = new InputStreamEntity(inputStream, inputStream.available());

            httpput.setEntity(entity);
            HttpResponse response = httpclient.execute(httpput);
            int resCode = response.getStatusLine().getStatusCode();
            System.out.println("Response: "+ response.getStatusLine().getStatusCode());
            resp.put("code", (Object) resCode);
            resp.put("reponse", response.toString());
            System.out.println(response.toString());
            ologger.info("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
            ologger.info(String.valueOf(resCode));
            ologger.info(response.toString());
            ologger.info("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");

//            URL url = new URL (urlString + "/");
//            HttpURLConnection con = (HttpURLConnection)url.openConnection();
////            con.setRequestMethod("PUT");
////			con.setRequestProperty("Connection","close");
////			con.setRequestProperty("Accept", "application/json");
//
////			System.out.println("The Reuest Sent is ----->" + oJSONObject.toString());
////            con.setDoInput(true);
//            con.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
//            con.setRequestProperty("Connection", "Keep-Alive");
//            con.setRequestProperty("Content-Type", "application/octet-stream");
//            con.setDoOutput(true);
//            con.setRequestMethod("PUT");
//            try(DataOutputStream os = new DataOutputStream(con.getOutputStream())) {
////                File file = new File(fileName);
//                byte[] buf = new byte[1024];
//                int count;
//                int total = 0;
//                long fileSize = getFileSize(fUri);
////			    byte[] input = oJSONObject.toString().getBytes("utf-8");
////			    os.writeBytes(oJSONObject.toString());
//                InputStream inputStream = (FileInputStream) AppProps.getInstance().getActivity().getContentResolver().openInputStream(fUri);;//new FileInputStream(file);
//                while ((count =inputStream.read(buf)) != -1)
//                {
//                    if (Thread.interrupted())
//                    {
//                        throw new InterruptedException();
//                    }
//                    os.write(buf, 0, count);
//                    total += count;
//                    int pctComplete = new Double(new Double(total) / new Double(fileSize) * 100).intValue();
//
//                    System.out.print("\r");
//                    System.out.print(String.format("PCT Complete: %d", pctComplete));
//                }
//
//                os.close();
//            }
////			os.close();
//
//            con.setRequestMethod("PUT");
////            con.connect();
//            int resCode = con.getResponseCode();
//            resp.put("code", (Object) resCode);
//            InputStream is;
//            try {
//                is = con.getInputStream();
//            }catch(IOException e) {
//                is = con.getErrorStream();
//            }
//            try(BufferedReader br = new BufferedReader( new InputStreamReader(is, "utf-8"))) {
//                StringBuilder response = new StringBuilder();
//                String responseLine = null;
//                while ((responseLine = br.readLine()) != null) {
//                    response.append(responseLine.trim());
//                }
//                System.out.println(response.toString());
//                ologger.info("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
//                ologger.info(String.valueOf(resCode));
//                ologger.info(response.toString());
//                ologger.info("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
//                try {
//                    if(response!=null && !response.toString().equalsIgnoreCase("")) {
//                        resp.put("reponse", response.toString());
//                        //toHashMap(new JSONObject(response.toString()));
//                    }
//                } catch (Exception e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//            }


        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        return resp;
    }

    public int getFileSize(Uri uri) {
        int result = 0;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = AppProps.getInstance().getActivity().getContentResolver().query(uri, null, null, null, null);
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


    public HashMap<String, Object> toHashMap(JSONObject json) throws JSONException {
        HashMap<String, Object> respObj = new HashMap<String, Object>();
        if(json!=null) {

            Iterator<String> itr = json.keys();
            while (itr.hasNext()) {
                String key = itr.next();
                Object jsonObj = json.get(key);
                if(jsonObj instanceof JSONArray) {
                    respObj.put(key, toList((JSONArray) jsonObj));
                } else if(jsonObj instanceof JSONObject) {
                    respObj.put(key, toHashMap((JSONObject) jsonObj));
                } else if(jsonObj!=null) {
                    respObj.put(key, jsonObj);
                }
            }

        }
        return respObj;
    }

    private List toList(JSONArray jsonObj) throws JSONException {
        List<Object> RespLst = new ArrayList();
        for(int i=0; i<jsonObj.length(); i++) {
            Object jObj = jsonObj.get(i);
            if (jObj instanceof JSONArray) {
                RespLst.add(toList((JSONArray) jObj));
            } else if (jObj instanceof JSONObject) {
                RespLst.add(toHashMap((JSONObject) jObj));
            } else if(jObj!=null) {
                RespLst.add(jObj);
            }
        }
        return RespLst;
    }

}
