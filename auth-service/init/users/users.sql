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

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.users_id_seq OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    id integer DEFAULT nextval('public.users_id_seq'::regclass) NOT NULL,
    first_name character varying NOT NULL,
    last_name character varying NOT NULL,
    email character varying NOT NULL,
    password character varying NOT NULL,
    date_of_birth date,
    street character varying,
    city character varying,
    postal_code integer
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (id, first_name, last_name, email, password, date_of_birth, street, city, postal_code) FROM stdin;
5	Miroslav	Kovacevic	miki@gmail.com	$2a$10$q5peirXSDmpKjyiAMVPTqep8MPMjYkUdbz7kMTq2tKUH0ZPuFJdUq	\N	Oml brigada	Beograd	11070
1	Dragan	Markovic	gagi@gmail.com	$2a$10$1dn3H/VyEr.9/xVBTpSgX.uDRAtMlBNQ31Xror6B037d43gRFoPo2	1974-12-31	Milutina Milankovica 95	Beograd	11070
21	Mewludin	Mandal	melko@gmail.com	$2a$10$hwOg5qwhbw4l/9oyt/t6SuoeuPxlqkbwPkwUEZoUq6QhPaM5FMC96	2025-10-09	Mose Pijade 23	Priboj	31330
8	Djordje	Kovacevic	djole@gmail.com	$2a$10$D4DWeBxLw0.HJGeQIkLRzu7kLtpdkQlKmxf3vZ9aMoUTtB9T2E.dy	1981-07-13	Clevelandska 25	Ljubljana	1251
4	Jovan	Vaduveskovic	joca@gmail.com	$2a$10$J2SnaIM7pkgpqhwQomFwL.BLH2oor2NPENoQc6ObSVljHvfi3IOkO	1988-07-15	Radnicka	Beograd	11000
\.


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.users_id_seq', 21, true);


--
-- Name: users users_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pk PRIMARY KEY (id);


--
-- PostgreSQL database dump complete
--

