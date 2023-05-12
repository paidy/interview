CREATE TABLE rates_cache (
  id           SERIAL      PRIMARY KEY,
  "from"       VARCHAR(3)  NOT NULL,
  "to"         VARCHAR(3)  NOT NULL,
  price        NUMERIC     NOT NULL,
  "timestamp"  TIMESTAMP   NOT NULL
);

CREATE UNIQUE INDEX idx_rates_cache_from_to on rates_cache ("from", "to");
