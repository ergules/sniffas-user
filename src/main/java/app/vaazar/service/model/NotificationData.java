package app.vaazar.service.model;

import java.util.StringJoiner;

public class NotificationData {
    private Long userId;
    private String userName;

    public NotificationData() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", NotificationData.class.getSimpleName() + "[", "]")
                .add("userId=" + userId)
                .add("userName='" + userName + "'")
                .toString();
    }
}
