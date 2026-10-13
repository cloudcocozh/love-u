package app.loveu;
public final class CoupleSyncFlowTest {
 public static void main(String[] args) throws Exception {
  StringBuilder order=new StringBuilder();
  try { CoupleSyncFlow.run(()->order.append("persist-ack;"),()->{order.append("send;");throw new Exception("queue full");});throw new AssertionError("failure hidden"); }
  catch(Exception expected){if(!"queue full".equals(expected.getMessage()))throw expected;}
  if(!"persist-ack;send;".equals(order.toString()))throw new AssertionError("outgoing failure blocked receipt");
  order.setLength(0);
  try { CoupleSyncFlow.run(()->{throw new Exception("untrusted inbox");},()->order.append("sent"));throw new AssertionError("failure hidden"); }
  catch(Exception expected){if(!"untrusted inbox".equals(expected.getMessage()))throw expected;}
  if(order.length()!=0)throw new AssertionError("receive security failure ignored");
  System.out.println("CoupleSyncFlowTest PASS");
 }
}