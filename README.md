# Spring Webflux

### SQL

```
CREATE TABLE anime.anime (
	id serial NOT NULL,
	"name" varchar NOT NULL,
	CONSTRAINT anime_pk PRIMARY KEY (id)
);
CREATE UNIQUE INDEX anime_id_uindex ON anime.anime USING btree (id);
```


