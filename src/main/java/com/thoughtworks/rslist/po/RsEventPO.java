package com.thoughtworks.rslist.po;

import com.thoughtworks.rslist.domain.RsEvent;
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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rs_event_id_gen")
    @SequenceGenerator(name = "rs_event_id_gen", sequenceName = "rs_event_id_seq")
    private long id;

    private String eventName;

    private String keyword;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserPO userPO;

}
