package org.example.util;

import org.example.model.Account;

public class AppContext {
    private static Account currentAccount;

    public static Account getCurrentAccount() {
        return currentAccount;
    }

    public static void setCurrentAccount(Account account) {
        currentAccount = account;
    }
}
