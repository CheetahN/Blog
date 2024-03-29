package main.service.impl;

import main.api.request.CommentRequest;
import main.api.request.PostRequest;
import main.api.response.*;
import main.model.Comment;
import main.model.Post;
import main.model.User;
import main.model.enums.GlobalSettingCode;
import main.model.enums.GlobalSettingValue;
import main.model.enums.ModerationStatus;
import main.repository.*;
import main.model.aggregations.IPostCount;
import main.service.PostService;
import main.service.TagService;
import main.service.exceptions.*;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static main.service.impl.UtilService.getLocalDateTime;
import static main.service.impl.UtilService.getTimestamp;

@Service
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final TagToPostRepository tagToPostRepository;
    private final UserServiceImpl userService;
    private final TagService tagService;
    private final CommentRepository commentRepository;
    private final SettingsRepository settingsRepository;


    @Autowired
    public PostServiceImpl(PostRepository postRepository, TagRepository tagRepository, TagToPostRepository tagToPostRepository, UserServiceImpl userService, TagService tagService, CommentRepository commentRepository, SettingsRepository settingsRepository) {
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
        this.tagToPostRepository = tagToPostRepository;
        this.userService = userService;
        this.tagService = tagService;
        this.commentRepository = commentRepository;
        this.settingsRepository = settingsRepository;
    }

    @Override
    public PostListReponse getPosts(int offset, int limit, String mode) {
        Pageable paging = getPage(offset, limit);
        Page<Post> posts;
        if ("recent".equals(mode)) {
            posts = postRepository.findPostsSortByTimeDesc(paging);

        } else if ("early".equals(mode)) {
            posts = postRepository.findPostsSortByTimeAsc(paging);

        } else if ("popular".equals(mode)) {
            posts = postRepository.findPostsSortByComments(paging);

        } else {
            posts = postRepository.findPostsSortByVotes(paging);
        }

        return new PostListReponse(posts.getTotalElements(), getList(posts));
    }

    //  search in text
    @Override
    public PostListReponse searchPosts(int offset, int limit, String query) {
        Pageable paging = getPage(offset, limit);
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
        List<Integer> tagToPostList = tagToPostRepository.findPostIdByTag(tagRepository.findByName(tag).orElseThrow(() -> new TagNotFoundException(tag)));
        Pageable paging = PageRequest.of( offset / 10, limit);
        Page<Post> posts = postRepository.findByIdInList(tagToPostList, paging);
        return new PostListReponse(posts.getTotalElements(), getList(posts));
    }

    @Override
    public CalendarResponse getCalendar(Integer year) {
        if (year == null)
            year = LocalDateTime.now().getYear();
        Map<String, Integer> postsCountsMap = new HashMap<>();
        List<IPostCount> postsCountsList = postRepository.countByDays(year);
        postsCountsList.forEach(postCount ->
                postsCountsMap.put(postCount.getDateString(), postCount.getTotalPosts())
        );

        return new CalendarResponse(postRepository.findYears(), postsCountsMap);
    }

    /**
     * Helper
     * Converting Page<Post> to List<PostResponse>
     */
    private List<PostResponse> getList(Page<Post> page){
        List<PostResponse> postResponseList = new ArrayList<>();

        return page.stream().map(post ->
            PostResponse.builder()
                    .id(post.getId())
                    .announce(Jsoup.parse(post.getText()).text().substring(0, Math.min(Jsoup.parse(post.getText()).text().length(), 200)))
                    .commentCount(post.getComments().size())
                    .dislikeCount(post.getVotes().stream().filter(vote -> vote.getValue() == -1).count())
                    .likeCount(post.getVotes().stream().filter(vote -> vote.getValue() == 1).count())
                    .title(post.getTitle())
                    .timestamp(getTimestamp(post.getTime()))
                    .viewCount(post.getViewCount())
                    .user(post.getUser().getId(), post.getUser().getName())
                    .build()
        ).collect(Collectors.toList());
    }

    @Override
    public PostExpandedResponse getPostById(int postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException(postId));
        User user = userService.getCurrentUser();
        if (user != null && user.getIsModerator() == 0 && !user.getId().equals(post.getUser().getId())) {
            postRepository.updateIncrementViewCount(postId);
        }
        return convertToResponse(post);
    }

    private List<CommentResponse> getCommentResponses(Post post) {
        return post.getComments().stream()
                .map(comment -> CommentResponse.builder()
                        .id(comment.getId())
                        .text(comment.getText())
                        .timestamp(getTimestamp(comment.getTime()))
                        .user(comment.getUser().getId(), comment.getUser().getName(), comment.getUser().getPhoto())
                        .build()
                ).collect(Collectors.toList());
    }

    private PostExpandedResponse convertToResponse(Post post) {
        return PostExpandedResponse.builder()
                .active(post.getIsActive() == 1)
                .id(post.getId())
                .timestamp(getTimestamp(post.getTime()))
                .user(post.getUser().getId(), post.getUser().getName())
                .title(post.getTitle())
                .text(post.getText())
                .viewCount(post.getViewCount())
                .likeCount(post.getVotes().stream().filter(vote -> vote.getValue() == 1).count())
                .dislikeCount(post.getVotes().stream().filter(vote -> vote.getValue() == -1).count())
                .tags(post.getTagToPostList().stream().map(tagToPost -> tagToPost.getTag().getName()).collect(Collectors.toList()))
                .comments(getCommentResponses(post))
                .build();
    }

    @Override
    public PostListReponse getPostsForModeration(int offset, int limit, String status) {
        User currentUser = userService.getCurrentUser();
        Sort sort = Sort.by(Sort.Direction.DESC, "time");
        Pageable paging = getPage(offset, limit, sort);

        Page<Post> posts;
        if ("new".equals(status)) {
            posts = postRepository.findNew(paging);
        } else
            posts = postRepository.findByIsActiveAndModerationStatusAndModerator((byte) 1, Enum.valueOf(ModerationStatus.class, status.toUpperCase()), currentUser, paging);

        return new PostListReponse(posts.getTotalElements(), getList(posts));
    }

    @Override
    public PostListReponse getPostsMy(int offset, int limit, String status) {
        User currentUser = userService.getCurrentUser();
        Sort sort = Sort.by(Sort.Direction.DESC, "time");
        Pageable paging = getPage(offset, limit, sort);

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

    @Override
    public ResultResponse createPost(PostRequest postRequest) {
        Map<String, String> errors = checkTextTitleForPost(postRequest);

        if (errors.isEmpty()) {
            long currentTime = Instant.now().getEpochSecond();
            if (currentTime > postRequest.getTimestamp()) {
                postRequest.setTimestamp(currentTime);
            }
            User user = userService.getCurrentUser();
            Post post = Post.builder()
                    .time(getLocalDateTime(postRequest.getTimestamp()))
                    .user(user)
                    .isActive(postRequest.getActive())
                    .text(postRequest.getText())
                    .title(postRequest.getTitle())
                    .moderationStatus(user.getIsModerator() == 1
                            || settingsRepository.findByCode(GlobalSettingCode.POST_PREMODERATION).getValue().equals(GlobalSettingValue.NO)
                            ?  ModerationStatus.ACCEPTED : ModerationStatus.NEW)
                    .build();
            Post savedPost = postRepository.save(post);
            postRequest.getTags().forEach(tag -> tagService.addTag(tag ,savedPost.getId()));
            return new ResultResponse(true);
        }
        return new ResultResponse(false, errors);
    }

    @Override
    public ResultResponse changePost(PostRequest postRequest, Integer id) {

        Map<String, String> errors = checkTextTitleForPost(postRequest);

        if (errors.isEmpty()) {
            long currentTime = Instant.now().getEpochSecond();
            if (currentTime > postRequest.getTimestamp()) {
                postRequest.setTimestamp(currentTime);
            }
            Post post = postRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id));
            post.setTime(getLocalDateTime(postRequest.getTimestamp()));
            post.setIsActive(postRequest.getActive());
            post.setText(postRequest.getText());
            post.setTitle(postRequest.getTitle());
            post.setModerationStatus(userService.getCurrentUser().getIsModerator() == 1
                        || settingsRepository.findByCode(GlobalSettingCode.POST_PREMODERATION).getValue().equals(GlobalSettingValue.NO)
                        ?  ModerationStatus.ACCEPTED : ModerationStatus.NEW);
            Post savedPost = postRepository.save(post);
            tagService.removeTagsFromPost(id);
            postRequest.getTags().forEach(tag -> tagService.addTag(tag ,savedPost.getId()));
            return new ResultResponse(true);
        }
        throw new BadRequestException(errors);
    }

    @Override
    public Integer addComment(CommentRequest commentRequest) {
        Integer id = null;
        Map<String, String> errors = new HashMap<>();
        if (commentRequest.getParentId() != null && !commentRequest.getParentId().isEmpty()) {
            id = Integer.valueOf(commentRequest.getParentId());
            if (commentRepository.findById(id).isEmpty()) {
                errors.put("comment", "parental comment not found in database: " + id);
            }
        }
        if (postRepository.findById(commentRequest.getPostId()).isEmpty()) {
            errors.put("post", "post not found in database: " + commentRequest.getPostId());
        }

        if (commentRequest.getText() == null || commentRequest.getText().length()<3) {
            errors.put("text", "Текст комментария не задан или слишком короткий");
        }
        if (!errors.isEmpty()) {
            throw new BadRequestException(errors);
        }
        Comment comment = new Comment();
        comment.setText(commentRequest.getText());
        comment.setPost(postRepository.getOne(commentRequest.getPostId()));
        comment.setTime(LocalDateTime.now());
        comment.setUser(userService.getCurrentUser());
        if (id != null) comment.setComment(commentRepository.getOne(id));
        Comment savedComment = commentRepository.save(comment);
        return savedComment.getId();
    }

    private Map<String, String> checkTextTitleForPost(PostRequest postRequest) {
        Map<String, String> errors = new HashMap<>();
        if (postRequest.getText() == null || postRequest.getText().equals(""))
            errors.put("text", "Поле текст не заполнено");
        else if (postRequest.getText().length() < 50)
            errors.put("text", "Текст публикации слишком короткий");
        if (postRequest.getTitle() == null || postRequest.getTitle().equals(""))
            errors.put("title", "Заголовок не установлен");
        else if (postRequest.getTitle().length() < 3)
            errors.put("title", "Заголовок слишком короткий");
        return errors;
    }

    private Pageable getPage(int offset, int limit) {
        return PageRequest.of( offset / limit, limit);
    }
    private Pageable getPage(int offset, int limit, Sort sort) {
        return PageRequest.of( offset / limit, limit, sort);
    }
}
