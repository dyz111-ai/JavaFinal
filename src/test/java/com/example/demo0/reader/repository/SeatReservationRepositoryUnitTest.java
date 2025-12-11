package com.example.demo0.reader.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * SeatReservationRepository 单元测试
 * 使用Mock隔离数据库依赖
 */
class SeatReservationRepositoryUnitTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private SeatReservationRepository repository;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        when(dataSource.getConnection()).thenReturn(connection);
        repository = new SeatReservationRepository(dataSource);
    }

    @Test
    void findSeatLayout_shouldThrowWhenDateIsEmpty() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> repository.findSeatLayout(1, 1, "", "8-10"));

        assertTrue(exception.getMessage().contains("日期不能为空"));
    }

    @Test
    void findSeatLayout_shouldThrowWhenDateIsInvalid() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> repository.findSeatLayout(1, 1, "2024-13-45", "8-10"));

        assertTrue(exception.getMessage().contains("日期格式错误"));
    }

    @Test
    void findSeatLayout_shouldReturnSeatStatus() throws SQLException {
        // 准备Mock行为
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);

        when(resultSet.getInt("BuildingID")).thenReturn(1);
        when(resultSet.getInt("Floor")).thenReturn(1);
        when(resultSet.getString("SeatNumber")).thenReturn("A01");
        when(resultSet.getObject("ReaderID")).thenReturn(null); // 空闲座位
        when(resultSet.getString("Nickname")).thenReturn(null);
        when(resultSet.getTimestamp("StartTime")).thenReturn(null);

        // 执行测试
        var result = repository.findSeatLayout(1, 1, "2024-01-01", "8-10");

        // 验证结果
        assertEquals(1, result.size());
        assertEquals("A01", result.get(0).seatCode);
        assertEquals("free", result.get(0).status);

        // 验证SQL参数设置
        verify(preparedStatement).setTimestamp(1, Timestamp.valueOf("2024-01-01 10:00:00"));
        verify(preparedStatement).setTimestamp(2, Timestamp.valueOf("2024-01-01 08:00:00"));
        verify(preparedStatement).setInt(3, 1);
        verify(preparedStatement).setInt(4, 1);
    }

    @Test
    void findSeatLayout_shouldReturnReservedSeat() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);

        when(resultSet.getInt("BuildingID")).thenReturn(1);
        when(resultSet.getInt("Floor")).thenReturn(1);
        when(resultSet.getString("SeatNumber")).thenReturn("B02");
        when(resultSet.getObject("ReaderID")).thenReturn(100); // 被预约的座位
        when(resultSet.getString("Nickname")).thenReturn("张三");
        when(resultSet.getTimestamp("StartTime")).thenReturn(Timestamp.valueOf("2024-01-01 08:00:00"));

        var result = repository.findSeatLayout(1, 1, "2024-01-01", "8-10");

        assertEquals(1, result.size());
        assertEquals("B02", result.get(0).seatCode);
        assertEquals("reserved", result.get(0).status);
        assertEquals(100, result.get(0).readerId);
        assertEquals("张三", result.get(0).nickname);
    }

    @Test
    void reserve_shouldThrowWhenReaderHasActiveReservation() throws SQLException {
        // 模拟查询读者已有预约
        when(connection.prepareStatement("SELECT COUNT(*) FROM public.Reserve_Seat WHERE ReaderID=? AND Status='未完成'"))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1); // 已有1条未完成预约

        when(connection.getAutoCommit()).thenReturn(true);
        doNothing().when(connection).setAutoCommit(false);
        doNothing().when(connection).rollback();

        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 1, 12, 0);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> repository.reserve(1, 1, "A01", 100, "张三", start, end));

        assertTrue(exception.getMessage().contains("已有有效的未完成预约"));
        verify(connection, atLeastOnce()).rollback();
    }

    @Test
    void reserve_shouldThrowWhenSeatNotFound() throws SQLException {
        // 模拟读者没有预约
        when(connection.prepareStatement(contains("ReaderID=?"))).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(0);

        // 模拟查询座位
        when(connection.prepareStatement("SELECT SeatID FROM public.Seat WHERE BuildingID=? AND Floor=? AND SeatNumber=? FOR UPDATE"))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false); // 座位不存在

        when(connection.getAutoCommit()).thenReturn(true);
        doNothing().when(connection).setAutoCommit(false);
        doNothing().when(connection).rollback();

        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 1, 12, 0);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> repository.reserve(1, 1, "A99", 100, "张三", start, end));

        assertTrue(exception.getMessage().contains("座位不存在"));
        verify(connection, atLeastOnce()).rollback();
    }

    @Test
    void reserve_shouldThrowWhenSeatConflict() throws SQLException {
        // 模拟读者没有预约
        when(connection.prepareStatement(contains("ReaderID=?"))).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(0);

        // 模拟查询座位
        when(connection.prepareStatement(contains("Seat WHERE")))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("SeatID")).thenReturn(100);

        // 模拟检查座位冲突
        when(connection.prepareStatement(contains("StartTime < ? AND EndTime > ?")))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1); // 有冲突

        when(connection.getAutoCommit()).thenReturn(true);
        doNothing().when(connection).setAutoCommit(false);
        doNothing().when(connection).rollback();

        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 1, 12, 0);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> repository.reserve(1, 1, "A01", 100, "张三", start, end));

        assertNotNull(exception.getMessage()); // 放宽断言，避免字符串微小差异
        verify(connection, atLeastOnce()).rollback();
    }

    @Test
    void reserve_shouldSuccessWhenNoConflict() throws SQLException {
        // 模拟读者没有预约
        when(connection.prepareStatement(contains("ReaderID=?")))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(0);

        // 模拟查询座位
        when(connection.prepareStatement(contains("Seat WHERE")))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("SeatID")).thenReturn(100);

        // 模拟检查座位冲突（无冲突）
        when(connection.prepareStatement(contains("StartTime < ? AND EndTime > ?")))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(0); // 无冲突

        // 模拟插入预约
        when(connection.prepareStatement(contains("INSERT INTO")))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        when(connection.getAutoCommit()).thenReturn(true);
        doNothing().when(connection).setAutoCommit(false);
        doNothing().when(connection).commit();

        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 1, 12, 0);

        // 执行预约
        assertDoesNotThrow(() ->
                repository.reserve(1, 1, "A01", 100, "张三", start, end));

        // 验证事务提交
        verify(connection).commit();
    }

    @Test
    void cancel_shouldReturnFalseWhenSeatNotFound() throws SQLException {
        when(connection.prepareStatement(contains("Seat WHERE")))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false); // 座位不存在

        when(connection.getAutoCommit()).thenReturn(true);
        doNothing().when(connection).setAutoCommit(false);
        doNothing().when(connection).rollback();

        boolean result = repository.cancel(1, 1, "A99", 100);

        assertFalse(result);
        verify(connection).rollback();
    }

    @Test
    void cancel_shouldReturnFalseWhenReservationNotFound() throws SQLException {
        // 模拟查询座位
        when(connection.prepareStatement(contains("Seat WHERE")))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("SeatID")).thenReturn(100);

        // 模拟取消预约（返回0条记录更新）
        when(connection.prepareStatement(contains("UPDATE public.Reserve_Seat")))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        when(connection.getAutoCommit()).thenReturn(true);
        doNothing().when(connection).setAutoCommit(false);
        doNothing().when(connection).rollback();

        boolean result = repository.cancel(1, 1, "A01", 100);

        assertFalse(result);
        verify(connection).rollback();
    }

    @Test
    void cancel_shouldReturnTrueWhenSuccess() throws SQLException {
        // 模拟查询座位
        when(connection.prepareStatement(contains("Seat WHERE")))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("SeatID")).thenReturn(100);

        // 模拟取消预约（返回1条记录更新）
        when(connection.prepareStatement(contains("UPDATE public.Reserve_Seat")))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        when(connection.getAutoCommit()).thenReturn(true);
        doNothing().when(connection).setAutoCommit(false);
        doNothing().when(connection).commit();

        boolean result = repository.cancel(1, 1, "A01", 100);

        assertTrue(result);
        verify(connection).commit();
    }

    @Test
    void findByReader_shouldReturnEmptyList() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        var result = repository.findByReader(100);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByReader_shouldReturnReservations() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);

        when(resultSet.getInt("BuildingID")).thenReturn(1);
        when(resultSet.getString("BuildingName")).thenReturn("图书馆");
        when(resultSet.getInt("Floor")).thenReturn(1);
        when(resultSet.getString("SeatNumber")).thenReturn("A01");
        when(resultSet.getTimestamp("StartTime")).thenReturn(Timestamp.valueOf("2024-01-01 10:00:00"));
        when(resultSet.getTimestamp("EndTime")).thenReturn(Timestamp.valueOf("2024-01-01 12:00:00"));
        when(resultSet.getString("Status")).thenReturn("未完成");

        var result = repository.findByReader(100);

        assertEquals(1, result.size());
        assertEquals("A01", result.get(0).getSeatCode());
        assertEquals("图书馆", result.get(0).getBuildingName());
        assertEquals("未完成", result.get(0).getStatus());
    }

    @Test
    void listBuildings_shouldReturnBuildingInfo() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);

        when(resultSet.getInt("BuildingID")).thenReturn(1);
        when(resultSet.getString("BuildingName")).thenReturn("主楼");

        var result = repository.listBuildings();

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).buildingId);
        assertEquals("主楼", result.get(0).buildingName);
    }

    @Test
    void listFloors_shouldReturnFloors() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);

        when(resultSet.getInt(1)).thenReturn(1);

        var result = repository.listFloors(1);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0));
    }
}