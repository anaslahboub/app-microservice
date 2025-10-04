package com.anas.groupservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileDownloadDTO {
    private byte[] data;
    private String fileName;
    private String contentType;
}