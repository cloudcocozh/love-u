package app.loveu;

/** Receive durably before attempting outbound delivery, which may be blocked. */
final class CoupleSyncFlow {
 interface Step { void run() throws Exception; }
 static void run(Step receive, Step send) throws Exception { receive.run(); send.run(); }
}