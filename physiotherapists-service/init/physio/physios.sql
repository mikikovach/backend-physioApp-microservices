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
-- Name: physiotherapists; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.physiotherapists (
    id bigint NOT NULL,
    first_name character varying(100) NOT NULL,
    last_name character varying(100) NOT NULL,
    specialization character varying(255)
);


ALTER TABLE public.physiotherapists OWNER TO postgres;

--
-- Name: physiotherapists_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.physiotherapists_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.physiotherapists_id_seq OWNER TO postgres;

--
-- Name: physiotherapists_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.physiotherapists_id_seq OWNED BY public.physiotherapists.id;


--
-- Name: physiotherapists id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.physiotherapists ALTER COLUMN id SET DEFAULT nextval('public.physiotherapists_id_seq'::regclass);


--
-- Data for Name: physiotherapists; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.physiotherapists (id, first_name, last_name, specialization) FROM stdin;
1	Nemanja	Bukvic	sports medicine
2	Verica	Bukvic	massage
3	Krsman	Pantovic	sports medicine
4	Zvezdana	Petkovic	kinesio therapy
5	Milos	Zivkovic	RSQ
\.


--
-- Name: physiotherapists_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.physiotherapists_id_seq', 5, true);


--
-- Name: physiotherapists physiotherapists_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.physiotherapists
    ADD CONSTRAINT physiotherapists_pkey PRIMARY KEY (id);


--
-- PostgreSQL database dump complete
--

