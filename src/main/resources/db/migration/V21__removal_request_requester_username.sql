-- Store the requester's username as an audit snapshot at request time.
ALTER TABLE plate_removal_requests ADD COLUMN requester_username VARCHAR(255);
