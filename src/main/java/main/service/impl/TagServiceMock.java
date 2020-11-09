package main.service.impl;

import main.api.response.TagDTO;
import main.api.response.TagResponse;
import main.service.TagService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TagServiceMock implements TagService {

    @Override
    public TagResponse getTag(String query) {
        List<TagDTO> tags = new ArrayList<>();
        tags.add(new TagDTO("Java", 1F));
        tags.add(new TagDTO("Alcohol", 0.6F));
        tags.add(new TagDTO("Chaos", 0.5F));
        tags.add(new TagDTO("F1", 0.7F));
        tags.add(new TagDTO("Drama", 0.3F));

        return new TagResponse(tags);
    }
}
