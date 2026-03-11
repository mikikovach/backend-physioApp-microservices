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
-- Name: appointment_slots; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.appointment_slots (
    id bigint NOT NULL,
    start_time timestamp without time zone,
    reserved boolean,
    physio_id bigint
);


ALTER TABLE public.appointment_slots OWNER TO postgres;

--
-- Name: appointment_slots_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.appointment_slots_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.appointment_slots_id_seq OWNER TO postgres;

--
-- Name: appointment_slots_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.appointment_slots_id_seq OWNED BY public.appointment_slots.id;


--
-- Name: appointment_slots id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.appointment_slots ALTER COLUMN id SET DEFAULT nextval('public.appointment_slots_id_seq'::regclass);


--
-- Data for Name: appointment_slots; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.appointment_slots (id, start_time, reserved, physio_id) FROM stdin;
8	2026-02-06 12:00:00	f	4
9	2026-02-04 15:00:00	f	4
19	2026-02-04 16:00:00	f	4
7	2026-02-05 15:00:00	f	3
15	2026-02-05 12:00:00	f	4
2	2026-02-09 12:00:00	f	3
3	2026-02-09 15:00:00	f	3
4	2026-02-09 16:00:00	f	3
11	2026-02-10 11:00:00	f	4
14	2026-02-10 15:00:00	f	4
18	2026-02-09 16:00:00	f	4
16	2026-02-09 13:00:00	f	4
13	2026-02-09 11:00:00	f	4
10	2026-02-09 15:00:00	f	4
17	2026-02-05 10:00:00	f	4
5	2026-02-06 13:00:00	f	3
1	2026-02-06 12:00:00	f	3
6	2026-02-06 14:00:00	f	3
12	2026-02-06 09:00:00	f	4
\.


--
-- Name: appointment_slots_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.appointment_slots_id_seq', 19, true);


--
-- Name: appointment_slots appointment_slots_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.appointment_slots
    ADD CONSTRAINT appointment_slots_pk PRIMARY KEY (id);


--
-- PostgreSQL database dump complete
--

