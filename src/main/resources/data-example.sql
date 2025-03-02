delete from participant_group_link;
delete from participant_group;
delete from participant;
delete from participant_role;

insert into participant_role values ('ADMIN');
insert into participant_role values ('USER');

-- password=0 for all users
-- List of users:
--  'admin@example.org' - has a superadmin role
--  10 ordinary users from 'user1@example.org' to 'user10@example.org'
insert into participant (
    uuid,
    email,
    password,
    service_action_token,
    service_action_token_date,
    locale,
    timezone_offset,
    timezone_id,
    is_superadmin,
    receive_notifications,
    info,
    created_date,
    modified_date,
    modified_by
)
values (
        '99bd192a-39d7-4af6-ba9d-86f0b6ea2dda',
        'admin@example.org',
        '$argon2id$v=19$m=16384,t=2,p=1$NWYT1dEA+PofixtG8pB6iw$MkXLTeEWCqm+3GhshchHGrOutKIBMb4NgIpckGh66yU',
        null,
        null,
        'en',
        10800000,
        'Europe/Moscow',
        true,
        false,
        'Conqueeftador',
        current_timestamp,
        current_timestamp,
        '99bd192a-39d7-4af6-ba9d-86f0b6ea2dda'
       );
insert into participant (
    uuid,
    email,
    password,
    service_action_token,
    service_action_token_date,
    locale,
    timezone_offset,
    timezone_id,
    is_superadmin,
    receive_notifications,
    info,
    created_date,
    modified_date,
    modified_by
)
values (
           'ec0af9b7-4176-4f9d-b4f1-938f9ae0e9ee',
           'user1@example.org',
           '$argon2id$v=19$m=16384,t=2,p=1$NWYT1dEA+PofixtG8pB6iw$MkXLTeEWCqm+3GhshchHGrOutKIBMb4NgIpckGh66yU',
           null,
           null,
           'en',
           10800000,
           'Europe/Moscow',
           false,
           false,
           'Steve Harvey',
           current_timestamp,
           current_timestamp,
           '99bd192a-39d7-4af6-ba9d-86f0b6ea2dda'
       );
insert into participant (
    uuid,
    email,
    password,
    service_action_token,
    service_action_token_date,
    locale,
    timezone_offset,
    timezone_id,
    is_superadmin,
    receive_notifications,
    info,
    created_date,
    modified_date,
    modified_by
)
values (
           '16f5878e-d947-4de3-b67e-5a346f528681',
           'user2@example.org',
           '$argon2id$v=19$m=16384,t=2,p=1$NWYT1dEA+PofixtG8pB6iw$MkXLTeEWCqm+3GhshchHGrOutKIBMb4NgIpckGh66yU',
           null,
           null,
           'en',
           10800000,
           'Europe/Moscow',
           false,
           false,
           'Chaz Lamar',
           current_timestamp,
           current_timestamp,
           '99bd192a-39d7-4af6-ba9d-86f0b6ea2dda'
       );
insert into participant (
    uuid,
    email,
    password,
    service_action_token,
    service_action_token_date,
    locale,
    timezone_offset,
    timezone_id,
    is_superadmin,
    receive_notifications,
    info,
    created_date,
    modified_date,
    modified_by
)
values (
           '0d48604e-e5c9-4636-83d9-094576118b96',
           'user3@example.org',
           '$argon2id$v=19$m=16384,t=2,p=1$NWYT1dEA+PofixtG8pB6iw$MkXLTeEWCqm+3GhshchHGrOutKIBMb4NgIpckGh66yU',
           null,
           null,
           'en',
           10800000,
           'Europe/Moscow',
           false,
           false,
           'Wayne Collins',
           current_timestamp,
           current_timestamp,
           '99bd192a-39d7-4af6-ba9d-86f0b6ea2dda'
       );
insert into participant (
    uuid,
    email,
    password,
    service_action_token,
    service_action_token_date,
    locale,
    timezone_offset,
    timezone_id,
    is_superadmin,
    receive_notifications,
    info,
    created_date,
    modified_date,
    modified_by
)
values (
           '5663b2f8-68bb-4f54-bf8e-2c8d8b6215c7',
           'user4@example.org',
           '$argon2id$v=19$m=16384,t=2,p=1$NWYT1dEA+PofixtG8pB6iw$MkXLTeEWCqm+3GhshchHGrOutKIBMb4NgIpckGh66yU',
           null,
           null,
           'en',
           10800000,
           'Europe/Moscow',
           false,
           false,
           'Benjamin LeVert',
           current_timestamp,
           current_timestamp,
           '99bd192a-39d7-4af6-ba9d-86f0b6ea2dda'
       );
