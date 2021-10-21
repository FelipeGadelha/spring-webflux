CREATE TABLE IF NOT EXISTS anime (
	id serial not null,
	name varchar not null,
    primary key (id),
    UNIQUE (id, name)
);