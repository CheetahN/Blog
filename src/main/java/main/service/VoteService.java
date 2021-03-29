package main.service;

import org.springframework.stereotype.Service;

@Service
public interface VoteService {

    public boolean like(int postId);

    public boolean dislike(int postId);
}
