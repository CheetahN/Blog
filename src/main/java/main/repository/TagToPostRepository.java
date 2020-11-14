package main.repository;

import main.model.Post;
import main.model.Tag;
import main.model.TagToPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagToPostRepository extends JpaRepository<TagToPost, Integer> {
    @Query("select T.post.id from #{#entityName} T where T.tag = :tag")
    public List<Integer> findPostIdByTag(Tag tag);

    @Query("select distinct T.tag.name from #{#entityName} T where T.post = :post")
    public List<String> findTagsByPost(Post post);

    @Query("select T.tag.name, count(*) from #{#entityName} T where T.post.isActive = 1 and T.post.moderationStatus = 'ACCEPTED' and T.post.time < CURRENT_TIME() group by T.tag.name")
    public List<Object[]> countAggregatedTags();
}
