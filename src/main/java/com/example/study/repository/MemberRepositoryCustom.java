package com.example.study.repository;

import com.example.study.entity.Member;

import java.util.List;

// QueryDSL 용 repository로 사용할 인터페이스
public interface MemberRepositoryCustom {
    // JpaRepository를 상속(extends)하지 않았기 때문에 하기 findByName은 JPA에 속한 Query 메서드가 아니다.
    List<Member> findByName(String name);

    List<Member> findUser(String nameParam, Integer ageParam);
}
