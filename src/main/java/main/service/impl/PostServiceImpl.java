package main.service.impl;

import main.api.response.PostResponse;
import main.model.Post;
import main.model.enums.ModerationStatus;
import main.repository.PostRepository;
import main.repository.PostVoteRepository;
import main.repository.TagRepository;
import main.repository.TagToPostRepository;
import main.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
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
@Primary
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final PostVoteRepository postVoteRepository;
    private final TagRepository tagRepository;
    private final TagToPostRepository tagToPostRepository;


    @Autowired
    public PostServiceImpl(PostRepository postRepository, PostVoteRepository postVoteRepository, TagRepository tagRepository, TagToPostRepository tagToPostRepository) {
        this.postRepository = postRepository;
        this.postVoteRepository = postVoteRepository;
        this.tagRepository = tagRepository;
        this.tagToPostRepository = tagToPostRepository;
    }


    public Map<String, Object> getPosts(int offset, int limit, String mode, String query, String dateQuery, String tag) {

        Sort sort;
        if ("recent".equals(mode)) {
            sort = Sort.by(Sort.Direction.DESC, "time");
        } else
            sort = Sort.by(Sort.Direction.ASC, "time");

        Pageable paging = PageRequest.of(offset, limit, sort);

        Page<Post> posts;
        if (query != null) {
            // search in text
            posts = postRepository.findByIsActiveAndModerationStatusAndTimeBeforeAndTextContaining((byte) 1, ModerationStatus.ACCEPTED, LocalDateTime.now(), query, paging);

        }
        else if (dateQuery != null) {
            // search by date
            posts = postRepository.findByDate(LocalDateTime.now(), dateQuery, paging);

        }
        else if (tag != null) {
            // search by tag
            List<Integer> tagToPostList = tagToPostRepository.findPostIdByTag(tagRepository.findByName(tag));
            posts = postRepository.findByIdInAndIsActiveAndModerationStatusAndTimeBefore(tagToPostList, (byte) 1, ModerationStatus.ACCEPTED, LocalDateTime.now(), paging );

        }
        else {
            // get all
            posts = postRepository.findByIsActiveAndModerationStatusAndTimeBefore((byte) 1, ModerationStatus.ACCEPTED, LocalDateTime.now(), paging);

        }

        Map<String, Object> response = new HashMap<>();
        response.put("count", (int) posts.getTotalElements());
        response.put("posts", getList(posts));
        return response;
    }

    public Map<String, Object> getCalendar(int year) {

        Map<String, Object> response = new HashMap<>();
        response.put("years", postRepository.findYears());

        Map<String, Integer> postsCountsMap = new HashMap<>();
        List<Object[]> postsCountsList = postRepository.countByDays(year);
        postsCountsList.stream().forEach(objects ->
                postsCountsMap.put((String) objects[0], ((BigInteger) objects[1]).intValue())
        );
        response.put("posts", postsCountsMap);
        return response;
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
                    //.commentCount(post.getComments().size())
                    .commentCount(33)
                    .dislikeCount(postVoteRepository.countByPostAndValue(post, (byte) -1))
                    .likeCount(postVoteRepository.countByPostAndValue(post, (byte) 1))
                    .title(post.getTitle())
                    .timestamp(post.getTime().atZone(ZoneId.of("Asia/Dhaka")).toEpochSecond())
                    .viewCount(post.getViewCount())
                    .build();
            postResponse.addUser(post.getUser().getId(), post.getUser().getName());
            postResponseList.add(postResponse);
        }
        ));
        return postResponseList;
    }
}