insert into participant (
    uuid,
    email,
    password,
    service_action_token,
    service_action_token_date,
    locale,
    timezone_offset,
    timezone_id,
    is_superadmin,
    receive_notifications,
    info,
    created_date,
    modified_date,
    modified_by
)
values (
           '85be01fd-73b2-460a-8c0f-2b0019163183',
           'user5@example.org',
           '$argon2id$v=19$m=16384,t=2,p=1$NWYT1dEA+PofixtG8pB6iw$MkXLTeEWCqm+3GhshchHGrOutKIBMb4NgIpckGh66yU',
           null,
           null,
           'en',
           10800000,
           'Europe/Moscow',
           false,
           false,
           'Madge Sinclair',
           current_timestamp,
           current_timestamp,
           '99bd192a-39d7-4af6-ba9d-86f0b6ea2dda'
       );
insert into participant (
    uuid,
    email,
    password,
    service_action_token,
    service_action_token_date,
    locale,
    timezone_offset,
    timezone_id,
    is_superadmin,
    receive_notifications,
    info,
    created_date,
    modified_date,
    modified_by
)
values (
           '400d7b13-22dc-4658-a349-9c6e41601091',
           'user6@example.org',
           '$argon2id$v=19$m=16384,t=2,p=1$NWYT1dEA+PofixtG8pB6iw$MkXLTeEWCqm+3GhshchHGrOutKIBMb4NgIpckGh66yU',
           null,
           null,
           'en',
           10800000,
           'Europe/Moscow',
           false,
           false,
           'Beavis',
           current_timestamp,
           current_timestamp,
           '99bd192a-39d7-4af6-ba9d-86f0b6ea2dda'
       );
insert into participant (
    uuid,
    email,
    password,
    service_action_token,
    service_action_token_date,
    locale,
    timezone_offset,
    timezone_id,
    is_superadmin,
    receive_notifications,
    info,
    created_date,
    modified_date,
    modified_by
)
values (
           'bf4a0874-0d87-4856-a486-5c914f1c682c',
           'user7@example.org',
           '$argon2id$v=19$m=16384,t=2,p=1$NWYT1dEA+PofixtG8pB6iw$MkXLTeEWCqm+3GhshchHGrOutKIBMb4NgIpckGh66yU',
           null,
           null,
           'en',
           10800000,
           'Europe/Moscow',
           false,
           false,
           'Butthead',
           current_timestamp,
           current_timestamp,
           '99bd192a-39d7-4af6-ba9d-86f0b6ea2dda'
       );
insert into participant (
    uuid,
    email,
    password,
    service_action_token,
    service_action_token_date,
    locale,
    timezone_offset,
    timezone_id,
    is_superadmin,
    receive_notifications,
    info,
    created_date,
    modified_date,
    modified_by
)
values (
           '4c9eb71a-07a5-4aba-9194-d2a762c421b4',
           'user8@example.org',
           '$argon2id$v=19$m=16384,t=2,p=1$NWYT1dEA+PofixtG8pB6iw$MkXLTeEWCqm+3GhshchHGrOutKIBMb4NgIpckGh66yU',
           null,
           null,
           'en',
           10800000,
           'Europe/Moscow',
           false,
           false,
           'Jerry-Daphne',
           current_timestamp,
           current_timestamp,
           '99bd192a-39d7-4af6-ba9d-86f0b6ea2dda'
       );
insert into participant (
    uuid,
    email,
    password,
    service_action_token,
    service_action_token_date,
    locale,
    timezone_offset,
    timezone_id,
    is_superadmin,
    receive_notifications,
    info,
    created_date,
    modified_date,
    modified_by
)
values (
           '48ee5636-f1a6-492c-8077-eca1f720a18a',
           'user9@example.org',
           '$argon2id$v=19$m=16384,t=2,p=1$NWYT1dEA+PofixtG8pB6iw$MkXLTeEWCqm+3GhshchHGrOutKIBMb4NgIpckGh66yU',
           null,
           null,
           'en',
           10800000,
           'Europe/Moscow',
           false,
           false,
           'Joe-Josephine',
           current_timestamp,
           current_timestamp,
           '99bd192a-39d7-4af6-ba9d-86f0b6ea2dda'
       );
