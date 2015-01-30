# --- Created by Slick DDL
# To stop Slick DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table "OAUTH_APPS" ("id" VARCHAR NOT NULL PRIMARY KEY,"secret" VARCHAR NOT NULL,"user_id" VARCHAR NOT NULL);
create table "OAUTH_REDIRECT_URL" ("id" BIGINT NOT NULL PRIMARY KEY,"app_id" VARCHAR NOT NULL);
create table "USERS" ("email" VARCHAR NOT NULL PRIMARY KEY,"full_name" VARCHAR NOT NULL,"pass_hash" CHAR(256) NOT NULL);
alter table "OAUTH_APPS" add constraint "fk_user_id" foreign key("user_id") references "USERS"("email") on update NO ACTION on delete NO ACTION;
alter table "OAUTH_REDIRECT_URL" add constraint "fk_app_id" foreign key("app_id") references "OAUTH_APPS"("id") on update NO ACTION on delete NO ACTION;

# --- !Downs

alter table "OAUTH_REDIRECT_URL" drop constraint "fk_app_id";
alter table "OAUTH_APPS" drop constraint "fk_user_id";
drop table "USERS";
drop table "OAUTH_REDIRECT_URL";
drop table "OAUTH_APPS";

