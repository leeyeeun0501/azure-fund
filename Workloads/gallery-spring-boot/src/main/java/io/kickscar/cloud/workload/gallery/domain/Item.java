package io.kickscar.cloud.workload.gallery.domain;

import lombok.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    private Long id;
    private String url;
    private String comment;

    public Item(String url, String comment) {
        this.url = url;
        this.comment = comment;
    }
}
