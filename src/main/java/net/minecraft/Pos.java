package net.minecraft;

public class Pos implements Comparable<Pos> {
   public int x;
   public int y;
   public int z;

   public Pos() {
   }

   public Pos(int var1, int var2, int var3) {
      this.x = var1;
      this.y = var2;
      this.z = var3;
   }

   public boolean equals(Object var1) {
      if (!(var1 instanceof Pos)) {
         return false;
      } else {
         Pos var2 = (Pos)var1;
         return this.x == var2.x && this.y == var2.y && this.z == var2.z;
      }
   }

   public int hashCode() {
      return this.x + this.z << 8 + this.y << 16;
   }

   public int compareTo(Pos var1) {
      if (this.y == var1.y) {
         return this.z == var1.z ? this.x - var1.x : this.z - var1.z;
      } else {
         return this.y - var1.y;
      }
   }

   public Pos offset(int var1, int var2, int var3) {
      return new Pos(this.x + var1, this.y + var2, this.z + var3);
   }

   public void set(int var1, int var2, int var3) {
      this.x = var1;
      this.y = var2;
      this.z = var3;
   }

   public void set(Pos var1) {
      this.x = var1.x;
      this.y = var1.y;
      this.z = var1.z;
   }

   public Pos above() {
      return new Pos(this.x, this.y + 1, this.z);
   }

   public Pos above(int var1) {
      return new Pos(this.x, this.y + var1, this.z);
   }

   public Pos below() {
      return new Pos(this.x, this.y - 1, this.z);
   }

   public Pos below(int var1) {
      return new Pos(this.x, this.y - var1, this.z);
   }

   public Pos north() {
      return new Pos(this.x, this.y, this.z - 1);
   }

   public Pos north(int var1) {
      return new Pos(this.x, this.y, this.z - var1);
   }

   public Pos south() {
      return new Pos(this.x, this.y, this.z + 1);
   }

   public Pos south(int var1) {
      return new Pos(this.x, this.y, this.z + var1);
   }

   public Pos west() {
      return new Pos(this.x - 1, this.y, this.z);
   }

   public Pos west(int var1) {
      return new Pos(this.x - 1, this.y, this.z);
   }

   public Pos east() {
      return new Pos(this.x + 1, this.y, this.z);
   }

   public Pos east(int var1) {
      return new Pos(this.x + var1, this.y, this.z);
   }

   public void move(int var1, int var2, int var3) {
      this.x += var1;
      this.y += var2;
      this.z += var3;
   }

   public void move(Pos var1) {
      this.x += var1.x;
      this.y += var1.y;
      this.z += var1.z;
   }

   public void moveX(int var1) {
      this.x += var1;
   }

   public void moveY(int var1) {
      this.y += var1;
   }

   public void moveZ(int var1) {
      this.z += var1;
   }

   public void moveUp(int var1) {
      this.y += var1;
   }

   public void moveUp() {
      ++this.y;
   }

   public void moveDown(int var1) {
      this.y -= var1;
   }

   public void moveDown() {
      --this.y;
   }

   public void moveEast(int var1) {
      this.x += var1;
   }

   public void moveEast() {
      ++this.x;
   }

   public void moveWest(int var1) {
      this.x -= var1;
   }

   public void moveWest() {
      --this.x;
   }

   public void moveNorth(int var1) {
      this.z -= var1;
   }

   public void moveNorth() {
      --this.z;
   }

   public void moveSouth(int var1) {
      this.z += var1;
   }

   public void moveSouth() {
      ++this.z;
   }
}
