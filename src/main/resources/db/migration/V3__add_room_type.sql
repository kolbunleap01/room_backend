alter table rooms add column room_type varchar(20);

update rooms
set room_type = 'PRIVATE_ROOM'
where room_type is null;

alter table rooms alter column room_type set not null;

alter table rooms
    add constraint ck_rooms_room_type
    check (room_type in ('PRIVATE_ROOM', 'SHARED_ROOM', 'STUDIO', 'APARTMENT'));
