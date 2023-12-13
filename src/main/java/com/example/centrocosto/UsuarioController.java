package com.example.centrocosto;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
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
import oracle.jdbc.OracleTypes;

@Controller
public class UsuarioController {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);
    private final JdbcTemplate jdbcTemplate;

    public UsuarioController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/usuarios")
    public String getUsuarios(Model model) {
        CallableStatement cstmt = null;
        try {
            String sqlCall = "{call GetUsuarios(?)}";
            cstmt = jdbcTemplate.getDataSource().getConnection().prepareCall(sqlCall);
    

            cstmt.registerOutParameter(1, OracleTypes.CURSOR);
    

            cstmt.execute();

            ResultSet rs = (ResultSet) cstmt.getObject(1);
            List<Map<String, Object>> usuarios = new ArrayList<>();
    
            while (rs.next()) {
                Map<String, Object> usuario = new HashMap<>();
                usuario.put("ID_usuario", rs.getInt("ID_usuario"));
                usuario.put("nombre", rs.getString("nombre"));
                usuario.put("correo_electronico", rs.getString("correo_electronico"));
                usuario.put("ID_centro_costo", rs.getInt("ID_centro_costo"));
                usuario.put("contrasena", rs.getString("contrasena"));
                usuarios.add(usuario);
            }
    
            logger.info("Consulta ejecutada, usuarios encontrados: " + usuarios.size());
            model.addAttribute("usuarios", usuarios);
            return "usuarios";
        } catch (Exception e) {
            logger.error("Error al obtener usuarios", e);
            model.addAttribute("mensaje", "Error al obtener usuarios");
            return "usuarios";
        } finally {
            if (cstmt != null) {
                try {
                    cstmt.close();
                } catch (SQLException e) {
                    logger.error("Error closing CallableStatement", e);
                }
            }
        }
    }

    @GetMapping("/editar-usuario")
    public String mostrarFormularioEditarUsuario(@RequestParam("ID_usuario") Integer ID_usuario, Model model) {
        CallableStatement cstmt = null;
        ResultSet rs = null;
        try {
            Logger logger = LoggerFactory.getLogger(this.getClass());
            logger.info("Iniciando mostrarFormularioEditarUsuario para ID_usuario: " + ID_usuario);
    
            cstmt = jdbcTemplate.getDataSource().getConnection().prepareCall("{call GetUserById(?, ?)}");
            cstmt.setInt(1, ID_usuario);
            cstmt.registerOutParameter(2, OracleTypes.CURSOR); // Registrar el segundo parámetro como un cursor
            logger.info("Llamada al procedimiento almacenado GetUserById ejecutada");
    
            cstmt.execute();
    
            rs = (ResultSet) cstmt.getObject(2); // Obtener el cursor del segundo parámetro
            if (rs.next()) {
                Map<String, Object> usuario = new HashMap<>();
                usuario.put("ID_usuario", rs.getInt("ID_usuario"));
                usuario.put("nombre", rs.getString("nombre"));
                usuario.put("correo_electronico", rs.getString("correo_electronico"));
                usuario.put("contrasena", rs.getString("contrasena"));
    
                logger.info("Usuario encontrado: " + usuario);
                model.addAttribute("usuario", usuario);
            } else {
                logger.warn("No se encontró usuario con ID_usuario: " + ID_usuario);
            }
            return "formulario-editar-usuario";
        } catch (Exception e) {
            logger.error("Error al obtener detalles del usuario: ", e);
            model.addAttribute("error", "Error al obtener detalles del usuario: " + e.getMessage());
            return "/usuarios";
        } finally {
            try {
                if (rs != null) rs.close();
                if (cstmt != null) cstmt.close();
                logger.info("Recursos cerrados correctamente");
            } catch (SQLException e) {
                logger.error("Error cerrando recursos: ", e);
            }
        }
    }
    
    @PostMapping("/editar-usuario")
public String editarUsuario(
    @RequestParam("ID_usuario") Integer ID_usuario,
    @RequestParam("nombre") String nombre,
    @RequestParam("correo_electronico") String correoElectronico,
    @RequestParam("contrasena") String contrasena,
    Model model) {

    CallableStatement cstmt = null;
    try {
        cstmt = jdbcTemplate.getDataSource().getConnection()
                .prepareCall("{call EditUsuario(?, ?, ?, ?)}");
        cstmt.setInt(1, ID_usuario);
        cstmt.setString(2, nombre);
        cstmt.setString(3, correoElectronico);
        cstmt.setString(4, contrasena);
        
        cstmt.executeUpdate();
        return "redirect:/usuarios";
    } catch (Exception e) {
        model.addAttribute("error", "Error al editar el usuario: " + e.getMessage());
        return "usuarios";
    } finally {
        if (cstmt != null) {
            try {
                cstmt.close();
            } catch (SQLException e) {
            }
        }
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
        CallableStatement cstmt = null;
        try {
            String sqlCall = "{call AddUsuario(?, ?, ?, ?)}";
            cstmt = jdbcTemplate.getDataSource().getConnection().prepareCall(sqlCall);
    

            cstmt.setString(1, nombre);
            cstmt.setString(2, correo_electronico);
            cstmt.setLong(3, ID_centro_costo);
            cstmt.setString(4, contrasena);
    

            cstmt.executeUpdate();
    
            redirectAttributes.addFlashAttribute("mensaje", "Usuario añadido exitosamente");
            return "redirect:/usuarios";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al añadir usuario");
            return "redirect:/usuarios/anadir";
        } finally {
            if (cstmt != null) {
                try {
                    cstmt.close();
                } catch (SQLException e) {

                }
            }
        }
    }
    
    @PostMapping("/eliminar-usuario")
public String eliminarUsuario(@RequestParam("ID_usuario") Integer ID_usuario, Model model) {
    CallableStatement cstmt = null;
    try {
        String sqlCall = "{call DeleteUsuario(?)}";
        cstmt = jdbcTemplate.getDataSource().getConnection().prepareCall(sqlCall);
        cstmt.setInt(1, ID_usuario);
        cstmt.executeUpdate();
        return "redirect:/usuarios"; 
    } catch (Exception e) {
        model.addAttribute("error", "Error al eliminar usuario: " + e.getMessage());
        return "usuarios"; 
    } finally {
        if (cstmt != null) {
            try {
                cstmt.close();
            } catch (SQLException e) {
            }
        }
    }
}

    
@GetMapping("/logout")
public String logout(HttpSession session) {
    clearTempUserSession(); 
    session.invalidate(); 
    return "redirect:/index"; 
}

private void clearTempUserSession() {
    CallableStatement cstmt = null;
    try {
        cstmt = jdbcTemplate.getDataSource().getConnection().prepareCall("{call ClearTempUserSession()}");
        cstmt.execute();
    } catch (SQLException e) {
        // Handle SQL Exception
    } finally {
        if (cstmt != null) {
            try {
                cstmt.close();
            } catch (SQLException e) {
            }
        }
    }
}

}
