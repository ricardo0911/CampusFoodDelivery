package com.campus.ordering.controller.customer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.ordering.entity.Post;
import com.campus.ordering.service.PostService;
import com.campus.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/customer/post")
public class PostController {

    @Autowired
    private PostService postService;

    /**
     * 动态列表
     */
    @GetMapping("/list")
    public Result<?> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Page<Post> result = postService.getPostList(page, size, userId);
        return Result.success(result);
    }

    /**
     * 动态详情
     */
    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Post post = postService.getPostDetail(id, userId);
        if (post == null) {
            return Result.error("动态不存在");
        }
        return Result.success(Map.of(
                "post", post,
                "comments", postService.getComments(id)
        ));
    }

    /**
     * 发布动态
     */
    @PostMapping("/create")
    public Result<?> create(@RequestBody Post post, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        post.setUserId(userId);
        post.setLikes(0);
        post.setComments(0);
        post.setStatus(1);
        postService.save(post);
        return Result.success("发布成功", post);
    }

    /**
     * 点赞
     */
    @PostMapping("/{id}/like")
    public Result<?> like(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        postService.likePost(id, userId);
        return Result.success("点赞成功");
    }

    /**
     * 取消点赞
     */
    @DeleteMapping("/{id}/like")
    public Result<?> unlike(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        postService.unlikePost(id, userId);
        return Result.success("取消点赞成功");
    }

    /**
     * 评论
     */
    @PostMapping("/{id}/comment")
    public Result<?> comment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String content = (String) params.get("content");
        Long parentId = params.get("parentId") != null ?
                Long.valueOf(params.get("parentId").toString()) : null;

        postService.addComment(id, userId, content, parentId);
        return Result.success("评论成功");
    }
}
