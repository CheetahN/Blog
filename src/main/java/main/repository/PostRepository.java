package main.repository;

import main.model.Post;
import main.model.enums.ModerationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer>{
    public Page<Post> findByIsActiveAndModerationStatusAndTimeBefore(byte active, ModerationStatus moderationStatus, LocalDateTime time, Pageable paging);

    /**
     * Seach request
     * @param query - text to search
     */
    public Page<Post> findByIsActiveAndModerationStatusAndTimeBeforeAndTextContaining(byte active, ModerationStatus moderationStatus, LocalDateTime time, String query, Pageable paging);

    /**
     * Seach request by date
     * @param dateQuery - date to search
     */
    @Query(value = "SELECT * FROM posts WHERE TIME LIKE ?2% AND is_active = 1 AND moderation_status = 'ACCEPTED' AND TIME < ?1", nativeQuery = true)
    public Page<Post> findByDate(LocalDateTime time, String dateQuery, Pageable paging);

    /**
     *
     *
     * @return list of years with posts
     */
    @Query(value = "SELECT EXTRACT(YEAR FROM TIME) AS Year FROM posts GROUP BY Year ORDER BY Year", nativeQuery = true)
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
     *
     * @param idList list if ID of posts
     *
     */
    public Page<Post> findByIdInAndIsActiveAndModerationStatusAndTimeBefore(List<Integer> idList, byte active, ModerationStatus moderationStatus, LocalDateTime time, Pageable paging);


    public long countByModerationStatus(ModerationStatus moderationStatus);
}
