package gui;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import java.util.Observer;

/**
 * Created by xeniu on 07.04.2017.
 */
public class TableModelMemory extends AbstractTableModel implements TableModelListener {

    private final String[] columns = new String[]{"~ row ~", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
    private int[] memory;
    private int fromMemory, toMemory;

    private GUI gui;

    public TableModelMemory(GUI gui) {
        memory = new int[4096];
        fromMemory = 0x200;
        toMemory = 0x300;

        this.gui = gui;

        this.addTableModelListener(this);
    }

    @Override
    public int getRowCount() {
        return (toMemory - fromMemory) / 16 + 1;
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (col == 0) {
            return "0x" + Integer.toHexString(fromMemory + row * 16);
        }
        return Integer.toHexString(memory[fromMemory + row * 16 + col - 1]);
    }

    @Override
    public String getColumnName(int i) {
        return this.columns[i];
    }

    @Override
    public void tableChanged(TableModelEvent tableModelEvent) {
        gui.updateMemory(this.memory);
    }

    public void setMemory(int[] memory) {
        this.memory = memory;
        fireTableDataChanged();
    }

    public boolean isCellEditable(int row, int col) {
        return col > 0;
    }

    public void setValueAt(Object value, int row, int col) throws NumberFormatException {
        int v = Integer.parseInt((String) value, 16);
        memory[fromMemory + row * 16 + col - 1] = v & 0xFF;
        fireTableCellUpdated(row, col);
    }

    public void setRange(int from, int to) {
        this.fromMemory = from;
        this.toMemory = to;
        this.fireTableDataChanged();
    }

    public int getFromMemory() {
        return fromMemory;
    }

    public int getToMemory() {
        return toMemory;
    }
}
