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

import jakarta.servlet.http.HttpSession;
import oracle.jdbc.OracleTypes;

@Controller
public class HomeController {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);

    @GetMapping("/home")
    public String index(HttpSession session, Model model) {

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

            
        } catch (Exception e) {
            logger.error("Error al obtener datos", e);
            model.addAttribute("mensaje", "Error al obtener datos");

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
        return "Home";

    }


    @GetMapping("/index")
    public String Index() {
        return "index";
    }
}