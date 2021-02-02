package main.service;

import main.api.response.CalendarResponse;
import main.api.response.PostExpandedResponse;
import main.api.response.PostListReponse;
import main.api.response.ResultResponse;
import org.springframework.http.ResponseEntity;

/**
 * Service for posts
 */

public interface PostService {

    /**
     * get all posts without authorization
     * @param offset for pagination
     * @param limit posts on each page
     * @param mode sorting mode
     * @return posts amount and page of posts
     */
    public PostListReponse getPosts(int offset, int limit, String mode);

    /**
     * @param dateQuery format "YYYY-MM-DD". search post by date
     */
    public PostListReponse getPostsByDate(int offset, int limit, String dateQuery);

    /**
     * @param query search in text
     *
     */
    public PostListReponse searchPosts(int offset, int limit, String query);

    /**
     * @param tag search by tag
     *
     */
    public PostListReponse getPostsByTag(int offset, int limit, String tag);

    
    /**
     *  The "years" parameter returns a list of all years where any post exists, in ascending order.
     *  The "posts" returns the number of posts for each date of the @param year.
     *
     * @return
     */
    public CalendarResponse getCalendar(Integer year);

    public PostExpandedResponse getPostById(int id);

    public PostListReponse getPostsForModeration(int offset, int limit, String status);

    public PostListReponse getPostsMy(int offset, int limit, String status);

    public boolean moderate(String sessionId, int postId, String decision);
}
