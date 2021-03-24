package main.service.impl;

import main.api.response.StatisticsResponse;
import main.model.User;
import main.model.enums.GlobalSettingCode;
import main.model.enums.GlobalSettingValue;
import main.repository.PostRepository;
import main.repository.SettingsRepository;
import main.repository.VoteRepository;
import main.service.StatService;
import main.service.UserService;
import main.service.exceptions.UnauthorizedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static main.service.impl.UtilService.getTimestamp;

@Service
public class StatServiceImpl implements StatService {

    private final UserService userService;
    private final SettingsRepository settingsRepository;
    private final PostRepository postRepository;
    private final VoteRepository voteRepository;

    public StatServiceImpl(UserService userService, SettingsRepository settingsRepository, PostRepository postRepository, VoteRepository voteRepository) {
        this.userService = userService;
        this.settingsRepository = settingsRepository;
        this.postRepository = postRepository;
        this.voteRepository = voteRepository;
    }

    @Override
    public StatisticsResponse getMyStatistics() {
        User user = userService.getCurrentUser();
        return StatisticsResponse.builder()
                .postsCount(postRepository.countByAuthorId(user.getId()))
                .likesCount(voteRepository.countByAuthorIdAndValue(user.getId(),(byte) 1))
                .dislikesCount(voteRepository.countByAuthorIdAndValue(user.getId(),(byte) -1))
                .viewsCount(postRepository.sumViewsByAuthorId(user.getId()).orElse(0))
                .firstPublication(postRepository.findFirstDateByAuthorId(user.getId()).isPresent() ?
                        getTimestamp(postRepository.findFirstDateByAuthorId(user.getId()).get()) : 0 )
                .build();

    }

    @Override
    public StatisticsResponse getStatistics() {
        User user = userService.getCurrentUser();
        if (settingsRepository.findByCode(GlobalSettingCode.STATISTICS_IS_PUBLIC).getValue() == GlobalSettingValue.NO
                && user.getIsModerator() == 0) {
            throw new UnauthorizedException(user.getEmail());
        }
        return StatisticsResponse.builder()
                .postsCount(postRepository.countPublished())
                .likesCount(voteRepository.countBydValue((byte) 1))
                .dislikesCount(voteRepository.countBydValue((byte) -1))
                .viewsCount(postRepository.sumViews().orElse(0))
                .firstPublication(postRepository.findFirstDate().isPresent() ?
                        getTimestamp(postRepository.findFirstDate().get()) : 0)
                .build();
    }
}
