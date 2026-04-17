package org.example.ui;

import org.example.model.Book;
import org.example.model.BorrowRecord;
import org.example.model.Reader;
import org.example.service.BookService;
import org.example.service.BorrowService;
import org.example.service.ReaderService;
import org.example.util.FileUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.stream.Collectors;

public class ReturnPanel extends JPanel {

    private final ReaderService readerService = new ReaderService();
    private final BookService bookService = new BookService();
    private final BorrowService borrowService = new BorrowService();

    private final JTextField borrowIdField = new JTextField(18);
    private final JTextArea detailArea = new JTextArea(10, 40);
    private final JLabel feeLabel = new JLabel(" ");
    private final JButton returnBtn = new JButton("Xác nhận trả sách");
    private final JButton lostBtn = new JButton("Báo mất sách");
    private final JPanel detailPanel;

    private BorrowRecord currentRecord;

    public ReturnPanel() {
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        searchPanel.add(new JLabel("Mã phiếu mượn:"));
        searchPanel.add(borrowIdField);
        JButton findBtn = new JButton("Tìm");
        searchPanel.add(findBtn);
        add(searchPanel, BorderLayout.NORTH);

        detailArea.setEditable(false);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        detailArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        detailArea.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        feeLabel.setForeground(Color.RED);
        feeLabel.setFont(feeLabel.getFont().deriveFont(Font.BOLD, 13f));
        feeLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        lostBtn.setForeground(new Color(180, 0, 0));
        actionPanel.add(returnBtn);
        actionPanel.add(lostBtn);

        detailPanel = new JPanel(new BorderLayout(0, 4));
        detailPanel.setBorder(BorderFactory.createTitledBorder("Chi tiết phiếu mượn"));
        detailPanel.add(feeLabel, BorderLayout.NORTH);
        detailPanel.add(new JScrollPane(detailArea), BorderLayout.CENTER);
        detailPanel.add(actionPanel, BorderLayout.SOUTH);
        detailPanel.setVisible(false);
        add(detailPanel, BorderLayout.CENTER);

        findBtn.addActionListener(e -> lookupBorrow());
        returnBtn.addActionListener(e -> confirmReturn());
        lostBtn.addActionListener(e -> markLost());
    }

    private void lookupBorrow() {
        String id = borrowIdField.getText().trim();
        if (id.isEmpty()) return;

        BorrowRecord record = borrowService.getAll().stream()
                .filter(r -> r.getBorrowId().equals(id))
                .findFirst().orElse(null);

        if (record == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy phiếu mượn với mã: " + id,
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            detailPanel.setVisible(false);
            currentRecord = null;
            return;
        }

        currentRecord = record;
        detailPanel.setVisible(true);

        if (!"BORROWING".equals(record.getStatus())) {
            detailArea.setText("Phiếu này đã được xử lý.\nTrạng thái: " + record.getStatus());
            feeLabel.setText(" ");
            returnBtn.setEnabled(false);
            lostBtn.setEnabled(false);
            return;
        }

        returnBtn.setEnabled(true);
        lostBtn.setEnabled(true);

        Reader reader = readerService.findById(record.getReaderId());
        String readerName = reader != null ? reader.getFullName() : "(không tìm thấy)";

        String bookList = record.getIsbns().stream().map(isbn -> {
            Book book = bookService.findByIsbn(isbn);
            return "  - " + isbn + (book != null ? " | " + book.getTitle() : " | (không tìm thấy)");
        }).collect(Collectors.joining("\n"));

        detailArea.setText(
                "Mã phiếu       : " + record.getBorrowId() + "\n" +
                "Mã độc giả     : " + record.getReaderId() + "\n" +
                "Tên độc giả    : " + readerName + "\n" +
                "Ngày mượn      : " + record.getBorrowDate().format(FileUtil.DATE_FMT) + "\n" +
                "Ngày trả dự kiến: " + record.getExpectedReturnDate().format(FileUtil.DATE_FMT) + "\n" +
                "Danh sách sách :\n" + bookList
        );
        detailArea.setCaretPosition(0);

        long lateFee = borrowService.calculateLateFee(record);
        feeLabel.setText(lateFee > 0
                ? "Tiền phạt trễ hạn: " + String.format("%,.0f đ", (double) lateFee)
                : " ");
    }

    private void confirmReturn() {
        if (currentRecord == null) return;
        long fee = borrowService.calculateLateFee(currentRecord);
        borrowService.returnBooks(currentRecord.getBorrowId(), LocalDate.now());
        String msg = "Trả sách thành công!";
        if (fee > 0) msg += "\nTiền phạt trễ hạn: " + String.format("%,.0f đ", (double) fee);
        JOptionPane.showMessageDialog(this, msg, "Thành công", JOptionPane.INFORMATION_MESSAGE);
        clearForm();
    }

    private void markLost() {
        if (currentRecord == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Xác nhận báo mất tất cả sách trong phiếu này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        long totalLostFee = currentRecord.getIsbns().stream().mapToLong(isbn -> {
            Book book = bookService.findByIsbn(isbn);
            return book != null ? borrowService.calculateLostFee(isbn, book.getPrice()) : 0L;
        }).sum();

        borrowService.markLost(currentRecord.getBorrowId());
        JOptionPane.showMessageDialog(this,
                "Đã ghi nhận mất sách!\nTiền phạt mất sách: " + String.format("%,.0f đ", (double) totalLostFee),
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        clearForm();
    }

    private void clearForm() {
        borrowIdField.setText("");
        detailArea.setText("");
        feeLabel.setText(" ");
        detailPanel.setVisible(false);
        currentRecord = null;
    }
}
