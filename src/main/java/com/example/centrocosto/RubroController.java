package com.example.centrocosto;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RubroController {

    private static final Logger logger = LoggerFactory.getLogger(RubroController.class);
    private final JdbcTemplate jdbcTemplate;

    public RubroController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/presupuesstos")
    public String getRubros(Model model) {
        try {
            String sql = "SELECT ID_rubro, nombre_rubro FROM rubro";
            logger.info("Ejecutando consulta SQL para obtener rubros");
            List<Map<String, Object>> rubros = jdbcTemplate.queryForList(sql);
            logger.info("Consulta ejecutada, rubros encontrados: " + rubros.size());

            model.addAttribute("rubros", rubros);
            return "presupuesto"; // Nombre de la plantilla Thymeleaf
        } catch (Exception e) {
            logger.error("Error al obtener rubros", e);
            model.addAttribute("mensaje", "Error al obtener rubros");
            return "presupuesto";
        }
    }
}
