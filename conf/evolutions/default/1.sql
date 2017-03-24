# User schema

# --- !Ups
create table `customers` (
  `customer_id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `name` TEXT NOT NULL,
  `contact` TEXT NOT NULL
);

create table `products` (
  `product_id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `product_name` TEXT NOT NULL,
  `price` DOUBLE NOT NULL
);

create table `campaigns` (
  `campaign_id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `campaign_name` TEXT NOT NULL,
  `product_id` BIGINT,
  `camp_start_date` DATETIME NOT NULL,
  `camp_end_date` DATETIME NOT NULL,
   FOREIGN KEY (product_id) REFERENCES products(product_id)
);

create table `subscriptions` (
  `subs_id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `customer_id` BIGINT NOT NULL,
  `start_date` DATETIME NOT NULL,
  `end_date` DATETIME NOT NULL,
  `campaign_id` BIGINT NOT NULL,
  FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
  FOREIGN KEY (campaign_id) REFERENCES campaigns(campaign_id)
);



drop database playscalaslickexample;
create database playscalaslickexample;
use playscalaslickexample;



# --- !Downs
drop table `customers`;
drop table `subscriptions`;
drop table `campaigns`;
drop table `products`;