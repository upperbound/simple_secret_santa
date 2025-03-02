create table if not exists participant_role(
    id varchar(30) not null primary key
);

create table if not exists participant_group(
    uuid varchar(36) not null primary key,
    description varchar(100),
    has_drawn boolean default false not null,
    created_date timestamp(0) without time zone default CURRENT_TIMESTAMP() not null,
    created_by varchar(36) not null,
    modified_date timestamp(0) without time zone,
    modified_by varchar(36)
);

create table if not exists participant(
    uuid varchar(36) not null primary key,
    email varchar(320) not null,
    password varchar(300) not null,
    service_action_token varchar(36),
    service_action_token_date timestamp(0) without time zone,
    locale varchar(8) default 'en' not null,
    timezone_offset integer default 10800000 not null,
    timezone_id varchar(36) default 'Europe/Volgograd' not null,
    is_superadmin boolean default false not null,
    receive_notifications boolean default true not null,
    info varchar(2000) not null,
    created_date timestamp(0) without time zone default CURRENT_TIMESTAMP() not null,
    modified_date timestamp(0) without time zone,
    modified_by varchar(36)
);

create unique index uc_participant_email_group on participant(email);

create table if not exists participant_group_link(
    group_uuid varchar(36) not null,
    participant_uuid varchar(36) not null,
    role varchar(30) default 'USER' not null,
    giftee_uuid varchar(36),
    wishes varchar(4000),
    created_date timestamp(0) without time zone default CURRENT_TIMESTAMP() not null,
    created_by varchar(36) not null,
    modified_date timestamp(0) without time zone,
    modified_by varchar(36)
);

alter table participant_group_link add primary key (group_uuid, participant_uuid);
alter table participant_group_link add constraint fk_participant_group_link_01
    foreign key (group_uuid) references participant_group (uuid);
alter table participant_group_link add constraint fk_participant_group_link_02
    foreign key (participant_uuid) references participant (uuid);
alter table participant_group_link add constraint fk_participant_group_link_03
    foreign key (role) references participant_role (id);
alter table participant_group_link add constraint fk_participant_group_link_04
    foreign key (giftee_uuid) references participant (uuid);
