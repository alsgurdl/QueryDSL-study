package com.example.study.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString(exclude = "team") @EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tbl_member")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    private String userName;

    private int age;

    // Member랑 Team은 연관관계, Member 기준으로 Member는 여러 명, Team은 한 개라 @ManyToOne으로 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

}
