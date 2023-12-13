
package com.example.centrocosto;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
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

import jakarta.servlet.http.HttpSession;
import oracle.jdbc.OracleTypes;

@Controller
public class PresupuestoController {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);
    private final JdbcTemplate jdbcTemplate;

    public PresupuestoController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/presupuesto")
    public String getPresupuestos(HttpSession session, Model model) {

        Integer idCentroCosto = (Integer) session.getAttribute("userCentroCostoId");
        logger.info("idCentroCosto from session: " + idCentroCosto);
        if (idCentroCosto == null) {

            return "redirect:/login";
        }
        CallableStatement cstmtMontoTotal = null;
        try {
            String sqlCallMontoTotal = "{call GetMontoTotal(?, ?)}";
            cstmtMontoTotal = jdbcTemplate.getDataSource().getConnection().prepareCall(sqlCallMontoTotal);
            cstmtMontoTotal.setInt(1, idCentroCosto);
            cstmtMontoTotal.registerOutParameter(2, Types.NUMERIC);
            cstmtMontoTotal.execute();

            BigDecimal montoTotal = cstmtMontoTotal.getBigDecimal(2);
            logger.info("Monto total retrieved: " + montoTotal);
            model.addAttribute("montoTotal", montoTotal);
        } catch (SQLException i) {
            logger.error("Error retrieving monto total", i);
        } finally {
            if (cstmtMontoTotal != null) {
                try {
                    cstmtMontoTotal.close();
                } catch (SQLException i) {
                    logger.error("Error closing CallableStatement for monto total", i);
                }
            }
        }
        CallableStatement cstmtPresupuestos = null;
        CallableStatement cstmtRubros = null;
        ResultSet rsPresupuestos = null;
        ResultSet rsRubros = null;
        try {

            String sqlCallPresupuestos = "{call GetPresupuestos(?, ?)}";
            cstmtPresupuestos = jdbcTemplate.getDataSource().getConnection().prepareCall(sqlCallPresupuestos);
            cstmtPresupuestos.registerOutParameter(1, OracleTypes.CURSOR); 
            cstmtPresupuestos.registerOutParameter(2, Types.NUMERIC); 
            cstmtPresupuestos.execute();

            rsPresupuestos = (ResultSet) cstmtPresupuestos.getObject(1);
            List<Map<String, Object>> presupuestos = new ArrayList<>();
            while (rsPresupuestos.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("ID_presupuesto", rsPresupuestos.getInt("ID_presupuesto"));
                row.put("nombre_centro_costo", rsPresupuestos.getString("nombre_centro_costo"));
                row.put("nombre_rubro", rsPresupuestos.getString("nombre_rubro"));
                row.put("mes", rsPresupuestos.getInt("mes"));
                row.put("anio", rsPresupuestos.getInt("anio"));
                row.put("monto_presupuestado", rsPresupuestos.getBigDecimal("monto_presupuestado"));
                row.put("monto_real", rsPresupuestos.getBigDecimal("monto_real"));
                row.put("monto_gastado", rsPresupuestos.getBigDecimal("monto_gastado"));

                presupuestos.add(row);
            }
            String sqlCallRubros = "{call GetRubros(?)}";
            cstmtRubros = jdbcTemplate.getDataSource().getConnection().prepareCall(sqlCallRubros);
            cstmtRubros.registerOutParameter(1, OracleTypes.CURSOR);
            cstmtRubros.execute();
            rsRubros = (ResultSet) cstmtRubros.getObject(1);
            List<Map<String, Object>> rubros = new ArrayList<>();
            while (rsRubros.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("ID_rubro", rsRubros.getInt("ID_rubro"));
                row.put("nombre_rubro", rsRubros.getString("nombre_rubro"));
                rubros.add(row);
            }

            model.addAttribute("presupuestos", presupuestos);
            model.addAttribute("rubros", rubros);

            return "presupuesto";
        } catch (Exception e) {
            logger.error("Error al obtener datos", e);
            model.addAttribute("mensaje", "Error al obtener datos");

            return "presupuesto";
        } finally {
            try {
                if (rsPresupuestos != null)
                    rsPresupuestos.close();
                if (cstmtPresupuestos != null)
                    cstmtPresupuestos.close();
            } catch (SQLException e) {
                logger.error("Error closing resources for presupuestos", e);
            }
            try {
                if (rsRubros != null)
                    rsRubros.close();
                if (cstmtRubros != null)
                    cstmtRubros.close();
            } catch (SQLException e) {
                logger.error("Error closing resources for rubros", e);
            }
        }
    }

    @PostMapping("/formulario_anadir")
    public String agregarPresupuesto(
            @RequestParam("idCentroCosto") Integer idCentroCosto,
            @RequestParam("idRubro") Integer idRubro,
            @RequestParam("mes") Integer mes,
            @RequestParam("anio") Integer anio,
            @RequestParam("montoPresupuestado") BigDecimal montoPresupuestado,
            Model model) {

        CallableStatement cstmt = null;
        try {
            String sqlCall = "{call AddPresupuesto(?, ?, ?, ?, ?)}";
            cstmt = jdbcTemplate.getDataSource().getConnection().prepareCall(sqlCall);

            cstmt.setInt(1, idCentroCosto);
            cstmt.setInt(2, idRubro);
            cstmt.setInt(3, mes);
            cstmt.setInt(4, anio);
            cstmt.setBigDecimal(5, montoPresupuestado);

            cstmt.executeUpdate();

            return "redirect:/presupuesto";
        } catch (Exception e) {
            model.addAttribute("error", "Error al agregar el presupuesto: " + e.getMessage());
            return "formulario-anadir-presupuesto";
        } finally {
            if (cstmt != null) {
                try {
                    cstmt.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    @PostMapping("/anadir-rubro")
    public String agregarOEditarRubro(@RequestParam("nombreRubro") String nombreRubro,
            @RequestParam(value = "idRubro", required = false) Integer idRubro,
            Model model) {
        CallableStatement cstmt = null;
        try {
            String sqlCall = "{call AddOrUpdateRubro(?, ?, ?)}";
            cstmt = jdbcTemplate.getDataSource().getConnection().prepareCall(sqlCall);

            cstmt.setInt(1, idRubro != null ? idRubro : 0); 
            cstmt.setString(2, nombreRubro);
            cstmt.setInt(3, idRubro != null ? 1 : 0); 

            cstmt.executeUpdate();
            return "redirect:/presupuesto";
        } catch (Exception e) {
            model.addAttribute("error", "Error al agregar o editar el rubro: " + e.getMessage());
            return "presupuesto";
        } finally {
            if (cstmt != null) {
                try {
                    cstmt.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    @Transactional
    @PostMapping("/eliminar-rubro")
    public String eliminarRubro(@RequestParam("idRubro") Integer idRubro, Model model) {
        CallableStatement cstmt = null;
        try {
            String sqlCall = "{call DeleteRubro(?)}";
            cstmt = jdbcTemplate.getDataSource().getConnection().prepareCall(sqlCall);
            cstmt.setInt(1, idRubro);
            cstmt.executeUpdate();
            logger.info("Rubro and associated presupuestos deleted successfully");
        } catch (Exception e) {
            logger.error("Error deleting rubro and associated presupuestos", e);
            model.addAttribute("error", "Error deleting rubro: " + e.getMessage());
            return "presupuesto";
        } finally {
            if (cstmt != null) {
                try {
                    cstmt.close();
                } catch (SQLException e) {
                    logger.error("Error closing CallableStatement", e);
                }
            }
        }
        return "redirect:/presupuesto";
    }

    @GetMapping("/editar-rubro")
    public String mostrarFormularioEdicion(@RequestParam("idRubro") Integer idRubro, Model model) {
        CallableStatement cstmt = null;
        try {
            String sqlCall = "{call GetRubroById(?, ?)}";
            cstmt = jdbcTemplate.getDataSource().getConnection().prepareCall(sqlCall);
            cstmt.setInt(1, idRubro);
            cstmt.registerOutParameter(2, OracleTypes.CURSOR);
            cstmt.execute();

            ResultSet rs = (ResultSet) cstmt.getObject(2);
            Map<String, Object> rubro = new HashMap<>();
            if (rs.next()) {
                rubro.put("ID_rubro", rs.getInt("ID_rubro"));
                rubro.put("nombre_rubro", rs.getString("nombre_rubro"));
            }
            model.addAttribute("rubroEditar", rubro);
        } catch (Exception e) {
            logger.error("Error retrieving rubro details", e);
            model.addAttribute("error", "Error retrieving rubro details: " + e.getMessage());
        } finally {
            if (cstmt != null) {
                try {
                    cstmt.close();
                } catch (SQLException e) {
                    logger.error("Error closing CallableStatement", e);
                }
            }
        }
        return "presupuesto";
    }

    @GetMapping("/formulario-anadir-presupuesto")
    public String mostrarFormularioAnadirPresupuesto(Model model) {
        CallableStatement cstmt = null;
        try {
            String sqlCall = "{call GetRubrosCentrosCosto(?, ?)}";
            cstmt = jdbcTemplate.getDataSource().getConnection().prepareCall(sqlCall);
            cstmt.registerOutParameter(1, OracleTypes.CURSOR);
            cstmt.registerOutParameter(2, OracleTypes.CURSOR);
            cstmt.execute();

            ResultSet rsRubros = (ResultSet) cstmt.getObject(1);
            List<Map<String, Object>> rubros = new ArrayList<>();
            while (rsRubros.next()) {
                Map<String, Object> rubro = new HashMap<>();
                rubro.put("ID_rubro", rsRubros.getInt("ID_rubro"));
                rubro.put("nombre_rubro", rsRubros.getString("nombre_rubro"));
                rubros.add(rubro);
            }

            ResultSet rsCentrosCosto = (ResultSet) cstmt.getObject(2);
            List<Map<String, Object>> centrosCosto = new ArrayList<>();
            while (rsCentrosCosto.next()) {
                Map<String, Object> centroCosto = new HashMap<>();
                centroCosto.put("ID_centro_costo", rsCentrosCosto.getInt("ID_centro_costo"));
                centroCosto.put("nombre_centro_costo", rsCentrosCosto.getString("nombre_centro_costo"));
                centrosCosto.add(centroCosto);
            }

            model.addAttribute("rubros", rubros);
            model.addAttribute("centrosCosto", centrosCosto);
        } catch (Exception e) {
            logger.error("Error fetching rubros and centros de costo", e);
            model.addAttribute("error", "Error fetching data: " + e.getMessage());
        } finally {
            if (cstmt != null) {
                try {
                    cstmt.close();
                } catch (SQLException e) {
                    logger.error("Error closing CallableStatement", e);
                }
            }
        }
        return "formulario-anadir-presupuesto";
    }

    @PostMapping("/eliminar-presupuesto")
    public String eliminarPresupuesto(@RequestParam("idPresupuesto") Integer idPresupuesto, Model model) {
        CallableStatement cstmt = null;
        try {
            String sqlCall = "{call DeletePresupuesto(?)}";
            cstmt = jdbcTemplate.getDataSource().getConnection().prepareCall(sqlCall);
            cstmt.setInt(1, idPresupuesto);

            cstmt.executeUpdate();
            return "redirect:/presupuesto"; 
        } catch (Exception e) {
            model.addAttribute("error", "Error al eliminar el presupuesto: " + e.getMessage());
            return "presupuesto"; 
        } finally {
            if (cstmt != null) {
                try {
                    cstmt.close();
                } catch (SQLException e) {
                    
                }
            }
        }
    }
    @GetMapping("/editar-presupuesto")
    public String mostrarFormularioEdicionPresupuesto(@RequestParam("ID_presupuesto") Integer ID_presupuesto, Model model) {
        CallableStatement cstmt = null;
        ResultSet rsPresupuesto = null;
        ResultSet rsRubros = null;
        ResultSet rsCentrosCosto = null;
        try {
            cstmt = jdbcTemplate.getDataSource().getConnection().prepareCall("{call GetPresupuestoById(?, ?)}");
            cstmt.setInt(1, ID_presupuesto);
            cstmt.registerOutParameter(2, OracleTypes.CURSOR);
            cstmt.execute();
            rsPresupuesto = (ResultSet) cstmt.getObject(2); 
    
            Map<String, Object> presupuesto = new HashMap<>();
            if (rsPresupuesto.next()) {
                presupuesto.put("ID_presupuesto", rsPresupuesto.getInt("ID_presupuesto"));
                presupuesto.put("nombre_centro_costo", rsPresupuesto.getString("nombre_centro_costo"));
               
            }
            model.addAttribute("presupuesto", presupuesto);
    

            String sqlCall = "{call GetRubrosCentrosCosto(?, ?)}";
            cstmt = jdbcTemplate.getDataSource().getConnection().prepareCall(sqlCall);
            cstmt.registerOutParameter(1, OracleTypes.CURSOR);
            cstmt.registerOutParameter(2, OracleTypes.CURSOR);
            cstmt.execute();
    
            rsRubros = (ResultSet) cstmt.getObject(1);
            List<Map<String, Object>> rubros = new ArrayList<>();
            while (rsRubros.next()) {
                Map<String, Object> rubro = new HashMap<>();
                rubro.put("ID_rubro", rsRubros.getInt("ID_rubro"));
                rubro.put("nombre_rubro", rsRubros.getString("nombre_rubro"));
                rubros.add(rubro);
            }
    
            rsCentrosCosto = (ResultSet) cstmt.getObject(2);
            List<Map<String, Object>> centrosCosto = new ArrayList<>();
            while (rsCentrosCosto.next()) {
                Map<String, Object> centroCosto = new HashMap<>();
                centroCosto.put("ID_centro_costo", rsCentrosCosto.getInt("ID_centro_costo"));
                centroCosto.put("nombre_centro_costo", rsCentrosCosto.getString("nombre_centro_costo"));
                centrosCosto.add(centroCosto);
            }
    
            model.addAttribute("rubros", rubros);
            model.addAttribute("centrosCosto", centrosCosto);
    
            return "formulario-editar-presupuesto";
        } catch (SQLException e) {
            logger.error("Error during editing presupuesto: ", e);
            return "presupuesto"; 
        } finally {
            try {
                if (rsPresupuesto != null) rsPresupuesto.close();
                if (rsRubros != null) rsRubros.close();
                if (rsCentrosCosto != null) rsCentrosCosto.close();
                if (cstmt != null) cstmt.close();
            } catch (SQLException e) {
                logger.error("Error closing resources: ", e);
            }
        }
    }
@PostMapping("/actualizar-presupuesto")
public String actualizarPresupuesto(@RequestParam("ID_presupuesto") Integer ID_presupuesto,
                                    @RequestParam("idCentroCosto") Integer idCentroCosto,
                                    @RequestParam("idRubro") Integer idRubro,
                                    @RequestParam("mes") Integer mes,
                                    @RequestParam("anio") Integer anio,
                                    @RequestParam("montoPresupuestado") BigDecimal montoPresupuestado,
                                    Model model) {
    CallableStatement cstmt = null;
    try {
        cstmt = jdbcTemplate.getDataSource().getConnection()
                .prepareCall("{call UpdatePresupuesto(?, ?, ?, ?, ?, ?)}");
        cstmt.setInt(1, ID_presupuesto);
        cstmt.setInt(2, idCentroCosto);
        cstmt.setInt(3, idRubro);
        cstmt.setInt(4, mes);
        cstmt.setInt(5, anio);
        cstmt.setBigDecimal(6, montoPresupuestado);
        cstmt.executeUpdate();
        return "redirect:/presupuesto";
    } catch (Exception e) {
        model.addAttribute("error", "Error al actualizar el presupuesto: " + e.getMessage());
        return "formulario-editar-presupuesto";
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
