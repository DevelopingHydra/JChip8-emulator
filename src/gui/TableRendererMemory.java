package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Created by xeniu on 07.04.2017.
 */
public class TableRendererMemory extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable jTable, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
//        Component component = super.getTableCellRendererComponent(jTable, value, isSelected, hasFocus, row, col);
        if (col == 0) {
            JLabel l = new JLabel((String) value);
            return l;
        }

        JTextField tf = new JTextField((String) value);
        tf.setBorder(null);
//        tf.setHorizontalAlignment(SwingConstants.CENTER);
        return tf;
    }
}
