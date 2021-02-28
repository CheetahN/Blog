package main.service.impl;

import main.model.Post;
import main.model.User;
import main.model.Vote;
import main.repository.PostRepository;
import main.repository.VoteRepository;
import main.service.UserService;
import main.service.VoteService;
import main.service.exceptions.PostNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class VoteServiceImpl implements VoteService {
    private final VoteRepository voteRepository;
    private final UserService userService;
    private final PostRepository postRepository;

    @Autowired
    public VoteServiceImpl(VoteRepository voteRepository, UserService userService, PostRepository postRepository) {
        this.voteRepository = voteRepository;
        this.userService = userService;
        this.postRepository = postRepository;
    }

    @Override
    public boolean like(int postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException(postId));
        User user = userService.getCurrentUser();
        Vote vote = voteRepository.findByPostAndUser(post, user);
        if (vote == null) {
            vote = new Vote();
            vote.setPost(post);
            vote.setTime(LocalDateTime.now());
            vote.setUser(user);
        } else {
            if (vote.getValue() == 1)
                return false;
        }
        vote.setValue((byte) 1);

        try {
            voteRepository.saveAndFlush(vote);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean dislike(int postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException(postId));
        User user = userService.getCurrentUser();
        Vote vote = voteRepository.findByPostAndUser(post, user);
        if (vote == null) {
            vote = new Vote();
            vote.setPost(post);
            vote.setTime(LocalDateTime.now());
            vote.setUser(user);
        } else {
            if (vote.getValue() == -1)
                return false;
        }
        vote.setValue((byte) -1);

        try {
            voteRepository.saveAndFlush(vote);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
