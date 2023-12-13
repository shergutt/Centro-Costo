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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import oracle.jdbc.OracleTypes;

@Controller
public class GastosController {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);

    @GetMapping("/gastos")

    public String mostrarMiFormulario(HttpSession session, Model model) {

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
        CallableStatement cstmtMontoGastado = null;
        try {
            String sqlCallMontoTotal = "{call GetMontoGastado(?, ?)}";
            cstmtMontoGastado = jdbcTemplate.getDataSource().getConnection().prepareCall(sqlCallMontoTotal);
            cstmtMontoGastado.setInt(1, idCentroCosto);
            cstmtMontoGastado.registerOutParameter(2, Types.NUMERIC);
            cstmtMontoGastado.execute();

            BigDecimal montoGastado = cstmtMontoGastado.getBigDecimal(2);
            logger.info("Monto Gastado retrieved: " + montoGastado);
            model.addAttribute("montoGastado", montoGastado);
        } catch (SQLException i) {
            logger.error("Error retrieving monto gastado", i);
        } finally {
            if (cstmtMontoTotal != null) {
                try {
                    cstmtMontoTotal.close();
                } catch (SQLException i) {
                    logger.error("Error closing CallableStatement for monto total", i);
                }
            }
        }

        CallableStatement cstmtMontoEnProceso = null;
        try {
            String sqlCallMontoEnProceso = "{call GetMontoEnProceso(?, ?)}";
            cstmtMontoEnProceso = jdbcTemplate.getDataSource().getConnection().prepareCall(sqlCallMontoEnProceso);
            cstmtMontoEnProceso.setInt(1, idCentroCosto);
            cstmtMontoEnProceso.registerOutParameter(2, Types.NUMERIC);
            cstmtMontoEnProceso.execute();

            BigDecimal montoEnProceso = cstmtMontoEnProceso.getBigDecimal(2);
            logger.info("Monto total retrieved: " + montoEnProceso);
            model.addAttribute("montoEnProceso", montoEnProceso);
        } catch (SQLException i) {
            logger.error("Error retrieving monto total", i);
        } finally {
            if (cstmtMontoEnProceso != null) {
                try {
                    cstmtMontoEnProceso.close();
                } catch (SQLException i) {
                    logger.error("Error closing CallableStatement for monto total", i);
                }
            }
        }

        CallableStatement cstmt = null;
        ResultSet rs = null;
        CallableStatement cstmtComprasDetalles = null;
        ResultSet rsComprasDetalles = null;

        try {
            cstmt = jdbcTemplate.getDataSource().getConnection().prepareCall("{call GetGastos(?)}");
            cstmt.registerOutParameter(1, OracleTypes.CURSOR);
            cstmt.execute();

            rs = (ResultSet) cstmt.getObject(1);
            List<Map<String, Object>> gastos = new ArrayList<>();

            while (rs.next()) {
                Map<String, Object> gasto = new HashMap<>();
                gasto.put("idPermiso", rs.getInt("ID_permiso"));
                gasto.put("rubro", rs.getString("nombre_rubro"));
                gasto.put("cantidad", rs.getBigDecimal("cantidad"));
                gasto.put("precio", rs.getBigDecimal("monto"));
                gastos.add(gasto);
            }

            cstmtComprasDetalles = jdbcTemplate.getDataSource().getConnection()
                    .prepareCall("{call GetComprasDetalles(?)}");
            cstmtComprasDetalles.registerOutParameter(1, OracleTypes.CURSOR);
            cstmtComprasDetalles.execute();

            rsComprasDetalles = (ResultSet) cstmtComprasDetalles.getObject(1);
            List<Map<String, Object>> comprasDetalles = new ArrayList<>();

            while (rsComprasDetalles.next()) {
                Map<String, Object> detalle = new HashMap<>();
                detalle.put("rubro", rsComprasDetalles.getString("nombre_rubro"));
                detalle.put("cantidadGastada", rsComprasDetalles.getBigDecimal("cantidadGastada"));
                detalle.put("numeroComprobante", rsComprasDetalles.getString("numero_comprobante"));
                detalle.put("fechaComprobante", rsComprasDetalles.getDate("fecha_compra"));
                comprasDetalles.add(detalle);
            }

            model.addAttribute("gastos", gastos);
            model.addAttribute("comprasDetalles", comprasDetalles);

            return "gastos";
        } catch (SQLException e) {
            model.addAttribute("error", "Error al obtener los gastos o detalles de compras: " + e.getMessage());
            return "gastos";
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (cstmt != null)
                    cstmt.close();
                if (rsComprasDetalles != null)
                    rsComprasDetalles.close();
                if (cstmtComprasDetalles != null)
                    cstmtComprasDetalles.close();
            } catch (SQLException e) {
            }
        }
    }

    @GetMapping("/formulario-anadir-gasto")
    public String Index(Model model) {
        CallableStatement cstmt = null;
        ResultSet rs = null;
        try {
            String sqlCall = "{call GetRubros(?)}";
            cstmt = jdbcTemplate.getDataSource().getConnection().prepareCall(sqlCall);
            cstmt.registerOutParameter(1, OracleTypes.CURSOR);
            cstmt.execute();

            rs = (ResultSet) cstmt.getObject(1);
            List<Map<String, Object>> rubros = new ArrayList<>();

            while (rs.next()) {
                Map<String, Object> rubro = new HashMap<>();
                rubro.put("ID_rubro", rs.getInt("ID_rubro"));
                rubro.put("nombre_rubro", rs.getString("nombre_rubro"));
                rubros.add(rubro);
            }

            model.addAttribute("rubros", rubros);
        } catch (Exception e) {
        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {

                }
            if (cstmt != null)
                try {
                    cstmt.close();
                } catch (SQLException e) {
                }
        }
        return "formulario-anadir-gasto";
    }

    @PostMapping("/crear-permiso-compra")
    public String crearPermisoCompra(@RequestParam("idRubro") Integer idRubro,
            @RequestParam("cantidad") BigDecimal cantidad,
            @RequestParam("monto") BigDecimal monto,
            HttpSession session, Model model) {
        Integer idCentroCosto = (Integer) session.getAttribute("userCentroCostoId");
        if (idCentroCosto == null) {
            return "redirect:/login";
        }

        CallableStatement cstmt = null;
        try {
            cstmt = jdbcTemplate.getDataSource().getConnection()
                    .prepareCall("{call CreatePermisoCompra(?, ?, ?, ?, ?)}");
            cstmt.setInt(1, idCentroCosto);
            cstmt.setInt(2, idRubro);
            cstmt.setBigDecimal(3, cantidad);
            cstmt.setBigDecimal(4, monto);
            cstmt.registerOutParameter(5, Types.VARCHAR);

            cstmt.execute();

            String estado = cstmt.getString(5);
            if ("No Concedido".equals(estado)) {
                model.addAttribute("message", "Permiso de compra no concedido debido a fondos insuficientes.");
                return "formulario-anadir-gasto";
            }

            return "redirect:/gastos";
        } catch (Exception e) {
            model.addAttribute("error", "Error al crear permiso de compra: " + e.getMessage());
            return "formulario-anadir-gasto";
        } finally {
            if (cstmt != null) {
                try {
                    cstmt.close();
                } catch (SQLException e) {

                }
            }
        }
    }

    @PostMapping("/comprar")
    public String comprar(@RequestParam("idPermiso") Integer idPermiso, Model model) {
        CallableStatement cstmt = null;
        try {
            cstmt = jdbcTemplate.getDataSource().getConnection().prepareCall("{call CreateCompra(?)}");
            cstmt.setInt(1, idPermiso);
    
            cstmt.executeUpdate();
            return "redirect:/gastos";
        } catch (SQLException e) {
            if (e.getErrorCode() == -20001) { // Check if it's the specific error
                model.addAttribute("error", "Monto insuficiente para la transacción.");
            } else {
                model.addAttribute("error", "Error al realizar la compra: " + e.getMessage());
            }
            return "gastos";
        } finally {
            if (cstmt != null) {
                try {
                    cstmt.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    @PostMapping("/eliminar-permiso-compra")
    public String eliminarPermisoCompra(@RequestParam("idPermiso") Integer idPermiso, Model model) {
        CallableStatement cstmt = null;
        try {
            String sqlCall = "{call DeletePermisoCompra(?)}";
            cstmt = jdbcTemplate.getDataSource().getConnection().prepareCall(sqlCall);
            cstmt.setInt(1, idPermiso);

            cstmt.executeUpdate();
            return "redirect:/gastos";
        } catch (Exception e) {
            model.addAttribute("error", "Error al eliminar permiso de compra: " + e.getMessage());
            return "gastos";
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