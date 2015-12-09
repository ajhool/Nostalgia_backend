package utils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nostalgia.persistence.model.Video;

/**
 * Created by alex on 10/30/15.
 */

/**
 * Created by alex on 8/30/15.
 */



public class VideoUploadTask extends Thread {

    private static final String server = "http://104.131.81.78";
  private static final   int port = 10004;
    private static final String UPLOAD_URL_METADATA= server + ":" + port+ "/api/v0/video/new";
    private static final String UPLOAD_URL_VIDDATA= server + ":" + port + "/api/v0/video/data";

    
    public static boolean uploadFileForVid(String vidId, String MD5, File toUpload) throws Exception{
        String charset = "UTF-8";
        HttpPost httppost= new HttpPost(UPLOAD_URL_VIDDATA + "?vidId=" + vidId + "&checksum=" + MD5);

        boolean exists = toUpload.exists();
        HttpClient a_client = new DefaultHttpClient();
        InputStreamEntity reqEntity = new InputStreamEntity(
                new FileInputStream(toUpload), -1);
        reqEntity.setContentType("binary/octet-stream");
        reqEntity.setChunked(true); // Send in multiple parts if needed
        httppost.setEntity(reqEntity);
        HttpResponse response = a_client.execute(httppost);
        return response.getStatusLine().getStatusCode() == 200;
    }

    public String sendFileToServer(String vidId, String MD5, File toUpload) {
        final String urlServer = UPLOAD_URL_VIDDATA + "?vidId=" + vidId + "&checksum=" + MD5;
        String response = "error";
        System.out.println("Image filename" + toUpload.toString());
        System.out.println("url" + urlServer);
        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;
        // DataInputStream inputStream = null;

        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        DateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024;
        try {
            FileInputStream fileInputStream = new FileInputStream(toUpload);

            URL url = new URL(urlServer);
            connection = (HttpURLConnection) url.openConnection();

            // Allow Inputs & Outputs
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setChunkedStreamingMode(1024);
            // Enable POST method
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type",
                    "multipart/form-data;boundary=" + boundary);

            outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);

            String connstr = null;
            connstr = "Content-Disposition: form-data; name=\"uploadedfile\";filename=\""
                    + toUpload.getName() + "\"" + lineEnd;
            System.out.println("Connstr" + connstr);

            outputStream.writeBytes(connstr);
            outputStream.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // Read file
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            System.out.println("Image length "+ bytesAvailable + "");
            try {
                while (bytesRead > 0) {
                    try {
                        outputStream.write(buffer, 0, bufferSize);
                    } catch (OutOfMemoryError e) {
                        e.printStackTrace();
                        response = "outofmemoryerror";
                        return response;
                    }
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }
            } catch (Exception e) {
                e.printStackTrace();
                response = "error";
                return response;
            }
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + twoHyphens
                    + lineEnd);

            // Responses from the server (code and message)
            int serverResponseCode = connection.getResponseCode();
            String serverResponseMessage = connection.getResponseMessage();
            System.out.println("Server Response Code " + "" + serverResponseCode);
            System.out.println("Server Response Message" + serverResponseMessage);

            if (serverResponseCode == 200) {
                response = "true";
            }

            String CDate = null;
            Date serverTime = new Date(connection.getDate());
            try {
                CDate = df.format(serverTime);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Date Exception" + e.getMessage() + " Parse Exception");
            }
            System.out.println("Server Response Time" + CDate + "");
//
//            String filename = CDate
//                    + filename.substring(filename.lastIndexOf("."),
//                    filename.length());
//            Log.i("File Name in Server : ", filename);

            fileInputStream.close();
            outputStream.flush();
            outputStream.close();
            outputStream = null;
        } catch (Exception ex) {
            // Exception handling
            response = "error";
            System.out.println("Send file Exception" + ex.getMessage() + "");
            ex.printStackTrace();
        }
        return response;
    }

    public static String uploadVidMetadata(Video metadata) throws Exception{
        String charset = "UTF-8";
        HttpPost httppost= new HttpPost(UPLOAD_URL_METADATA + "?auto=true");

        HttpClient a_client = new DefaultHttpClient();
        ObjectMapper om = new ObjectMapper();

        String videoAsJSON = om.writeValueAsString(metadata);
        StringEntity se = new StringEntity(videoAsJSON);
        //se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        httppost.setEntity(se); // Send in multiple parts if needed
        httppost.setHeader("Accept", "application/json");
        httppost.setHeader("Content-type", "application/json");
        HttpResponse response = a_client.execute(httppost);
        String uploaded = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
        return uploaded;
    }


    public void setFinishedListener(UploadTaskFinishedListener finishedListener) {
        this.finishedListener = finishedListener;
    }

    public interface UploadTaskFinishedListener {
        void onTaskFinished(); // If you want to pass something back to the listener add a param to this method
    }

    public static final String TAG = "LoadingTask";
    // This is the progress bar you want to update while the task is in progress
    private UploadTaskFinishedListener finishedListener;

    private final Video toUpload;
    private final String targetPath;




    public VideoUploadTask(String focusedFilePath, Video thisVideo) {
        this.toUpload = thisVideo;
        this.targetPath = focusedFilePath;
    }

    @Override
    public void run(){

////upload metadata
        String savedId = null;
        try {
           savedId = uploadVidMetadata(toUpload);
        } catch (Exception e) {
            e.printStackTrace();
        }

        File in = new File(targetPath);

        FileInputStream fis = null;
        String md5 = null;
        try {
            fis = new FileInputStream(in);
            long size = fis.getChannel().size();
         md5 = new String(Hex.encodeHex(DigestUtils.md5(fis)));
        fis.close();
        } catch (Exception e) {
        	System.err.println(TAG + "error generating md5" + e);
        }
        //using returned data, upload video
        try {

            if(!in.exists()) {
                throw new Exception("file not found");
            }
            boolean result = uploadFileForVid(savedId, md5, in);
            if(result){
                //upload was successful, can delete old file
                FileUtils.forceDelete(in);
            }

           // sendFileToServer(savedId, md5, in);
        } catch (Exception e) {
            System.err.println(TAG + "error uploading video" + e);
        }

        if(finishedListener != null) {
            finishedListener.onTaskFinished(); // Tell whoever was listening we have finished
        }
        return;
    }



  

    
}
