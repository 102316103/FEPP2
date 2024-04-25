DELETE FROM BATCH_FIRED_TRIGGERS;
DELETE FROM BATCH_SIMPLE_TRIGGERS;
DELETE FROM BATCH_SIMPROP_TRIGGERS;
DELETE FROM BATCH_CRON_TRIGGERS;
DELETE FROM BATCH_BLOB_TRIGGERS;
DELETE FROM BATCH_TRIGGERS;
DELETE FROM BATCH_JOB_DETAILS;
DELETE FROM BATCH_CALENDARS;
DELETE FROM BATCH_PAUSED_TRIGGER_GRPS;
DELETE FROM BATCH_LOCKS;
DELETE FROM BATCH_SCHEDULER_STATE;

DROP TABLE BATCH_FIRED_TRIGGERS;
DROP TABLE BATCH_PAUSED_TRIGGER_GRPS;
DROP TABLE BATCH_SCHEDULER_STATE;
DROP TABLE BATCH_LOCKS;
DROP TABLE BATCH_SIMPLE_TRIGGERS;
DROP TABLE BATCH_SIMPROP_TRIGGERS;
DROP TABLE BATCH_CRON_TRIGGERS;
DROP TABLE BATCH_TRIGGERS;
DROP TABLE BATCH_JOB_DETAILS;
DROP TABLE BATCH_CALENDARS;
DROP TABLE BATCH_BLOB_TRIGGERS;

create table batch_job_details(
sched_name varchar(120) not null,
job_name varchar(80) not null,
job_group varchar(80) not null,
description varchar(120),
job_class_name varchar(128) not null,
is_durable integer not null,
is_nonconcurrent integer not null,
is_update_data integer not null,
requests_recovery integer not null,
job_data blob(2000),
CONSTRAINT pk_batch_job_details primary key (sched_name,job_name,job_group)
);

create table batch_triggers(
sched_name varchar(120) not null,
trigger_name varchar(80) not null,
trigger_group varchar(80) not null,
job_name varchar(80) not null,
job_group varchar(80) not null,
description varchar(120),
next_fire_time bigint,
prev_fire_time bigint,
priority integer,
trigger_state varchar(16) not null,
trigger_type varchar(8) not null,
start_time bigint not null,
end_time bigint,
calendar_name varchar(80),
misfire_instr smallint,
job_data blob(2000),
CONSTRAINT pk_batch_triggers primary key (sched_name,trigger_name,trigger_group),
CONSTRAINT fk_batch_triggers foreign key (sched_name,job_name,job_group) references batch_job_details(sched_name,job_name,job_group)
);

create table batch_simple_triggers(
sched_name varchar(120) not null,
trigger_name varchar(80) not null,
trigger_group varchar(80) not null,
repeat_count bigint not null,
repeat_interval bigint not null,
times_triggered bigint not null,
CONSTRAINT pk_batch_simple_triggers primary key (sched_name,trigger_name,trigger_group),
CONSTRAINT fk_batch_simple_triggers foreign key (sched_name,trigger_name,trigger_group) references batch_triggers(sched_name,trigger_name,trigger_group)
);

create table batch_cron_triggers(
sched_name varchar(120) not null,
trigger_name varchar(80) not null,
trigger_group varchar(80) not null,
cron_expression varchar(120) not null,
time_zone_id varchar(80),
CONSTRAINT pk_batch_cron_triggers primary key (sched_name,trigger_name,trigger_group),
CONSTRAINT fk_batch_cron_triggers foreign key (sched_name,trigger_name,trigger_group) references batch_triggers(sched_name,trigger_name,trigger_group)
);

CREATE TABLE batch_simprop_triggers
  (          
    sched_name varchar(120) not null,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    STR_PROP_1 VARCHAR(512),
    STR_PROP_2 VARCHAR(512),
    STR_PROP_3 VARCHAR(512),
    INT_PROP_1 INT,
    INT_PROP_2 INT,
    LONG_PROP_1 BIGINT,
    LONG_PROP_2 BIGINT,
    DEC_PROP_1 NUMERIC(13,4),
    DEC_PROP_2 NUMERIC(13,4),
    BOOL_PROP_1 VARCHAR(1),
    BOOL_PROP_2 VARCHAR(1),
    CONSTRAINT pk_batch_simprop_triggers PRIMARY KEY (sched_name,TRIGGER_NAME,TRIGGER_GROUP),
    CONSTRAINT fk_batch_simprop_triggers FOREIGN KEY (sched_name,TRIGGER_NAME,TRIGGER_GROUP) 
    REFERENCES batch_TRIGGERS(sched_name,TRIGGER_NAME,TRIGGER_GROUP)
);

create table batch_blob_triggers(
sched_name varchar(120) not null,
trigger_name varchar(80) not null,
trigger_group varchar(80) not null,
blob_data blob(2000),
CONSTRAINT pk_batch_blob_triggers primary key (sched_name,trigger_name,trigger_group),
CONSTRAINT fk_batch_blob_triggers foreign key (sched_name,trigger_name,trigger_group) references batch_triggers(sched_name,trigger_name,trigger_group)
);

create table batch_calendars(
sched_name varchar(120) not null,
calendar_name varchar(80) not null,
calendar blob(2000) not null,
CONSTRAINT pk_batch_calendars primary key (calendar_name)
);

create table batch_fired_triggers(
sched_name varchar(120) not null,
entry_id varchar(95) not null,
trigger_name varchar(80) not null,
trigger_group varchar(80) not null,
instance_name varchar(80) not null,
fired_time bigint not null,
sched_time bigint not null,
priority integer not null,
state varchar(16) not null,
job_name varchar(80),
job_group varchar(80),
is_nonconcurrent integer,
requests_recovery integer,
CONSTRAINT pk_batch_fired_triggers primary key (sched_name,entry_id)
);

create table batch_paused_trigger_grps(
sched_name varchar(120) not null,
trigger_group varchar(80) not null,
CONSTRAINT pk_batch_paused_trigger_grps primary key (sched_name,trigger_group)
);

create table batch_scheduler_state(
sched_name varchar(120) not null,
instance_name varchar(80) not null,
last_checkin_time bigint not null,
checkin_interval bigint not null,
CONSTRAINT pk_batch_scheduler_state primary key (sched_name,instance_name)
);

create table batch_locks(
sched_name varchar(120) not null,
lock_name varchar(40) not null,
CONSTRAINT pk_batch_locks primary key (sched_name,lock_name)
);
