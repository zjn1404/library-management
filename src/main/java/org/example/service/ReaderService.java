package org.example.service;

import org.example.model.Reader;
import org.example.util.FileUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReaderService {

    private static final String FILE = "readers.txt";

    private Reader deserialize(String line) {
        String[] p = line.split("\\|", -1);
        return new Reader(
                p[0], p[1], p[2],
                LocalDate.parse(p[3], FileUtil.DATE_FMT),
                p[4], p[5], p[6],
                LocalDate.parse(p[7], FileUtil.DATE_FMT),
                LocalDate.parse(p[8], FileUtil.DATE_FMT)
        );
    }

    private String serialize(Reader r) {
        return String.join("|",
                r.getReaderId(),
                r.getFullName(),
                r.getIdCard(),
                r.getDob().format(FileUtil.DATE_FMT),
                r.getGender(),
                r.getEmail(),
                r.getAddress(),
                r.getCardDate().format(FileUtil.DATE_FMT),
                r.getExpiryDate().format(FileUtil.DATE_FMT)
        );
    }

    private void persist(List<Reader> readers) {
        List<String> lines = new ArrayList<>();
        for (Reader r : readers) lines.add(serialize(r));
        FileUtil.writeLines(FILE, lines);
    }

    public List<Reader> getAll() {
        List<Reader> readers = new ArrayList<>();
        for (String line : FileUtil.readLines(FILE)) {
            try {
                readers.add(deserialize(line));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return readers;
    }

    public void add(Reader reader) {
        reader.setReaderId("DG" + System.currentTimeMillis());
        List<Reader> readers = getAll();
        readers.add(reader);
        persist(readers);
    }

    public boolean update(Reader reader) {
        List<Reader> readers = getAll();
        for (int i = 0; i < readers.size(); i++) {
            if (readers.get(i).getReaderId().equals(reader.getReaderId())) {
                readers.set(i, reader);
                persist(readers);
                return true;
            }
        }
        return false;
    }

    public boolean delete(String readerId) {
        List<Reader> readers = getAll();
        boolean removed = readers.removeIf(r -> r.getReaderId().equals(readerId));
        if (removed) persist(readers);
        return removed;
    }

    public Reader findById(String readerId) {
        for (Reader r : getAll()) {
            if (r.getReaderId().equals(readerId)) return r;
        }
        return null;
    }

    public List<Reader> findByIdCard(String idCard) {
        List<Reader> result = new ArrayList<>();
        String lower = idCard.toLowerCase();
        for (Reader r : getAll()) {
            if (r.getIdCard().toLowerCase().contains(lower)) result.add(r);
        }
        return result;
    }

    public List<Reader> findByName(String name) {
        List<Reader> result = new ArrayList<>();
        String lower = name.toLowerCase();
        for (Reader r : getAll()) {
            if (r.getFullName().toLowerCase().contains(lower)) result.add(r);
        }
        return result;
    }
}
