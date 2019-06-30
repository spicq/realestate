CREATE TABLE REALESTATE
(
  ID VARCHAR(32) NOT NULL,
  POST_CODE VARCHAR(5),
  SURFACE DOUBLE PRECISION,
  PRICE DOUBLE PRECISION,
  PRICE_PER_M2 DOUBLE PRECISION,
  NUM_ROOMS SMALLINT,
  NUM_BED_ROOMS SMALLINT,
  PROPERTY_TYPE_AS_STRING VARCHAR(32),
  DETAIL_URL VARCHAR(2048),
  title VARCHAR(256),
  CONSTRAINT REALESTATE_PKEY PRIMARY KEY (ID)
);