package com.example.demo0.reader.service;

import com.example.demo0.auth.model.Reader;
import com.example.demo0.reader.repository.SeatReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SeatReservationServiceTest {

    private SeatReservationService service;

    @Mock
    private SeatReservationRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        // 避免调用构造函数导致 JNDI 查找，使用 Unsafe 绕过实例化，再注入 mock 仓储
        service = allocateWithoutConstructor();
        Field repoField = SeatReservationService.class.getDeclaredField("repository");
        repoField.setAccessible(true);
        repoField.set(service, repository);
    }

    @Test
    void reserve_shouldCallRepositoryOnValidInput() {
        Reader reader = buildReader(2, "Kate");
        // 使用固定的时间段：8-10（固定2小时）
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 8, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 1, 10, 0, 0);

        assertDoesNotThrow(() -> service.reserve(1, 2, "B02", reader, start, end));

        verify(repository).reserve(1, 2, "B02", 2, "Kate", start, end);
    }

    @Test
    void reserve_shouldPropagateConflictFromRepository() {
        Reader reader = buildReader(3, "Tom");
        // 使用固定的时间段：10-12（固定2小时）
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

        doThrow(new IllegalStateException("该座位在此时间段已被预约，请选择其他时间")).when(repository)
                .reserve(anyInt(), anyInt(), anyString(), anyInt(), anyString(), any(), any());

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.reserve(1, 1, "C03", reader, start, end));
        assertTrue(ex.getMessage().contains("已被预约"));
    }

    @Test
    void reserve_shouldPropagateReaderHasActiveReservationException() {
        Reader reader = buildReader(4, "Alice");
        // 使用固定的时间段：14-16（固定2小时）
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 14, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 1, 16, 0, 0);

        doThrow(new IllegalStateException("您已有有效的未完成预约，无法再次预约")).when(repository)
                .reserve(anyInt(), anyInt(), anyString(), anyInt(), anyString(), any(), any());

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.reserve(1, 1, "D04", reader, start, end));
        assertTrue(ex.getMessage().contains("已有有效的未完成预约"));
    }

    @Test
    void reserve_shouldWorkWithDifferentTimeSlots() {
        Reader reader = buildReader(5, "Bob");
        // 测试另一个固定时间段：16-18（固定2小时）
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 16, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 1, 18, 0, 0);

        assertDoesNotThrow(() -> service.reserve(2, 3, "E05", reader, start, end));

        verify(repository).reserve(2, 3, "E05", 5, "Bob", start, end);
    }

    @Test
    void reserve_shouldThrowWhenEndTimeBeforeStartTime() {
        Reader reader = buildReader(6, "Charlie");
        // 防御性测试：结束时间早于开始时间（虽然业务上不应该发生，但作为防御性校验）
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 1, 8, 0, 0);

        assertThrows(IllegalArgumentException.class,
                () -> service.reserve(1, 1, "F06", reader, start, end));

        verify(repository, never()).reserve(anyInt(), anyInt(), anyString(), anyInt(), anyString(), any(), any());
    }

    @Test
    void cancel_shouldReturnRepositoryResult() {
        when(repository.cancel(1, 1, "A01", 10)).thenReturn(true);
        assertTrue(service.cancel(1, 1, "A01", 10));

        when(repository.cancel(1, 1, "A01", 10)).thenReturn(false);
        assertFalse(service.cancel(1, 1, "A01", 10));
    }

    private Reader buildReader(int id, String nickname) {
        Reader r = new Reader();
        r.setReaderId(id);
        r.setNickname(nickname);
        return r;
    }

    /**
     * 使用 Unsafe 分配实例，避免执行构造函数中的 JNDI 查找。
     */
    private SeatReservationService allocateWithoutConstructor() throws Exception {
        // 获取 Unsafe
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        Object unsafe = theUnsafe.get(null);

        Method allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
        return (SeatReservationService) allocateInstance.invoke(unsafe, SeatReservationService.class);
    }
}

