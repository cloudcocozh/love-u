package app.loveu;
/** A drag/cancel never becomes a tap even if the pointer returns to its start. */
public final class TapGesture {
 private final float slop;private float x,y;private boolean active;
 public TapGesture(float slop){this.slop=slop;}
 public void start(float x,float y){this.x=x;this.y=y;active=true;}
 public void move(float x,float y){if(Math.abs(x-this.x)>slop||Math.abs(y-this.y)>slop)active=false;}
 public void cancel(){active=false;}
 public boolean finish(float x,float y){move(x,y);boolean tap=active;active=false;return tap;}
}
