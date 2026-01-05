package net.minecraft.client;

public class CrashReport {
   public final String title;
   public final Throwable e;

   public CrashReport(String title, Throwable e) {
      this.title = title;
      this.e = e;
   }
}
