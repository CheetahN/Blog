package main.service;

import main.api.response.CalendarResponse;
import main.api.response.PostExpandedResponse;
import main.api.response.PostListReponse;

/**
 * Service for working with posts
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
    public CalendarResponse getCalendar(int year);

    public PostExpandedResponse getPost(int id, String sessionId);
}
