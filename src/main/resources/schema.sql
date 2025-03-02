create table participant_role(
    id varchar2(30) not null primary key
);

create table participant_group(
    uuid varchar2(36) not null primary key,
    description varchar2(100),
    has_drawn numeric(1) default 0 not null
);

create unique index uc_group_description on participant_group(description);

create table participant(
    uuid varchar2(36) not null primary key,
    group_uuid varchar2(36) not null,
    role varchar2(30) default 'USER' not null,
    participant_to_gift_to varchar2(36),
    email varchar2(250) not null,
    password varchar2(36) not null,
    receive_notifications numeric(1) default 1 not null,
    info varchar2(2000) not null,
    wishes varchar2(2000)
);

create unique index uc_participant_email on participant(email);