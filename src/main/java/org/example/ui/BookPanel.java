package org.example.ui;

import org.example.model.Book;
import org.example.service.BookService;
import org.example.service.BorrowService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class BookPanel extends JPanel {

    private final BookService bookService = new BookService();
    private final BorrowService borrowService = new BorrowService();
    private final DefaultTableModel tableModel;
    private final JTable table;

    private static final String[] COLUMNS = {
        "STT", "ISBN", "Tên sách", "Tác giả", "NXB", "Năm", "Thể loại", "Giá", "Số quyển", "Còn lại"
    };

    public BookPanel() {
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        JComboBox<String> searchType = new JComboBox<>(new String[]{"Tìm theo tên sách", "Tìm theo ISBN"});
        JTextField searchField = new JTextField(18);
        JButton searchBtn = new JButton("Tìm kiếm");
        JButton refreshBtn = new JButton("Làm mới");
        northPanel.add(searchType);
        northPanel.add(searchField);
        northPanel.add(searchBtn);
        northPanel.add(refreshBtn);
        add(northPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        attachCopyMenu(table, 1, "Sao chép ISBN");
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        JButton addBtn = new JButton("Thêm");
        JButton editBtn = new JButton("Sửa");
        JButton deleteBtn = new JButton("Xóa");
        southPanel.add(addBtn);
        southPanel.add(editBtn);
        southPanel.add(deleteBtn);
        add(southPanel, BorderLayout.SOUTH);

        loadAll();

        searchBtn.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            if (keyword.isEmpty()) { loadAll(); return; }
            if (searchType.getSelectedIndex() == 0) {
                populateTable(bookService.findByTitle(keyword));
            } else {
                Book found = bookService.findByIsbn(keyword);
                populateTable(found != null ? List.of(found) : List.of());
            }
        });

        refreshBtn.addActionListener(e -> { searchField.setText(""); loadAll(); });

        addBtn.addActionListener(e -> openBookDialog(null));

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một cuốn sách.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String isbn = (String) tableModel.getValueAt(row, 1);
            openBookDialog(bookService.findByIsbn(isbn));
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một cuốn sách.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String isbn = (String) tableModel.getValueAt(row, 1);
            if (borrowService.countBorrowedCopies(isbn) > 0) {
                JOptionPane.showMessageDialog(this,
                        "Sách này đang được mượn, không thể xóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn xóa sách này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                bookService.delete(isbn);
                loadAll();
            }
        });
    }

    public void refresh() {
        loadAll();
    }

    private void loadAll() {
        populateTable(bookService.getAll());
    }

    private void populateTable(List<Book> books) {
        tableModel.setRowCount(0);
        int stt = 1;
        for (Book b : books) {
            int borrowed = borrowService.countBorrowedCopies(b.getIsbn());
            int available = bookService.getAvailableQuantity(b.getIsbn(), borrowed);
            tableModel.addRow(new Object[]{
                stt++,
                b.getIsbn(),
                b.getTitle(),
                b.getAuthor(),
                b.getPublisher(),
                b.getYear(),
                b.getCategory(),
                String.format("%,.0f đ", b.getPrice()),
                b.getQuantity(),
                available
            });
        }
    }

    private void openBookDialog(Book existing) {
        boolean isEdit = existing != null;
        JDialog dialog = new JDialog(
            SwingUtilities.getWindowAncestor(this),
            isEdit ? "Sửa sách" : "Thêm sách",
            Dialog.ModalityType.APPLICATION_MODAL
        );
        dialog.setSize(440, 380);
        dialog.setLocationRelativeTo(null);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 4, 5, 4);

        JTextField isbnField = new JTextField(20);
        JTextField titleField = new JTextField(20);
        JTextField authorField = new JTextField(20);
        JTextField publisherField = new JTextField(20);
        JSpinner yearSpinner = new JSpinner(new SpinnerNumberModel(2024, 1900, 2100, 1));
        ((JSpinner.DefaultEditor) yearSpinner.getEditor()).getTextField().setColumns(6);
        JTextField categoryField = new JTextField(20);
        JTextField priceField = new JTextField(20);
        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));

        if (isEdit) {
            isbnField.setText(existing.getIsbn());
            isbnField.setEnabled(false);
            titleField.setText(existing.getTitle());
            authorField.setText(existing.getAuthor());
            publisherField.setText(existing.getPublisher());
            yearSpinner.setValue(existing.getYear());
            categoryField.setText(existing.getCategory());
            priceField.setText(String.valueOf((long) existing.getPrice()));
            quantitySpinner.setValue(existing.getQuantity());
        }

        String[] labels = {"ISBN:", "Tên sách:", "Tác giả:", "NXB:", "Năm xuất bản:", "Thể loại:", "Giá (đ):", "Số quyển:"};
        JComponent[] fields = {isbnField, titleField, authorField, publisherField, yearSpinner, categoryField, priceField, quantitySpinner};
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.35;
            form.add(new JLabel(labels[i]), gbc);
            gbc.gridx = 1; gbc.weightx = 0.65;
            form.add(fields[i], gbc);
        }

        JButton saveBtn = new JButton("Lưu");
        gbc.gridx = 0; gbc.gridy = labels.length; gbc.gridwidth = 2; gbc.weightx = 1.0;
        gbc.insets = new Insets(12, 4, 4, 4);
        form.add(saveBtn, gbc);

        saveBtn.addActionListener(e -> {
            try {
                String isbn = isbnField.getText().trim();
                String title = titleField.getText().trim();
                String author = authorField.getText().trim();
                String publisher = publisherField.getText().trim();
                int year = (Integer) yearSpinner.getValue();
                String category = categoryField.getText().trim();
                String priceText = priceField.getText().trim().replace(",", "").replace(".", "");
                double price = Double.parseDouble(priceText);
                int quantity = (Integer) quantitySpinner.getValue();

                if (isbn.isEmpty() || title.isEmpty() || author.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                            "Vui lòng điền đầy đủ thông tin bắt buộc.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!isEdit && bookService.findByIsbn(isbn) != null) {
                    JOptionPane.showMessageDialog(dialog,
                            "ISBN đã tồn tại trong hệ thống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Book book = new Book(isbn, title, author, publisher, year, category, price, quantity);
                if (isEdit) bookService.update(book);
                else bookService.add(book);

                loadAll();
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Giá tiền không hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setContentPane(new JScrollPane(form));
        dialog.setVisible(true);
    }

    private void attachCopyMenu(JTable tbl, int col, String label) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem item = new JMenuItem(label);
        menu.add(item);
        item.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row < 0) return;
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(tbl.getValueAt(row, col).toString()), null);
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
}
