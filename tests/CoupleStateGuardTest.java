package app.loveu;

/** Synthetic delayed-response regression: an old worker must not restore a removed peer/queue. */
public final class CoupleStateGuardTest {
 interface Attempt {void run();}
 private static void rejected(Attempt action){try{action.run();throw new AssertionError("stale write accepted");}catch(SecurityException expected){}}
 public static void main(String[] args){
  long accountRevision=CoupleStateGuard.nextRevision(0,0,false);
  if(accountRevision!=1)throw new AssertionError("first account write");
  long unpairRevision=CoupleStateGuard.nextRevision(accountRevision,accountRevision,false);
  rejected(()->CoupleStateGuard.nextRevision(accountRevision,unpairRevision,false));
  rejected(()->CoupleStateGuard.nextRevision(unpairRevision,unpairRevision,true));
  rejected(()->CoupleStateGuard.nextRevision(0,0,true));
  rejected(()->CoupleStateGuard.nextRevision(-1,-1,false));
  rejected(()->CoupleStateGuard.nextRevision(Long.MAX_VALUE,Long.MAX_VALUE,false));
  if(CoupleStateGuard.nextRevision(unpairRevision,unpairRevision,false)!=3)throw new AssertionError("fresh worker cannot persist");
  System.out.println("Couple state guard regressions passed");
 }
}
