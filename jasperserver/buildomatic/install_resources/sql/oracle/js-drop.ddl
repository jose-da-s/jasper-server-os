
    drop table JIAccessEvent cascade constraints;

    drop table JIAwsDatasource cascade constraints;

    drop table JIAzureSqlDatasource cascade constraints;

    drop table JIBeanDatasource cascade constraints;

    drop table JIContentResource cascade constraints;

    drop table JICustomDatasource cascade constraints;

    drop table JICustomDatasourceProperty cascade constraints;

    drop table JICustomDatasourceResource cascade constraints;

    drop table JIDataSnapshot cascade constraints;

    drop table JIDataSnapshotContents cascade constraints;

    drop table JIDataSnapshotParameter cascade constraints;

    drop table JIDataType cascade constraints;

    drop table JIExternalUserLoginEvents cascade constraints;

    drop table JIFavoriteResource cascade constraints;

    drop table JIFileResource cascade constraints;

    drop table JIFTPInfoProperties cascade constraints;

    drop table JIInputControl cascade constraints;

    drop table JIInputControlQueryColumn cascade constraints;

    drop table JIJdbcDatasource cascade constraints;

    drop table JIJNDIJdbcDatasource cascade constraints;

    drop table JIListOfValues cascade constraints;

    drop table JIListOfValuesItem cascade constraints;

    drop table JILogEvent cascade constraints;

    drop table JIObjectPermission cascade constraints;

    drop table JIProfileAttribute cascade constraints;

    drop table JIQuery cascade constraints;

    drop table JIReportAlertToAddress cascade constraints;

    drop table JIReportJob cascade constraints;

    drop table JIReportJobAlert cascade constraints;

    drop table JIReportJobCalendarTrigger cascade constraints;

    drop table JIReportJobMail cascade constraints;

    drop table JIReportJobMailRecipient cascade constraints;

    drop table JIReportJobOutputFormat cascade constraints;

    drop table JIReportJobParameter cascade constraints;

    drop table JIReportJobRepoDest cascade constraints;

    drop table JIReportJobSimpleTrigger cascade constraints;

    drop table JIReportJobTrigger cascade constraints;

    drop table JIReportThumbnail cascade constraints;

    drop table JIReportUnit cascade constraints;

    drop table JIReportUnitInputControl cascade constraints;

    drop table JIReportUnitResource cascade constraints;

    drop table JIRepositoryCache cascade constraints;

    drop table JIResource cascade constraints;

    drop table JIResourceFolder cascade constraints;

    drop table JIRole cascade constraints;

    drop table JITenant cascade constraints;

    drop table JIUser cascade constraints;

    drop table JIUserRole cascade constraints;

    drop table JIVirtualDatasource cascade constraints;

    drop table JIVirtualDataSourceUriMap cascade constraints;

    drop sequence hibernate_sequence;

    DROP INDEX JIQuery_dataSource_index ON JIQuery;

    DROP INDEX idx28_resource_id_idx ON JIReportThumbnail;

    DROP INDEX JIReportUnit_mainReport_index ON JIReportUnit;

    DROP INDEX JIReportUnit_query_index ON JIReportUnit;

    DROP INDEX idx29_reportDataSource_idx ON JIReportUnit;

    DROP INDEX JIRole_tenantId_index ON JIRole;

    DROP INDEX JIUserRole_roleId_index ON JIUserRole;

    DROP INDEX idx15_input_ctrl_id_idx ON JIInputControlQueryColumn;

    DROP INDEX JIUserRole_userId_index ON JIUserRole;

    DROP INDEX JITenant_parentId_index ON JITenant;

    DROP INDEX JIUser_tenantId_index ON JIUser;

    DROP INDEX idx27_destination_id_idx ON JIReportJobMailRecipient;

    DROP INDEX idx14_repodest_id_idx ON JIFTPInfoProperties;

    DROP INDEX idx34_item_reference_idx ON JIRepositoryCache;

    DROP INDEX JIFavoriteResource_user_id_idx ON JIFavoriteResource;

    DROP INDEX JIResource_childrenFolder_idx ON JIResource;

    DROP INDEX JIFileResource_reference_index ON JIFileResource;

    DROP INDEX JIResource_parent_folder_index ON JIResource;

    DROP INDEX idx35_parent_folder_idx ON JIResourceFolder;

    DROP INDEX idx36_resource_id_idx ON JIVirtualDataSourceUriMap;

    DROP INDEX JIResourceFolder_version_index ON JIResourceFolder;

    DROP INDEX uri_index ON JIObjectPermission;

    DROP INDEX JIResourceFolder_hidden_index ON JIResourceFolder;

    DROP INDEX idx21_recipientobjclass_idx ON JIObjectPermission;

    DROP INDEX JIInputControl_data_type_index ON JIInputControl;

    DROP INDEX idx22_recipientobjid_idx ON JIObjectPermission;

    DROP INDEX JIInputCtrl_list_of_values_idx ON JIInputControl;

    DROP INDEX JIInputControl_list_query_idx ON JIInputControl;

    DROP INDEX JILogEvent_userId_index ON JILogEvent;

    DROP INDEX JIReportJob_alert_index ON JIReportJob;

    DROP INDEX idx25_content_destination_idx ON JIReportJob;

    DROP INDEX JIReportJob_job_trigger_index ON JIReportJob;

    DROP INDEX idx30_input_ctrl_id_idx ON JIReportUnitInputControl;

    DROP INDEX idx26_mail_notification_idx ON JIReportJob;

    DROP INDEX idx31_report_unit_id_idx ON JIReportUnitInputControl;

    DROP INDEX JIReportJob_owner_index ON JIReportJob;

    DROP INDEX idxA1_resource_id_idx on JICustomDatasourceResource;

    DROP INDEX idx32_report_unit_id_idx ON JIReportUnitResource;

    DROP INDEX idx24_alert_id_idx ON JIReportAlertToAddress;

    DROP INDEX JIFavoriteResource_resource_id_idx ON JIFavoriteResource;

    DROP INDEX idx33_resource_id_idx ON JIReportUnitResource;
