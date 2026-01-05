package util;

public interface ProgressListener {
   void progressStartNoAbort(String var1);

   void progressStart(String var1);

   void progressStage(String var1);

   void progressStagePercentage(int var1);
}
