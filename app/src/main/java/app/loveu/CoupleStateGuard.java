package app.loveu;

/** Compare-and-set rule shared by encrypted couple persistence and desktop regression tests. */
public final class CoupleStateGuard {
 private CoupleStateGuard(){}
 public static long nextRevision(long expected,long current,boolean closed){
  if(closed)throw new SecurityException("双人页面已关闭，已停止保存连接状态。");
  if(expected<0||current<0||expected!=current||current==Long.MAX_VALUE)
   throw new SecurityException("连接状态已更新，请重新打开双人空间。");
  return current+1;
 }
}
