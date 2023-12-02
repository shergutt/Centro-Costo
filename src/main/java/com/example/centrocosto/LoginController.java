package com.example.centrocosto;

import java.util.Map;

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
        try {
            String sql = "SELECT ID_usuario, ID_centro_costo FROM usuarios WHERE correo_electronico = ? AND contrasena = ?";
            Map<String, Object> user = jdbcTemplate.queryForMap(sql, email, password);

            if (user != null && !user.isEmpty()) {
                // Inicio de sesión exitoso
                session.setAttribute("userEmail", email); // Guardar correo en la sesión
                session.setAttribute("userId", user.get("ID_usuario"));
                session.setAttribute("userCentroCostoId", user.get("ID_centro_costo")); // Guardar ID_centro_costo en la
                                                                                        // sesión
                return "redirect:/home"; // Redirecciona a la página de inicio
            } else {
                // Inicio de sesión fallido
                model.addAttribute("loginError", "Credenciales inválidas");
                return "index";
            }
        } catch (Exception e) {
            model.addAttribute("loginError", "Error al procesar el inicio de sesión");
            return "index";
        }
    }
}
