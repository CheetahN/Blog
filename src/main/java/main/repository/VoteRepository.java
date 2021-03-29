package main.repository;

import main.model.Post;
import main.model.User;
import main.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface VoteRepository extends JpaRepository<Vote, Integer> {

    public Vote findByPostAndUser(Post post, User user);

    @Query(value = "SELECT count(*) FROM Vote v WHERE v.value = :value AND v.post.user.id = :id AND v.post.isActive = 1 AND v.post.moderationStatus = 'ACCEPTED' AND v.post.time < CURRENT_TIME()")
    public Integer countByAuthorIdAndValue(Integer id, byte value);

    @Query(value = "SELECT count(*) FROM Vote v WHERE v.value = :value AND v.post.isActive = 1 AND v.post.moderationStatus = 'ACCEPTED' AND v.post.time < CURRENT_TIME()")
    public Integer countBydValue(byte value);

}
