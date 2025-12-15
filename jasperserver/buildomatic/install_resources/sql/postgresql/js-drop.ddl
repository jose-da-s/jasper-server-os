
    alter table JIAwsDatasource 
       drop constraint FKa2q6ho769d4h6k1inqfw0avbi;

    alter table JIAzureSqlDatasource 
       drop constraint FKq54ak99008wuueewun6lw3x8p;

    alter table JIAzureSqlDatasource 
       drop constraint FK88n34smbe9i5eiqyvts12427n;

    alter table JIBeanDatasource 
       drop constraint FKcq7pt3wmr5oua2omyaynj18wm;

    alter table JIContentResource 
       drop constraint FKc903d1j62d6q2alfowyml1qyc;

    alter table JICustomDatasource 
       drop constraint FK698qlo478w8q00ratagvxjigg;

    alter table JICustomDatasourceProperty 
       drop constraint FKlmhvtq6f4aj7nbgpkop6pvwaj;

    alter table JICustomDatasourceResource 
       drop constraint FKdqu6gyndmi8barxd4e4mwgiu1;

    alter table JICustomDatasourceResource 
       drop constraint FK2b8of47ft9ucqg6wxq51d94f6;

    alter table JIDataSnapshotParameter 
       drop constraint id_fk_idx;

    alter table JIDataType 
       drop constraint FKfowvvrdpyr4fsfdt0qekb6b31;

    alter table JIFavoriteResource 
       drop constraint FKe3ak4arnheeorbrsc4u8m8pi0;

    alter table JIFavoriteResource 
       drop constraint FK63man3dkmekfr2hgfifi3ne8c;

    alter table JIFileResource 
       drop constraint FK9cks6rnum2e1nwpltygmric0a;

    alter table JIFileResource 
       drop constraint FK7lou06p9h4uewmjilbvtiyfti;

    alter table JIFTPInfoProperties 
       drop constraint FKs9ui25pnlkwvymdhafps0eqox;

    alter table JIInputControl 
       drop constraint FK7gw3h08vhv4ehuscnk22lweb0;

    alter table JIInputControl 
       drop constraint FKidpnbmursposu1b72a37j99dg;

    alter table JIInputControl 
       drop constraint FK8igl58hkwa8csd2pptsj6sl48;

    alter table JIInputControl 
       drop constraint FKeji041b95gimh1lii27d3j66f;

    alter table JIInputControlQueryColumn 
       drop constraint FKawiyltd98xvdsp3syt7fllehw;

    alter table JIJdbcDatasource 
       drop constraint FKkjuw9e7bu5n4k5nm3osifg5gc;

    alter table JIJNDIJdbcDatasource 
       drop constraint FK2gd8opslbt6erc8yx74s6j0nw;

    alter table JIListOfValues 
       drop constraint FKaoih4o3b0gmj4vgvocwb2m9qp;

    alter table JIListOfValuesItem 
       drop constraint FK2eq5m33wjtmf3d61gp38aqq77;

    alter table JILogEvent 
       drop constraint FK7636lhqn8drpalfckmb5wlljb;

    alter table JIQuery 
       drop constraint FK1ql6x3q59eti9h2r042ogoj3i;

    alter table JIQuery 
       drop constraint FK6ff8ikqrr2celf9wvfbrcycpx;

    alter table JIReportAlertToAddress 
       drop constraint FKhaqpdt65o66idbve7gs97ye8p;

    alter table JIReportJob 
       drop constraint FKntl9s5ul4oy4k9ws8u5wer55w;

    alter table JIReportJob 
       drop constraint FKkclub0l9io38j4su6crr9amd8;

    alter table JIReportJob 
       drop constraint FK8ymdkrb9uvvyi3xw9padxdxdv;

    alter table JIReportJob 
       drop constraint FKrbhjr4v64eym1mg2du3fs9i95;

    alter table JIReportJob 
       drop constraint FKo8dw7hsyef0xa1vg9feiu1mea;

    alter table JIReportJob 
       drop constraint FKgg6i9vqj6rx0kgqxmoqigm3gr;

    alter table JIReportJobCalendarTrigger 
       drop constraint FK89c4gqc5f5myrmfrc9a5gw7vb;

    alter table JIReportJobMailRecipient 
       drop constraint FKoe0v23mvul37f23piq39ks6fh;

    alter table JIReportJobOutputFormat 
       drop constraint FKi5f8ideliwcf9juic989pn2lj;

    alter table JIReportJobParameter 
       drop constraint FKh72kmrkm333g8ldlu7kybkrcd;

    alter table JIReportJobRepoDest 
       drop constraint FKba2wg3iix8mr5wcjq6004ekvw;

    alter table JIReportJobSimpleTrigger 
       drop constraint FK7gwgexkgjb6h4hn0166h2ttyk;

    alter table JIReportThumbnail 
       drop constraint FKhcdwx2qpiib9xtract2ecv31;

    alter table JIReportThumbnail 
       drop constraint FK8msuqfe2w3o9qjo81g8i6mgpi;

    alter table JIReportUnit 
       drop constraint FK6cl7eluds59jg1emjofa30i23;

    alter table JIReportUnit 
       drop constraint FK88u05b8n58ciemd3qcrd1jxn;

    alter table JIReportUnit 
       drop constraint FKcenakwnolc02r8xbdio30du9h;

    alter table JIReportUnit 
       drop constraint FKi2qw1u7yutrxh03xkrgx9o37d;

    alter table JIReportUnitInputControl 
       drop constraint FK8i0f45gnyhwcfrgueufsrvaw1;

    alter table JIReportUnitInputControl 
       drop constraint FKkvxewxu2tyomdsg1kioplnfq;

    alter table JIReportUnitResource 
       drop constraint FK18lcqhapddcvgcl52yqhil0a4;

    alter table JIReportUnitResource 
       drop constraint FK2fjktehjwog75dmp2rrfgm958;

    alter table JIRepositoryCache 
       drop constraint FKah8ma0bnkbirohud6lvenjt0k;

    alter table JIResource 
       drop constraint FKtnvtjq7s7hviyarfmomkokjm4;

    alter table JIResource 
       drop constraint FKc2qblpikow4ay35q0xgf9rjub;

    alter table JIResourceFolder 
       drop constraint FKduwulvl4qwqkqpxonyuer65fi;

    alter table JIRole 
       drop constraint FKmrf25easnd1emk6juaeot4dkn;

    alter table JITenant 
       drop constraint FKqupdx83verq7860nxsd6l24y1;

    alter table JIUser 
       drop constraint FKdnd0cy83h5cc2ex1375wek3wf;

    alter table JIUserRole 
       drop constraint FKrnaojg2v9yc6u72wrl6pmmi60;

    alter table JIUserRole 
       drop constraint FKska4g96yuc7dsyrskhot6nccp;

    alter table JIVirtualDatasource 
       drop constraint FK8jua4kahyslb99ni7bbyjxdf6;

    alter table JIVirtualDataSourceUriMap 
       drop constraint FKbpwmqrxy4onvvbsnole8icjic;

    alter table JIVirtualDataSourceUriMap 
       drop constraint FK94bfn67jetx6l0ykl2g9n37w1;

    drop table if exists JIAccessEvent cascade;

    drop table if exists JIAwsDatasource cascade;

    drop table if exists JIAzureSqlDatasource cascade;

    drop table if exists JIBeanDatasource cascade;

    drop table if exists JIContentResource cascade;

    drop table if exists JICustomDatasource cascade;

    drop table if exists JICustomDatasourceProperty cascade;

    drop table if exists JICustomDatasourceResource cascade;

    drop table if exists JIDataSnapshot cascade;

    drop table if exists JIDataSnapshotContents cascade;

    drop table if exists JIDataSnapshotParameter cascade;

    drop table if exists JIDataType cascade;

    drop table if exists JIExternalUserLoginEvents cascade;

    drop table if exists JIFavoriteResource cascade;

    drop table if exists JIFileResource cascade;

    drop table if exists JIFTPInfoProperties cascade;

    drop table if exists JIInputControl cascade;

    drop table if exists JIInputControlQueryColumn cascade;

    drop table if exists JIJdbcDatasource cascade;

    drop table if exists JIJNDIJdbcDatasource cascade;

    drop table if exists JIListOfValues cascade;

    drop table if exists JIListOfValuesItem cascade;

    drop table if exists JILogEvent cascade;

    drop table if exists JIObjectPermission cascade;

    drop table if exists JIProfileAttribute cascade;

    drop table if exists JIQuery cascade;

    drop table if exists JIReportAlertToAddress cascade;

    drop table if exists JIReportJob cascade;

    drop table if exists JIReportJobAlert cascade;

    drop table if exists JIReportJobCalendarTrigger cascade;

    drop table if exists JIReportJobMail cascade;

    drop table if exists JIReportJobMailRecipient cascade;

    drop table if exists JIReportJobOutputFormat cascade;

    drop table if exists JIReportJobParameter cascade;

    drop table if exists JIReportJobRepoDest cascade;

    drop table if exists JIReportJobSimpleTrigger cascade;

    drop table if exists JIReportJobTrigger cascade;

    drop table if exists JIReportThumbnail cascade;

    drop table if exists JIReportUnit cascade;

    drop table if exists JIReportUnitInputControl cascade;

    drop table if exists JIReportUnitResource cascade;

    drop table if exists JIRepositoryCache cascade;

    drop table if exists JIResource cascade;

    drop table if exists JIResourceFolder cascade;

    drop table if exists JIRole cascade;

    drop table if exists JITenant cascade;

    drop table if exists JIUser cascade;

    drop table if exists JIUserRole cascade;

    drop table if exists JIVirtualDatasource cascade;

    drop table if exists JIVirtualDataSourceUriMap cascade;

    drop sequence if exists hibernate_sequence;

    DROP INDEX JIReportUnit_mainReport_index ON JIReportUnit;

    DROP INDEX JIReportUnit_query_index ON JIReportUnit;

    DROP INDEX idx29_reportDataSource_idx ON JIReportUnit;

    DROP INDEX idx30_input_ctrl_id_idx ON JIReportUnitInputControl;

    DROP INDEX idx31_report_unit_id_idx ON JIReportUnitInputControl;

    DROP INDEX idx32_report_unit_id_idx ON JIReportUnitResource;

    DROP INDEX idx33_resource_id_idx ON JIReportUnitResource;

    DROP INDEX JIUser_tenantId_index ON JIUser;

    DROP INDEX idx15_input_ctrl_id_idx ON JIInputControlQueryColumn;

    DROP INDEX JIQuery_dataSource_index ON JIQuery;

    DROP INDEX idx28_resource_id_idx ON JIReportThumbnail;

    DROP INDEX idx27_destination_id_idx ON JIReportJobMailRecipient;

    DROP INDEX idx14_repodest_id_idx ON JIFTPInfoProperties;

    DROP INDEX idx34_item_reference_idx ON JIRepositoryCache;

    DROP INDEX JIFavoriteResource_user_id_idx ON JIFavoriteResource;

    DROP INDEX idx21_recipientobjclass_idx ON JIObjectPermission;

    DROP INDEX JIFileResource_reference_index ON JIFileResource;

    DROP INDEX idx22_recipientobjid_idx ON JIObjectPermission;

    DROP INDEX idx35_parent_folder_idx ON JIResourceFolder;

    DROP INDEX JIRole_tenantId_index ON JIRole;

    DROP INDEX JIResourceFolder_version_index ON JIResourceFolder;

    DROP INDEX JIUserRole_roleId_index ON JIUserRole;

    DROP INDEX JIResourceFolder_hidden_index ON JIResourceFolder;

    DROP INDEX JIUserRole_userId_index ON JIUserRole;

    DROP INDEX JIInputControl_data_type_index ON JIInputControl;

    DROP INDEX JITenant_parentId_index ON JITenant;

    DROP INDEX JIInputCtrl_list_of_values_idx ON JIInputControl;

    DROP INDEX JIInputControl_list_query_idx ON JIInputControl;

    DROP INDEX JILogEvent_userId_index ON JILogEvent;

    DROP INDEX JIReportJob_alert_index ON JIReportJob;

    DROP INDEX idx25_content_destination_idx ON JIReportJob;

    DROP INDEX JIReportJob_job_trigger_index ON JIReportJob;

    DROP INDEX JIResource_childrenFolder_idx ON JIResource;

    DROP INDEX idx26_mail_notification_idx ON JIReportJob;

    DROP INDEX JIResource_parent_folder_index ON JIResource;

    DROP INDEX JIReportJob_owner_index ON JIReportJob;

    DROP INDEX idxA1_resource_id_idx on JICustomDatasourceResource;

    DROP INDEX idx36_resource_id_idx ON JIVirtualDataSourceUriMap;

    DROP INDEX idx24_alert_id_idx ON JIReportAlertToAddress;

    DROP INDEX JIFavoriteResource_resource_id_idx ON JIFavoriteResource;

    DROP INDEX uri_index ON JIObjectPermission;
