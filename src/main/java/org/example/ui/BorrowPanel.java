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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BorrowPanel extends JPanel {

    private final ReaderService readerService = new ReaderService();
    private final BookService bookService = new BookService();
    private final BorrowService borrowService = new BorrowService();

    private final JTextField readerIdField = new JTextField(16);
    private final JLabel readerInfoLabel = new JLabel(" ");
    private Reader selectedReader;

    private DefaultTableModel bookTableModel;
    private JTable bookTable;
    private final List<String> selectedIsbns = new ArrayList<>();

    public BorrowPanel() {
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        add(buildReaderPanel(), BorderLayout.NORTH);
        add(buildBookPanel(), BorderLayout.CENTER);
        add(buildSubmitPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildReaderPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        panel.setBorder(BorderFactory.createTitledBorder("Thông tin độc giả"));
        JButton findBtn = new JButton("Tìm");
        readerInfoLabel.setPreferredSize(new Dimension(420, 24));
        panel.add(new JLabel("Mã độc giả:"));
        panel.add(readerIdField);
        panel.add(findBtn);
        panel.add(readerInfoLabel);
        findBtn.addActionListener(e -> lookupReader());
        readerIdField.addActionListener(e -> lookupReader());
        return panel;
    }

    private JPanel buildBookPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBorder(BorderFactory.createTitledBorder("Danh sách sách mượn"));

        bookTableModel = new DefaultTableModel(new String[]{"ISBN", "Tên sách", "Còn lại"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        bookTable = new JTable(bookTableModel);
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookTable.getTableHeader().setReorderingAllowed(false);
        bookTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        bookTable.getColumnModel().getColumn(1).setPreferredWidth(300);
        bookTable.getColumnModel().getColumn(2).setPreferredWidth(60);
        panel.add(new JScrollPane(bookTable), BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton addBookBtn = new JButton("Thêm sách");
        JButton removeBookBtn = new JButton("Xóa sách");
        btnRow.add(addBookBtn);
        btnRow.add(removeBookBtn);
        panel.add(btnRow, BorderLayout.SOUTH);

        addBookBtn.addActionListener(e -> addBook());
        removeBookBtn.addActionListener(e -> {
            int row = bookTable.getSelectedRow();
            if (row >= 0) {
                selectedIsbns.remove(row);
                bookTableModel.removeRow(row);
            }
        });
        return panel;
    }

    private JPanel buildSubmitPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        JButton submitBtn = new JButton("Lập phiếu mượn");
        submitBtn.setFont(submitBtn.getFont().deriveFont(Font.BOLD, 13f));
        panel.add(submitBtn);
        submitBtn.addActionListener(e -> createBorrow());
        return panel;
    }

    private void lookupReader() {
        String id = readerIdField.getText().trim();
        if (id.isEmpty()) return;
        Reader reader = readerService.findById(id);
        if (reader == null) {
            readerInfoLabel.setText("Không tìm thấy độc giả.");
            readerInfoLabel.setForeground(Color.RED);
            selectedReader = null;
        } else if (reader.getExpiryDate().isBefore(LocalDate.now())) {
            readerInfoLabel.setText(reader.getFullName() + "  —  Thẻ đã hết hạn ngày "
                    + reader.getExpiryDate().format(FileUtil.DATE_FMT) + "!");
            readerInfoLabel.setForeground(Color.RED);
            selectedReader = null;
        } else {
            readerInfoLabel.setText(reader.getFullName() + "  —  Thẻ còn hiệu lực đến "
                    + reader.getExpiryDate().format(FileUtil.DATE_FMT));
            readerInfoLabel.setForeground(new Color(0, 140, 0));
            selectedReader = reader;
        }
    }

    private void addBook() {
        String isbn = JOptionPane.showInputDialog(this, "Nhập ISBN sách:", "Thêm sách", JOptionPane.PLAIN_MESSAGE);
        if (isbn == null || isbn.trim().isEmpty()) return;
        isbn = isbn.trim();

        if (selectedIsbns.contains(isbn)) {
            JOptionPane.showMessageDialog(this, "Sách này đã có trong danh sách.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Book book = bookService.findByIsbn(isbn);
        if (book == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy sách với ISBN: " + isbn,
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int available = bookService.getAvailableQuantity(isbn, borrowService.countBorrowedCopies(isbn));
        if (available <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Sách \"" + book.getTitle() + "\" hiện không còn bản nào để mượn.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        selectedIsbns.add(isbn);
        bookTableModel.addRow(new Object[]{isbn, book.getTitle(), available});
    }

    private void createBorrow() {
        if (selectedReader == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng tra cứu và chọn một độc giả hợp lệ.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (selectedIsbns.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng thêm ít nhất một cuốn sách.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            BorrowRecord record = borrowService.createBorrow(
                    selectedReader.getReaderId(), new ArrayList<>(selectedIsbns));
            JOptionPane.showMessageDialog(this,
                    "Lập phiếu mượn thành công!\n"
                            + "Mã phiếu: " + record.getBorrowId() + "\n"
                            + "Ngày mượn: " + record.getBorrowDate().format(FileUtil.DATE_FMT) + "\n"
                            + "Ngày trả dự kiến: " + record.getExpectedReturnDate().format(FileUtil.DATE_FMT),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        readerIdField.setText("");
        readerInfoLabel.setText(" ");
        readerInfoLabel.setForeground(UIManager.getColor("Label.foreground"));
        selectedReader = null;
        selectedIsbns.clear();
        bookTableModel.setRowCount(0);
    }
}
