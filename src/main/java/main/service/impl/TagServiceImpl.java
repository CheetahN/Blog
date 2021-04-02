package main.service.impl;

import main.api.response.TagResponse;
import main.api.response.TagListResponse;
import main.model.Tag;
import main.model.TagToPost;
import main.repository.PostRepository;
import main.repository.TagRepository;
import main.repository.TagToPostRepository;
import main.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Primary
public class TagServiceImpl implements TagService {
    private final TagToPostRepository tagToPostRepository;
    private final TagRepository tagRepository;
    private final PostRepository postRepository;

    @Autowired
    public TagServiceImpl(TagRepository tagRepository, TagToPostRepository tagToPostRepository, TagRepository tagRepository1, PostRepository postRepository) {
        this.tagToPostRepository = tagToPostRepository;
        this.tagRepository = tagRepository1;
        this.postRepository = postRepository;
    }

    @Override
    public TagListResponse getTag(String query) {
        List<TagResponse> response = new ArrayList<>();
        List<Object[]> pairs = tagToPostRepository.countAggregatedTags();
        long max = 0;
        for (Object[]  pair: pairs ) {
            if ((Long) pair[1] > max)
                max = (Long) pair[1];
        }

        long finalMax = max;

        String finalQuery;
        if (query == null)
            finalQuery = "";
        else finalQuery = query;

        pairs.forEach(pair -> {
            float weight = ((Long) pair[1]).floatValue() / finalMax;
            if (weight > 0.2 && ((String) pair[0]).startsWith(finalQuery))
                response.add(new TagResponse((String) pair[0], weight));
        });

        return new TagListResponse(response);
    }

    public void addTag(String tagName, Integer postId){
        Tag tag = tagRepository.findByName(tagName).orElse(new Tag());
        if (tag.getName() == null) {
            tag.setName(tagName);
            tag = tagRepository.save(tag);
        }
        tagToPostRepository.save(new TagToPost(postRepository.getOne(postId), tag));
    }

    public void removeTagsFromPost(Integer postId) {
        tagToPostRepository.deleteByPost(postRepository.getOne(postId));
    }
}
