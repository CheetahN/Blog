package main.service;

import org.springframework.stereotype.Service;

@Service
public interface VoteService {

    public boolean like(String sessionId, int postId);

    public boolean dislike(String sessionId, int postId);
}
