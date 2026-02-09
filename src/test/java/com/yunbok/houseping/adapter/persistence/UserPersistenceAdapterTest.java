package com.yunbok.houseping.adapter.persistence;

import com.yunbok.houseping.core.domain.User;
import com.yunbok.houseping.core.domain.UserRole;
import com.yunbok.houseping.core.domain.UserStatus;
import com.yunbok.houseping.entity.UserEntity;
import com.yunbok.houseping.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("UserPersistenceAdapter - 사용자 영속성 어댑터")
@ExtendWith(MockitoExtension.class)
class UserPersistenceAdapterTest {

    @Mock
    private UserRepository userRepository;

    private UserPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new UserPersistenceAdapter(userRepository);
    }

    @Nested
    @DisplayName("findByNaverId() - 네이버 ID로 조회")
    class FindByNaverId {

        @Test
        @DisplayName("존재하는 네이버 ID로 조회하면 사용자를 반환한다")
        void returnsUserWhenFound() {
            // given
            UserEntity entity = createEntity(1L, "naver-123");
            when(userRepository.findByNaverId("naver-123")).thenReturn(Optional.of(entity));

            // when
            Optional<User> result = adapter.findByNaverId("naver-123");

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getNaverId()).isEqualTo("naver-123");
        }

        @Test
        @DisplayName("존재하지 않는 네이버 ID로 조회하면 빈 Optional을 반환한다")
        void returnsEmptyWhenNotFound() {
            // given
            when(userRepository.findByNaverId("non-existent")).thenReturn(Optional.empty());

            // when
            Optional<User> result = adapter.findByNaverId("non-existent");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findById() - ID로 조회")
    class FindById {

        @Test
        @DisplayName("존재하는 ID로 조회하면 사용자를 반환한다")
        void returnsUserWhenFound() {
            // given
            UserEntity entity = createEntity(1L, "naver-123");
            when(userRepository.findById(1L)).thenReturn(Optional.of(entity));

            // when
            Optional<User> result = adapter.findById(1L);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 빈 Optional을 반환한다")
        void returnsEmptyWhenNotFound() {
            // given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // when
            Optional<User> result = adapter.findById(999L);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("save() - 사용자 저장")
    class Save {

        @Test
        @DisplayName("사용자를 저장하고 저장된 사용자를 반환한다")
        void savesAndReturnsUser() {
            // given
            User user = User.builder()
                    .naverId("new-naver-id")
                    .email("new@example.com")
                    .name("새사용자")
                    .role(UserRole.USER)
                    .status(UserStatus.PENDING)
                    .build();

            UserEntity savedEntity = createEntity(1L, "new-naver-id");
            when(userRepository.save(any(UserEntity.class))).thenReturn(savedEntity);

            // when
            User result = adapter.save(user);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(userRepository).save(any(UserEntity.class));
        }

        @Test
        @DisplayName("저장 시 도메인 모델을 엔티티로 변환한다")
        void convertsDomainToEntity() {
            // given
            User user = User.builder()
                    .naverId("convert-test")
                    .email("convert@example.com")
                    .name("변환테스트")
                    .role(UserRole.MASTER)
                    .status(UserStatus.ACTIVE)
                    .build();

            when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
                UserEntity entity = invocation.getArgument(0);
                entity.setId(1L);
                return entity;
            });

            // when
            adapter.save(user);

            // then
            ArgumentCaptor<UserEntity> entityCaptor = ArgumentCaptor.forClass(UserEntity.class);
            verify(userRepository).save(entityCaptor.capture());

            UserEntity capturedEntity = entityCaptor.getValue();
            assertThat(capturedEntity.getNaverId()).isEqualTo("convert-test");
            assertThat(capturedEntity.getEmail()).isEqualTo("convert@example.com");
            assertThat(capturedEntity.getName()).isEqualTo("변환테스트");
            assertThat(capturedEntity.getRole()).isEqualTo(UserRole.MASTER);
            assertThat(capturedEntity.getStatus()).isEqualTo(UserStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("findAll() - 전체 조회")
    class FindAll {

        @Test
        @DisplayName("모든 사용자를 반환한다")
        void returnsAllUsers() {
            // given
            List<UserEntity> entities = List.of(
                    createEntity(1L, "user1"),
                    createEntity(2L, "user2"),
                    createEntity(3L, "user3")
            );
            when(userRepository.findAll()).thenReturn(entities);

            // when
            List<User> result = adapter.findAll();

            // then
            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("사용자가 없으면 빈 리스트를 반환한다")
        void returnsEmptyListWhenNoUsers() {
            // given
            when(userRepository.findAll()).thenReturn(List.of());

            // when
            List<User> result = adapter.findAll();

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteById() - ID로 삭제")
    class DeleteById {

        @Test
        @DisplayName("ID로 사용자를 삭제한다")
        void deletesUserById() {
            // given
            doNothing().when(userRepository).deleteById(1L);

            // when
            adapter.deleteById(1L);

            // then
            verify(userRepository).deleteById(1L);
        }
    }

    private UserEntity createEntity(Long id, String naverId) {
        return UserEntity.builder()
                .id(id)
                .naverId(naverId)
                .email(naverId + "@example.com")
                .name("사용자" + id)
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .lastLoginAt(LocalDateTime.now())
                .build();
    }
}
