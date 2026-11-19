
USE MediConnectDB;
GO

BEGIN TRY
    BEGIN TRANSACTION;

    -- 1. Remove payment records
    DELETE FROM Payments;

    -- 2. Remove bill items
    DELETE FROM BillItems;

    -- 3. Remove bills
    DELETE FROM Bills;

    -- 4. Remove diagnoses and treatments
    DELETE FROM Diagnosis;
    DELETE FROM Treatments;

    -- 5. Remove visits
    DELETE FROM Visits;

    -- 6. Remove appointments
    DELETE FROM Appointments;

    -- 7. Permanently remove all patients,
    -- including previously archived patients
    DELETE FROM Patients;

    COMMIT TRANSACTION;

    PRINT 'Test data successfully cleared!';

END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;

    PRINT 'An error occurred. Changes have been rolled back.';
    PRINT ERROR_MESSAGE();
END CATCH;
GO

-- Verify remaining records
SELECT 'Patients' AS TableName, COUNT(*) AS Total FROM Patients
UNION ALL
SELECT 'Appointments', COUNT(*) FROM Appointments
UNION ALL
SELECT 'Visits', COUNT(*) FROM Visits
UNION ALL
SELECT 'Diagnosis', COUNT(*) FROM Diagnosis
UNION ALL
SELECT 'Treatments', COUNT(*) FROM Treatments
UNION ALL
SELECT 'Bills', COUNT(*) FROM Bills
UNION ALL
SELECT 'BillItems', COUNT(*) FROM BillItems
UNION ALL
SELECT 'Payments', COUNT(*) FROM Payments;
