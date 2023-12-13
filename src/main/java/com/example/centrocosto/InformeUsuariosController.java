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

import oracle.jdbc.OracleTypes;

@Controller
public class InformeUsuariosController {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);
    private final JdbcTemplate jdbcTemplate;

    public InformeUsuariosController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/reportes")
    public String getReportes(Model model) {
        retrieveInformeUsuarios(model);
        retrieveFacturas(model);
        return "reportes";
    }

    private void retrieveInformeUsuarios(Model model) {
        CallableStatement cstmtInforme = null;
        try {
            String sqlCallInforme = "{call GetInformeUsuarios(?)}";
            cstmtInforme = jdbcTemplate.getDataSource().getConnection().prepareCall(sqlCallInforme);
            cstmtInforme.registerOutParameter(1, OracleTypes.CURSOR);
            cstmtInforme.execute();
            ResultSet rsInforme = (ResultSet) cstmtInforme.getObject(1);
            List<Map<String, Object>> informes = new ArrayList<>();
            while (rsInforme.next()) {
                Map<String, Object> informe = new HashMap<>();
                informe.put("informe_id", rsInforme.getInt("informe_id"));
                informe.put("accion", rsInforme.getString("accion"));
                informe.put("usuario_id", rsInforme.getInt("usuario_id"));
                informe.put("timestamp", rsInforme.getDate("timestamp"));
                informe.put("detalles", rsInforme.getString("detalles"));
                informe.put("nombre", rsInforme.getString("nombre"));
                informes.add(informe);
            }
            model.addAttribute("informeUsuarios", informes);
        } catch (Exception e) {
            logger.error("Error al obtener informes de usuarios", e);
            model.addAttribute("mensajeInforme", "Error al obtener informes de usuarios");
        } finally {
            closeCallableStatement(cstmtInforme);
        }
    }

    private void retrieveFacturas(Model model) {
        CallableStatement cstmtFacturas = null;
        try {
            String sqlCallFacturas = "{call GetFacturas(?)}";
            cstmtFacturas = jdbcTemplate.getDataSource().getConnection().prepareCall(sqlCallFacturas);
            cstmtFacturas.registerOutParameter(1, OracleTypes.CURSOR);
            cstmtFacturas.execute();
            ResultSet rsFacturas = (ResultSet) cstmtFacturas.getObject(1);
            List<Map<String, Object>> facturas = new ArrayList<>();
            while (rsFacturas.next()) {
                Map<String, Object> factura = new HashMap<>();
                factura.put("factura_id", rsFacturas.getInt("factura_id"));
                factura.put("nombre", rsFacturas.getString("nombre"));
                factura.put("ID_permiso", rsFacturas.getInt("ID_permiso"));
                factura.put("cantidad", rsFacturas.getInt("cantidad"));
                factura.put("monto", rsFacturas.getDouble("monto"));
                factura.put("numero_comprobante", rsFacturas.getString("numero_comprobante"));
                factura.put("fecha_compra", rsFacturas.getDate("fecha_compra"));
                facturas.add(factura);
            }
            model.addAttribute("facturas", facturas);
        } catch (Exception e) {
            logger.error("Error al obtener facturas", e);
            model.addAttribute("mensajeFacturas", "Error al obtener facturas");
        } finally {
            closeCallableStatement(cstmtFacturas);
        }
    }

    private void closeCallableStatement(CallableStatement cstmt) {
        if (cstmt != null) {
            try {
                cstmt.close();
            } catch (SQLException e) {
                logger.error("Error closing CallableStatement", e);
            }
        }
    }
}
