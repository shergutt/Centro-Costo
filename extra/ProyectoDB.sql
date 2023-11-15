-- Creación de la tabla Centro de costo
CREATE TABLE centro_costo (
    ID_centro_costo NUMBER PRIMARY KEY,
    nombre_centro_costo VARCHAR2(100)
);

-- Creación de la tabla Rubro
CREATE TABLE rubro (
    ID_rubro NUMBER PRIMARY KEY,
    nombre_rubro VARCHAR2(100)
);

-- Creación de la tabla Presupuesto
CREATE TABLE presupuesto (
    ID_presupuesto NUMBER PRIMARY KEY,
    ID_centro_costo NUMBER,
    ID_rubro NUMBER,
    mes NUMBER,
    anio NUMBER,  -- Cambiado 'año' por 'anio'
    monto_presupuestado NUMBER,
    monto_real NUMBER,
    monto_gastado NUMBER,
    FOREIGN KEY (ID_centro_costo) REFERENCES centro_costo (ID_centro_costo),
    FOREIGN KEY (ID_rubro) REFERENCES rubro (ID_rubro)
);

-- Creación de la tabla Permiso de compra
CREATE TABLE permiso_compra (
    ID_permiso NUMBER PRIMARY KEY,
    ID_centro_costo NUMBER,
    ID_rubro NUMBER,
    cantidad NUMBER,
    monto NUMBER,
    estado VARCHAR2(50), 
    fecha_permiso DATE,
    FOREIGN KEY (ID_centro_costo) REFERENCES centro_costo (ID_centro_costo),
    FOREIGN KEY (ID_rubro) REFERENCES rubro (ID_rubro)
);

-- Creación de la tabla Compra
CREATE TABLE compra (
    ID_compra NUMBER PRIMARY KEY,
    ID_permiso NUMBER,
    numero_comprobante VARCHAR2(100),
    fecha_compra DATE,
    FOREIGN KEY (ID_permiso) REFERENCES permiso_compra (ID_permiso)
);

CREATE TABLE usuarios (
    ID_usuario NUMBER PRIMARY KEY,
    nombre VARCHAR2(100),
    correo_electronico VARCHAR2(100),
    ID_centro_costo NUMBER,
    contrasena VARCHAR2(255),
    FOREIGN KEY (ID_centro_costo) REFERENCES centro_costo (ID_centro_costo)
);

-- secuencias y triggers.

-- Crear Secuencia
CREATE SEQUENCE centro_costo_seq START WITH 1 INCREMENT BY 1;

-- Crear Trigger
CREATE OR REPLACE TRIGGER centro_costo_before_insert
BEFORE INSERT ON centro_costo
FOR EACH ROW
BEGIN
    SELECT centro_costo_seq.NEXTVAL
    INTO   :new.ID_centro_costo
    FROM   dual;
END;

-- Crear Secuencia
CREATE SEQUENCE rubro_seq START WITH 1 INCREMENT BY 1;

-- Crear Trigger
CREATE OR REPLACE TRIGGER rubro_before_insert
BEFORE INSERT ON rubro
FOR EACH ROW
BEGIN
    SELECT rubro_seq.NEXTVAL
    INTO   :new.ID_rubro
    FROM   dual;
END;

-- Crear Secuencia
CREATE SEQUENCE presupuesto_seq START WITH 1 INCREMENT BY 1;

-- Crear Trigger
CREATE OR REPLACE TRIGGER presupuesto_before_insert
BEFORE INSERT ON presupuesto
FOR EACH ROW
BEGIN
    SELECT presupuesto_seq.NEXTVAL
    INTO   :new.ID_presupuesto
    FROM   dual;
END;

-- Crear Secuencia
CREATE SEQUENCE permiso_compra_seq START WITH 1 INCREMENT BY 1;

-- Crear Trigger
CREATE OR REPLACE TRIGGER permiso_compra_before_insert
BEFORE INSERT ON permiso_compra
FOR EACH ROW
BEGIN
    SELECT permiso_compra_seq.NEXTVAL
    INTO   :new.ID_permiso
    FROM   dual;
END;

-- Crear Secuencia
CREATE SEQUENCE compra_seq START WITH 1 INCREMENT BY 1;

-- Crear Trigger
CREATE OR REPLACE TRIGGER compra_before_insert
BEFORE INSERT ON compra
FOR EACH ROW
BEGIN
    SELECT compra_seq.NEXTVAL
    INTO   :new.ID_compra
    FROM   dual;
END;

-- Crear Secuencia
CREATE SEQUENCE usuarios_seq START WITH 1 INCREMENT BY 1;

-- Crear Trigger
CREATE OR REPLACE TRIGGER usuarios_before_insert
BEFORE INSERT ON usuarios
FOR EACH ROW
BEGIN
    SELECT usuarios_seq.NEXTVAL
    INTO   :new.ID_usuario
    FROM   dual;
END;

