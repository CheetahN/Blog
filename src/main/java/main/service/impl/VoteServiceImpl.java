package main.service.impl;

import main.model.Post;
import main.model.User;
import main.model.Vote;
import main.repository.PostRepository;
import main.repository.SessionRepository;
import main.repository.UserRepository;
import main.repository.VoteRepository;
import main.service.VoteService;
import main.service.exceptions.NoUserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class VoteServiceImpl implements VoteService {
    private final VoteRepository voteRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Autowired
    public VoteServiceImpl(VoteRepository voteRepository, SessionRepository sessionRepository, UserRepository userRepository, PostRepository postRepository) {
        this.voteRepository = voteRepository;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    @Override
    public boolean like(String sessionId, int postId) {
        Integer userId = sessionRepository.getUserId(sessionId);
        if (userId == null)
            return false;
        Post post = postRepository.findById(postId);
        User user = userRepository.findById(userId).orElseThrow(() -> new NoUserException(userId));
        Vote vote = voteRepository.findByPostAndUser(post, user);
        if (vote == null) {
            vote = new Vote();
            vote.setPost(post);
            vote.setTime(LocalDateTime.now());
            vote.setUser(user);
            vote.setValue((byte) 1);
        } else {
            if (vote.getValue() == 1)
                return false;
            vote.setValue((byte) 1);
        }

        try {
            voteRepository.saveAndFlush(vote);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean dislike(String sessionId, int postId) {
        Integer userId = sessionRepository.getUserId(sessionId);
        if (userId == null)
            return false;
        Post post = postRepository.findById(postId);
        User user = userRepository.findById(userId).orElseThrow(() -> new NoUserException(userId));
        Vote vote = voteRepository.findByPostAndUser(post, user);
        if (vote == null) {
            vote = new Vote();
            vote.setPost(post);
            vote.setTime(LocalDateTime.now());
            vote.setUser(user);
            vote.setValue((byte) -1);
        } else {
            if (vote.getValue() == -1)
                return false;
            vote.setValue((byte) -1);
        }

        try {
            voteRepository.saveAndFlush(vote);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
