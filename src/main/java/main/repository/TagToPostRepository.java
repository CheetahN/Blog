package main.repository;

import main.model.Post;
import main.model.Tag;
import main.model.TagToPost;
import main.model.aggregations.ITagCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TagToPostRepository extends JpaRepository<TagToPost, Integer> {
    @Query("select T.post.id from #{#entityName} T where T.tag = :tag")
    public List<Integer> findPostIdByTag(Tag tag);

    @Query("select distinct T.tag.name from #{#entityName} T where T.post = :post")
    public List<String> findTagsByPost(Post post);

    @Query("select T.tag.name as name, count(*) as tagCount from #{#entityName} T where T.post.isActive = 1 and T.post.moderationStatus = 'ACCEPTED' and T.post.time < CURRENT_TIME() group by name")
    public List<ITagCount> countAggregatedTags();

    @Transactional
    public void deleteByPost(Post post);
}
