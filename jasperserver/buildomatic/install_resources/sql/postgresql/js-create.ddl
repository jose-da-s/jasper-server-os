create sequence hibernate_sequence start 1 increment 1;

    create table JIAccessEvent (
       id int8 not null,
        user_id varchar(255) not null,
        event_date timestamp not null,
        resource_uri varchar(255) not null,
        updating boolean not null,
        resource_type varchar(255) not null,
        hidden boolean not null,
        primary key (id)
    );

    create table JIAwsDatasource (
       id int8 not null,
        accessKey varchar(150),
        secretKey varchar(255),
        roleARN varchar(100),
        region varchar(100) not null,
        dbName varchar(100) not null,
        dbInstanceIdentifier varchar(100) not null,
        dbService varchar(100) not null,
        primary key (id)
    );

    create table JIAzureSqlDatasource (
       id int8 not null,
        keyStore_id int8 not null,
        keyStorePassword varchar(100),
        keyStoreType varchar(25),
        subscriptionId varchar(100),
        serverName varchar(100) not null,
        dbName varchar(100) not null,
        primary key (id)
    );

    create table JIBeanDatasource (
       id int8 not null,
        beanName varchar(100) not null,
        beanMethod varchar(100),
        primary key (id)
    );

    create table JIContentResource (
       id int8 not null,
        data bytea,
        file_type varchar(20),
        primary key (id)
    );

    create table JICustomDatasource (
       id int8 not null,
        serviceClass varchar(250) not null,
        primary key (id)
    );

    create table JICustomDatasourceProperty (
       ds_id int8 not null,
        name varchar(200) not null,
        value varchar(1000),
        primary key (ds_id, name)
    );

    create table JICustomDatasourceResource (
       ds_id int8 not null,
        name varchar(200) not null,
        resource_id int8 not null,
        primary key (ds_id, name)
    );

    create table JIDataSnapshot (
       id int8 not null,
        version int4 not null,
        snapshot_date timestamp,
        contents_id int8 not null,
        primary key (id)
    );

    create table JIDataSnapshotContents (
       id int8 not null,
        data bytea not null,
        primary key (id)
    );

    create table JIDataSnapshotParameter (
       id int8 not null,
        parameter_name varchar(100) not null,
        parameter_value bytea,
        primary key (id, parameter_name)
    );

    create table JIDataType (
       id int8 not null,
        type int2,
        maxLength int4,
        decimals int4,
        regularExpr varchar(255),
        minValue bytea,
        max_value bytea,
        strictMin boolean,
        strictMax boolean,
        primary key (id)
    );

    create table JIExternalUserLoginEvents (
       id int8 not null,
        username varchar(100) not null,
        enabled boolean,
        recordCreationDate timestamp,
        recordLastUpdateDate timestamp,
        numberOfFailedLoginAttempts int4 default 0,
        tenantId varchar(100),
        primary key (id)
    );

    create table JIFavoriteResource (
       id int8 not null,
        user_id int8 not null,
        resource_id int8 not null,
        creation_date timestamp not null,
        primary key (id)
    );

    create table JIFileResource (
       id int8 not null,
        data bytea,
        file_type varchar(20),
        reference int8,
        primary key (id)
    );

    create table JIFTPInfoProperties (
       repodest_id int8 not null,
        property_name varchar(100) not null,
        property_value varchar(250),
        primary key (repodest_id, property_name)
    );

    create table JIInputControl (
       id int8 not null,
        type int2,
        mandatory boolean,
        readOnly boolean,
        visible boolean,
        data_type int8,
        list_of_values int8,
        list_query int8,
        query_value_column varchar(200),
        defaultValue bytea,
        primary key (id)
    );

    create table JIInputControlQueryColumn (
       input_control_id int8 not null,
        column_index int4 not null,
        query_column varchar(200) not null,
        primary key (input_control_id, column_index)
    );

    create table JIJdbcDatasource (
       id int8 not null,
        driver varchar(100) not null,
        password varchar(250),
        connectionUrl varchar(500),
        username varchar(100),
        timezone varchar(100),
        primary key (id)
    );

    create table JIJNDIJdbcDatasource (
       id int8 not null,
        jndiName varchar(100) not null,
        timezone varchar(100),
        primary key (id)
    );

    create table JIListOfValues (
       id int8 not null,
        primary key (id)
    );

    create table JIListOfValuesItem (
       id int8 not null,
        idx int4 not null,
        label varchar(255),
        value bytea,
        primary key (id, idx)
    );

    create table JILogEvent (
       id int8 not null,
        occurrence_date timestamp not null,
        event_type int2 not null,
        component varchar(100),
        message varchar(250) not null,
        resource_uri varchar(250),
        event_text text,
        event_data bytea,
        event_state int2,
        userId int8,
        primary key (id)
    );

    create table JIMondrianConnection (
       id int8 not null,
        reportDataSource int8,
        mondrianSchema int8,
        primary key (id)
    );

    create table JIMondrianXMLADefinition (
       id int8 not null,
        catalog varchar(100) not null,
        mondrianConnection int8,
        primary key (id)
    );

    create table JIObjectPermission (
       id int8 not null,
        uri varchar(1000) not null,
        recipientobjectclass varchar(250),
        recipientobjectid int8,
        permissionMask int4 not null,
        primary key (id)
    );

    create table JIOlapClientConnection (
       id int8 not null,
        primary key (id)
    );

    create table JIOlapUnit (
       id int8 not null,
        olapClientConnection int8,
        mdx_query text not null,
        view_options bytea,
        primary key (id)
    );

    create table JIProfileAttribute (
       id int8 not null,
        attrName varchar(255) not null,
        attrValue varchar(2000),
        description varchar(255),
        owner varchar(255),
        principalobjectclass varchar(255) not null,
        principalobjectid int8 not null,
        primary key (id)
    );

    create table JIQuery (
       id int8 not null,
        dataSource int8,
        query_language varchar(40) not null,
        sql_query text not null,
        primary key (id)
    );

    create table JIReportAlertToAddress (
       alert_id int8 not null,
        to_address_idx int4 not null,
        to_address varchar(100) not null,
        primary key (alert_id, to_address_idx)
    );

    create table JIReportJob (
       id int8 not null,
        version int4 not null,
        owner int8 not null,
        label varchar(100) not null,
        description varchar(2000),
        creation_date timestamp,
        report_unit_uri varchar(250) not null,
        scheduledResource int8 not null,
        job_trigger int8 not null,
        base_output_name varchar(100) not null,
        output_locale varchar(20),
        content_destination int8,
        mail_notification int8,
        alert int8,
        primary key (id)
    );

    create table JIReportJobAlert (
       id int8 not null,
        version int4 not null,
        recipient int2 not null,
        subject varchar(100),
        message_text varchar(2000),
        message_text_when_job_fails varchar(2000),
        job_state int2 not null,
        including_stack_trace boolean not null,
        including_report_job_info boolean not null,
        primary key (id)
    );

    create table JIReportJobCalendarTrigger (
       id int8 not null,
        minutes varchar(200) not null,
        hours varchar(80) not null,
        days_type int2 not null,
        week_days varchar(20),
        month_days varchar(100),
        months varchar(40) not null,
        primary key (id)
    );

    create table JIReportJobMail (
       id int8 not null,
        version int4 not null,
        subject varchar(255) not null,
        message varchar(2000),
        send_type int2 not null,
        skip_empty boolean not null,
        message_text_when_job_fails varchar(2000),
        inc_stktrc_when_job_fails boolean not null,
        skip_notif_when_job_fails boolean not null,
        primary key (id)
    );

    create table JIReportJobMailRecipient (
       destination_id int8 not null,
        recipient_idx int4 not null,
        recipient_type int2 not null,
        address varchar(100) not null,
        primary key (destination_id, recipient_idx)
    );

    create table JIReportJobOutputFormat (
       report_job_id int8 not null,
        output_format int2 not null,
        primary key (report_job_id, output_format)
    );

    create table JIReportJobParameter (
       job_id int8 not null,
        parameter_name varchar(255) not null,
        parameter_value bytea,
        primary key (job_id, parameter_name)
    );

    create table JIReportJobRepoDest (
       id int8 not null,
        version int4 not null,
        folder_uri varchar(250),
        sequential_filenames boolean not null,
        overwrite_files boolean not null,
        save_to_repository boolean not null,
        output_description varchar(250),
        timestamp_pattern varchar(250),
        using_def_rpt_opt_folder_uri boolean not null,
        output_local_folder varchar(250),
        user_name varchar(50),
        password varchar(250),
        server_name varchar(150),
        folder_path varchar(250),
        ssh_private_key int8,
        primary key (id)
    );

    create table JIReportJobSimpleTrigger (
       id int8 not null,
        occurrence_count int4 not null,
        recurrence_interval int4,
        recurrence_interval_unit int2,
        primary key (id)
    );

    create table JIReportJobTrigger (
       id int8 not null,
        version int4 not null,
        timezone varchar(40),
        start_type int2 not null,
        start_date timestamp,
        end_date timestamp,
        calendar_name varchar(50),
        misfire_instruction int4 not null,
        primary key (id)
    );

    create table JIReportThumbnail (
       id int8 not null,
        user_id int8 not null,
        resource_id int8 not null,
        thumbnail bytea not null,
        primary key (id)
    );

    create table JIReportUnit (
       id int8 not null,
        reportDataSource int8,
        query int8,
        mainReport int8,
        controlrenderer varchar(100),
        reportrenderer varchar(100),
        promptcontrols boolean,
        controlslayout int2,
        data_snapshot_id int8,
        primary key (id)
    );

    create table JIReportUnitInputControl (
       report_unit_id int8 not null,
        control_index int4 not null,
        input_control_id int8 not null,
        primary key (report_unit_id, control_index)
    );

    create table JIReportUnitResource (
       report_unit_id int8 not null,
        resource_index int4 not null,
        resource_id int8 not null,
        primary key (report_unit_id, resource_index)
    );

    create table JIRepositoryCache (
       id int8 not null,
        uri varchar(250) not null,
        cache_name varchar(20) not null,
        data bytea,
        version int4 not null,
        version_date timestamp not null,
        item_reference int8,
        primary key (id)
    );

    create table JIResource (
       id int8 not null,
        version int4 not null,
        name varchar(200) not null,
        parent_folder int8 not null,
        childrenFolder int8,
        label varchar(200) not null,
        description varchar(250),
        resourceType varchar(255) not null,
        creation_date timestamp not null,
        update_date timestamp not null,
        primary key (id)
    );

    create table JIResourceFolder (
       id int8 not null,
        version int4 not null,
        uri varchar(250) not null,
        hidden boolean,
        name varchar(200) not null,
        label varchar(200) not null,
        description varchar(250),
        parent_folder int8,
        creation_date timestamp not null,
        update_date timestamp not null,
        primary key (id)
    );

    create table JIRole (
       id int8 not null,
        rolename varchar(100) not null,
        tenantId int8 not null,
        externallyDefined boolean,
        primary key (id)
    );

    create table JITenant (
       id int8 not null,
        tenantId varchar(100) not null,
        tenantAlias varchar(100) not null,
        parentId int8,
        tenantName varchar(100) not null,
        tenantDesc varchar(250),
        tenantNote varchar(250),
        tenantUri varchar(250) not null,
        tenantFolderUri varchar(250) not null,
        theme varchar(250),
        primary key (id)
    );

    create table JIUser (
       id int8 not null,
        username varchar(100) not null,
        tenantId int8 not null,
        fullname varchar(100) not null,
        emailAddress varchar(100),
        password varchar(250),
        externallyDefined boolean,
        enabled boolean,
        previousPasswordChangeTime timestamp,
        numberOfFailedLoginAttempts int4 default 0,
        primary key (id)
    );

    create table JIUserRole (
       roleId int8 not null,
        userId int8 not null,
        primary key (userId, roleId)
    );

    create table JIVirtualDatasource (
       id int8 not null,
        timezone varchar(100),
        primary key (id)
    );

    create table JIVirtualDataSourceUriMap (
       virtualDS_id int8 not null,
        data_source_name varchar(200) not null,
        resource_id int8 not null,
        primary key (virtualDS_id, data_source_name)
    );

    create table JIXMLAConnection (
       id int8 not null,
        catalog varchar(100) not null,
        username varchar(100) not null,
        password varchar(250) not null,
        datasource varchar(100) not null,
        uri varchar(100) not null,
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
