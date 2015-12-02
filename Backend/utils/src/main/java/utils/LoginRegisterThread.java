package utils;

/**
 * Created by alex on 11/7/15.
 */

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nostalgia.persistence.model.LoginResponse;
import com.nostalgia.persistence.model.User;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.geojson.Point;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class LoginRegisterThread extends Thread {
    private String mEmail;
    private String mUsername;
    private String mPassword;
    private String oAuth;
    private final boolean isRegister;
    String server = "http://104.131.81.78";
    int port = 10004;

    private final Point location;
    private User mNoob;
    private LoginResponse loginResponse;
    private final String type;
	private String id;

    public enum LoginTypes {
        facebook, nostalgia, google
    }

    public LoginRegisterThread(String email, String uname, String pass, String type, boolean isRegister, Point location, String string) {
        mEmail = email;
        mUsername = uname;
        mPassword = pass;
        this.type = type;
        this.isRegister = isRegister;
        this.id = string;
       
        this.location = location;

    }


    public void run() {
        mNoob = new User();
        mNoob.setEmail(mEmail);
        mNoob.setName(mUsername);
        mNoob.setToken(oAuth);
        mNoob.set_id(id);
        mNoob.setLastKnownLoc(location);
        ArrayList<String> devices = new ArrayList<String>();
        devices.add("39bb3308-f1de-4794-bd12-1b05aecafba5");
        mNoob.setAuthorizedDevices(devices);
        if(mPassword != null) {
            mNoob.setPassword(new String(Hex.encodeHex(DigestUtils.sha512(mPassword))));
        }
        
        System.out.println("registering new user: " + mNoob.getName());

        ObjectMapper mapper = new ObjectMapper();
        try {
            System.out.println(mapper.writeValueAsString(mNoob));
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        HttpPost httpPost = null;
        if(isRegister){
            httpPost = new HttpPost(server + ":" + port + "/api/v0/user/register?type=" + type);
        } else {
            httpPost = new HttpPost(server + ":" + port + "/api/v0/user/login?type=" + type);
        }
        try {
            httpPost.setEntity(new StringEntity(mapper.writeValueAsString(mNoob)));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");


        HttpResponse resp = null;
        try {
            resp = new DefaultHttpClient().execute(httpPost);
        } catch (ClientProtocolException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }


        try {
            if(resp.getEntity()!= null) {
                String contents = IOUtils.toString(resp.getEntity().getContent(), "UTF-8");
                loginResponse = mapper.readValue(contents, LoginResponse.class);
            }

        } catch (JsonParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        if(loginResponse != null){
            System.out.println( "User registered!");
        } else {
        	System.out.println(  "User NOT registered!");
        }
    }

    public LoginResponse getLoginResponse() {
        return loginResponse;
    }
}



