USE project_clarendon;

SELECT find_52w_high('MSFT'); 
SELECT find_52w_high('AMZN'); 
    
SELECT find_52w_low('MSFT');
SELECT find_52w_low('AMZN');

CALL build_basic_report('MSFT');
CALL build_basic_report('XOM');
CALL build_basic_report('JPM');
CALL build_basic_report('AMZN');

ALTER TABLE company_info DROP COLUMN average_rating;
ALTER TABLE company_info ADD average_rating DECIMAL(10, 2); 

CALL initialize_avg_rating('AMZN');
CALL initialize_avg_rating('BA');
CALL initialize_avg_rating('BBL');
CALL initialize_avg_rating('CCI');
CALL initialize_avg_rating('JNJ');
CALL initialize_avg_rating('JPM');
CALL initialize_avg_rating('MSFT');
CALL initialize_avg_rating('NEE');
CALL initialize_avg_rating('VZ');
CALL initialize_avg_rating('WMT');
CALL initialize_avg_rating('XOM');

-- TEST SCRIPTS BELOW SUCCESSFULLY UPDATED AVERAGE RATING FOR JPM. TRIGGER 'avg_rating_after_insert' WORKS.alter
/*DELETE FROM analyst_ratings 
WHERE rating_date = '2018-06-17' AND rating_out_of_5 = 3 AND rating_agency = 'Bloomberg' AND rating_ticker = 'JPM';
SELECT * FROM company_info;
INSERT INTO analyst_ratings (rating_out_of_5, rating_date, rating_agency, rating_ticker)
VALUES (3, '2019-06-17', 'Bloomberg', 'JPM');
SELECT * FROM company_info; */

SELECT * FROM company_info;


