# --- Created by Slick DDL
# To stop Slick DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table "ALBUMS" ("name" VARCHAR NOT NULL,"description" VARCHAR NOT NULL,"year" INTEGER NOT NULL,"artist_id" BIGINT NOT NULL,"id" BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY);
alter table "ALBUMS" add constraint "artist_fk" foreign key("artist_id") references "ALBUMS"("id") on update NO ACTION on delete NO ACTION;

# --- !Downs

alter table "ALBUMS" drop constraint "artist_fk";
drop table "ALBUMS";
