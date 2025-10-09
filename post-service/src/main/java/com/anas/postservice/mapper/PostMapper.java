package com.anas.postservice.mapper;

import com.anas.postservice.dto.PostRequest;
import com.anas.postservice.dto.PostResponse;
import com.anas.postservice.entities.Post;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PostMapper {
    PostResponse toDto(Post post);

}
