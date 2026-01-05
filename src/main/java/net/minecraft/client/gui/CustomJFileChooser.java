package net.minecraft.client.gui;

import java.awt.Component;
import java.awt.HeadlessException;
import javax.swing.JDialog;
import javax.swing.JFileChooser;

public class CustomJFileChooser extends JFileChooser {
   private static final long serialVersionUID = -5288046571202355447L;
   private JDialog dlg = null;

   public CustomJFileChooser() {
   }

   protected JDialog createDialog(Component parent) throws HeadlessException {
      this.dlg = super.createDialog(parent);
      this.dlg.setLocationRelativeTo(parent);
      this.dlg.setAlwaysOnTop(true);
      return this.dlg;
   }
}
