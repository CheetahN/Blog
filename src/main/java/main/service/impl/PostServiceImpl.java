package main.service.impl;

import main.api.response.*;
import main.model.Post;
import main.model.User;
import main.model.enums.ModerationStatus;
import main.repository.*;
import main.service.PostService;
import main.service.exceptions.NoUserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final TagToPostRepository tagToPostRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;


    @Autowired
    public PostServiceImpl(PostRepository postRepository, TagRepository tagRepository, TagToPostRepository tagToPostRepository, SessionRepository sessionRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
        this.tagToPostRepository = tagToPostRepository;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    public PostListReponse getPosts(int offset, int limit, String mode) {
        Pageable paging = PageRequest.of( offset / 10, limit);
        Page<Post> posts;
        if ("recent".equals(mode)) {
            posts = postRepository.findByIsActiveAndModerationStatusAndTimeBeforeAndSortByTimeDesc(paging);

        } else if ("early".equals(mode)) {
            posts = postRepository.findByIsActiveAndModerationStatusAndTimeBeforeAndSortByTimeAsc(paging);

        } else if ("popular".equals(mode)) {
            posts = postRepository.findByIsActiveAndModerationStatusAndTimeBeforeAndSortByComments(paging);

        } else {
            posts = postRepository.findByIsActiveAndModerationStatusAndTimeBeforeAndSortByVotes(paging);
        }

        return new PostListReponse(posts.getTotalElements(), getList(posts));
    }

    //  search in text
    public PostListReponse searchPosts(int offset, int limit, String query) {
        Pageable paging = PageRequest.of( offset / 10, limit);
        Page<Post> posts = postRepository.findByTextContaining(query, paging);
        return new PostListReponse(posts.getTotalElements(), getList(posts));
    }

    //  search by date
    public PostListReponse getPostsByDate(int offset, int limit, String dateQuery) {
        Pageable paging = PageRequest.of( offset / 10, limit);
        Page<Post> posts = postRepository.findByDate(dateQuery, paging);
        return new PostListReponse(posts.getTotalElements(), getList(posts));
    }

    //  search by tag
    public PostListReponse getPostsByTag(int offset, int limit, String tag) {
        List<Integer> tagToPostList = tagToPostRepository.findPostIdByTag(tagRepository.findByName(tag));
        Pageable paging = PageRequest.of( offset / 10, limit);
        Page<Post> posts = postRepository.findByIdInList(tagToPostList, paging);
        return new PostListReponse(posts.getTotalElements(), getList(posts));
    }


    public CalendarResponse getCalendar(int year) {
        Map<String, Integer> postsCountsMap = new HashMap<>();
        List<Object[]> postsCountsList = postRepository.countByDays(year);
        postsCountsList.forEach(objects ->
                postsCountsMap.put((String) objects[0], ((BigInteger) objects[1]).intValue())
        );

        return new CalendarResponse(postRepository.findYears(), postsCountsMap);
    }

    /**
     * Helper
     * Converting Page<Posts> to List<PostResponse>
     */
    private List<PostResponse> getList(Page<Post> page){
        List<PostResponse> postResponseList = new ArrayList<>();
        page.forEach((post -> {
            PostResponse postResponse = PostResponse.builder()
                    .id(post.getId())
                    .announce(post.getText().substring(0, Math.min(post.getText().length(), 200)))
                    .commentCount(post.getComments().size())
                    .dislikeCount(post.getVotes().stream().filter(vote -> vote.getValue() == -1).count())
                    .likeCount(post.getVotes().stream().filter(vote -> vote.getValue() == 1).count())
                    .title(post.getTitle())
                    .timestamp(post.getTime().atZone(ZoneId.of("UTC")).toEpochSecond())
                    .viewCount(post.getViewCount())
                    .user(post.getUser().getId(), post.getUser().getName())
                    .build();
            postResponseList.add(postResponse);
        }
        ));
        return postResponseList;
    }

    public PostExpandedResponse getPost(int postID, String sessionId) {
        Integer userID = sessionRepository.getUserId(sessionId);
        boolean isModerator = false;
        boolean isAuthor = false;
        Post post = postRepository.findById(postID);
        if (post == null)
            return null;

        if (userID != null) {
            User user = userRepository.findById(userID).orElseThrow(() -> new NoUserException(userID));
            isModerator = user.getIsModerator() == 1;
            isAuthor = post.getUser().getId() == userID;
        }

        if (!(isAuthor || isModerator)) {
            if (post.getTime().isAfter(LocalDateTime.now()) ||
                post.getIsActive() == 0 ||
                !(post.getModerationStatus() == ModerationStatus.ACCEPTED)) {
                    return null;
            }
            postRepository.updateIncrementViewCount(postID);
        }

        List<CommentDTO> commentsDTO = new ArrayList<>();
        post.getComments().forEach(comment -> {
            commentsDTO.add(
            CommentDTO.builder()
                    .id(comment.getId())
                    .text(comment.getText())
                    .timestamp(comment.getTime().atZone(ZoneId.of("Europe/Moscow")).toEpochSecond())
                    .user(comment.getUser().getId(), comment.getUser().getName(), comment.getUser().getPhoto())
                    .build()
            );
        });


        return PostExpandedResponse.builder()
                .active(post.getIsActive() == 1)
                .id(postID)
                .timestamp(post.getTime().atZone(ZoneId.of("Europe/Moscow")).toEpochSecond())
                .user(post.getUser().getId(), post.getUser().getName())
                .title(post.getTitle())
                .text(post.getText())
                .viewCount(post.getViewCount())
                .likeCount(post.getVotes().stream().filter(vote -> vote.getValue() == 1).count())
                .dislikeCount(post.getVotes().stream().filter(vote -> vote.getValue() == -1).count())
                .tags(tagToPostRepository.findTagsByPost(post))
                .comments(commentsDTO)
                .build();
    }
}
