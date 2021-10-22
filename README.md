# Spring Webflux

### SQL

```
CREATE TABLE anime.animes (
	id serial NOT NULL,
	"name" varchar NOT NULL,
	
	CONSTRAINT anime_pk PRIMARY KEY (id)
);

CREATE TABLE anime.users (
	id serial not null,
	name varchar not null,
	username varchar(100) not null,
	password varchar not null,
	authorities varchar(150) not null,
	
    UNIQUE(username),
    CONSTRAINT users_pk PRIMARY KEY (id)
);
```

### JWT

- Create class SecurityConfig
- 




