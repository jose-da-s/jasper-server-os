create sequence hibernate_sequence start with 1 increment by  1;

    create table JIAccessEvent (
       id number(19,0) not null,
        user_id nvarchar2(255) not null,
        event_date date not null,
        resource_uri nvarchar2(255) not null,
        updating number(1,0) not null,
        resource_type nvarchar2(255) not null,
        hidden number(1,0) not null,
        primary key (id)
    );

    create table JIAwsDatasource (
       id number(19,0) not null,
        accessKey nvarchar2(150),
        secretKey nvarchar2(255),
        roleARN nvarchar2(100),
        region nvarchar2(100) not null,
        dbName nvarchar2(100) not null,
        dbInstanceIdentifier nvarchar2(100) not null,
        dbService nvarchar2(100) not null,
        primary key (id)
    );

    create table JIAzureSqlDatasource (
       id number(19,0) not null,
        keyStore_id number(19,0) not null,
        keyStorePassword nvarchar2(100),
        keyStoreType nvarchar2(25),
        subscriptionId nvarchar2(100),
        serverName nvarchar2(100) not null,
        dbName nvarchar2(100) not null,
        primary key (id)
    );

    create table JIBeanDatasource (
       id number(19,0) not null,
        beanName nvarchar2(100) not null,
        beanMethod nvarchar2(100),
        primary key (id)
    );

    create table JIContentResource (
       id number(19,0) not null,
        data blob,
        file_type nvarchar2(20),
        primary key (id)
    );

    create table JICustomDatasource (
       id number(19,0) not null,
        serviceClass nvarchar2(250) not null,
        primary key (id)
    );

    create table JICustomDatasourceProperty (
       ds_id number(19,0) not null,
        name nvarchar2(200) not null,
        value nvarchar2(1000),
        primary key (ds_id, name)
    );

    create table JICustomDatasourceResource (
       ds_id number(19,0) not null,
        name nvarchar2(200) not null,
        resource_id number(19,0) not null,
        primary key (ds_id, name)
    );

    create table JIDataSnapshot (
       id number(19,0) not null,
        version number(10,0) not null,
        snapshot_date date,
        contents_id number(19,0) not null,
        primary key (id)
    );

    create table JIDataSnapshotContents (
       id number(19,0) not null,
        data blob not null,
        primary key (id)
    );

    create table JIDataSnapshotParameter (
       id number(19,0) not null,
        parameter_name nvarchar2(100) not null,
        parameter_value blob,
        primary key (id, parameter_name)
    );

    create table JIDataType (
       id number(19,0) not null,
        type number(3,0),
        maxLength number(10,0),
        decimals number(10,0),
        regularExpr nvarchar2(255),
        minValue raw(1000),
        max_value raw(1000),
        strictMin number(1,0),
        strictMax number(1,0),
        primary key (id)
    );

    create table JIExternalUserLoginEvents (
       id number(19,0) not null,
        username nvarchar2(100) not null,
        enabled number(1,0),
        recordCreationDate date,
        recordLastUpdateDate date,
        numberOfFailedLoginAttempts number(10,0) default 0,
        tenantId nvarchar2(100),
        primary key (id)
    );

    create table JIFavoriteResource (
       id number(19,0) not null,
        user_id number(19,0) not null,
        resource_id number(19,0) not null,
        creation_date date not null,
        primary key (id)
    );

    create table JIFileResource (
       id number(19,0) not null,
        data blob,
        file_type nvarchar2(20),
        reference number(19,0),
        primary key (id)
    );

    create table JIFTPInfoProperties (
       repodest_id number(19,0) not null,
        property_name nvarchar2(100) not null,
        property_value nvarchar2(250),
        primary key (repodest_id, property_name)
    );

    create table JIInputControl (
       id number(19,0) not null,
        type number(3,0),
        mandatory number(1,0),
        readOnly number(1,0),
        visible number(1,0),
        data_type number(19,0),
        list_of_values number(19,0),
        list_query number(19,0),
        query_value_column nvarchar2(200),
        defaultValue raw(255),
        primary key (id)
    );

    create table JIInputControlQueryColumn (
       input_control_id number(19,0) not null,
        column_index number(10,0) not null,
        query_column nvarchar2(200) not null,
        primary key (input_control_id, column_index)
    );

    create table JIJdbcDatasource (
       id number(19,0) not null,
        driver nvarchar2(100) not null,
        password nvarchar2(250),
        connectionUrl nvarchar2(500),
        username nvarchar2(100),
        timezone nvarchar2(100),
        primary key (id)
    );

    create table JIJNDIJdbcDatasource (
       id number(19,0) not null,
        jndiName nvarchar2(100) not null,
        timezone nvarchar2(100),
        primary key (id)
    );

    create table JIListOfValues (
       id number(19,0) not null,
        primary key (id)
    );

    create table JIListOfValuesItem (
       id number(19,0) not null,
        idx number(10,0) not null,
        label nvarchar2(255),
        value blob,
        primary key (id, idx)
    );

    create table JILogEvent (
       id number(19,0) not null,
        occurrence_date date not null,
        event_type number(3,0) not null,
        component nvarchar2(100),
        message nvarchar2(250) not null,
        resource_uri nvarchar2(250),
        event_text nclob,
        event_data blob,
        event_state number(3,0),
        userId number(19,0),
        primary key (id)
    );

    create table JIMondrianConnection (
       id number(19,0) not null,
        reportDataSource number(19,0),
        mondrianSchema number(19,0),
        primary key (id)
    );

    create table JIMondrianXMLADefinition (
       id number(19,0) not null,
        catalog nvarchar2(100) not null,
        mondrianConnection number(19,0),
        primary key (id)
    );

    create table JIObjectPermission (
       id number(19,0) not null,
        uri nvarchar2(1000) not null,
        recipientobjectclass nvarchar2(250),
        recipientobjectid number(19,0),
        permissionMask number(10,0) not null,
        primary key (id)
    );

    create table JIOlapClientConnection (
       id number(19,0) not null,
        primary key (id)
    );

    create table JIOlapUnit (
       id number(19,0) not null,
        olapClientConnection number(19,0),
        mdx_query nclob not null,
        view_options blob,
        primary key (id)
    );

    create table JIProfileAttribute (
       id number(19,0) not null,
        attrName nvarchar2(255) not null,
        attrValue nvarchar2(2000),
        description nvarchar2(255),
        owner nvarchar2(255),
        principalobjectclass nvarchar2(255) not null,
        principalobjectid number(19,0) not null,
        primary key (id)
    );

    create table JIQuery (
       id number(19,0) not null,
        dataSource number(19,0),
        query_language nvarchar2(40) not null,
        sql_query nclob not null,
        primary key (id)
    );

    create table JIReportAlertToAddress (
       alert_id number(19,0) not null,
        to_address_idx number(10,0) not null,
        to_address nvarchar2(100) not null,
        primary key (alert_id, to_address_idx)
    );

    create table JIReportJob (
       id number(19,0) not null,
        version number(10,0) not null,
        owner number(19,0) not null,
        label nvarchar2(100) not null,
        description nvarchar2(2000),
        creation_date date,
        report_unit_uri nvarchar2(250) not null,
        scheduledResource number(19,0) not null,
        job_trigger number(19,0) not null,
        base_output_name nvarchar2(100) not null,
        output_locale nvarchar2(20),
        content_destination number(19,0),
        mail_notification number(19,0),
        alert number(19,0),
        primary key (id)
    );

    create table JIReportJobAlert (
       id number(19,0) not null,
        version number(10,0) not null,
        recipient number(3,0) not null,
        subject nvarchar2(100),
        message_text nvarchar2(2000),
        message_text_when_job_fails nvarchar2(2000),
        job_state number(3,0) not null,
        including_stack_trace number(1,0) not null,
        including_report_job_info number(1,0) not null,
        primary key (id)
    );

    create table JIReportJobCalendarTrigger (
       id number(19,0) not null,
        minutes nvarchar2(200) not null,
        hours nvarchar2(80) not null,
        days_type number(3,0) not null,
        week_days nvarchar2(20),
        month_days nvarchar2(100),
        months nvarchar2(40) not null,
        primary key (id)
    );

    create table JIReportJobMail (
       id number(19,0) not null,
        version number(10,0) not null,
        subject nvarchar2(255) not null,
        message nvarchar2(2000),
        send_type number(3,0) not null,
        skip_empty number(1,0) not null,
        message_text_when_job_fails nvarchar2(2000),
        inc_stktrc_when_job_fails number(1,0) not null,
        skip_notif_when_job_fails number(1,0) not null,
        primary key (id)
    );

    create table JIReportJobMailRecipient (
       destination_id number(19,0) not null,
        recipient_idx number(10,0) not null,
        recipient_type number(3,0) not null,
        address nvarchar2(100) not null,
        primary key (destination_id, recipient_idx)
    );

    create table JIReportJobOutputFormat (
       report_job_id number(19,0) not null,
        output_format number(3,0) not null,
        primary key (report_job_id, output_format)
    );

    create table JIReportJobParameter (
       job_id number(19,0) not null,
        parameter_name nvarchar2(255) not null,
        parameter_value blob,
        primary key (job_id, parameter_name)
    );

    create table JIReportJobRepoDest (
       id number(19,0) not null,
        version number(10,0) not null,
        folder_uri nvarchar2(250),
        sequential_filenames number(1,0) not null,
        overwrite_files number(1,0) not null,
        save_to_repository number(1,0) not null,
        output_description nvarchar2(250),
        timestamp_pattern nvarchar2(250),
        using_def_rpt_opt_folder_uri number(1,0) not null,
        output_local_folder nvarchar2(250),
        user_name nvarchar2(50),
        password nvarchar2(250),
        server_name nvarchar2(150),
        folder_path nvarchar2(250),
        ssh_private_key number(19,0),
        primary key (id)
    );

    create table JIReportJobSimpleTrigger (
       id number(19,0) not null,
        occurrence_count number(10,0) not null,
        recurrence_interval number(10,0),
        recurrence_interval_unit number(3,0),
        primary key (id)
    );

    create table JIReportJobTrigger (
       id number(19,0) not null,
        version number(10,0) not null,
        timezone nvarchar2(40),
        start_type number(3,0) not null,
        start_date date,
        end_date date,
        calendar_name nvarchar2(50),
        misfire_instruction number(10,0) not null,
        primary key (id)
    );

    create table JIReportThumbnail (
       id number(19,0) not null,
        user_id number(19,0) not null,
        resource_id number(19,0) not null,
        thumbnail blob not null,
        primary key (id)
    );

    create table JIReportUnit (
       id number(19,0) not null,
        reportDataSource number(19,0),
        query number(19,0),
        mainReport number(19,0),
        controlrenderer nvarchar2(100),
        reportrenderer nvarchar2(100),
        promptcontrols number(1,0),
        controlslayout number(3,0),
        data_snapshot_id number(19,0),
        primary key (id)
    );

    create table JIReportUnitInputControl (
       report_unit_id number(19,0) not null,
        control_index number(10,0) not null,
        input_control_id number(19,0) not null,
        primary key (report_unit_id, control_index)
    );

    create table JIReportUnitResource (
       report_unit_id number(19,0) not null,
        resource_index number(10,0) not null,
        resource_id number(19,0) not null,
        primary key (report_unit_id, resource_index)
    );

    create table JIRepositoryCache (
       id number(19,0) not null,
        uri nvarchar2(250) not null,
        cache_name nvarchar2(20) not null,
        data blob,
        version number(10,0) not null,
        version_date date not null,
        item_reference number(19,0),
        primary key (id)
    );

    create table JIResource (
       id number(19,0) not null,
        version number(10,0) not null,
        name nvarchar2(200) not null,
        parent_folder number(19,0) not null,
        childrenFolder number(19,0),
        label nvarchar2(200) not null,
        description nvarchar2(250),
        resourceType nvarchar2(255) not null,
        creation_date date not null,
        update_date date not null,
        primary key (id)
    );

    create table JIResourceFolder (
       id number(19,0) not null,
        version number(10,0) not null,
        uri nvarchar2(250) not null,
        hidden number(1,0),
        name nvarchar2(200) not null,
        label nvarchar2(200) not null,
        description nvarchar2(250),
        parent_folder number(19,0),
        creation_date date not null,
        update_date date not null,
        primary key (id)
    );

    create table JIRole (
       id number(19,0) not null,
        rolename nvarchar2(100) not null,
        tenantId number(19,0) not null,
        externallyDefined number(1,0),
        primary key (id)
    );

    create table JITenant (
       id number(19,0) not null,
        tenantId nvarchar2(100) not null,
        tenantAlias nvarchar2(100) not null,
        parentId number(19,0),
        tenantName nvarchar2(100) not null,
        tenantDesc nvarchar2(250),
        tenantNote nvarchar2(250),
        tenantUri nvarchar2(250) not null,
        tenantFolderUri nvarchar2(250) not null,
        theme nvarchar2(250),
        primary key (id)
    );

    create table JIUser (
       id number(19,0) not null,
        username nvarchar2(100) not null,
        tenantId number(19,0) not null,
        fullname nvarchar2(100) not null,
        emailAddress nvarchar2(100),
        password nvarchar2(250),
        externallyDefined number(1,0),
        enabled number(1,0),
        previousPasswordChangeTime date,
        numberOfFailedLoginAttempts number(10,0) default 0,
        primary key (id)
    );

    create table JIUserRole (
       roleId number(19,0) not null,
        userId number(19,0) not null,
        primary key (userId, roleId)
    );

    create table JIVirtualDatasource (
       id number(19,0) not null,
        timezone nvarchar2(100),
        primary key (id)
    );

    create table JIVirtualDataSourceUriMap (
       virtualDS_id number(19,0) not null,
        data_source_name nvarchar2(200) not null,
        resource_id number(19,0) not null,
        primary key (virtualDS_id, data_source_name)
    );

    create table JIXMLAConnection (
       id number(19,0) not null,
        catalog nvarchar2(100) not null,
        username nvarchar2(100) not null,
        password nvarchar2(250) not null,
        datasource nvarchar2(100) not null,
        uri nvarchar2(100) not null,
        primary key (id)
    );
