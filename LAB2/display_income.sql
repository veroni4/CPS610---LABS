SET SERVEROUTPUT ON;
CREATE OR REPLACE PROCEDURE display_low_income_professors AS
    v_count NUMBER := 0;
BEGIN
    FOR prof IN (SELECT Name, Emp_id, Income 
                 FROM Professor 
                 WHERE Income < 40000 
                 ORDER BY Income) LOOP
        
        v_count := v_count + 1;
        DBMS_OUTPUT.PUT_LINE('Name: ' || prof.Name);
        DBMS_OUTPUT.PUT_LINE('Employee ID: ' || prof.Emp_id);
        DBMS_OUTPUT.PUT_LINE('Income: $' || TO_CHAR(prof.Income, '999,999.99'));
        DBMS_OUTPUT.PUT_LINE('------------------------------------------');
    END LOOP;
    
    IF v_count = 0 THEN
        DBMS_OUTPUT.PUT_LINE('No professors found with income less than $40,000');
    ELSE
        DBMS_OUTPUT.PUT_LINE('Total professors found: ' || v_count);
    END IF;
END;
/
EXECUTE display_low_income_professors;

