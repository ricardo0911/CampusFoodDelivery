package com.campus.ordering.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.ordering.entity.Post;
import com.campus.ordering.entity.PostComment;

import java.util.List;

public interface PostService extends IService<Post> {
    Page<Post> getPostList(int page, int size, Long userId);
    Post getPostDetail(Long postId, Long userId);
    void likePost(Long postId, Long userId);
    void unlikePost(Long postId, Long userId);
    List<PostComment> getComments(Long postId);
    void addComment(Long postId, Long userId, String content, Long parentId);
}
