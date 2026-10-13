package app.loveu;

import android.content.Context;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.webrtc.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/** Optional foreground data transport. Signals must be authenticated by CoupleCrypto first.
 * No STUN/TURN service is contacted. Delivery still requires the caller's durable application ACK.
 * Listener callbacks run on our worker and must never block on this object's send(). */
public final class DirectChannel {
 public interface Listener {
  void onSignal(JSONObject signal);
  void onEnvelope(JSONObject envelope);
  void onOpen(boolean actualDirect);
  void onClosed();
 }
 private final Context context;
 private final boolean offerer;
 private final Listener listener;
 private final ScheduledExecutorService worker=Executors.newSingleThreadScheduledExecutor();
 private final List<IceCandidate> pending=new ArrayList<>(), outgoing=new ArrayList<>();
 private PeerConnectionFactory factory;
 private PeerConnection peer;
 private DataChannel channel;
 private String session;
 private long expires;
 private boolean started,closed,remoteReady,remoteSdpSeen,localSdpSent,batchScheduled,openReported;
 private volatile Thread workerThread;
 private static boolean initialized;
 private static final int MAX_BYTES=48*1024, MAX_CANDIDATES=64;
 public DirectChannel(Context context,boolean offerer,Listener listener){this.context=context.getApplicationContext();this.offerer=offerer;this.listener=listener;}
 private void execute(Runnable task){try{worker.execute(()->{workerThread=Thread.currentThread();if(!closed)try{task.run();}catch(Exception e){Log.w("DirectChannel","worker failure: "+e.getClass().getSimpleName());finish();}});}catch(RejectedExecutionException ignored){}}
 public void start(){execute(()->{
  if(started)return;started=true;
  synchronized(DirectChannel.class){if(!initialized){PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions.builder(context).createInitializationOptions());initialized=true;}}
  factory=PeerConnectionFactory.builder().createPeerConnectionFactory();
  PeerConnection.RTCConfiguration config=new PeerConnection.RTCConfiguration(Collections.emptyList());
  peer=factory.createPeerConnection(config,new PeerConnection.Observer(){
   public void onSignalingChange(PeerConnection.SignalingState state){Log.d("DirectChannel","signaling "+state);}
   public void onIceConnectionChange(PeerConnection.IceConnectionState state){Log.d("DirectChannel","ICE "+state);if(state==PeerConnection.IceConnectionState.FAILED||state==PeerConnection.IceConnectionState.CLOSED)close();}
   public void onIceConnectionReceivingChange(boolean receiving){}
   public void onIceGatheringChange(PeerConnection.IceGatheringState state){Log.d("DirectChannel","gathering "+state);if(state==PeerConnection.IceGatheringState.COMPLETE)execute(()->flushCandidates());}
   public void onIceCandidate(IceCandidate candidate){execute(()->{if(outgoing.size()<MAX_CANDIDATES)outgoing.add(candidate);if(!batchScheduled){batchScheduled=true;worker.schedule(()->execute(()->flushCandidates()),400,TimeUnit.MILLISECONDS);}});}
   public void onIceCandidatesRemoved(IceCandidate[] candidates){}
   public void onAddStream(MediaStream stream){}
   public void onRemoveStream(MediaStream stream){}
   public void onDataChannel(DataChannel data){execute(()->attach(data));}
   public void onRenegotiationNeeded(){}
  });
  if(peer==null){finish();return;}peer.setAudioRecording(false);peer.setAudioPlayout(false);
  worker.schedule(()->execute(()->{if(!openReported)finish();}),15,TimeUnit.SECONDS);
  if(offerer){session=UUID.randomUUID().toString();expires=System.currentTimeMillis()+60000;attach(peer.createDataChannel("love-u",new DataChannel.Init()));createSdp(true);}
 });}
 private abstract class Sdp implements SdpObserver {
  public void onCreateSuccess(SessionDescription sdp){}
  public void onSetSuccess(){}
  public void onCreateFailure(String error){Log.w("DirectChannel","SDP creation failed");close();}
  public void onSetFailure(String error){Log.w("DirectChannel","SDP installation failed");close();}
 }
 private void createSdp(boolean offer){Sdp callback=new Sdp(){public void onCreateSuccess(SessionDescription sdp){execute(()->peer.setLocalDescription(new Sdp(){public void onSetSuccess(){execute(()->{try{listener.onSignal(signal(offer?"offer":"answer").put("sdp",sdp.description));localSdpSent=true;flushCandidates();}catch(Exception e){finish();}});}},sdp));}};if(offer)peer.createOffer(callback,new MediaConstraints());else peer.createAnswer(callback,new MediaConstraints());}
 private JSONObject signal(String type)throws Exception{return new JSONObject().put("sessionId",session).put("generation",1).put("expiresAt",expires).put("type",type);}
 private void flushCandidates(){batchScheduled=false;if(!localSdpSent||session==null||outgoing.isEmpty()||System.currentTimeMillis()>expires)return;try{JSONArray items=new JSONArray();for(IceCandidate c:outgoing)items.put(new JSONObject().put("mid",c.sdpMid).put("line",c.sdpMLineIndex).put("candidate",c.sdp));outgoing.clear();listener.onSignal(signal("ice").put("candidates",items));}catch(Exception e){finish();}}
 public void receiveSignal(JSONObject input){final JSONObject copy;try{copy=new JSONObject(input.toString());}catch(Exception e){return;}execute(()->{
  if(!started||peer==null||copy.toString().length()>MAX_BYTES)return;
  String id=copy.optString("sessionId"),type=copy.optString("type");long until=copy.optLong("expiresAt"),now=System.currentTimeMillis();
  if(!id.matches("[a-fA-F0-9-]{36}")||copy.optInt("generation")!=1||until<now||until>now+90000)return;
  if("offer".equals(type)){if(offerer||session!=null)return;session=id;expires=until;}
  if(session==null||!session.equals(id)||until!=expires)return;
  if("offer".equals(type)||"answer".equals(type)){
   if(remoteSdpSeen||("answer".equals(type)&&!offerer))return;
   String text=copy.optString("sdp");if(text.isEmpty()||text.length()>32768)return;remoteSdpSeen=true;
   peer.setRemoteDescription(new Sdp(){public void onSetSuccess(){execute(()->{remoteReady=true;for(IceCandidate c:pending)peer.addIceCandidate(c);pending.clear();if(!offerer)createSdp(false);});}},new SessionDescription("offer".equals(type)?SessionDescription.Type.OFFER:SessionDescription.Type.ANSWER,text));
  }else if("ice".equals(type)){
   JSONArray candidates=copy.optJSONArray("candidates");if(candidates==null||candidates.length()>MAX_CANDIDATES)return;
   for(int i=0;i<candidates.length();i++){JSONObject c=candidates.optJSONObject(i);if(c==null)continue;String value=c.optString("candidate");int line=c.optInt("line",-1);if(value.length()>4096||!value.startsWith("candidate:")||line<0||line>4)continue;IceCandidate candidate=new IceCandidate(c.optString("mid"),line,value);if(remoteReady)peer.addIceCandidate(candidate);else if(pending.size()<MAX_CANDIDATES)pending.add(candidate);}
  }
 });}
 private void attach(DataChannel data){if(data==null||channel!=null){if(data!=null)data.dispose();return;}channel=data;data.registerObserver(new DataChannel.Observer(){
  public void onBufferedAmountChange(long previous){}
  public void onStateChange(){execute(()->checkChannelState());}
  public void onMessage(DataChannel.Buffer buffer){if(buffer.data.remaining()>MAX_BYTES)return;byte[] bytes=new byte[buffer.data.remaining()];buffer.data.get(bytes);execute(()->{try{listener.onEnvelope(new JSONObject(new String(bytes,StandardCharsets.UTF_8)));}catch(Exception ignored){}});}
 });checkChannelState();}
 private void checkChannelState(){if(channel==null)return;DataChannel.State state=channel.state();Log.d("DirectChannel","channel "+state);if(state==DataChannel.State.OPEN&&!openReported){openReported=true;/* No ICE relay servers are configured, hence OPEN necessarily uses a direct candidate. */listener.onOpen(true);}else if(state==DataChannel.State.CLOSED)finish();}
 private boolean sendNow(JSONObject envelope){if(closed||channel==null||channel.state()!=DataChannel.State.OPEN||channel.bufferedAmount()>256*1024)return false;byte[] bytes=envelope.toString().getBytes(StandardCharsets.UTF_8);return bytes.length<=MAX_BYTES&&channel.send(new DataChannel.Buffer(ByteBuffer.wrap(bytes),false));}
 public boolean send(JSONObject envelope){try{final JSONObject copy=new JSONObject(envelope.toString());if(Thread.currentThread()==workerThread)return sendNow(copy);return worker.submit(()->sendNow(copy)).get(2,TimeUnit.SECONDS);}catch(Exception e){return false;}}
 public void close(){execute(()->finish());}
 private void finish(){if(closed)return;closed=true;try{if(channel!=null){channel.unregisterObserver();channel.close();channel.dispose();}if(peer!=null){peer.close();peer.dispose();}if(factory!=null)factory.dispose();}finally{worker.shutdownNow();listener.onClosed();}}
}
