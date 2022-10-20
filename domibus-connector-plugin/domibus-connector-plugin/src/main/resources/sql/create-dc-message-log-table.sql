-- MySQL example:
-- The column ID_PK must be incremented (no order required, just make sure it is a PK) by the DBMS
CREATE TABLE DC_PLUGIN_TB_MESSAGE_LOG (
    ID_PK INTEGER PRIMARY KEY AUTO_INCREMENT,
    MESSAGE_ID VARCHAR(255),
    RECEIVED TIMESTAMP(6),
    DOMAIN_CODE VARCHAR(255)
)