insert into participant (
    uuid,
    email,
    password,
    service_action_token,
    service_action_token_date,
    locale,
    timezone_offset,
    timezone_id,
    is_superadmin,
    receive_notifications,
    info,
    created_date,
    modified_date,
    modified_by
)
values (
        '809243f5-d561-4cfc-9b95-a68adc2396a1',
        'user10@example.org',
        '$argon2id$v=19$m=16384,t=2,p=1$NWYT1dEA+PofixtG8pB6iw$MkXLTeEWCqm+3GhshchHGrOutKIBMb4NgIpckGh66yU',
        null,
        null,
        'en',
        10800000,
        'Europe/Moscow',
        false,
        false,
        'Sugar "Kane"',
        current_timestamp,
        current_timestamp,
        '99bd192a-39d7-4af6-ba9d-86f0b6ea2dda'
);

-- List of groups --
insert into participant_group(
    uuid,
    description,
    has_drawn,
    created_date,
    created_by,
    modified_date,
    modified_by
)
values (
           '933187f2-4420-4789-82dc-2c170f1a6ede',
           'Me and the Boys',
           false,
           current_timestamp,
           '99bd192a-39d7-4af6-ba9d-86f0b6ea2dda',
           current_timestamp,
           '99bd192a-39d7-4af6-ba9d-86f0b6ea2dda'
       );
insert into participant_group(
    uuid,
    description,
    has_drawn,
    created_date,
    created_by,
    modified_date,
    modified_by
)
values (
           'fd81c4da-23c6-4b49-b36a-f135dc88a60c',
           'Four and a half',
           false,
           current_timestamp,
           '99bd192a-39d7-4af6-ba9d-86f0b6ea2dda',
           current_timestamp,
           '99bd192a-39d7-4af6-ba9d-86f0b6ea2dda'
       );
insert into participant_group(
    uuid,
    description,
    has_drawn,
    created_date,
    created_by,
    modified_date,
    modified_by
)
values (
           'dab7e383-1fb8-40ea-b46a-34f790038c03',
           'Some Like It Hot',
           false,
           current_timestamp,
           '99bd192a-39d7-4af6-ba9d-86f0b6ea2dda',
           current_timestamp,
           '99bd192a-39d7-4af6-ba9d-86f0b6ea2dda'
       );

-- Links --
insert into participant_group_link(
    group_uuid,
    participant_uuid,
    role,
    giftee_uuid,
    wishes,
    created_date,
    created_by,
    modified_date,
    modified_by
)
values(
       '933187f2-4420-4789-82dc-2c170f1a6ede',
       'ec0af9b7-4176-4f9d-b4f1-938f9ae0e9ee',
       'ADMIN',
       null,
       '',
       current_timestamp,
       '99bd192a-39d7-4af6-ba9d-86f0b6ea2dda',
       current_timestamp,
       '99bd192a-39d7-4af6-ba9d-86f0b6ea2dda'
      );
insert into participant_group_link(
    group_uuid,
    participant_uuid,
    role,
    giftee_uuid,
    wishes,
    created_date,
    created_by,
    modified_date,
    modified_by
)
values(
          '933187f2-4420-4789-82dc-2c170f1a6ede',
          '16f5878e-d947-4de3-b67e-5a346f528681',
          'USER',
          null,
          '',
          current_timestamp,
          '99bd192a-39d7-4af6-ba9d-86f0b6ea2dda',
          current_timestamp,
          '99bd192a-39d7-4af6-ba9d-86f0b6ea2dda'
      );
insert into participant_group_link(
    group_uuid,
    participant_uuid,
    role,
    giftee_uuid,
    wishes,
    created_date,
    created_by,
    modified_date,
    modified_by
)
values(
          '933187f2-4420-4789-82dc-2c170f1a6ede',
          '0d48604e-e5c9-4636-83d9-094576118b96',
          'USER',
          null,
          '',
          current_timestamp,
          '99bd192a-39d7-4af6-ba9d-86f0b6ea2dda',
          current_timestamp,
          '99bd192a-39d7-4af6-ba9d-86f0b6ea2dda'
      );
