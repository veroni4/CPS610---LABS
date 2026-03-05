SET SERVEROUTPUT ON;
CREATE OR REPLACE PROCEDURE display_average_income AS
    v_avg_income NUMBER(10,2);
    v_total_profs NUMBER;
BEGIN
    -- Calculate average income and count of professors
    SELECT AVG(Income), COUNT(*) 
    INTO v_avg_income, v_total_profs
    FROM Professor;
    
    -- Display the results
    DBMS_OUTPUT.PUT_LINE('Total Professors: ' || v_total_profs);
    DBMS_OUTPUT.PUT_LINE('Average Income: $' || TO_CHAR(v_avg_income, '999,999.99'));
END;
/
EXECUTE display_average_income;
