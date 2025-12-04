package com.example.demo0.db;

import jakarta.annotation.Resource;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

@WebServlet("/db/test")
public class TestDataSourceServlet extends HttpServlet {

    @Resource(lookup = "java:/jdbc/LibraryDS")
    private DataSource dataSource;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("text/html");
        try (PrintWriter out = resp.getWriter()) {

            if (dataSource == null) {
                out.println("❌ 数据源未注入");
                return;
            }

            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {

                // 顺便查一下总数
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM BookInfo");
                if (rs.next()) {
                    out.println("<p>📊 总记录数: " + rs.getInt(1) + "</p>");
                }

            } catch (Exception e) {
                out.println("<h3>❌ 错误: " + e.getMessage() + "</h3>");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}