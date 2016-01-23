-- Create the USER tables to track MOOD
-- Use unique ideas or use the randomly generated id that is given to us by Paypal
CREATE TABLE users (
	id serial primary key,
	username varchar(255),
	f_name varchar(255),
	l_name varchar(255)
);

-- Create the MOOD table
CREATE TABLE mood (
	id serial primary key,
	user_id int REFERENCES users(id) ON DELETE CASCADE,
	anger decimal,
	contempt decimal,
	disgust decimal,
	fear decimal,
	happiness decimal,
	neutral decimal,
	sadness decimal,
	surprise decimal
);

-- Create the Snapshots table
CREATE TABLE snapshots (
	id serial primary key,
	user_id int REFERENCES users(id) ON DELETE CASCADE,
	url varchar(255)
);