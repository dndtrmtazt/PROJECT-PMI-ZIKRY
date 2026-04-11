package config;

import model.User;

/**
 * Singleton class to manage user session across the application
 */
public class UserSession {
    private static UserSession instance;
    private User currentUser;

    private UserSession() {
    }

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        System.out.println("[UserSession] User logged in: " + user.getUsername() + " (" + user.getRole() + ")");
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public String getUserId() {
        return currentUser != null ? currentUser.getUsername() : null;
    }

    public String getUserRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }

    public boolean isKasir() {
        return currentUser != null && "kasir".equalsIgnoreCase(currentUser.getRole());
    }

    public boolean isPemilik() {
        return currentUser != null && "pemilik".equalsIgnoreCase(currentUser.getRole());
    }

    public void logout() {
        System.out.println("[UserSession] User logged out: " + (currentUser != null ? currentUser.getUsername() : "Unknown"));
        currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
