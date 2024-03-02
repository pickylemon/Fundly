package com.fundly.project.service;

import com.fundly.project.exception.ProjectDoesntExistsException;
import com.fundly.project.model.ProjectMapper;
import com.persistence.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {
    @Mock
    ProjectMapper projectMapper;
    @InjectMocks
    ProjectServiceImpl projectServiceImpl;

    ProjectAddRequest 프로젝트생성요청;

    @BeforeEach
    void setUp() {
        프로젝트생성요청 = ProjectAddRequest.builder().user_id("dbswoi123").build();
    }

    @Test
    @DisplayName("정말 정말 최소한의 테스트")
    public void addPj() {
//        테스트 대상 메서드 실행
        ProjectAddResponse result = projectServiceImpl.add(프로젝트생성요청);

//        테스트 결과 확인
        assertNotNull(result);
    }

    @Test
    @DisplayName("프로젝트 추가")
    public void add() {
//       컨트롤러로부터 프로젝트 추가 요청을 받아 프로젝트 추가후. 저장된 프로젝트를 리턴한다.
        ProjectDto 프로젝트 = 프로젝트생성요청.toProject();
//        프로젝트 추가요청을 받아 응답객체를 돌려준다.
        ProjectAddResponse 응답 = projectServiceImpl.add(프로젝트생성요청);
//        프로젝트 응답객체에는 판매자 아이디, 프로젝트번호, 및 유저 정보가 들어있어야 한다.
        assertThat(응답).isNotNull();
        assertThat(응답.getSel_id()).isEqualTo(프로젝트생성요청.getUser_id());
        assertThat(프로젝트.getPj_id()).isNotEmpty();
    }

    @Test
    @DisplayName("프로젝트 추가시 키 중복되는 경우, 재귀호출")
    void duplicateKey() {
//        ProjectDto pj = 프로젝트생성요청.toProject();
        given(projectMapper.insert(any())).willThrow(DuplicateKeyException.class).willReturn(1);
//        프로젝트를 추가하려는데. 생성된 프로젝트의 키가 데이터베이스에 이미 존재한다.
        ProjectAddResponse projectAddResponse = assertDoesNotThrow(() -> projectServiceImpl.add(프로젝트생성요청));

//        그러면 프로젝트 객체에게 새로운 키를 만들 것을 요청한다? 아니면 프로젝트 요청을 통해 새로운 프로젝트 객체를 생성한다?

//        둘다 가능함
        verify(projectMapper, times(2)).insert(any());
    }

    @Test
    @DisplayName("프로젝트 템플릿 가져오기")
    void get() {
        String pj_id = "pj01";
        ProjectDto pj = ProjectDto.builder().pj_id(pj_id).pj_sel_id("mulgom").pj_short_title("한윤재의 프로젝트").build();
        given(projectMapper.getByPjId(pj_id)).willReturn(pj);

        ProjectTemplate pjTemplate = projectServiceImpl.getById(pj_id);
        assertThat(pjTemplate).isNotNull();

        verify(projectMapper).getByPjId(pj_id);
    }

    @Test
    @DisplayName("존재하지 않는 프로젝트가 조회되면 예외를 발생시킨다.")
    void getByNonExistId() {
        given(projectMapper.getByPjId(any())).willReturn(null);

        assertThatExceptionOfType(ProjectDoesntExistsException.class).isThrownBy(() -> projectServiceImpl.getById("hello"));
    }

    @Test
    @DisplayName("프로젝트를 업데이트 한다.")
    void updateProject() {
        String pj_id = "01";
        ProjectDto pj = ProjectDto
                .builder()
                .pj_id(pj_id)
                .ctg("가전제품")
                .sub_ctg("밥솥")
                .pj_short_title("맛있는밥")
                .pj_long_title("매일 매일 만들어먹는 집밥")
                .build();
        ProjectInfoUpdateRequest pjInfoUpdate = ProjectInfoUpdateRequest
                .builder()
                .pj_id(pj_id)
                .ctg("반려동물")
                .sub_ctg("사료")
                .pj_short_intro("맛있는 츄르")
                .pj_long_title("우리집 고양이 츄르를 좋아해")
                .build();

        given(projectMapper.getByPjId(pjInfoUpdate.getPj_id())).willReturn(pj);

        ProjectInfoUpdateResponse resp =  projectServiceImpl.updatePjInfo(pjInfoUpdate);

        assertThat(resp).isNotNull();
        assertThat(resp.getCtg()).isEqualTo(pjInfoUpdate.getCtg());
        assertThat(resp.getSub_ctg()).isEqualTo(pjInfoUpdate.getSub_ctg());
        assertThat(resp.getPj_short_title()).isEqualTo(pjInfoUpdate.getPj_short_title());
        assertThat(resp.getPj_long_title()).isEqualTo(pjInfoUpdate.getPj_long_title());
    }
}