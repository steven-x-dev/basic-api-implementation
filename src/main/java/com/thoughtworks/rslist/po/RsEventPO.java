package com.thoughtworks.rslist.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "rs_event")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RsEventPO {

    @Id
    @GeneratedValue
    private int id;

    private String eventName;

    private String keyword;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserPO userPO;

}
