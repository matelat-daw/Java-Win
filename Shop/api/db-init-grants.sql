CREATE USER IF NOT EXISTS 'clients'@'%' IDENTIFIED BY 'clients';
GRANT ALL PRIVILEGES ON `clients`.* TO 'clients'@'%';
GRANT ALL PRIVILEGES ON `myikea`.* TO 'clients'@'%';
FLUSH PRIVILEGES;

