package com.thoughtworks.rslist.domain;

import com.thoughtworks.rslist.po.RsEventPO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RsEvent {

    private Integer id;

    @NotNull
    private String eventName;

    @NotNull
    private String keyword;

    @NotNull
    private Integer userId;

    public RsEvent(@NotNull String eventName, @NotNull String keyword, @NotNull Integer userId) {
        this.eventName = eventName;
        this.keyword = keyword;
        this.userId = userId;
    }

    public RsEvent(RsEventPO rsEventPO) {
        id = rsEventPO.getId();
        eventName = rsEventPO.getEventName();
        keyword = rsEventPO.getKeyword();
        userId = rsEventPO.getUserPO().getId();
    }

}
