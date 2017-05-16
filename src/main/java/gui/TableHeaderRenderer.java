package gui;

/**
 * Created by David on 14-May-17.
 */

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ResourceBundle;

/**
 * Renderer class for viewing translator table head
 */
public class TableHeaderRenderer extends DefaultTableCellRenderer {

    DefaultTableCellRenderer renderer;

    public TableHeaderRenderer(JTable table) {
        renderer = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
        renderer.setHorizontalAlignment(JLabel.CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        Component comp = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        renderer.setText((ResourceBundle.getBundle("GUI_names").getString((String)value)));
        return comp;
    }
}
