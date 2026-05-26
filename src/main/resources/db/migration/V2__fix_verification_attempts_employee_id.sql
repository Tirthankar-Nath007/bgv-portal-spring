-- Fix BGV_VERIFICATION_ATTEMPTS.employee_id: original schema used NUMBER, must be VARCHAR2.
-- The Spring Boot entity maps employee_id as String (stores codes like "EMP001").
-- This migration converts the column type safely.

-- Step 1: Drop the existing unique constraint (covers verifier_id, employee_id)
ALTER TABLE BGV_VERIFICATION_ATTEMPTS DROP CONSTRAINT uk_va_verifier_employee;

-- Step 2: Add new VARCHAR2 column
ALTER TABLE BGV_VERIFICATION_ATTEMPTS ADD employee_id_new VARCHAR2(100);

-- Step 3: Copy data (converting number to string)
UPDATE BGV_VERIFICATION_ATTEMPTS SET employee_id_new = TO_CHAR(employee_id);

-- Step 4: Drop old NUMBER column
ALTER TABLE BGV_VERIFICATION_ATTEMPTS DROP COLUMN employee_id;

-- Step 5: Rename new column to employee_id
ALTER TABLE BGV_VERIFICATION_ATTEMPTS RENAME COLUMN employee_id_new TO employee_id;

-- Step 6: Apply NOT NULL
ALTER TABLE BGV_VERIFICATION_ATTEMPTS MODIFY (employee_id VARCHAR2(100) NOT NULL);

-- Step 7: Re-create unique constraint
ALTER TABLE BGV_VERIFICATION_ATTEMPTS ADD CONSTRAINT uk_va_verifier_employee UNIQUE (verifier_id, employee_id);