create index access_user_index on JIAccessEvent (user_id);
create index access_date_index on JIAccessEvent (event_date);
create index access_res_uri_index on JIAccessEvent (resource_uri);
create index access_upd_index on JIAccessEvent (updating);
create index access_res_type_index on JIAccessEvent (resource_type);
create index access_hid_index on JIAccessEvent (hidden);
create index idx_keyStore_id on JIAzureSqlDatasource (keyStore_id);

    alter table JIFavoriteResource 
       add constraint UKrj25jnmtcmddlfp23n7duhpv unique (user_id, resource_id);
create index idx_scheduled_res on JIReportJob (scheduledResource);
create index idx_ssh_private_key on JIReportJobRepoDest (ssh_private_key);

    alter table JIReportThumbnail 
       add constraint UKby8mikd2bdxyvgguosocs0yn3 unique (user_id, resource_id);

    alter table JIRepositoryCache 
       add constraint UKt25kjcmwyu0v0jvmgj7jpe7fq unique (uri, cache_name);
create index resource_type_index on JIResource (resourceType);

    alter table JIResource 
       add constraint UKq0fsg83g1w6207k40fixjobra unique (name, parent_folder);

    alter table JIResourceFolder 
       add constraint UK_emu9w2irh08lh7kw07be7u1vp unique (uri);

    alter table JIRole 
       add constraint UKqrupjb1yd43e3t6po2nk038t9 unique (rolename, tenantId);

    alter table JITenant 
       add constraint UK1jv9e2tsi2vn74xivgia15g42 unique (tenantId);

    alter table JIUser 
       add constraint UKrw3wi1dqcub2iiom9pvdtuso unique (username, tenantId);

    alter table JIAwsDatasource 
       add constraint FKa2q6ho769d4h6k1inqfw0avbi 
       foreign key (id) 
       references JIJdbcDatasource;

    alter table JIAzureSqlDatasource 
       add constraint FKq54ak99008wuueewun6lw3x8p 
       foreign key (id) 
       references JIJdbcDatasource;

    alter table JIAzureSqlDatasource 
       add constraint FK88n34smbe9i5eiqyvts12427n 
       foreign key (keyStore_id) 
       references JIResource;

    alter table JIBeanDatasource 
       add constraint FKcq7pt3wmr5oua2omyaynj18wm 
       foreign key (id) 
       references JIResource;

    alter table JIContentResource 
       add constraint FKc903d1j62d6q2alfowyml1qyc 
       foreign key (id) 
       references JIResource;

    alter table JICustomDatasource 
       add constraint FK698qlo478w8q00ratagvxjigg 
       foreign key (id) 
       references JIResource;

    alter table JICustomDatasourceProperty 
       add constraint FKlmhvtq6f4aj7nbgpkop6pvwaj 
       foreign key (ds_id) 
       references JICustomDatasource;

    alter table JICustomDatasourceResource 
       add constraint FKdqu6gyndmi8barxd4e4mwgiu1 
       foreign key (resource_id) 
       references JIResource;

    alter table JICustomDatasourceResource 
       add constraint FK2b8of47ft9ucqg6wxq51d94f6 
       foreign key (ds_id) 
       references JICustomDatasource;

    alter table JIDataSnapshotParameter 
       add constraint id_fk_idx 
       foreign key (id) 
       references JIDataSnapshot;

    alter table JIDataType 
       add constraint FKfowvvrdpyr4fsfdt0qekb6b31 
       foreign key (id) 
       references JIResource;

    alter table JIFavoriteResource 
       add constraint FKe3ak4arnheeorbrsc4u8m8pi0 
       foreign key (user_id) 
       references JIUser 
       on delete cascade;

    alter table JIFavoriteResource 
       add constraint FK63man3dkmekfr2hgfifi3ne8c 
       foreign key (resource_id) 
       references JIResource 
       on delete cascade;

    alter table JIFileResource 
       add constraint FK9cks6rnum2e1nwpltygmric0a 
       foreign key (id) 
       references JIResource;

    alter table JIFileResource 
       add constraint FK7lou06p9h4uewmjilbvtiyfti 
       foreign key (reference) 
       references JIFileResource;

    alter table JIFTPInfoProperties 
       add constraint FKs9ui25pnlkwvymdhafps0eqox 
       foreign key (repodest_id) 
       references JIReportJobRepoDest;

    alter table JIInputControl 
       add constraint FK7gw3h08vhv4ehuscnk22lweb0 
       foreign key (id) 
       references JIResource;

    alter table JIInputControl 
       add constraint FKidpnbmursposu1b72a37j99dg 
       foreign key (data_type) 
       references JIDataType;

    alter table JIInputControl 
       add constraint FK8igl58hkwa8csd2pptsj6sl48 
       foreign key (list_of_values) 
       references JIListOfValues;

    alter table JIInputControl 
       add constraint FKeji041b95gimh1lii27d3j66f 
       foreign key (list_query) 
       references JIQuery;

    alter table JIInputControlQueryColumn 
       add constraint FKawiyltd98xvdsp3syt7fllehw 
       foreign key (input_control_id) 
       references JIInputControl;

    alter table JIJdbcDatasource 
       add constraint FKkjuw9e7bu5n4k5nm3osifg5gc 
       foreign key (id) 
       references JIResource;

    alter table JIJNDIJdbcDatasource 
       add constraint FK2gd8opslbt6erc8yx74s6j0nw 
       foreign key (id) 
       references JIResource;

    alter table JIListOfValues 
       add constraint FKaoih4o3b0gmj4vgvocwb2m9qp 
       foreign key (id) 
       references JIResource;

    alter table JIListOfValuesItem 
       add constraint FK2eq5m33wjtmf3d61gp38aqq77 
       foreign key (id) 
       references JIListOfValues;

    alter table JILogEvent 
       add constraint FK7636lhqn8drpalfckmb5wlljb 
       foreign key (userId) 
       references JIUser;

    alter table JIMondrianConnection 
       add constraint FKm9glomusslw0ouy1xev0kafql 
       foreign key (id) 
       references JIOlapClientConnection;

    alter table JIMondrianConnection 
       add constraint FK8yiwytorg3lwqq1gag9fng7rf 
       foreign key (reportDataSource) 
       references JIResource;

    alter table JIMondrianConnection 
       add constraint FKamcjhut3kc0ko4rypemusdn7d 
       foreign key (mondrianSchema) 
       references JIFileResource;

    alter table JIMondrianXMLADefinition 
       add constraint FKclv0lm19k3nvkmbv41epbfs34 
       foreign key (id) 
       references JIResource;

    alter table JIMondrianXMLADefinition 
       add constraint FKnmn2j9pevf2slm0i314ghs1sq 
       foreign key (mondrianConnection) 
       references JIMondrianConnection;

    alter table JIOlapClientConnection 
       add constraint FKqtji02a7ga296baj2y3avol24 
       foreign key (id) 
       references JIResource;

    alter table JIOlapUnit 
       add constraint FKtj0u3bnnfbe2h6w5v9jue5xr1 
       foreign key (id) 
       references JIResource;

    alter table JIOlapUnit 
       add constraint FKakvumwho658vijmoaaxddp4xo 
       foreign key (olapClientConnection) 
       references JIOlapClientConnection;

    alter table JIQuery 
       add constraint FK1ql6x3q59eti9h2r042ogoj3i 
       foreign key (id) 
       references JIResource;

    alter table JIQuery 
       add constraint FK6ff8ikqrr2celf9wvfbrcycpx 
       foreign key (dataSource) 
       references JIResource;

    alter table JIReportAlertToAddress 
       add constraint FKhaqpdt65o66idbve7gs97ye8p 
       foreign key (alert_id) 
       references JIReportJobAlert;

    alter table JIReportJob 
       add constraint FKntl9s5ul4oy4k9ws8u5wer55w 
       foreign key (owner) 
       references JIUser;

    alter table JIReportJob 
       add constraint FKkclub0l9io38j4su6crr9amd8 
       foreign key (scheduledResource) 
       references JIResource;

    alter table JIReportJob 
       add constraint FK8ymdkrb9uvvyi3xw9padxdxdv 
       foreign key (job_trigger) 
       references JIReportJobTrigger;

    alter table JIReportJob 
       add constraint FKrbhjr4v64eym1mg2du3fs9i95 
       foreign key (content_destination) 
       references JIReportJobRepoDest;

    alter table JIReportJob 
       add constraint FKo8dw7hsyef0xa1vg9feiu1mea 
       foreign key (mail_notification) 
       references JIReportJobMail;

    alter table JIReportJob 
       add constraint FKgg6i9vqj6rx0kgqxmoqigm3gr 
       foreign key (alert) 
       references JIReportJobAlert;

    alter table JIReportJobCalendarTrigger 
       add constraint FK89c4gqc5f5myrmfrc9a5gw7vb 
       foreign key (id) 
       references JIReportJobTrigger;

    alter table JIReportJobMailRecipient 
       add constraint FKoe0v23mvul37f23piq39ks6fh 
       foreign key (destination_id) 
       references JIReportJobMail;

    alter table JIReportJobOutputFormat 
       add constraint FKi5f8ideliwcf9juic989pn2lj 
       foreign key (report_job_id) 
       references JIReportJob;

    alter table JIReportJobParameter 
       add constraint FKh72kmrkm333g8ldlu7kybkrcd 
       foreign key (job_id) 
       references JIReportJob;

    alter table JIReportJobRepoDest 
       add constraint FKba2wg3iix8mr5wcjq6004ekvw 
       foreign key (ssh_private_key) 
       references JIResource;

    alter table JIReportJobSimpleTrigger 
       add constraint FK7gwgexkgjb6h4hn0166h2ttyk 
       foreign key (id) 
       references JIReportJobTrigger;

    alter table JIReportThumbnail 
       add constraint FKhcdwx2qpiib9xtract2ecv31 
       foreign key (user_id) 
       references JIUser 
       on delete cascade;

    alter table JIReportThumbnail 
       add constraint FK8msuqfe2w3o9qjo81g8i6mgpi 
       foreign key (resource_id) 
       references JIResource 
       on delete cascade;

    alter table JIReportUnit 
       add constraint FK6cl7eluds59jg1emjofa30i23 
       foreign key (id) 
       references JIResource;

    alter table JIReportUnit 
       add constraint FK88u05b8n58ciemd3qcrd1jxn 
       foreign key (reportDataSource) 
       references JIResource;

    alter table JIReportUnit 
       add constraint FKcenakwnolc02r8xbdio30du9h 
       foreign key (query) 
       references JIQuery;

    alter table JIReportUnit 
       add constraint FKi2qw1u7yutrxh03xkrgx9o37d 
       foreign key (mainReport) 
       references JIFileResource;

    alter table JIReportUnitInputControl 
       add constraint FK8i0f45gnyhwcfrgueufsrvaw1 
       foreign key (input_control_id) 
       references JIInputControl;

    alter table JIReportUnitInputControl 
       add constraint FKkvxewxu2tyomdsg1kioplnfq 
       foreign key (report_unit_id) 
       references JIReportUnit;

    alter table JIReportUnitResource 
       add constraint FK18lcqhapddcvgcl52yqhil0a4 
       foreign key (resource_id) 
       references JIFileResource;

    alter table JIReportUnitResource 
       add constraint FK2fjktehjwog75dmp2rrfgm958 
       foreign key (report_unit_id) 
       references JIReportUnit;

    alter table JIRepositoryCache 
       add constraint FKah8ma0bnkbirohud6lvenjt0k 
       foreign key (item_reference) 
       references JIRepositoryCache;

    alter table JIResource 
       add constraint FKtnvtjq7s7hviyarfmomkokjm4 
       foreign key (parent_folder) 
       references JIResourceFolder;

    alter table JIResource 
       add constraint FKc2qblpikow4ay35q0xgf9rjub 
       foreign key (childrenFolder) 
       references JIResourceFolder;

    alter table JIResourceFolder 
       add constraint FKduwulvl4qwqkqpxonyuer65fi 
       foreign key (parent_folder) 
       references JIResourceFolder;

    alter table JIRole 
       add constraint FKmrf25easnd1emk6juaeot4dkn 
       foreign key (tenantId) 
       references JITenant;

    alter table JITenant 
       add constraint FKqupdx83verq7860nxsd6l24y1 
       foreign key (parentId) 
       references JITenant;

    alter table JIUser 
       add constraint FKdnd0cy83h5cc2ex1375wek3wf 
       foreign key (tenantId) 
       references JITenant;

    alter table JIUserRole 
       add constraint FKrnaojg2v9yc6u72wrl6pmmi60 
       foreign key (userId) 
       references JIUser;

    alter table JIUserRole 
       add constraint FKska4g96yuc7dsyrskhot6nccp 
       foreign key (roleId) 
       references JIRole;

    alter table JIVirtualDatasource 
       add constraint FK8jua4kahyslb99ni7bbyjxdf6 
       foreign key (id) 
       references JIResource;

    alter table JIVirtualDataSourceUriMap 
       add constraint FKbpwmqrxy4onvvbsnole8icjic 
       foreign key (resource_id) 
       references JIResource;

    alter table JIVirtualDataSourceUriMap 
       add constraint FK94bfn67jetx6l0ykl2g9n37w1 
       foreign key (virtualDS_id) 
       references JIVirtualDatasource;

    alter table JIXMLAConnection 
       add constraint FK27s5ja8sxgrylp7cf0wyscl79 
       foreign key (id) 
       references JIOlapClientConnection;
