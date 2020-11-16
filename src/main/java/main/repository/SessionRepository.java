package main.repository;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class SessionRepository {
    private static Map<String, Integer> activeSessions = new HashMap<>();

    public Integer getUserId(String sessionId) {
        return activeSessions.get(sessionId);
    }

    public void addSession(String sessionId, Integer userId) {
        activeSessions.put(sessionId, userId);
    }

    public void remove(String sessionId) {
        activeSessions.remove(sessionId);
    }




}