insert into participant_group_link(
    group_uuid,
    participant_uuid,
    role,
    giftee_uuid,
    wishes,
    created_date,
    created_by,
    modified_date,
    modified_by
)
values(
          '933187f2-4420-4789-82dc-2c170f1a6ede',
          '5663b2f8-68bb-4f54-bf8e-2c8d8b6215c7',
          'USER',
          null,
          '',
          current_timestamp,
          '99bd192a-39d7-4af6-ba9d-86f0b6ea2dda',
          current_timestamp,
          '99bd192a-39d7-4af6-ba9d-86f0b6ea2dda'
      );
insert into participant_group_link(
    group_uuid,
    participant_uuid,
    role,
    giftee_uuid,
    wishes,
    created_date,
    created_by,
    modified_date,
    modified_by
)
values(
          '933187f2-4420-4789-82dc-2c170f1a6ede',
          '85be01fd-73b2-460a-8c0f-2b0019163183',
          'USER',
          null,
          '',
          current_timestamp,
          '99bd192a-39d7-4af6-ba9d-86f0b6ea2dda',
          current_timestamp,
          '99bd192a-39d7-4af6-ba9d-86f0b6ea2dda'
      );

insert into participant_group_link(
    group_uuid,
    participant_uuid,
    role,
    giftee_uuid,
    wishes,
    created_date,
    created_by,
    modified_date,
    modified_by
)
values(
          'fd81c4da-23c6-4b49-b36a-f135dc88a60c',
          '400d7b13-22dc-4658-a349-9c6e41601091',
          'USER',
          null,
          '',
          current_timestamp,
          '99bd192a-39d7-4af6-ba9d-86f0b6ea2dda',
          current_timestamp,
          '99bd192a-39d7-4af6-ba9d-86f0b6ea2dda'
      );
insert into participant_group_link(
    group_uuid,
    participant_uuid,
    role,
    giftee_uuid,
    wishes,
    created_date,
    created_by,
    modified_date,
    modified_by
)
values(
          'fd81c4da-23c6-4b49-b36a-f135dc88a60c',
          'bf4a0874-0d87-4856-a486-5c914f1c682c',
          'USER',
          null,
          '',
          current_timestamp,
          '99bd192a-39d7-4af6-ba9d-86f0b6ea2dda',
          current_timestamp,
          '99bd192a-39d7-4af6-ba9d-86f0b6ea2dda'
      );

insert into participant_group_link(
    group_uuid,
    participant_uuid,
    role,
    giftee_uuid,
    wishes,
    created_date,
    created_by,
    modified_date,
    modified_by
)
values(
          'dab7e383-1fb8-40ea-b46a-34f790038c03',
          '809243f5-d561-4cfc-9b95-a68adc2396a1',
          'ADMIN',
          null,
          '',
          current_timestamp,
          '99bd192a-39d7-4af6-ba9d-86f0b6ea2dda',
          current_timestamp,
          '99bd192a-39d7-4af6-ba9d-86f0b6ea2dda'
      );
insert into participant_group_link(
    group_uuid,
    participant_uuid,
    role,
    giftee_uuid,
    wishes,
    created_date,
    created_by,
    modified_date,
    modified_by
)
values(
          'dab7e383-1fb8-40ea-b46a-34f790038c03',
          '4c9eb71a-07a5-4aba-9194-d2a762c421b4',
          'USER',
          null,
          '',
          current_timestamp,
          '99bd192a-39d7-4af6-ba9d-86f0b6ea2dda',
          current_timestamp,
          '99bd192a-39d7-4af6-ba9d-86f0b6ea2dda'
      );
insert into participant_group_link(
    group_uuid,
    participant_uuid,
    role,
    giftee_uuid,
    wishes,
    created_date,
    created_by,
    modified_date,
    modified_by
)
values(
          'dab7e383-1fb8-40ea-b46a-34f790038c03',
          '48ee5636-f1a6-492c-8077-eca1f720a18a',
          'USER',
          null,
          '',
          current_timestamp,
          '99bd192a-39d7-4af6-ba9d-86f0b6ea2dda',
          current_timestamp,
          '99bd192a-39d7-4af6-ba9d-86f0b6ea2dda'
      );