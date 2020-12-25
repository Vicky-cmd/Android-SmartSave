import os
import logging
import pymysql
import RDB_Config as rconfig


logger = logging.getLogger()
logger.setLevel(logging.INFO)

class Constants:
    __instance = None
    BUCKET_NAME = os.environ['BUCKET_NAME']
    rds_host  = rconfig.instance_endpoint
    name = rconfig.db_username
    password = rconfig.db_password
    db_name = rconfig.db_name
    otp_expiry_time = 3600
    auth_key_expiry_time = 86400
    auth_key_expiry_time_signOut = 86400
    size = 200, 200
    
    try:
        conn = pymysql.connect(rds_host, user=name, passwd=password, db=db_name, connect_timeout=5)
    except pymysql.MySQLError as e:
        logger.error("ERROR: Unexpected error: Could not connect to MySQL instance.")
        logger.error(e)
        sys.exit()
    
    logger.info("SUCCESS: Connection to RDS MySQL instance succeeded  ")

    @staticmethod
    def getInstance():
        if Constants.__instance == None:
            Constants()
        return Constants.__instance
    def __init__(self):
        if Constants.__instance != None:
            raise Exception ("This is a Singleton Class")
        else:
            Constants.__instance = self
        