create index JILogEvent_userId_index on JILogEvent (userId);
create index idx17_reportDataSource_idx on JIMondrianConnection (reportDataSource);
create index JIUser_tenantId_index on JIUser (tenantId);
create index JIReportJob_alert_index on JIReportJob (alert);
create index idx20_mondrianConnection_idx on JIMondrianXMLADefinition (mondrianConnection);
create index idx25_content_destination_idx on JIReportJob (content_destination);
create index idx23_olapClientConnection_idx on JIOlapUnit (olapClientConnection);
create index JIReportJob_job_trigger_index on JIReportJob (job_trigger);
create index idx26_mail_notification_idx on JIReportJob (mail_notification);
create index idx36_resource_id_idx on JIVirtualDataSourceUriMap (resource_id);
create index JIResourceFolder_version_index on JIResourceFolder (version);
create index uri_index on JIObjectPermission (uri);
create index JIResourceFolder_hidden_index on JIResourceFolder (hidden);
create index idx21_recipientobjclass_idx on JIObjectPermission (recipientobjectclass);
create index JIInputControl_data_type_index on JIInputControl (data_type);
create index idx22_recipientobjid_idx on JIObjectPermission (recipientobjectid);
create index JIInputCtrl_list_of_values_idx on JIInputControl (list_of_values);
create index JIRole_tenantId_index on JIRole (tenantId);
create index JIInputControl_list_query_idx on JIInputControl (list_query);
create index JIUserRole_roleId_index on JIUserRole (roleId);
create index idx15_input_ctrl_id_idx on JIInputControlQueryColumn (input_control_id);
create index JIUserRole_userId_index on JIUserRole (userId);
create index idx16_mondrianSchema_idx on JIMondrianConnection (mondrianSchema);
create index JITenant_parentId_index on JITenant (parentId);
create index JIResource_parent_folder_index on JIResource (parent_folder);
create index JIFavoriteResource_resource_id_idx on JIFavoriteResource (resource_id);
create index idx33_resource_id_idx on JIReportUnitResource (resource_id);
create index JIFavoriteResource_user_id_idx on JIFavoriteResource (user_id);
create index JIResource_childrenFolder_idx on JIResource (childrenFolder);
create index JIFileResource_reference_index on JIFileResource (reference);
create index idx35_parent_folder_idx on JIResourceFolder (parent_folder);
create index JIQuery_dataSource_index on JIQuery (dataSource);
create index idx28_resource_id_idx on JIReportThumbnail (resource_id);
create index JIReportJob_owner_index on JIReportJob (owner);
create index JIReportUnit_mainReport_index on JIReportUnit (mainReport);
create index idx24_alert_id_idx on JIReportAlertToAddress (alert_id);
create index JIReportUnit_query_index on JIReportUnit (query);
create index idx27_destination_id_idx on JIReportJobMailRecipient (destination_id);
create index idx29_reportDataSource_idx on JIReportUnit (reportDataSource);
create index idx14_repodest_id_idx on JIFTPInfoProperties (repodest_id);
create index idx30_input_ctrl_id_idx on JIReportUnitInputControl (input_control_id);
create index idx34_item_reference_idx on JIRepositoryCache (item_reference);
create index idx31_report_unit_id_idx on JIReportUnitInputControl (report_unit_id);
create index idxA1_resource_id_idx on JICustomDatasourceResource (resource_id);
create index idx32_report_unit_id_idx on JIReportUnitResource (report_unit_id);
