-- Change column subject from "nvarchar(100)" to "nvarchar(255)"
ALTER TABLE JIReportJobMail MODIFY (subject nvarchar2(255));