package main.repository;

import main.model.Post;
import main.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostVoteRepository extends JpaRepository<Vote, Integer> {
    public long countByPostAndValue(Post post, byte value);
}
