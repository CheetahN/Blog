package main.service;

import main.api.response.TagResponse;
import org.springframework.stereotype.Service;

@Service
public interface TagService {
    public TagResponse getTag(String query);
    public void addTag(String tagName, Integer postId);
    public void removeTagsFromPost(Integer postId);
}
