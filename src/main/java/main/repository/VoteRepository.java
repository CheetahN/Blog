package main.repository;

import main.model.Post;
import main.model.User;
import main.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote, Integer> {

    public Vote findByPostAndUser(Post post, User user);

}
