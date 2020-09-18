package dev.l2j.tesla.loginserver.model;

import java.util.Objects;

public final class AccountInfo {
    private final String _login;
    private final String _passHash;
    private final int _accessLevel;
    private final int _lastServer;

    public AccountInfo(final String login, final String passHash, final int accessLevel, final int lastServer) {
        Objects.requireNonNull(login, "login");
        Objects.requireNonNull(passHash, "passHash");

        if (login.isEmpty())
            throw new IllegalArgumentException("login");

        if (passHash.isEmpty())
            throw new IllegalArgumentException("passHash");

        _login = login.toLowerCase();
        _passHash = passHash;
        _accessLevel = accessLevel;
        _lastServer = lastServer;
    }

    public String getLogin() {
        return _login;
    }

    public int getAccessLevel() {
        return _accessLevel;
    }

    public int getLastServer() {
        return _lastServer;
    }
	
    public String getPassHash() {
        return _passHash;
    }
}