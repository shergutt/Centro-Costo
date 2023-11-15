CREATE OR REPLACE FUNCTION validate_user_login(email IN VARCHAR2, password IN VARCHAR2)
RETURN NUMBER
IS
    user_id NUMBER;
BEGIN
    SELECT ID_usuario INTO user_id
    FROM usuarios
    WHERE correo_electronico = email AND contrasena = password;

    IF SQL%ROWCOUNT = 0 THEN
        RETURN 0; -- Return 0 if no user is found
    ELSE
        RETURN 1; -- Return 1 if a user is found
    END IF;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN 0; -- Return 0 if no user is found
    WHEN OTHERS THEN
        RETURN -1; -- Return -1 if there's another error
END;
