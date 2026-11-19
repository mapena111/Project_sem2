
USE MediConnectDB;
GO

-- 1. STAFF TABLE
CREATE TABLE Staff (
    StaffID INT IDENTITY(1,1) PRIMARY KEY,
    FullName VARCHAR(100) NOT NULL,
    Username VARCHAR(50) UNIQUE NOT NULL,
    PasswordHash VARCHAR(255) NOT NULL,
    Role VARCHAR(30) NOT NULL,
    Status VARCHAR(20) DEFAULT 'Active'
);
GO

-- 2. DOCTORS TABLE
CREATE TABLE Doctors (
    DoctorID INT IDENTITY(1,1) PRIMARY KEY,
    FullName VARCHAR(100) NOT NULL,
    Specialization VARCHAR(100),
    Phone VARCHAR(20),
    Email VARCHAR(100),
    ConsultationFee DECIMAL(10,2) DEFAULT 0
);
GO

-- 3. PATIENTS TABLE
CREATE TABLE Patients (
    PatientID INT IDENTITY(1,1) PRIMARY KEY,
    FullName VARCHAR(100) NOT NULL,
    Gender VARCHAR(10),
    DateOfBirth DATE,
    Phone VARCHAR(20),
    Email VARCHAR(100),
    Address VARCHAR(200),
    RegistrationDate DATE DEFAULT GETDATE()
);
GO

-- 4. APPOINTMENTS TABLE
CREATE TABLE Appointments (
    AppointmentID INT IDENTITY(1,1) PRIMARY KEY,
    PatientID INT NOT NULL,
    DoctorID INT NOT NULL,
    AppointmentDate DATE NOT NULL,
    AppointmentTime TIME NOT NULL,
    Status VARCHAR(20) DEFAULT 'Scheduled',

    FOREIGN KEY (PatientID) REFERENCES Patients(PatientID),
    FOREIGN KEY (DoctorID) REFERENCES Doctors(DoctorID)
);
GO

-- 5. VISITS TABLE
CREATE TABLE Visits (
    VisitID INT IDENTITY(1,1) PRIMARY KEY,
    PatientID INT NOT NULL,
    DoctorID INT NOT NULL,
    AppointmentID INT NULL,
    VisitDate DATETIME DEFAULT GETDATE(),
    Notes VARCHAR(MAX),

    FOREIGN KEY (PatientID) REFERENCES Patients(PatientID),
    FOREIGN KEY (DoctorID) REFERENCES Doctors(DoctorID),
    FOREIGN KEY (AppointmentID) REFERENCES Appointments(AppointmentID)
);
GO

-- 6. DIAGNOSIS TABLE
CREATE TABLE Diagnosis (
    DiagnosisID INT IDENTITY(1,1) PRIMARY KEY,
    VisitID INT NOT NULL,
    DiagnosisDescription VARCHAR(MAX) NOT NULL,

    FOREIGN KEY (VisitID) REFERENCES Visits(VisitID)
);
GO

-- 7. TREATMENTS TABLE
CREATE TABLE Treatments (
    TreatmentID INT IDENTITY(1,1) PRIMARY KEY,
    VisitID INT NOT NULL,
    TreatmentDescription VARCHAR(MAX) NOT NULL,
    TreatmentCost DECIMAL(10,2) DEFAULT 0,

    FOREIGN KEY (VisitID) REFERENCES Visits(VisitID)
);
GO

-- 8. BILLS TABLE
CREATE TABLE Bills (
    BillID INT IDENTITY(1,1) PRIMARY KEY,
    PatientID INT NOT NULL,
    VisitID INT NULL,
    BillDate DATE DEFAULT GETDATE(),
    TotalAmount DECIMAL(10,2) DEFAULT 0,
    AmountPaid DECIMAL(10,2) DEFAULT 0,
    PaymentStatus VARCHAR(20) DEFAULT 'Unpaid',

    FOREIGN KEY (PatientID) REFERENCES Patients(PatientID),
    FOREIGN KEY (VisitID) REFERENCES Visits(VisitID)
);
GO

-- 9. BILL ITEMS TABLE
CREATE TABLE BillItems (
    BillItemID INT IDENTITY(1,1) PRIMARY KEY,
    BillID INT NOT NULL,
    Description VARCHAR(200) NOT NULL,
    Amount DECIMAL(10,2) NOT NULL,

    FOREIGN KEY (BillID) REFERENCES Bills(BillID)
);
GO

-- 10. PAYMENTS TABLE
CREATE TABLE Payments (
    PaymentID INT IDENTITY(1,1) PRIMARY KEY,
    BillID INT NOT NULL,
    Amount DECIMAL(10,2) NOT NULL,
    PaymentMethod VARCHAR(30),
    PaymentDate DATETIME DEFAULT GETDATE(),
    ReferenceNumber VARCHAR(100),

    FOREIGN KEY (BillID) REFERENCES Bills(BillID)
);
GO
