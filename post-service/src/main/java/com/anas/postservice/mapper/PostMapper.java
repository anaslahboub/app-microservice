package com.anas.postservice.mapper;

import com.anas.postservice.dto.PostRequest;
import com.anas.postservice.dto.PostResponse;
import com.anas.postservice.entities.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PostMapper {
    //@Mapping(target = "authorName", ignore = true) // We'll set this separately
    PostResponse toDto(Post post);
}