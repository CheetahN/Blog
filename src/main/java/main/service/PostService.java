package main.service;

import java.util.Map;

/**
 * Service for working with posts
 */

public interface PostService {
    /**
     * get posts without autorization
     * @param offset for paginagion
     * @param limit  fosts for each page
     * @param mode   sorting mode
     * @param dateQuery format "YYYY-MM-DD". Optional. search post by date
     * @param query serch in text. Optional
     * @param tag serch by tag. Optional
     * @return  list of found posts and count
     */
    public Map<String, Object> getPosts(int offset, int limit, String mode, String query, String dateQuery, String tag);


    /**
     *  The "years" parameter returns a list of all years where any post exists, in ascending order.
     *  The "posts" returns the number of posts for each date of the @param year.
     *
     * @return
     */
    public Map<String, Object> getCalendar(int year);
}
