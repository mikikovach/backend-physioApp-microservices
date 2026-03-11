--
-- PostgreSQL database dump
--

-- Dumped from database version 16.2
-- Dumped by pg_dump version 16.2

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: reservations_table; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.reservations_table (
    id bigint NOT NULL,
    user_id bigint,
    slot_id bigint,
    created_at timestamp without time zone
);


ALTER TABLE public.reservations_table OWNER TO postgres;

--
-- Name: reservations_table_reservation_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.reservations_table_reservation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.reservations_table_reservation_id_seq OWNER TO postgres;

--
-- Name: reservations_table_reservation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.reservations_table_reservation_id_seq OWNED BY public.reservations_table.id;


--
-- Name: reservations_table id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reservations_table ALTER COLUMN id SET DEFAULT nextval('public.reservations_table_reservation_id_seq'::regclass);


--
-- Data for Name: reservations_table; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.reservations_table (id, user_id, slot_id, created_at) FROM stdin;
\.


--
-- Name: reservations_table_reservation_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.reservations_table_reservation_id_seq', 53, true);


--
-- Name: reservations_table reservations_table_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reservations_table
    ADD CONSTRAINT reservations_table_pk PRIMARY KEY (id);


--
-- PostgreSQL database dump complete
--

