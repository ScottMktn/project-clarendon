DROP DATABASE IF EXISTS project_clarendon;
CREATE DATABASE project_clarendon;
USE project_clarendon;

DROP TABLE IF EXISTS sector_info;
CREATE TABLE sector_info
(
sector_name  ENUM('Real Estate', 'Materials', 'Utilities', 'Industrials', 'Consumer Staples', 'Health care', 'Financials', 'Energy', 'Consumer Discretionary', 'Information Technology', 'Communication Services')
 PRIMARY KEY, 
 performance_week DECIMAL(3,2) NOT NULL,
 performance_1mo DECIMAL(3,2) NOT NULL,
 performance_3mo DECIMAL(3,2) NOT NULL, 
 performance_1yr DECIMAL(3,2) NOT NULL,
 performance_3yr DECIMAL(3,2) NOT NULL
);

DROP TABLE IF EXISTS company_info;
CREATE TABLE company_info
(
ticker_name VARCHAR(5) NOT NULL PRIMARY KEY,
company_name VARCHAR(355) NOT NULL,
company_sector ENUM ('Real Estate', 'Materials', 'Utilities', 'Industrials', 'Consumer Staples', 'Health care', 'Financials', 'Energy', 'Consumer Discretionary', 'Information Technology', 'Communication Services')
NOT NULL,
CONSTRAINT company_sector_fk
	FOREIGN KEY (company_sector)
    REFERENCES sector_info (sector_name)
);

DROP TABLE IF EXISTS analyst_ratings;
CREATE TABLE analyst_ratings
(
rating_id INT AUTO_INCREMENT PRIMARY KEY,
rating_out_of_5 DOUBLE NOT NULL,
rating_date DATE NOT NULL, 
rating_agency VARCHAR(50) NOT NULL,
rating_ticker VARCHAR(5) NOT NULL,
CONSTRAINT rating_ticker_fk
	FOREIGN KEY (rating_ticker)
    REFERENCES company_info (ticker_name),
CONSTRAINT rating_validity
	CHECK (rating_out_of_5 <= 5)
);

DROP TABLE IF EXISTS price_data;
CREATE TABLE price_data
(
data_id INT PRIMARY KEY AUTO_INCREMENT,
data_date DATE NOT NULL,
data_open DECIMAL NOT NULL,
data_high DECIMAL NOT NULL,
data_low DECIMAL NOT NULL,
data_close DECIMAL NOT NULL,
data_ticker VARCHAR(5) NOT NULL,
CONSTRAINT data_ticker_fk
	FOREIGN KEY (data_ticker)
    REFERENCES company_info (ticker_name),
CONSTRAINT price_validity 
	CHECK (data_high >= data_low AND data_close BETWEEN data_low AND data_high)
);
