package main.service.impl;

import main.api.response.*;
import main.model.Post;
import main.model.User;
import main.model.enums.ModerationStatus;
import main.repository.*;
import main.service.PostService;
import main.service.UserService;
import main.service.exceptions.PostNotFoundException;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final UserRepository userRepository;
    private final UserService userService;


    @Autowired
    public PostServiceImpl(PostRepository postRepository, TagRepository tagRepository, TagToPostRepository tagToPostRepository, UserRepository userRepository, UserService userService) {
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
        this.tagToPostRepository = tagToPostRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Override
    public PostListReponse getPosts(int offset, int limit, String mode) {
        Pageable paging = PageRequest.of( offset / limit, limit);
        Page<Post> posts;
        if ("recent".equals(mode)) {
            posts = postRepository.findByIsActiveAndModerationStatusAndTimeBeforeAndSortByTimeDesc(paging);

        } else if ("early".equals(mode)) {
            posts = postRepository.findByIsActiveAndModerationStatusAndTimeBeforeAndSortByTimeAsc(paging);

        } else if ("popular".equals(mode)) {
            posts = postRepository.findByIsActiveAndModerationStatusAndTimeBeforeAndSortByComments(paging);

        } else {
            posts = postRepository.findByIsActiveAndModerationStatusAndTimeBeforeAndSortByVotes((byte) 1, paging);
        }

        return new PostListReponse(posts.getTotalElements(), getList(posts));
    }

    //  search in text
    @Override
    public PostListReponse searchPosts(int offset, int limit, String query) {
        Pageable paging = PageRequest.of( offset / 10, limit);
        Page<Post> posts = postRepository.findByTextContaining(query, paging);
        return new PostListReponse(posts.getTotalElements(), getList(posts));
    }

    //  search by date
    @Override
    public PostListReponse getPostsByDate(int offset, int limit, String dateQuery) {
        Pageable paging = PageRequest.of( offset / 10, limit);
        Page<Post> posts = postRepository.findByDate(dateQuery, paging);
        return new PostListReponse(posts.getTotalElements(), getList(posts));
    }

    //  search by tag
    @Override
    public PostListReponse getPostsByTag(int offset, int limit, String tag) {
        List<Integer> tagToPostList = tagToPostRepository.findPostIdByTag(tagRepository.findByName(tag));
        Pageable paging = PageRequest.of( offset / 10, limit);
        Page<Post> posts = postRepository.findByIdInList(tagToPostList, paging);
        return new PostListReponse(posts.getTotalElements(), getList(posts));
    }

    @Override
    public CalendarResponse getCalendar(Integer year) {
        if (year == null)
            year = LocalDateTime.now().getYear();
        Map<String, Integer> postsCountsMap = new HashMap<>();
        List<Object[]> postsCountsList = postRepository.countByDays(year);
        postsCountsList.forEach(objects ->
                postsCountsMap.put((String) objects[0], ((BigInteger) objects[1]).intValue())
        );

        return new CalendarResponse(postRepository.findYears(), postsCountsMap);
    }

    /**
     * Helper
     * Converting Page<Post> to List<PostResponse>
     */
    private List<PostResponse> getList(Page<Post> page){
        List<PostResponse> postResponseList = new ArrayList<>();

        page.forEach((post -> {
            String plainText = Jsoup.parse(post.getText()).text();
            PostResponse postResponse = PostResponse.builder()
                    .id(post.getId())
                    .announce(plainText.substring(0, Math.min(plainText.length(), 200)))
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

    @Override
    public PostExpandedResponse getPostById(int postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException(postId));
        User user = userService.getCurrentUser();
        if (user != null && user.getIsModerator() == 0) {
            postRepository.updateIncrementViewCount(postId);
        }

        List<CommentDTO> commentsDTO = new ArrayList<>();
        post.getComments().forEach(comment -> {
            commentsDTO.add(
                    CommentDTO.builder()
                            .id(comment.getId())
                            .text(comment.getText())
                            .timestamp(comment.getTime().atZone(ZoneId.of("UTC")).toEpochSecond())
                            .user(comment.getUser().getId(), comment.getUser().getName(), comment.getUser().getPhoto())
                            .build()
            );
        });


        return PostExpandedResponse.builder()
                .active(post.getIsActive() == 1)
                .id(postId)
                .timestamp(post.getTime().atZone(ZoneId.of("UTC")).toEpochSecond())
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

    @Override
    public PostListReponse getPostsForModeration(int offset, int limit, String status) {
        User currentUser = userService.getCurrentUser();
        Sort sort = Sort.by(Sort.Direction.DESC, "time");
        Pageable paging = PageRequest.of( offset / limit, limit, sort);

        Page<Post> posts;
        if ("new".equals(status)) {
            posts = postRepository.findByIsActiveAndModerationStatusNew(paging);
        } else
            posts = postRepository.findByIsActiveAndModerationStatusAndModerator((byte) 1, Enum.valueOf(ModerationStatus.class, status.toUpperCase()), currentUser, paging);

        return new PostListReponse(posts.getTotalElements(), getList(posts));
    }

    @Override
    public PostListReponse getPostsMy(int offset, int limit, String status) {
        User currentUser = userService.getCurrentUser();
        Sort sort = Sort.by(Sort.Direction.DESC, "time");
        Pageable paging = PageRequest.of( offset / limit, limit, sort);

        Page<Post> posts;
        if ("inactive".equals(status)) {
            posts = postRepository.findByIsActiveAndUser((byte) 0, currentUser,paging);
        } else {
            ModerationStatus moderationStatus;
            if ("pending".equals(status))
                moderationStatus = ModerationStatus.NEW;
            else if ("declined".equals(status))
                moderationStatus = ModerationStatus.DECLINED;
            else
                moderationStatus = ModerationStatus.ACCEPTED;
            posts = postRepository.findByIsActiveAndModerationStatusAndUser((byte) 1, moderationStatus, currentUser, paging);
        }
        return new PostListReponse(posts.getTotalElements(), getList(posts));
    }

    @Override
    public boolean moderate(int postId, String decision) {
        User currentUser = userService.getCurrentUser();
        Post post = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException(postId));
        ModerationStatus status;
        if ("accept".equals(decision)) {
            status = ModerationStatus.ACCEPTED;
        } else if ("decline".equals(decision)) {
            status = ModerationStatus.DECLINED;
        } else {
            return false;
        }
        post.setModerationStatus(status);
        post.setModerator(currentUser);
        try {
            postRepository.saveAndFlush(post);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
