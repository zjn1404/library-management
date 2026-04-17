package org.example.service;

import org.example.model.Book;
import org.example.util.FileUtil;

import java.util.ArrayList;
import java.util.List;

public class BookService {

    private static final String FILE = "books.txt";

    public List<Book> getAll() {
        List<Book> books = new ArrayList<>();
        for (String line : FileUtil.readLines(FILE)) {
            books.add(deserialize(line));
        }
        return books;
    }

    public void add(Book book) {
        List<String> lines = FileUtil.readLines(FILE);
        lines.add(serialize(book));
        FileUtil.writeLines(FILE, lines);
    }

    public boolean update(Book book) {
        List<String> lines = FileUtil.readLines(FILE);
        for (int i = 0; i < lines.size(); i++) {
            Book b = deserialize(lines.get(i));
            if (b.getIsbn().equals(book.getIsbn())) {
                lines.set(i, serialize(book));
                FileUtil.writeLines(FILE, lines);
                return true;
            }
        }
        return false;
    }

    public boolean delete(String isbn) {
        List<String> lines = FileUtil.readLines(FILE);
        boolean removed = lines.removeIf(line -> deserialize(line).getIsbn().equals(isbn));
        if (removed) FileUtil.writeLines(FILE, lines);
        return removed;
    }

    public Book findByIsbn(String isbn) {
        for (String line : FileUtil.readLines(FILE)) {
            Book b = deserialize(line);
            if (b.getIsbn().equals(isbn)) return b;
        }
        return null;
    }

    public List<Book> findByTitle(String title) {
        List<Book> result = new ArrayList<>();
        String lower = title.toLowerCase();
        for (String line : FileUtil.readLines(FILE)) {
            Book b = deserialize(line);
            if (b.getTitle().toLowerCase().contains(lower)) result.add(b);
        }
        return result;
    }

    public int getAvailableQuantity(String isbn, int borrowedCount) {
        Book book = findByIsbn(isbn);
        if (book == null) return 0;
        return Math.max(0, book.getQuantity() - borrowedCount);
    }

    private String serialize(Book b) {
        return String.join("|",
                b.getIsbn(),
                b.getTitle(),
                b.getAuthor(),
                b.getPublisher(),
                String.valueOf(b.getYear()),
                b.getCategory(),
                String.valueOf(b.getPrice()),
                String.valueOf(b.getQuantity())
        );
    }

    private Book deserialize(String line) {
        String[] p = line.split("\\|", -1);
        return new Book(
                p[0], p[1], p[2], p[3],
                Integer.parseInt(p[4]),
                p[5],
                Double.parseDouble(p[6]),
                Integer.parseInt(p[7])
        );
    }
}
