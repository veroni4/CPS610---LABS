SET SERVEROUTPUT ON;
DECLARE
    v_tax NUMBER(10,2);
BEGIN
    FOR prof IN (SELECT Name, Emp_id, Income FROM Professor ORDER BY Emp_id) LOOP
        v_tax := prof.Income * 0.30;
        
        DBMS_OUTPUT.PUT_LINE('Professor: ' || prof.Name);
        DBMS_OUTPUT.PUT_LINE('Employee ID: ' || prof.Emp_id);
        DBMS_OUTPUT.PUT_LINE('Income: $' || TO_CHAR(prof.Income, '999,999.99'));
        DBMS_OUTPUT.PUT_LINE('Tax (30%): $' || TO_CHAR(v_tax, '999,999.99'));
        DBMS_OUTPUT.PUT_LINE('-----------------------------------');
    END LOOP;
END;
/
