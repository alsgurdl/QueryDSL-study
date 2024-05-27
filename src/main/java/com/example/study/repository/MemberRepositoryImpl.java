package com.example.study.repository;


import com.example.study.entity.Member;
import com.example.study.entity.QMember;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.example.study.entity.QMember.member;

// QueryDSL용 인터페이스 구현체는 반드시 클래스명이 Impl로 끝나야 자동으로 인식되어서 원본 인터페이스 타입(MemberRepository)의 객체로도 사용이 가능하다.
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom{
    @Override
    public List<Member> findUser(String nameParam, Integer ageParam) {
        return queryFactory.selectFrom(member)
                //.where(member.userName.eq(nameParam)
                .where(nameEq(nameParam), ageEq(ageParam))
                        //.and(member.age.eq(ageParam)))
                .fetch();
    }

    // WHERE절에 BooleanExpression을 리턴하는 메서드를 직접 작성한다.
    // nameEq() 메서드는 전달받은 값이 없다면 null을 리턴하고, 그렇지 않은 경우 논리 표현식 결과를 리턴한다.
    // WHERE 절에서는 null 값인 경우 조건을 건너 뛴다(쿼리를 완성하지 않음)
    private BooleanExpression nameEq(String nameParam) {
        if(nameParam != null ) {
            return member.userName.eq(nameParam);
        } return null;
    }

    private BooleanExpression ageEq(Integer ageParam) {
        /*
        if(ageParam != null) {
            return member.age.eq(ageParam);
        } return null;
         */
        return ageParam != null ? member.age.eq(ageParam) : null;
    }

    private final JPAQueryFactory queryFactory;
    @Override
    public List<Member> findByName(String name) {
        return queryFactory.selectFrom(member)
                .where(member.userName.eq(name))
                .fetch();
    }
}
