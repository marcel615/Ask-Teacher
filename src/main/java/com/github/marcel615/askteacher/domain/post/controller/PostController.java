package com.github.marcel615.askteacher.domain.post.controller;

import com.github.marcel615.askteacher.domain.post.dto.PostCreateRequest;
import com.github.marcel615.askteacher.domain.post.dto.PostCreateResponse;
import com.github.marcel615.askteacher.domain.post.service.PostService;
import com.github.marcel615.askteacher.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PostCreateResponse> createPost(@Valid @RequestBody PostCreateRequest postCreateRequest){
        PostCreateResponse postCreateResponse = postService.postCreate(postCreateRequest);
        return ApiResponse.success(201, "게시글이 작성되었습니다.", postCreateResponse);
    }

}
