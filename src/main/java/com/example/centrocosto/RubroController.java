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
public class RubroController {

    private static final Logger logger = LoggerFactory.getLogger(RubroController.class);
    private final JdbcTemplate jdbcTemplate;

    public RubroController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/presupuestos")
    public String getRubros(Model model) {
        CallableStatement cstmt = null;
        try {
            String sqlCall = "{call GetRubros(?)}";
            cstmt = jdbcTemplate.getDataSource().getConnection().prepareCall(sqlCall);
    

            cstmt.registerOutParameter(1, OracleTypes.CURSOR); 
    

            cstmt.execute();

            ResultSet rs = (ResultSet) cstmt.getObject(1);
            List<Map<String, Object>> rubros = new ArrayList<>();
    
            while (rs.next()) {
                Map<String, Object> rubro = new HashMap<>();
                rubro.put("ID_rubro", rs.getInt("ID_rubro"));
                rubro.put("nombre_rubro", rs.getString("nombre_rubro"));
                rubros.add(rubro);
            }
    
            logger.info("Consulta ejecutada, rubros encontrados: " + rubros.size());
            model.addAttribute("rubros", rubros);
            return "presupuesto";
        } catch (Exception e) {
            logger.error("Error al obtener rubros", e);
            model.addAttribute("mensaje", "Error al obtener rubros");
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
    }
    
}