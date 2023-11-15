package com.example.centrocosto;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
public class UsuarioController {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);
    private final JdbcTemplate jdbcTemplate;

    public UsuarioController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/usuarios")
    public String getUsuarios(Model model) {
        try {
            String sql = "SELECT ID_usuario, nombre, correo_electronico, ID_centro_costo, contrasena FROM Usuarios";
            logger.info("Ejecutando consulta SQL para obtener usuarios");
            List<Map<String, Object>> usuarios = jdbcTemplate.queryForList(sql);
            logger.info("Consulta ejecutada, usuarios encontrados: " + usuarios.size());

            model.addAttribute("usuarios", usuarios);
            return "usuarios"; // Nombre de la plantilla Thymeleaf

        } catch (Exception e) {
            logger.error("Error al obtener usuarios", e);
            model.addAttribute("mensaje", "Error al obtener usuarios");
            return "usuarios";
        }
    }

    @GetMapping("/usuarios/anadir")
    public String mostrarFormularioAnadir(Model model) {

        return "formulario-anadir-usuario";

    }

    @PostMapping("/usuarios/anadir")
    public String anadirUsuario(@RequestParam String nombre,
            @RequestParam String correo_electronico,
            @RequestParam Long ID_centro_costo,
            @RequestParam String contrasena,
            RedirectAttributes redirectAttributes) {
        try {
            String sql = "INSERT INTO Usuarios (nombre, correo_electronico, ID_centro_costo, contrasena) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(sql, nombre, correo_electronico, ID_centro_costo, contrasena);

            redirectAttributes.addFlashAttribute("mensaje", "Usuario añadido exitosamente");
            return "redirect:/usuarios";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al añadir usuario");
            return "redirect:/usuarios/anadir";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); 
        return "redirect:/index"; 
    }

}