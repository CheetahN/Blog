package main.repository;

import main.model.Post;
import main.model.enums.ModerationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer>{
    // sort by comments count
    @Query(value = "select p from Post p where p.time < CURRENT_TIME() and p.moderationStatus = 'ACCEPTED' and p.isActive = 1 order by size(p.comments) desc")
    public Page<Post> findByIsActiveAndModerationStatusAndTimeBeforeAndSortByComments(Pageable pageable);

    // sort by time desc
    @Query(value = "select p from Post p where p.time < CURRENT_TIME() and p.moderationStatus = 'ACCEPTED' and p.isActive = 1 order by p.time desc")
    public Page<Post> findByIsActiveAndModerationStatusAndTimeBeforeAndSortByTimeDesc(Pageable pageable);

    // sort by time asc
    @Query(value = "select p from Post p where p.time < CURRENT_TIME() and p.moderationStatus = 'ACCEPTED' and p.isActive = 1 order by p.time asc")
    public Page<Post> findByIsActiveAndModerationStatusAndTimeBeforeAndSortByTimeAsc(Pageable pageable);

    // sort by time asc likes. this should be fixed!
    @Query(value = "select p from Post p where p.time < CURRENT_TIME() and p.moderationStatus = 'ACCEPTED' and p.isActive = 1 order by size(p.votes) desc")
    public Page<Post> findByIsActiveAndModerationStatusAndTimeBeforeAndSortByVotes(Pageable paging);


    /**
     * Seach request
     * @param query - text to search
     */
    @Query(value = "select p from Post p where p.time < CURRENT_TIME() and p.moderationStatus = 'ACCEPTED' and p.isActive = 1 and p.text like CONCAT('%',UPPER(:query),'%') order by p.time desc")
    public Page<Post> findByTextContaining(String query, Pageable paging);

    /**
     * Seach request by date
     * @param dateQuery - date to search
     */
    @Query(value = "select p from Post p where p.time < CURRENT_TIME() and p.moderationStatus = 'ACCEPTED' and p.isActive = 1 and p.time like CONCAT(:dateQuery,'%') order by p.time desc")
    public Page<Post> findByDate(String dateQuery, Pageable paging);

    /**
     *
     *
     * @return list of years with posts
     */
    @Query(value = "select year(p.time) as y from Post p group by y order by y")
    public List<Integer> findYears();

    /**
     *
     * @param year
     * @return list of pairs: "date: count"
     * date format YYYY-MM-DD, e.g. day of year
     * count = posts in a day
     */
    @Query(value = "SELECT DATE_FORMAT(time, '%Y-%m-%d') AS dat, COUNT(*) FROM posts WHERE YEAR(TIME) = ?1 AND is_active = 1 AND moderation_status = 'ACCEPTED' AND TIME < CURRENT_TIME() Group BY dat", nativeQuery = true)
    public List<Object[]> countByDays(int year);

    /**
     * @param idList list if ID of posts
     *
     */
    @Query(value = "select p from Post p where p.id in ?1 and p.time < CURRENT_TIME() and p.moderationStatus = 'ACCEPTED' and p.isActive = 1 order by p.time desc")
    public Page<Post> findByIdInList(List<Integer> idList, Pageable paging);


    public long countByModerationStatus(ModerationStatus moderationStatus);

}
