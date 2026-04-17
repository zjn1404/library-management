package org.example.ui;

import org.example.model.Book;
import org.example.model.BorrowRecord;
import org.example.model.Reader;
import org.example.service.BookService;
import org.example.service.BorrowService;
import org.example.service.ReaderService;
import org.example.util.FileUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class StatisticsPanel extends JPanel {

    private final BookService bookService = new BookService();
    private final BorrowService borrowService = new BorrowService();
    private final ReaderService readerService = new ReaderService();

    private JLabel lblTotalTitles, lblTotalCopies, lblBorrowingCopies;
    private JLabel lblTotalReaders, lblMale, lblFemale, lblValid, lblExpired;
    private JLabel lblTotalBorrows, lblActiveBorrows, lblReturnedBorrows;
    private DefaultTableModel categoryTableModel;
    private DefaultTableModel overdueTableModel;

    public StatisticsPanel() {
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        JButton refreshBtn = new JButton("Làm mới");
        topBar.add(refreshBtn);
        add(topBar, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 10, 10));
        grid.add(buildBookCard());
        grid.add(buildCategoryCard());
        grid.add(buildReaderCard());
        grid.add(buildBorrowCard());
        add(new JScrollPane(grid), BorderLayout.CENTER);

        refreshBtn.addActionListener(e -> refresh());
        refresh();
    }

    private JPanel buildBookCard() {
        JPanel card = new JPanel(new GridLayout(3, 1, 0, 4));
        card.setBorder(BorderFactory.createTitledBorder("Tổng quan sách"));
        lblTotalTitles = new JLabel();
        lblTotalCopies = new JLabel();
        lblBorrowingCopies = new JLabel();
        card.add(wrap(lblTotalTitles));
        card.add(wrap(lblTotalCopies));
        card.add(wrap(lblBorrowingCopies));
        return card;
    }

    private JPanel buildCategoryCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createTitledBorder("Sách theo thể loại"));
        categoryTableModel = new DefaultTableModel(
                new String[]{"Thể loại", "Số đầu sách", "Tổng số quyển"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(categoryTableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    private JPanel buildReaderCard() {
        JPanel card = new JPanel(new GridLayout(5, 1, 0, 4));
        card.setBorder(BorderFactory.createTitledBorder("Độc giả"));
        lblTotalReaders = new JLabel();
        lblMale = new JLabel();
        lblFemale = new JLabel();
        lblValid = new JLabel();
        lblExpired = new JLabel();
        for (JLabel lbl : new JLabel[]{lblTotalReaders, lblMale, lblFemale, lblValid, lblExpired}) {
            card.add(wrap(lbl));
        }
        return card;
    }

    private JPanel buildBorrowCard() {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBorder(BorderFactory.createTitledBorder("Phiếu mượn"));

        JPanel statsPane = new JPanel(new GridLayout(3, 1, 0, 4));
        lblTotalBorrows = new JLabel();
        lblActiveBorrows = new JLabel();
        lblReturnedBorrows = new JLabel();
        statsPane.add(wrap(lblTotalBorrows));
        statsPane.add(wrap(lblActiveBorrows));
        statsPane.add(wrap(lblReturnedBorrows));
        card.add(statsPane, BorderLayout.NORTH);

        overdueTableModel = new DefaultTableModel(
                new String[]{"Mã phiếu", "Mã độc giả", "Tên độc giả", "Ngày trả dự kiến", "Tiền phạt"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable overdueTable = new JTable(overdueTableModel);
        overdueTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        overdueTable.getTableHeader().setReorderingAllowed(false);
        attachCopyMenu(overdueTable, 0, "Sao chép mã phiếu", 1, "Sao chép mã độc giả");

        JPanel overduePane = new JPanel(new BorderLayout(0, 2));
        JLabel overdueTitle = new JLabel("Danh sách độc giả quá hạn:");
        overdueTitle.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        overduePane.add(overdueTitle, BorderLayout.NORTH);
        overduePane.add(new JScrollPane(overdueTable), BorderLayout.CENTER);
        card.add(overduePane, BorderLayout.CENTER);

        return card;
    }

    private void attachCopyMenu(JTable tbl, int col1, String label1, int col2, String label2) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem item1 = new JMenuItem(label1);
        JMenuItem item2 = new JMenuItem(label2);
        menu.add(item1);
        menu.add(item2);
        item1.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row < 0) return;
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(tbl.getValueAt(row, col1).toString()), null);
        });
        item2.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row < 0) return;
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(tbl.getValueAt(row, col2).toString()), null);
        });
        tbl.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) show(e);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) show(e);
            }
            private void show(MouseEvent e) {
                int row = tbl.rowAtPoint(e.getPoint());
                if (row >= 0) tbl.setRowSelectionInterval(row, row);
                menu.show(tbl, e.getX(), e.getY());
            }
        });
    }

    private JPanel wrap(JLabel label) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        p.add(label);
        return p;
    }

    private void refresh() {
        List<Book> books = bookService.getAll();
        List<Reader> readers = readerService.getAll();
        List<BorrowRecord> allBorrows = borrowService.getAll();
        List<BorrowRecord> activeRecords = borrowService.getActiveRecords();
        List<BorrowRecord> overdueRecords = borrowService.getOverdueRecords();
        LocalDate today = LocalDate.now();

        int totalTitles = books.size();
        int totalCopies = books.stream().mapToInt(Book::getQuantity).sum();
        int borrowingCopies = activeRecords.stream().mapToInt(r -> r.getIsbns().size()).sum();
        lblTotalTitles.setText("Tổng số đầu sách: " + totalTitles);
        lblTotalCopies.setText("Tổng số quyển sách: " + totalCopies);
        lblBorrowingCopies.setText("Số quyển đang được mượn: " + borrowingCopies);

        categoryTableModel.setRowCount(0);
        Map<String, int[]> catMap = new TreeMap<>();
        for (Book b : books) {
            catMap.computeIfAbsent(b.getCategory(), k -> new int[2]);
            catMap.get(b.getCategory())[0]++;
            catMap.get(b.getCategory())[1] += b.getQuantity();
        }
        catMap.forEach((cat, arr) -> categoryTableModel.addRow(new Object[]{cat, arr[0], arr[1]}));

        long maleCount = readers.stream().filter(r -> "Nam".equals(r.getGender())).count();
        long femaleCount = readers.stream().filter(r -> "Nữ".equals(r.getGender())).count();
        long validCount = readers.stream().filter(r -> !r.getExpiryDate().isBefore(today)).count();
        long expiredCount = readers.stream().filter(r -> r.getExpiryDate().isBefore(today)).count();
        lblTotalReaders.setText("Tổng số độc giả: " + readers.size());
        lblMale.setText("Nam: " + maleCount);
        lblFemale.setText("Nữ: " + femaleCount);
        lblValid.setText("Thẻ còn hiệu lực: " + validCount);
        lblExpired.setText("Thẻ hết hạn: " + expiredCount);

        long activeCount = allBorrows.stream().filter(r -> "BORROWING".equals(r.getStatus())).count();
        long returnedCount = allBorrows.stream().filter(r -> "RETURNED".equals(r.getStatus())).count();
        lblTotalBorrows.setText("Tổng số phiếu mượn: " + allBorrows.size());
        lblActiveBorrows.setText("Đang mượn: " + activeCount);
        lblReturnedBorrows.setText("Đã trả: " + returnedCount);

        overdueTableModel.setRowCount(0);
        for (BorrowRecord r : overdueRecords) {
            Reader reader = readerService.findById(r.getReaderId());
            String name = reader != null ? reader.getFullName() : r.getReaderId();
            long fee = borrowService.calculateLateFee(r);
            overdueTableModel.addRow(new Object[]{
                    r.getBorrowId(),
                    r.getReaderId(),
                    name,
                    r.getExpectedReturnDate().format(FileUtil.DATE_FMT),
                    String.format("%,.0f đ", (double) fee)
            });
        }
    }
}
