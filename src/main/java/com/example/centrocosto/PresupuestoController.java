
package com.example.centrocosto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PresupuestoController {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);
    private final JdbcTemplate jdbcTemplate;

    public PresupuestoController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/presupuesto")
    public String getPresupuestos(Model model) {
        try {
            String sqlPresupuestos = "SELECT p.ID_presupuesto, c.nombre_centro_costo, r.nombre_rubro, p.mes, p.anio, p.monto_presupuestado, p.monto_real, p.monto_gastado "
                    +
                    "FROM presupuesto p " +
                    "JOIN centro_costo c ON p.ID_centro_costo = c.ID_centro_costo " +
                    "JOIN rubro r ON p.ID_rubro = r.ID_rubro";

            List<Map<String, Object>> presupuestos = jdbcTemplate.queryForList(sqlPresupuestos);
            model.addAttribute("presupuestos", presupuestos);

            String sqlTotal = "SELECT SUM(monto_presupuestado) AS total FROM presupuesto";
            Map<String, Object> totalMap = jdbcTemplate.queryForMap(sqlTotal);
            BigDecimal totalPresupuestado = (BigDecimal) totalMap.get("total");

            model.addAttribute("totalPresupuestado", totalPresupuestado);

            // Obtener rubros
            String sqlRubros = "SELECT ID_rubro, nombre_rubro FROM rubro";
            List<Map<String, Object>> rubros = jdbcTemplate.queryForList(sqlRubros);

            model.addAttribute("presupuestos", presupuestos);
            model.addAttribute("rubros", rubros);

            return "presupuesto";
        } catch (Exception e) {
            logger.error("Error al obtener datos", e);
            model.addAttribute("mensaje", "Error al obtener datos");
            return "presupuesto";
        }
    }

    @PostMapping("/formulario_anadir")
    public String agregarPresupuesto(
            @RequestParam("idCentroCosto") Integer idCentroCosto,
            @RequestParam("idRubro") Integer idRubro,
            @RequestParam("mes") Integer mes,
            @RequestParam("anio") Integer anio,
            @RequestParam("montoPresupuestado") BigDecimal montoPresupuestado,
            @RequestParam("montoReal") BigDecimal montoReal,
            @RequestParam("montoGastado") BigDecimal montoGastado,
            Model model) {

        try {
            String sql = "INSERT INTO presupuesto (ID_centro_costo, ID_rubro, mes, anio, monto_presupuestado, monto_real, monto_gastado) VALUES (?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, idCentroCosto, idRubro, mes, anio, montoPresupuestado, montoReal, montoGastado);

            return "redirect:/presupuesto"; // Redirige a otra página después de agregar el presupuesto
        } catch (Exception e) {
            model.addAttribute("error", "Error al agregar el presupuesto: " + e.getMessage());
            return "formulario-anadir-presupuesto"; // Devuelve a la página del formulario en caso de error
        }
    }

    @PostMapping("/anadir-rubro")
    public String agregarOEditarRubro(@RequestParam("nombreRubro") String nombreRubro,
            @RequestParam(value = "idRubro", required = false) Integer idRubro,
            Model model) {
        try {
            if (idRubro != null) {
                // Lógica para actualizar el rubro existente
                String sqlUpdate = "UPDATE rubro SET nombre_rubro = ? WHERE ID_rubro = ?";
                jdbcTemplate.update(sqlUpdate, nombreRubro, idRubro);
            } else {
                // Lógica para añadir un nuevo rubro
                String sqlInsert = "INSERT INTO rubro (nombre_rubro) VALUES (?)";
                jdbcTemplate.update(sqlInsert, nombreRubro);
            }
            return "redirect:/presupuesto"; // Redirige a la página de presupuestos
        } catch (Exception e) {
            model.addAttribute("error", "Error al agregar o editar el rubro: " + e.getMessage());
            return "presupuesto"; // Devuelve a la página del formulario en caso de error
        }
    }

    @Transactional
    @PostMapping("/eliminar-rubro")
    public String eliminarRubro(@RequestParam("idRubro") Integer idRubro, Model model) {
        try {
            // Primero, eliminar todos los presupuestos asociados a este rubro
            String sqlEliminarPresupuestos = "DELETE FROM presupuesto WHERE ID_rubro = ?";
            jdbcTemplate.update(sqlEliminarPresupuestos, idRubro);

            // Luego, eliminar el rubro
            String sqlEliminarRubro = "DELETE FROM rubro WHERE ID_rubro = ?";
            jdbcTemplate.update(sqlEliminarRubro, idRubro);

            logger.info("Rubro y presupuestos asociados eliminados con éxito");
        } catch (Exception e) {
            logger.error("Error al eliminar rubro y presupuestos asociados", e);
            model.addAttribute("error", "Error al eliminar rubro y presupuestos asociados: " + e.getMessage());
            return "presupuesto"; // Puedes redirigir a una página de error o manejarlo de otra manera
        }

        return "redirect:/presupuesto";
    }

    @GetMapping("/editar-rubro")
    public String mostrarFormularioEdicion(@RequestParam("idRubro") Integer idRubro, Model model) {
        Map<String, Object> rubro = jdbcTemplate
                .queryForMap("SELECT ID_rubro, nombre_rubro FROM rubro WHERE ID_rubro = ?", idRubro);
        model.addAttribute("rubroEditar", rubro);
        return "presupuesto";
    }

    @GetMapping("/formulario-anadir-presupuesto")
    public String mostrarFormularioAnadirPresupuesto(Model model) {
        List<Map<String, Object>> rubros = jdbcTemplate.queryForList("SELECT ID_rubro, nombre_rubro FROM rubro");
        model.addAttribute("rubros", rubros);
        return "formulario-anadir-presupuesto";
    }
}