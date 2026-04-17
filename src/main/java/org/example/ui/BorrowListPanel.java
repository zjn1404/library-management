package org.example.ui;

import org.example.model.BorrowRecord;
import org.example.model.Reader;
import org.example.service.BorrowService;
import org.example.service.ReaderService;
import org.example.util.FileUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.stream.Collectors;

public class BorrowListPanel extends JPanel {

    private final BorrowService borrowService = new BorrowService();
    private final ReaderService readerService = new ReaderService();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JComboBox<String> filterBox;

    private static final String[] COLUMNS = {
        "STT", "Mã phiếu", "Mã độc giả", "Tên độc giả",
        "Ngày mượn", "Ngày trả DK", "Ngày trả TT", "Trạng thái", "Sách mượn"
    };

    public BorrowListPanel() {
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterBox = new JComboBox<>(new String[]{"Tất cả", "Đang mượn", "Đã trả", "Mất sách"});
        JButton refreshBtn = new JButton("Làm mới");
        northPanel.add(new JLabel("Trạng thái:"));
        northPanel.add(filterBox);
        northPanel.add(refreshBtn);
        add(northPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        configureColumnWidths();
        attachCopyMenu();
        add(new JScrollPane(table), BorderLayout.CENTER);

        refresh();

        filterBox.addActionListener(e -> refresh());
        refreshBtn.addActionListener(e -> refresh());
    }

    public void refresh() {
        List<BorrowRecord> records = borrowService.getAll();
        int idx = filterBox.getSelectedIndex();
        if (idx == 1) records = records.stream().filter(r -> "BORROWING".equals(r.getStatus())).collect(Collectors.toList());
        else if (idx == 2) records = records.stream().filter(r -> "RETURNED".equals(r.getStatus())).collect(Collectors.toList());
        else if (idx == 3) records = records.stream().filter(r -> "LOST".equals(r.getStatus())).collect(Collectors.toList());

        tableModel.setRowCount(0);
        int stt = 1;
        for (BorrowRecord r : records) {
            Reader reader = readerService.findById(r.getReaderId());
            String name = reader != null ? reader.getFullName() : "";
            String actual = r.getActualReturnDate() != null ? r.getActualReturnDate().format(FileUtil.DATE_FMT) : "";
            String status = switch (r.getStatus()) {
                case "BORROWING" -> "Đang mượn";
                case "RETURNED" -> "Đã trả";
                case "LOST" -> "Mất sách";
                default -> r.getStatus();
            };
            tableModel.addRow(new Object[]{
                stt++,
                r.getBorrowId(),
                r.getReaderId(),
                name,
                r.getBorrowDate().format(FileUtil.DATE_FMT),
                r.getExpectedReturnDate().format(FileUtil.DATE_FMT),
                actual,
                status,
                String.join(", ", r.getIsbns())
            });
        }
    }

    private void configureColumnWidths() {
        int[] widths = {40, 140, 120, 150, 90, 90, 90, 90, 220};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    private void attachCopyMenu() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem copyBorrowId = new JMenuItem("Sao chép mã phiếu");
        JMenuItem copyReaderId = new JMenuItem("Sao chép mã độc giả");
        JMenuItem copyIsbn = new JMenuItem("Sao chép mã sách");
        menu.add(copyBorrowId);
        menu.add(copyReaderId);
        menu.add(copyIsbn);

        copyBorrowId.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(table.getValueAt(row, 1).toString()), null);
        });
        copyReaderId.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(table.getValueAt(row, 2).toString()), null);
        });
        copyIsbn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(table.getValueAt(row, 8).toString()), null);
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) show(e);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) show(e);
            }
            private void show(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) table.setRowSelectionInterval(row, row);
                menu.show(table, e.getX(), e.getY());
            }
        });
    }
}
