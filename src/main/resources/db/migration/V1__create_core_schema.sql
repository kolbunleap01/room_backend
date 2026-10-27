create table users (
    id uuid primary key,
    email varchar(320) not null unique,
    password_hash varchar(100) not null,
    full_name varchar(120) not null,
    phone varchar(30),
    role varchar(20) not null check (role in ('USER', 'ADMIN')),
    enabled boolean not null default true,
    failed_login_attempts integer not null default 0,
    locked_until timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create index idx_users_email_lower on users (lower(email));

create table refresh_tokens (
    id uuid primary key,
    user_id uuid not null references users(id) on delete cascade,
    token_hash varchar(64) not null unique,
    expires_at timestamptz not null,
    created_at timestamptz not null,
    revoked_at timestamptz,
    replaced_by_token_id uuid
);

create index idx_refresh_tokens_user_id on refresh_tokens(user_id);
create index idx_refresh_tokens_expires_at on refresh_tokens(expires_at);

create table rooms (
    id uuid primary key,
    owner_id uuid not null references users(id),
    title varchar(160) not null,
    description varchar(5000) not null,
    address varchar(255) not null,
    city varchar(100) not null,
    district varchar(100),
    monthly_rent numeric(12,2) not null check (monthly_rent > 0),
    deposit numeric(12,2) check (deposit >= 0),
    bedrooms integer not null check (bedrooms >= 0),
    bathrooms integer not null check (bathrooms >= 1),
    area_sqm numeric(10,2) check (area_sqm > 0),
    furnished boolean not null default false,
    available_from date,
    status varchar(20) not null check (status in ('ACTIVE', 'INACTIVE')),
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create index idx_rooms_active_created on rooms(status, created_at desc);
create index idx_rooms_owner on rooms(owner_id, created_at desc);
create index idx_rooms_city on rooms(lower(city));

create table favorites (
    user_id uuid not null references users(id) on delete cascade,
    room_id uuid not null references rooms(id) on delete cascade,
    created_at timestamptz not null,
    primary key (user_id, room_id)
);

create index idx_favorites_user_created on favorites(user_id, created_at desc);

create table rental_requests (
    id uuid primary key,
    room_id uuid not null references rooms(id),
    applicant_id uuid not null references users(id),
    owner_id uuid not null references users(id),
    move_in_date date,
    message varchar(2000),
    status varchar(20) not null check (status in ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED')),
    decision_note varchar(2000),
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create index idx_rental_requests_applicant on rental_requests(applicant_id, created_at desc);
create index idx_rental_requests_owner on rental_requests(owner_id, created_at desc);
create unique index uq_pending_rental_request on rental_requests(room_id, applicant_id) where status = 'PENDING';

create table audit_logs (
    id uuid primary key,
    actor_user_id uuid references users(id) on delete set null,
    event_type varchar(80) not null,
    ip_address varchar(64),
    details varchar(500),
    created_at timestamptz not null
);

create index idx_audit_logs_created on audit_logs(created_at desc);
create index idx_audit_logs_actor on audit_logs(actor_user_id, created_at desc);
