-- Time warp 1997-1998 to 2018-2019

UPDATE currency SET "DATE" = ADD_MONTHS("DATE", 21*12);
UPDATE promotion SET start_date = ADD_MONTHS(start_date, 21*12);
UPDATE expense_fact SET exp_date = ADD_MONTHS(exp_date, 21*12);
UPDATE agg_g_ms_pcat_sales_fact_1997 SET the_year = the_year + 21;
UPDATE time_by_day SET the_year = the_year + 21;
UPDATE reserve_employee SET hire_date = ADD_MONTHS(hire_date, 21*12);
UPDATE employee SET hire_date = ADD_MONTHS(hire_date, 21*12);
UPDATE time_by_day SET the_date = ADD_MONTHS(the_date, 21*12);
UPDATE agg_lc_100_sales_fact_1997 SET the_year = the_year + 21;
UPDATE agg_c_10_sales_fact_1997 SET the_year = the_year + 21;
UPDATE agg_c_14_sales_fact_1997 SET the_year = the_year + 21;
UPDATE agg_c_special_sales_fact_1997 SET time_year = time_year + 21;
UPDATE store SET first_opened_date = ADD_MONTHS(first_opened_date, 15*12);
UPDATE store SET last_remodel_date = ADD_MONTHS(last_remodel_date, 15*12);
UPDATE store_ragged SET first_opened_date = ADD_MONTHS(first_opened_date, 15*12);
UPDATE store_ragged SET last_remodel_date = ADD_MONTHS(last_remodel_date, 15*12);

-- add numeric day of week field to time_by_day

ALTER TABLE time_by_day ADD day_of_week int;
UPDATE time_by_day SET day_of_week = 1 WHERE the_day = 'Monday';
UPDATE time_by_day SET day_of_week = 2 WHERE the_day = 'Tuesday';
UPDATE time_by_day SET day_of_week = 3 WHERE the_day = 'Wednesday';
UPDATE time_by_day SET day_of_week = 4 WHERE the_day = 'Thursday';
UPDATE time_by_day SET day_of_week = 5 WHERE the_day = 'Friday';
UPDATE time_by_day SET day_of_week = 6 WHERE the_day = 'Saturday';
UPDATE time_by_day SET day_of_week = 7 WHERE the_day = 'Sunday';

--
--
-- Updates to make SuperMart reports query execution faster
--
-- ([^])(time_id)

CREATE TABLE
     monthly_profit AS
SELECT
        min(time_by_day.the_date) as the_date,
        time_by_day.the_year AS the_year,
        time_by_day.the_month AS the_month,
        time_by_day.quarter AS the_quarter,
        time_by_day.month_of_year AS time_by_day_month_of_year,
        sum(sales_fact_1997.store_sales) AS store_sales,
        sum(sales_fact_1997.store_sales)-sum(sales_fact_1997.store_cost) AS profit,
    store.store_state AS store_state
FROM
        sales_fact_1997 sales_fact_1997 INNER JOIN store ON sales_fact_1997.store_id = store.store_id
        INNER JOIN time_by_day time_by_day ON sales_fact_1997.time_id = time_by_day.time_id
WHERE
        store.store_state IS NOT NULL /* when param PickState contains ALL use NULL otherwise use PickState */
GROUP BY
        time_by_day.the_year,
        time_by_day.the_month,
        time_by_day.quarter,
        time_by_day.month_of_year,
        store.store_state;

CREATE TABLE
     product_sales AS
SELECT
     product.product_name AS product_product_name,
     sum(sales_fact_1997.store_sales)AS store_sales,
     product.product_id AS product_product_id
FROM
     sales_fact_1997 sales_fact_1997,
     product product
WHERE
     sales_fact_1997.product_id = product.product_id
GROUP BY
     product.product_name,
     product.product_id;

CREATE TABLE
     customer_sales AS
SELECT
     customer.fullname AS customer_fullname,
     sum(sales_fact_1997.store_sales)AS store_sales,
     customer.customer_id AS customer_id
FROM
     sales_fact_1997 sales_fact_1997,
     customer customer
WHERE
     sales_fact_1997.customer_id = customer.customer_id
GROUP BY
     customer.fullname,
     customer.customer_id;

CREATE TABLE
     promotion_sales AS
SELECT
     promotion.promotion_name AS promotion_promotion_name,
     sum(sales_fact_1997.store_sales)AS store_sales,
     promotion.promotion_id AS promotion_promotion_id
FROM
     sales_fact_1997 sales_fact_1997,
     promotion promotion
WHERE
     sales_fact_1997.promotion_id = promotion.promotion_id
GROUP BY
     promotion.promotion_name,
     promotion.promotion_id
HAVING
     promotion.promotion_name <> 'No Promotion';