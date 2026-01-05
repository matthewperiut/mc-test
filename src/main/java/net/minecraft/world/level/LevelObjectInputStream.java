package net.minecraft.world.level;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.HashSet;
import java.util.Set;

public class LevelObjectInputStream extends ObjectInputStream {
   private Set<String> autoReplacers = new HashSet();

   public LevelObjectInputStream(InputStream var1) throws IOException {
      super(var1);
      this.autoReplacers.add("com.mojang.minecraft.player.Player$1");
      this.autoReplacers.add("com.mojang.minecraft.mob.Creeper$1");
      this.autoReplacers.add("com.mojang.minecraft.mob.Skeleton$1");
   }

   protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
      ObjectStreamClass var1 = super.readClassDescriptor();
      return this.autoReplacers.contains(var1.getName()) ? ObjectStreamClass.lookup(Class.forName(var1.getName())) : var1;
   }
}
