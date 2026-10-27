create table login_attempts (
    id uuid primary key,
    user_id uuid references users(id) on delete set null,
    email varchar(320) not null,
    ip_address varchar(64),
    status varchar(20) not null check (status in ('SUCCESS', 'FAILED', 'BLOCKED')),
    details varchar(500),
    created_at timestamptz not null
);

create index idx_login_attempts_user_created on login_attempts(user_id, created_at desc);
create index idx_login_attempts_email_created on login_attempts(lower(email), created_at desc);
create index idx_login_attempts_status_created on login_attempts(status, created_at desc);

create table security_events (
    id uuid primary key,
    actor_user_id uuid references users(id) on delete set null,
    event_type varchar(80) not null,
    ip_address varchar(64),
    status varchar(20) not null check (status in ('SUCCESS', 'FAILURE', 'INFO')),
    details varchar(500),
    created_at timestamptz not null
);

create index idx_security_events_created on security_events(created_at desc);
create index idx_security_events_actor_created on security_events(actor_user_id, created_at desc);
create index idx_security_events_type_created on security_events(event_type, created_at desc);
create index idx_security_events_status_created on security_events(status, created_at desc);
