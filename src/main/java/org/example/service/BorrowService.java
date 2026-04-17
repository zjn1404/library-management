package org.example.service;

import org.example.model.Book;
import org.example.model.BorrowRecord;
import org.example.util.FileUtil;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class BorrowService {

    private static final String FILE = "borrows.txt";
    private final BookService bookService = new BookService();

    public List<BorrowRecord> getAll() {
        List<BorrowRecord> records = new ArrayList<>();
        for (String line : FileUtil.readLines(FILE)) {
            String[] p = line.split("\\|", -1);
            if (p.length < 7) continue;
            String borrowId = p[0];
            String readerId = p[1];
            LocalDate borrowDate = LocalDate.parse(p[2], FileUtil.DATE_FMT);
            LocalDate expectedReturnDate = LocalDate.parse(p[3], FileUtil.DATE_FMT);
            LocalDate actualReturnDate = p[4].isBlank() ? null : LocalDate.parse(p[4], FileUtil.DATE_FMT);
            String status = p[5];
            List<String> isbns = p[6].isBlank() ? new ArrayList<>() : new ArrayList<>(Arrays.asList(p[6].split(",")));
            BorrowRecord record = new BorrowRecord(borrowId, readerId, borrowDate, actualReturnDate, isbns, status);
            record.setExpectedReturnDate(expectedReturnDate);
            records.add(record);
        }
        return records;
    }

    private void saveAll(List<BorrowRecord> records) {
        List<String> lines = new ArrayList<>();
        for (BorrowRecord r : records) {
            String actualStr = r.getActualReturnDate() == null ? "" : r.getActualReturnDate().format(FileUtil.DATE_FMT);
            String isbnStr = String.join(",", r.getIsbns());
            lines.add(String.join("|",
                    r.getBorrowId(),
                    r.getReaderId(),
                    r.getBorrowDate().format(FileUtil.DATE_FMT),
                    r.getExpectedReturnDate().format(FileUtil.DATE_FMT),
                    actualStr,
                    r.getStatus(),
                    isbnStr
            ));
        }
        FileUtil.writeLines(FILE, lines);
    }

    public BorrowRecord createBorrow(String readerId, List<String> isbns) {
        String borrowId = "PM" + System.currentTimeMillis();
        LocalDate today = LocalDate.now();
        BorrowRecord record = new BorrowRecord(borrowId, readerId, today, null, new ArrayList<>(isbns), "BORROWING");
        List<BorrowRecord> all = getAll();
        all.add(record);
        saveAll(all);
        return record;
    }

    public boolean returnBooks(String borrowId, LocalDate actualReturnDate) {
        List<BorrowRecord> all = getAll();
        for (BorrowRecord r : all) {
            if (r.getBorrowId().equals(borrowId)) {
                r.setActualReturnDate(actualReturnDate);
                r.setStatus("RETURNED");
                saveAll(all);
                return true;
            }
        }
        return false;
    }

    public boolean markLost(String borrowId) {
        List<BorrowRecord> all = getAll();
        for (BorrowRecord r : all) {
            if (r.getBorrowId().equals(borrowId)) {
                r.setStatus("LOST");
                r.setActualReturnDate(LocalDate.now());
                saveAll(all);
                for (String isbn : r.getIsbns()) {
                    Book b = bookService.findByIsbn(isbn);
                    if (b != null && b.getQuantity() > 0) {
                        b.setQuantity(b.getQuantity() - 1);
                        bookService.update(b);
                    }
                }
                return true;
            }
        }
        return false;
    }

    public List<BorrowRecord> getActiveRecords() {
        return getAll().stream()
                .filter(r -> "BORROWING".equals(r.getStatus()))
                .collect(Collectors.toList());
    }

    public List<BorrowRecord> getOverdueRecords() {
        LocalDate today = LocalDate.now();
        return getAll().stream()
                .filter(r -> "BORROWING".equals(r.getStatus()) && r.getExpectedReturnDate().isBefore(today))
                .collect(Collectors.toList());
    }

    public List<BorrowRecord> getByReaderId(String readerId) {
        return getAll().stream()
                .filter(r -> r.getReaderId().equals(readerId))
                .collect(Collectors.toList());
    }

    public long calculateLateFee(BorrowRecord record) {
        LocalDate reference = "RETURNED".equals(record.getStatus()) ? record.getActualReturnDate() : LocalDate.now();
        if (reference == null) return 0L;
        long days = java.time.temporal.ChronoUnit.DAYS.between(record.getExpectedReturnDate(), reference);
        return Math.max(0, days) * 5000L;
    }

    public long calculateLostFee(String isbn, double bookPrice) {
        return (long) (bookPrice * 2);
    }

    public int countBorrowedCopies(String isbn) {
        int count = 0;
        for (BorrowRecord r : getActiveRecords()) {
            for (String i : r.getIsbns()) {
                if (i.equals(isbn)) count++;
            }
        }
        return count;
    }

    public int getTotalBorrowing() {
        return (int) getAll().stream().filter(r -> "BORROWING".equals(r.getStatus())).count();
    }

    public Map<String, Long> getBorrowCountByCategory(BookService bookService) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (BorrowRecord record : getAll()) {
            for (String isbn : record.getIsbns()) {
                var book = bookService.findByIsbn(isbn);
                if (book != null) {
                    map.merge(book.getCategory(), 1L, Long::sum);
                }
            }
        }
        return map;
    }
}
