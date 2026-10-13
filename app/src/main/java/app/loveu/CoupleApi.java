package app.loveu;

import org.json.JSONObject;
import org.json.JSONException;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

/** Synchronous HTTPS relay client. Invoke only from a background worker. */
public class CoupleApi {
 public static final String BASE_URL=BuildConfig.RELAY_BASE_URL;
 private static final int MAX_BYTES=1024*1024;
 // Portable builds use system DNS, without a private deployment's fixed IP or DNS override.
 private static final okhttp3.OkHttpClient HTTP=new okhttp3.OkHttpClient.Builder().connectTimeout(5,java.util.concurrent.TimeUnit.SECONDS).readTimeout(10,java.util.concurrent.TimeUnit.SECONDS).callTimeout(25,java.util.concurrent.TimeUnit.SECONDS).followRedirects(false).build();
 private final String baseUrl;
 private final java.util.Set<okhttp3.Call> calls=java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<okhttp3.Call,Boolean>());
 private volatile boolean cancelled;
 public void cancel(){cancelled=true;for(okhttp3.Call call:calls)call.cancel();}
 public CoupleApi(){baseUrl=BASE_URL;}
 // Tests may inject a local HTTPS server; application callers use the build-time configured endpoint.
 CoupleApi(String baseUrl){
  try{URL url=new URL(baseUrl);if(!"https".equals(url.getProtocol())||url.getUserInfo()!=null||url.getQuery()!=null||url.getRef()!=null)throw new IllegalArgumentException("HTTPS endpoint required");}
  catch(MalformedURLException e){throw new IllegalArgumentException("HTTPS endpoint required");}
  this.baseUrl=baseUrl.endsWith("/")?baseUrl.substring(0,baseUrl.length()-1):baseUrl;
 }
 public static final class ApiException extends IOException {
  public final int status;
  public ApiException(int status){super(status==401?"登录凭证已失效，请重新连接":status==429?"请求较多，请稍后重试":"连接请求失败（"+status+"）");this.status=status;}
 }
 public JSONObject get(String path,String token)throws IOException{return request("GET",path,token,null);}
 public JSONObject post(String path,String token,JSONObject body)throws IOException{return request("POST",path,token,body);}
 public JSONObject delete(String path,String token)throws IOException{return request("DELETE",path,token,null);}
 public JSONObject request(String method,String path,String token,JSONObject body)throws IOException {
  if(!method.equals("GET")&&!method.equals("POST")&&!method.equals("DELETE"))throw new IllegalArgumentException("Unsupported request");
  if(path==null||!path.matches("/[a-z]+(?:/[a-z]+)*"))throw new IllegalArgumentException("Invalid relay path");
  if(token!=null&&!token.matches("[A-Za-z0-9_-]{1,512}"))throw new IllegalArgumentException("Invalid authorization");
  byte[] payload=body==null?new byte[0]:body.toString().getBytes(StandardCharsets.UTF_8);if(payload.length>MAX_BYTES)throw new IOException("请求内容过大");
  okhttp3.Request.Builder request=new okhttp3.Request.Builder().url(baseUrl+path).header("Accept","application/json");
  if(token!=null&&!token.isEmpty())request.header("Authorization","Bearer "+token);
  request.method(method,method.equals("POST")?okhttp3.RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),payload):null);
  if(cancelled||Thread.currentThread().isInterrupted())throw new java.io.InterruptedIOException("Cancelled");
  okhttp3.Call call=HTTP.newCall(request.build());calls.add(call);if(cancelled)call.cancel();
  try(okhttp3.Response response=call.execute()){
   int status=response.code();if(status<200||status>=300)throw new ApiException(status);
   okhttp3.ResponseBody bodyResponse=response.body();if(bodyResponse==null)return new JSONObject();if(bodyResponse.contentLength()>MAX_BYTES)throw new IOException("服务器响应过大");
   ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(InputStream in=bodyResponse.byteStream()){byte[] buffer=new byte[8192];int count;while((count=in.read(buffer))!=-1){if(bytes.size()+count>MAX_BYTES)throw new IOException("服务器响应过大");bytes.write(buffer,0,count);}}
   if(bytes.size()==0)return new JSONObject();try{return new JSONObject(new String(bytes.toByteArray(),StandardCharsets.UTF_8));}catch(JSONException invalid){throw new IOException("服务器响应格式错误");}
  }finally{calls.remove(call);}
 }
}