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

    private Long id;

    private String eventName;

    private String keyword;

    @NotNull
    private Long userId;

    public RsEvent(String eventName, String keyword, @NotNull Long userId) {
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

    public RsEvent(RsEvent rsEvent) {
        id = rsEvent.getId();
        eventName = rsEvent.getEventName();
        keyword = rsEvent.getKeyword();
        userId = rsEvent.getUserId();
    }

}
