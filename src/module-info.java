
module MediConnect {

    requires javafx.controls;
    requires javafx.fxml;

    requires java.sql;

    requires com.microsoft.sqlserver.jdbc;
    requires jbcrypt;

    // PDF generation
    requires org.apache.pdfbox;
    requires org.apache.commons.logging;

    exports com.MediConnect.database;

    opens com.MediConnect to javafx.graphics;
    opens com.MediConnect.database to javafx.graphics;
    
    

}
