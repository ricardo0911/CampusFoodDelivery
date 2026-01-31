package com.campus.ordering.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.ordering.entity.*;
import com.campus.ordering.mapper.*;
import com.campus.ordering.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    @Autowired
    private PostLikeMapper postLikeMapper;

    @Autowired
    private PostCommentMapper postCommentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private ShopMapper shopMapper;

    @Override
    public Page<Post> getPostList(int page, int size, Long userId) {
        Page<Post> pageParam = new Page<>(page, size);
        Page<Post> result = this.page(pageParam, new LambdaQueryWrapper<Post>()
                .eq(Post::getStatus, 1)
                .orderByDesc(Post::getCreatedAt));

        for (Post post : result.getRecords()) {
            enrichPost(post, userId);
        }
        return result;
    }

    @Override
    public Post getPostDetail(Long postId, Long userId) {
        Post post = this.getById(postId);
        if (post != null) {
            enrichPost(post, userId);
        }
        return post;
    }

    @Override
    @Transactional
    public void likePost(Long postId, Long userId) {
        // 检查是否已点赞
        Long count = postLikeMapper.selectCount(new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getPostId, postId)
                .eq(PostLike::getUserId, userId));

        if (count == 0) {
            PostLike like = new PostLike();
            like.setPostId(postId);
            like.setUserId(userId);
            postLikeMapper.insert(like);

            // 更新点赞数
            Post post = this.getById(postId);
            if (post != null) {
                post.setLikes(post.getLikes() + 1);
                this.updateById(post);
            }
        }
    }

    @Override
    @Transactional
    public void unlikePost(Long postId, Long userId) {
        int deleted = postLikeMapper.delete(new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getPostId, postId)
                .eq(PostLike::getUserId, userId));

        if (deleted > 0) {
            Post post = this.getById(postId);
            if (post != null && post.getLikes() > 0) {
                post.setLikes(post.getLikes() - 1);
                this.updateById(post);
            }
        }
    }

    @Override
    public List<PostComment> getComments(Long postId) {
        List<PostComment> comments = postCommentMapper.selectList(
                new LambdaQueryWrapper<PostComment>()
                        .eq(PostComment::getPostId, postId)
                        .orderByAsc(PostComment::getCreatedAt));

        for (PostComment comment : comments) {
            User user = userMapper.selectById(comment.getUserId());
            if (user != null) {
                comment.setUserNickname(user.getNickname());
                comment.setUserAvatar(user.getAvatar());
            }
            if (comment.getParentId() != null) {
                PostComment parent = postCommentMapper.selectById(comment.getParentId());
                if (parent != null) {
                    User parentUser = userMapper.selectById(parent.getUserId());
                    if (parentUser != null) {
                        comment.setReplyToNickname(parentUser.getNickname());
                    }
                }
            }
        }
        return comments;
    }

    @Override
    @Transactional
    public void addComment(Long postId, Long userId, String content, Long parentId) {
        PostComment comment = new PostComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setParentId(parentId);
        postCommentMapper.insert(comment);

        // 更新评论数
        Post post = this.getById(postId);
        if (post != null) {
            post.setComments(post.getComments() + 1);
            this.updateById(post);
        }
    }

    private void enrichPost(Post post, Long userId) {
        User user = userMapper.selectById(post.getUserId());
        if (user != null) {
            post.setUserNickname(user.getNickname());
            post.setUserAvatar(user.getAvatar());
        }

        if (post.getDishId() != null) {
            Dish dish = dishMapper.selectById(post.getDishId());
            if (dish != null) {
                post.setDishName(dish.getName());
            }
        }

        if (post.getShopId() != null) {
            Shop shop = shopMapper.selectById(post.getShopId());
            if (shop != null) {
                post.setShopName(shop.getName());
            }
        }

        if (userId != null) {
            Long likeCount = postLikeMapper.selectCount(new LambdaQueryWrapper<PostLike>()
                    .eq(PostLike::getPostId, post.getId())
                    .eq(PostLike::getUserId, userId));
            post.setLiked(likeCount > 0);
        }
    }
}
