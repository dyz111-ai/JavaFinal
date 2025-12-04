package com.example.demo0;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class TestPgConnection {
    public static void main(String[] args) {
        // Railway 提供的信息
        String host = "maglev.proxy.rlwy.net";
        String port = "48984";
        String database = "railway";
        String user = "postgres";
        String password = "fuaBbxZiovCAcxPPuhxbonkUeiOryZVM";

        // Railway 通常需要 SSL
        String url = String.format("jdbc:postgresql://%s:%s/%s?sslmode=require", host, port, database);

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("✅ 数据库连接成功！\n");

            // 2) 查询 test 表（位于 public 模式），动态打印列名与值
            String sql = "select * from public.test limit 5";
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();

                // 打印表头
                StringBuilder header = new StringBuilder();
                for (int i = 1; i <= cols; i++) {
                    if (i > 1) header.append(" | ");
                    header.append(meta.getColumnLabel(i));
                }
                System.out.println(header);
                System.out.println("-".repeat(Math.max(10, header.length())));

                // 打印数据行
                int rowCount = 0;
                while (rs.next()) {
                    StringBuilder row = new StringBuilder();
                    for (int i = 1; i <= cols; i++) {
                        if (i > 1) row.append(" | ");
                        Object val = rs.getObject(i);
                        row.append(val);
                    }
                    System.out.println(row);
                    rowCount++;
                }
                if (rowCount == 0) {
                    System.out.println("(无数据)");
                }
            }
        } catch (Exception e) {
            System.out.println("❌ 连接或查询失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
