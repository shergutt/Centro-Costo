package com.example.centrocosto;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

@PostMapping("/login")
public String login(@RequestParam String email, @RequestParam String password, HttpSession session, Model model) {
    CallableStatement cstmt = null;
    try {
        String sqlCall = "{call LoginUser(?, ?, ?, ?, ?)}";
        cstmt = jdbcTemplate.getDataSource().getConnection().prepareCall(sqlCall);

        cstmt.setString(1, email);
        cstmt.setString(2, password);
        cstmt.registerOutParameter(3, Types.INTEGER); // userID
        cstmt.registerOutParameter(4, Types.INTEGER); // userCentroCostoID
        cstmt.registerOutParameter(5, Types.VARCHAR); // resultMessage

        cstmt.execute();

        Integer userID = cstmt.getInt(3);
        Integer userCentroCostoID = cstmt.getInt(4);
        String resultMessage = cstmt.getString(5);

        if (resultMessage.equals("Usuario encontrado")) {
            session.setAttribute("userEmail", email);
            session.setAttribute("userId", userID);
            session.setAttribute("userCentroCostoId", userCentroCostoID);

            // Insert user ID into the temporary table
            insertUserIdIntoTempTable(session.getId(), userID);

            return "redirect:/home";
        } else {
            model.addAttribute("loginError", resultMessage);
            return "index";
        }
    } catch (Exception e) {
        model.addAttribute("loginError", "Error al procesar el inicio de sesión");
        return "index";
    } finally {
        if (cstmt != null) {
            try {
                cstmt.close();
            } catch (SQLException e) {
            }
        }
    }
}
private void insertUserIdIntoTempTable(String sessionId, Integer userId) {
    String sql = "MERGE INTO temp_user_session t USING " +
                 "(SELECT ? AS session_id, ? AS user_id FROM dual) s " +
                 "ON (t.session_id = s.session_id) " +
                 "WHEN MATCHED THEN " +
                 "UPDATE SET t.user_id = s.user_id " +
                 "WHEN NOT MATCHED THEN " +
                 "INSERT (session_id, user_id) VALUES (s.session_id, s.user_id)";
    jdbcTemplate.update(sql, sessionId, userId);
}
}

