package main.api.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatisticsResponse {

    private int postsCount;
    private int likesCount;
    private int dislikesCount;
    private int viewsCount;
    private long firstPublication;
}
