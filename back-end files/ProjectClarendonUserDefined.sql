USE project_clarendon;

DROP PROCEDURE IF EXISTS initialize_avg_rating;
DELIMITER //
CREATE PROCEDURE initialize_avg_rating(IN input_ticker_name VARCHAR(50))
	BEGIN
        UPDATE company_info
			SET average_rating = (SELECT AVG(rating_out_of_5) FROM analyst_ratings WHERE rating_ticker = input_ticker_name)
            WHERE ticker_name = input_ticker_name;
    END //
DELIMITER ;

/*
	This trigger activates after there is an insertion in the analyst ratings table
*/
DROP TRIGGER IF EXISTS avg_rating_after_insert;
DELIMITER $$ 
CREATE TRIGGER avg_rating_after_insert AFTER INSERT
	ON analyst_ratings FOR EACH ROW
		BEGIN 
			CALL initialize_avg_rating(NEW.rating_ticker);
		END $$
DELIMITER ;

/*
	This procedure yields a basic financial report on an input stock symbol
*/
DROP PROCEDURE IF EXISTS build_basic_report;
DELIMITER $$
CREATE PROCEDURE build_basic_report(IN stock_symbol VARCHAR(50))
	BEGIN
		SELECT company_name, ticker_name,
			(SELECT find_52w_high(stock_symbol)) AS _52w_high,
			(SELECT find_52w_low(stock_symbol)) AS _52w_low,
			(SELECT AVG(rating_out_of_5) FROM analyst_ratings WHERE rating_ticker = stock_symbol) AS avg_rating,
            company_sector, 
            (SELECT performance_1yr FROM sector_info WHERE sector_name = company_sector) AS sector_performance_1yr
        FROM company_info WHERE ticker_name = stock_symbol;
	END $$
DELIMITER ;

/*
	This function yields the 52week high of a provided input stock
*/
DROP FUNCTION IF EXISTS find_52w_high;
DELIMITER $$
CREATE FUNCTION find_52w_high(stock_symbol VARCHAR(50)) 
	RETURNS DECIMAL(10,0)
    DETERMINISTIC
	BEGIN
		DECLARE high DECIMAL(10, 0);
		SET high = (SELECT MAX(data_open)
        FROM (
			SELECT data_open FROM price_data WHERE data_ticker = stock_symbol AND data_date BETWEEN '2018-06-17' AND '2019-06-17'
			UNION ALL
            SELECT data_high FROM price_data WHERE data_ticker = stock_symbol AND data_date BETWEEN '2018-06-17' AND '2019-06-17'
            UNION ALL 
            SELECT data_low FROM price_data WHERE data_ticker = stock_symbol AND data_date BETWEEN '2018-06-17' AND '2019-06-17'
            UNION ALL 
            SELECT data_close FROM price_data WHERE data_ticker = stock_symbol AND data_date BETWEEN '2018-06-17' AND '2019-06-17'
		) AS temp1 ORDER BY data_open);
        RETURN high;
	END $$
DELIMITER ;

/*
	This function yields the 52week low of a provided input stock
*/
DROP FUNCTION IF EXISTS find_52w_low;
DELIMITER $$
CREATE FUNCTION find_52w_low(stock_symbol VARCHAR(50)) 
	RETURNS DECIMAL(10,0)
    DETERMINISTIC
	BEGIN
		DECLARE low DECIMAL(10, 0);
		SET low = (SELECT MIN(data_open)
        FROM (
			SELECT data_open FROM price_data WHERE data_ticker = stock_symbol AND data_date BETWEEN '2018-06-17' AND '2019-06-17'
			UNION ALL
            SELECT data_high FROM price_data WHERE data_ticker = stock_symbol AND data_date BETWEEN '2018-06-17' AND '2019-06-17'
            UNION ALL 
            SELECT data_low FROM price_data WHERE data_ticker = stock_symbol AND data_date BETWEEN '2018-06-17' AND '2019-06-17'
            UNION ALL 
            SELECT data_close FROM price_data WHERE data_ticker = stock_symbol AND data_date BETWEEN '2018-06-17' AND '2019-06-17'
		) AS temp1 ORDER BY data_open);
        RETURN low;
	END $$
DELIMITER ;
    
    

