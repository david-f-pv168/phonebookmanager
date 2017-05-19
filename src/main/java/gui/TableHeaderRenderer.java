package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ResourceBundle;

/**
 * Renderer class for viewing Contacts table head
 */
public class TableHeaderRenderer extends DefaultTableCellRenderer {

    private DefaultTableCellRenderer renderer;

    public TableHeaderRenderer(JTable table) {
        renderer = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
        renderer.setHorizontalAlignment(JLabel.CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        Component comp = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        renderer.setText((ResourceBundle.getBundle("gui_names").getString((String)value)));
        return comp;
    }
}
