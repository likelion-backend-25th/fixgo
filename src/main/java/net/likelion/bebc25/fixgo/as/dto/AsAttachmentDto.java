package net.likelion.bebc25.fixgo.as.dto;

public class AsAttachmentDto {

    public record Response(

            String fileUrl,

            String originalName
    ) {}

}
