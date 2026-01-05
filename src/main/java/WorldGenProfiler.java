import java.io.File;
import net.minecraft.world.level.Level;

public class WorldGenProfiler {
   private Level level;
   private File workDir = new File("leveldata");

   public WorldGenProfiler() {
   }

   public void run() {
      if (!this.workDir.exists()) {
         this.workDir.mkdir();
      }

      String var1 = "Test";
      System.out.print("Deleting level..");
      long var2 = System.currentTimeMillis();
      Level.deleteLevel(this.workDir, var1);
      long var4 = System.currentTimeMillis();
      System.out.println(" " + (var4 - var2) + " ms");
      System.out.print("Creating level..");
      var2 = System.currentTimeMillis();
      this.selectLevel(var1);
      var4 = System.currentTimeMillis();
      System.out.println(" " + (var4 - var2) + " ms");
   }

   public void selectLevel(String var1) {
      this.level = new Level(new File(this.workDir, "saves"), var1);
      this.prepareLevel();

      while(this.level.getLightsToUpdate() > 0) {
         this.level.updateLights();
      }

   }

   private void prepareLevel() {
      short var1 = 128;
      int var2 = this.level.xSpawn;
      int var3 = this.level.zSpawn;

      for(int var4 = -var1; var4 <= var1; var4 += 16) {
         for(int var5 = -var1; var5 <= var1; var5 += 16) {
            this.level.getTile(var2 + var4, 64, var3 + var5);
         }
      }

   }

   public static void main(String[] var0) {
      (new WorldGenProfiler()).run();
   }
}
