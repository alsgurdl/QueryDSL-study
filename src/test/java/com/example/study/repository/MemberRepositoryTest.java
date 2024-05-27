package com.example.study.repository;

import com.example.study.entity.Member;
import com.example.study.entity.QMember;
import com.example.study.entity.Team;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.lang.model.SourceVersion;
import java.util.List;

import static com.example.study.entity.QMember.member;
import static com.example.study.entity.QTeam.team;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(false)
class MemberRepositoryTest {
    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    EntityManager em; // JPA 관리 핵심 객체

    // QueryDSL로 쿼리문을 작성하기 위핸 핵심 객체
    // 패키지 JPAQueryFactory 출신이 QueryDSL이다.
    JPAQueryFactory factory;

    @BeforeEach
    void settingObject() {
        factory = new JPAQueryFactory(em);
    }

    @Test
    void testInsertData() {

        /*
        Team teamA = Team.builder()
                .name("teamA")
                .build();
        Team teamB = Team.builder()
                .name("teamB")
                .build();

        teamRepository.save(teamA);
        teamRepository.save(teamB);
         */
        Team teamA = teamRepository.findById(1L).orElseThrow();
        Team teamB = teamRepository.findById(2L).orElseThrow();

        Member member1 = Member.builder()
                .userName("member9")
                .age(50)
                .team(teamA)
                .build();
        Member member2 = Member.builder()
                .userName("member10")
                .age(50)
                .team(teamA)
                .build();
        Member member3 = Member.builder()
                .userName("member11")
                .age(30)
                .team(teamB)
                .build();
        Member member4 = Member.builder()
                .userName("member12")
                .age(80)
                .team(teamB)
                .build();

        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);
        memberRepository.save(member4);
    }

    @Test
    @DisplayName("testJPA")
    void testJPA() {
        // 모든 멤버 출력하기
        List<Member> members = memberRepository.findAll();
        members.forEach(System.out::println);

    }

    @Test
    @DisplayName("testJPQL")
    void testJPQL() {
        // given
        // 스프링 JPQL을 사용하지 않았을 경우, userName을 조회하려면
        String jpqlQuery = "SELECT m FROM Member m WHERE m.userName = :userName";
        // when
        Member foundMember = em.createQuery(jpqlQuery, Member.class)
                .setParameter("userName", "member2")
                .getSingleResult();
        // then
        // QueryDSL로 insert, update, delete로 하는 경우는 거의 없다.
        // 동적 쿼리쪽(** select)에서 큰 힘을 발휘하는 QueryDSL,
        // 단, insert, update, delete는 jpa로 진행
        assertEquals("teamA", foundMember.getTeam().getName());
        System.out.println("\n\n\n");
        System.out.println("foundMember = " + foundMember);
        System.out.println("foundMember.getTeam() = " + foundMember.getTeam());
        System.out.println("\n\n\n");
    }

    @Test
    @DisplayName("TestQueryDSL")
    void testQueryDSL() {
        // given
        // QueryDSL에서 사용할 Q클래스 객체를 받아온다
        // --> 클래스 내에 상수로 객체가 선언되어 있기 때문에 따로 생성할 필요가 없다.
        QMember m = member;

        // when
        Member foundMember = factory.select(m)
                .from(m)
                .where(m.userName.eq("member1"))
                .fetchOne();
        // then
        assertEquals(foundMember.getUserName(), "member1");
    }

    @Test
    @DisplayName("search")
    void search() {
        // given
        String searchName = "member2";
        int searchAge = 20;

        // QMember m = member;
        // when

        Member foundMember = factory.selectFrom(member)
                //.where(member.userName.eq(searchName), member.age.eq(searchAge))
                .where(member.userName.eq(searchName).and(member.age.eq(searchAge)))
                .fetchOne();

        // then
        assertEquals("teamA", foundMember.getTeam().getName());

        /*
         JPAQueryFactory를 이용해서 쿼리문을 조립한 후 반환 인자를 결정합니다.
         - fetchOne(): 단일 건 조회. 여러 건 조회시 예외 발생.
         - fetchFirst(): 단일 건 조회. 여러 개가 조회돼도 첫 번째 값만 반환
         - fetch(): List 형태로 반환
         * JPQL이 제공하는 모든 검색 조건을 queryDsl에서도 사용 가능
         *
         * member.userName.eq("member1") // userName = 'member1'
         * member.userName.ne("member1") // userName != 'member1'
         * member.userName.eq("member1").not() // userName != 'member1'
         * member.userName.isNotNull() // 이름이 is not null
         * member.age.in(10, 20) // age in (10,20)
         * member.age.notIn(10, 20) // age not in (10,20)
         * member.age.between(10, 30) // age between 10, 30
         * member.age.goe(30) // age >= 30
         * member.age.gt(30) // age > 30
         * member.age.loe(30) // age <= 30
         * member.age.lt(30) // age < 30
         * member.userName.like("_김%") // userName LIKE '_김%'
         * member.userName.contains("김") // userName LIKE '%김%'
         * member.userName.startsWith("김") // userName LIKE '김%'
         * member.userName.endsWith("김") // userName LIKE '%김'
         */
    }
    @Test
    @DisplayName("여러 결과 반환하기")
    void testFetchRequest() {

        List<Member> fetch1 = factory.selectFrom(member).fetch();
        
        System.out.println("\n\n=========== fetch1 ===========");
        fetch1.forEach(System.out::println);
        
        // fetchOne
        Member fetch2 = factory.selectFrom(member)
                .where(member.id.eq(3L))
                .fetchOne();

        System.out.println("\n\n=========== fetch2 ===========");
        System.out.println("fetch2 = " + fetch2);
        
        // fetchFirts
        Member fetch3 = factory.selectFrom(member).fetchFirst();
        System.out.println("\n\n=========== fetch3 ===========");
        System.out.println("fetch3 = " + fetch3);


    }

    @Test
    @DisplayName("QueryDSL custom 설정 확인")
    void queryDslCustom() {
        // given
        String name = "member4";

        // when
        List<Member> result = memberRepository.findByName(name);
        // then
        assertEquals(1, result.size());
        assertEquals("teamB", result.get(0).getTeam().getName());
    }
    
    //////////////////////////////////////////////////////////////
    
    @Test
    @DisplayName("회원 정렬 조회")
    void sort() {
        // given
        
        // when
        List<Member> result = factory.selectFrom(member)
                //        .where(원하는 조건)
                .orderBy(member.age.desc())
                .fetch();
        // then
        // sql 실행 순서 from (table) -> join on -> where (조건) -> group by -> having -> select -> order by
        assertEquals(result.size(), 12);
        System.out.println("\n\n\n");
        result.forEach(System.out::println);
        System.out.println("\n\n\n");

    }
    @Test
    @DisplayName("queryDSL paging")
    void paging() {
        // given


        // when
        List<Member> result = factory.selectFrom(member)
                .orderBy(member.userName.desc()) // userName은 String 타입이다.
                .offset(3) // limit(3, 3)
                .limit(3)
                .fetch();
        // 사용자가 요청한 페이지, 페이지에 나오는 총 갯수, 1페이지로부터 게시물 10개 가져오기, page
        // then
        assertEquals(result.size(), 3);

        System.out.println("\n\n\n");
        result.forEach(System.out::println);
        System.out.println("\n\n\n");
    }

    @Test
    @DisplayName("그룹 함수의 종류")
    void aggregation() {
        // given

        // when
        List<Tuple> result = factory.select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )// select(member) SELECT * FROM member
                .from(member)
                .fetch();

        Tuple tuple = result.get(0); // result라는 tuple객체의 0번째 인덱스를 받아
        // then
        assertEquals(tuple.get(member.count()), 12);
        assertEquals(tuple.get(member.age.max()), 80);
        assertEquals(tuple.get(member.age.min()), 10);
        System.out.println("\n\n\n");
        System.out.println("result = " + result);
        // Tuple: QueryDSL에서 쿼리로 나온 결과 행들을 타입에 맞춰 담을 수 있게 제공되는 타입.
        System.out.println("tuple = " + tuple); // 조회시 하나의 행(tuple)으로 이루어진 첫 번째
        System.out.println("\n\n\n");
    }

    @Test
    @DisplayName("GROUP BY, HAVING")
    void testGroupBy() {
        // given

        // when
        List<Tuple> result = factory.select(member.age, member.age.count())
                .from(member) // tbl_member 라는 테이블에서
                .groupBy(member.age) // age컬럼을 기준으로 묶는데,
                .having(member.age.count().goe(2))
                // goe (grater than or equals to) --> 크거나 같은지, 조건은 2보다 크거나 같은, 2개 이상인(2명 이상인) 그룹만 그룹화를 하겠다.
                .orderBy(member.age.asc())
                .fetch();
        // then
        result.forEach(System.out::println);
    }


    @Test
    @DisplayName("join 해보기")
    void join() {
        // given

        // when
        List<Tuple> result = factory.select(member.userName, team.name)
                // .selectFrom(member) // select * FROM tbl_member
                // .join(join의 기준이 되는 entity.조인대상, 별칭(QClass))
                .from(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();
        // then
        System.out.println("\n\n\n");
        result.forEach(System.out::println);
        System.out.println("\n\n\n");
    }


    /*
        ex) 회원과 팀을 조인하면서,
        팀 이름이 teamA인 팀만 조회,
        회원은 모두 조회되어야 한다.

        SQL: SELECT m.*, t.* FROM tbl_member m LEFT JOIN tbl_team t ON m.team_id = t.team_id WHERE t.name = 'teamA';
        JPQL: SELECT m, t FROM Member m LEFT JOIN m.team ON t.name = 'teamA';
     */
    @Test
    @DisplayName("left outer join 테스트")
    void leftJoinTest() {
        // given


        // when
        List<Tuple> result = factory.select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("teamA"))
                .fetch();
        // then
        System.out.println("\n\n\n");
        result.forEach(System.out::println);
        System.out.println("\n\n\n");
    }
    
    @Test
    @DisplayName("sub query 사용하기 (나이가 가장 많은 회원을 조회) ")
    void subQueryTest() {
        // given
        // 같은 테이블에서 서브쿼리를 적용하려면 별도의 QClass의 객체를 생성해야 한다.
        QMember memberSub = new QMember("memberSub");
        // when
        List<Member> result = factory.selectFrom(member)
                .where(member.age.eq(
                        // 나이가 가장 많은 사람을 조회하는 서브쿼리문이 들어가야 함.
                        JPAExpressions // 서브쿼리를 사용할 수 있게 해 주는 클래스
                                // age = (SELECT MAX(age) FROM tbl_member)
                                .select(memberSub.age.max())
                                .from(memberSub)

                ))
                .fetch();


        // then
        result.forEach(System.out::println);
        // 결과 값: Member(id=12, userName=member12, age=80)
        // ** 서브 쿼리는 from 절에서는 구현이 안된다. **
    }



}