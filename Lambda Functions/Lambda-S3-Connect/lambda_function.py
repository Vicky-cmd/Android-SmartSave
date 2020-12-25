import json
import base64
import boto3
import os
import logging
import pymysql
import sys
import RDB_Config as rconfig
import Constants
import S3ConnOrc
import LoginOrc
import FileShareOrc

logger = logging.getLogger()
logger.setLevel(logging.INFO)

# logger.info("Before")
try:
    constants = Constants.Constants.getInstance()
except Exception as e:
    logger.error(e)
    sys.exit()
    

# logger.info("After")

BUCKET_NAME = os.environ['BUCKET_NAME']

# rds_host  = rconfig.instance_endpoint
# name = rconfig.db_username
# password = rconfig.db_password
# db_name = rconfig.db_name

# try:
#     conn = pymysql.connect(constants.rds_host, user=constants.name, passwd=constants.password, db=constants.db_name, connect_timeout=5)
# except pymysql.MySQLError as e:
#     logger.error("ERROR: Unexpected error: Could not connect to MySQL instance.")
#     logger.error(e)
#     sys.exit()

# logger.info("SUCCESS: Connection to RDS MySQL instance succeeded  ")




def lambda_handler(event, context):
    opType = event['opType']
    logger.info(opType)
    if(opType=="s3Connect" or opType=="getFilesList" or opType=="getFile" or opType=="deleteFile" or opType=="getThumbnailsData" or opType=="uploadFile" or opType=="UpdateuploadDtls" or opType=='downloadFile' or opType=='createFolder'): 
        logger.info("Proceeding into s3 Connection Endpoint")
        return S3ConnOrc.s3ConnectionEndpoint(event, context)
    elif(opType=="Login" or opType=="SignUp" or opType=="confirmLogin" or opType=="Logout" or opType=="ResetPassword" or opType=="checkUsrEmail" or opType=="sendOTP" or opType=="validateOTP"):
        logger.info("Into LoginSignUpEndpoint")
        return LoginOrc.loginSignupEndpoint(event, context)
    elif(opType=="makePublic" or opType=="getSharableUrl" or opType=="updateSharedFileDtls" or opType=="loadSharedFile"):
        logger.info("Into LoginSignUpEndpoint")
        return FileShareOrc.FileShareConEndpoint(event, context)    
    else: 
        return {
            'statusCode': 400,
            'headers' : {
                'Content-Type': 'application/json', 
                'Access-Control-Allow-Origin': '*',
            },
            'body': {
                'error_msg': 'Unable to map Operation With Backend'
            }
        }
    
    
    

def s3Connect(event, context):    
    if event['isencode']=="Y":
        file_content = base64.b64decode(event['content'])
    else:
        file_content = event['content']
        
    file_path = 'data/' + event['name']
    
    file_type = event['name'][event['name'].rfind("."):]
    
    if file_type==".txt":
        compFlg="N"
    else:
        compFlg="Y"
    
    s3 = boto3.client('s3')
    
    try:
        s3_response = s3.put_object(Bucket=BUCKET_NAME, Key=file_path, Body=file_content)
        
        stmt1 = 'insert into req_register (email, fileName, filePath, fileType, compFlg) values( "' +  event['email'] + '","' + event['name'] + '","' + file_path + '","' + file_type + '","' + compFlg + '" )'
        logger.info(stmt1)
        with conn.cursor() as cur:
            cur.execute(stmt1)
            conn.commit()
        
    except Exception as e:
        raise IOError(e)
    
    return {
        'statusCode': 200,
        'headers' : {
            'Content-Type': 'application/json', 
            'Access-Control-Allow-Origin': '*',
        },
        'body': {
            'file_path': file_path,
            'test': constants.name
        }
    }
