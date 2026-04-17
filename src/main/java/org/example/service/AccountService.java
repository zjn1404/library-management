package org.example.service;

import org.example.model.Account;
import org.example.util.FileUtil;

import java.util.ArrayList;
import java.util.List;

public class AccountService {

    private static final String FILE = "accounts.txt";

    public boolean hasAccounts() {
        return !FileUtil.readLines(FILE).isEmpty();
    }

    public boolean register(String username, String password, String fullName) {
        List<String> lines = FileUtil.readLines(FILE);
        for (String line : lines) {
            String[] parts = line.split("\\|", -1);
            if (parts[0].equalsIgnoreCase(username)) return false;
        }
        String hash = Account.hashPassword(password);
        lines.add(username + "|" + hash + "|" + fullName);
        FileUtil.writeLines(FILE, lines);
        return true;
    }

    public Account login(String username, String password) {
        String hash = Account.hashPassword(password);
        for (String line : FileUtil.readLines(FILE)) {
            String[] parts = line.split("\\|", -1);
            if (parts.length < 3) continue;
            if (parts[0].equalsIgnoreCase(username) && parts[1].equals(hash)) {
                return new Account(parts[0], parts[1], parts[2]);
            }
        }
        return null;
    }

    private List<Account> loadAll() {
        List<Account> accounts = new ArrayList<>();
        for (String line : FileUtil.readLines(FILE)) {
            String[] p = line.split("\\|", -1);
            if (p.length < 3) continue;
            accounts.add(new Account(p[0], p[1], p[2]));
        }
        return accounts;
    }

    private void saveAll(List<Account> accounts) {
        List<String> lines = new ArrayList<>();
        for (Account a : accounts) {
            lines.add(a.getUsername() + "|" + a.getPasswordHash() + "|" + a.getFullName());
        }
        FileUtil.writeLines(FILE, lines);
    }
}
