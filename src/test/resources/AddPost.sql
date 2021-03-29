INSERT INTO posts (id, is_active, moderation_status, time, title, view_count, moderator_id, user_id, text)
VALUES(42, 1, 'ACCEPTED', '2020-02-29 06:05:52.008525', "Accept ME", 15, 105, 101, "очень очень хороший пост! Обо всем и ни о чем" );
INSERT INTO tags (id, name) VALUES(5, "Drama" );
INSERT INTO tag2post (id, post_id, tag_id) VALUES(1, 42, 5);