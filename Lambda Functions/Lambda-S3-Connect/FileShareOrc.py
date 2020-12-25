import json
import base64
import boto3
import os
import logging
import Constants
from io import BytesIO
from PIL import Image
from datetime import datetime
import LoginOrc
import uuid
from botocore.exceptions import ClientError

logger = logging.getLogger()
logger.setLevel(logging.INFO)

try:
    constants = Constants.Constants.getInstance()
except Exception as e:
    logger.error(e)
    sys.exit()
    
conn = constants.conn
BUCKET_NAME = constants.BUCKET_NAME

def FileShareConEndpoint(event, context):
    logger.info(event)
    opType = event['opType']
    if(opType=="makePublic"): 
        logger.info("Proceeding into s3 Connect")
        return makePublic(event, context)
    if(opType=="getSharableUrl"): 
        logger.info("Proceeding into s3 Connect")
        return getSharableUrl(event, context)
    if(opType=="updateSharedFileDtls"): 
        logger.info("Proceeding into s3 Connect")
        return updateSharedFileDtls(event, context)
    if(opType=="loadSharedFile"): 
        logger.info("Proceeding into s3 Connect")
        return getSharedFile(event, context)
        
        
        
        
        
def makePublic(event, context):    
    logger.info("In the S3Connect Method!")
    
    
    try:
        event["otptype"] = ""
        otpResp = LoginOrc.validateOTP(event, context)
        logger.info("OTP Val Response")
        logger.info(otpResp)
        oValResp = otpResp["otp"]
    except Exception as e:
        logger.info(e)
        return otpResp
    
    # logger.info(file_path)
    s3 = boto3.client('s3')
    
    try:
        
        targetDir = ""
        fileName = ""
        if event['target_Dir'].strip(" ") != "":
            targetDir = event['target_Dir'].strip(" ").replace("|", "/")
            fileName = event['target_Dir'].strip(" ")+ "|" 
            targetDir = targetDir + '/'
        file_path = 'data/' + event['phone_no'] + "/" + targetDir + event['file_name']
        
        file_type = event['file_name'][event['file_name'].rfind("."):]
        
        fileName += event['file_name']
        
        item_count=0
        stmt1 = 'select * from req_register where phoneNo="'+ event['phone_no']  + '" and fileName="' + fileName + '"'
        logger.info(stmt1)
        with conn.cursor() as cur:
            cur.execute(stmt1)
            for row in cur:
                logger.info(row)
                item_count += 1
        
        if item_count>0:
            fileData = row[0]
            file_path = row[3]
            pubStat = "Y"
            if event['pubStat']=="N":
                pubStat = "N"
            
            
            if row[4] == "Folder":
                makePubFolders(file_path, pubStat)    
            
            timestamp = datetime.now()
            stmt1 = 'update req_register set Timestamp="'+ timestamp.strftime("%Y-%m-%d, %H:%M:%S") +'", isPublic = "' + pubStat + '" where fileName="'+ fileName +'" and phoneNo="' + event['phone_no'] +'"'
        else:
            return {
                    'statusCode': 200,
                    'headers' : {
                        'Content-Type': 'application/json', 
                        'Access-Control-Allow-Origin': '*',
                    },
                    'body': {
                        'status' : 210,
                        'error_msg' : 'File Not Found'
                    }
                }
        logger.info(stmt1)
        with conn.cursor() as cur:
            cur.execute(stmt1)
            conn.commit()
        
    except Exception as e:
        logger.info(e)
        return {
                'statusCode': 200,
                'headers' : {
                    'Content-Type': 'application/json', 
                    'Access-Control-Allow-Origin': '*',
                },
                'body': {
                    'status' : 210,
                    'error_msg' : 'Failed! to make the Files Public'
                }
            }
        raise IOError(e)
    
    return {
        'statusCode': 200,
        'headers' : {
            'Content-Type': 'application/json', 
            'Access-Control-Allow-Origin': '*',
        },
        'body': {
            'status' : 200,
            'fileData' : fileData,
            'file_path': file_path
        }
    }
    
    
    

def makePubFolders(file_path, pubStat):
    
    item_count = 0
    resultset = []
    stmt1 = 'select * from req_register where filePath like "'+ file_path  + '/%" and filePath  not like "' + file_path + '/%/%" order by Timestamp desc'
    logger.info(stmt1)
    with conn.cursor() as cur:
        cur.execute(stmt1)
        for row in cur:
            resultset.append(row)
            logger.info(resultset)
            item_count += 1
        logger.info(item_count)
    if(item_count ==0):
        return {
            'statusCode': 404,
            'headers' : {
                'Content-Type': 'application/json', 
                'Access-Control-Allow-Origin': '*',
            },
            'body': {
                'status': 210,
                'error_msg': 'No Files Found!!' 
            }
        }

    logger.info("File Found!")
    file_data = []
    file_count = 0
    for r_items in resultset:
        if r_items[4] == "Folder":
            makePubFolders(r_items[3], pubStat)
        
        # s3 = boto3.resource('s3')  #boto3.client('s3')    
        # s3Obj = s3.Object(BUCKET_NAME, r_items[2])
        # s3_response = s3Obj.delete()
        timestamp = datetime.now()
        stmt1 = 'update req_register set Timestamp="'+ timestamp.strftime("%Y-%m-%d, %H:%M:%S") +'", isPublic = "' + pubStat + '" where ID="'+ str(r_items[0]) +'"'
        logger.info(stmt1)
        with conn.cursor() as cur:
            cur.execute(stmt1)
            conn.commit()    
                

def getSharableUrl(event, context):
    try:
        event["otptype"] = ""
        otpResp = LoginOrc.validateOTP(event, context)
        logger.info("OTP Val Response")
        logger.info(otpResp)
    except Exception as e:
        oValResp = otpResp["otp"]
        logger.info(e)
        return otpResp
    
    # logger.info(file_path)
    s3 = boto3.client('s3')
    
    try:
        
        sharableLink = ""
        targetDir = ""
        fileName = ""
        if event['target_Dir'].strip(" ") != "":
            targetDir = event['target_Dir'].strip(" ").replace("|", "/")
            fileName = event['target_Dir'].strip(" ")+ "|" 
            targetDir = targetDir + '/'
        file_path = 'data/' + event['phone_no'] + "/" + targetDir + event['file_name']
        
        file_type = event['file_name'][event['file_name'].rfind("."):]
        
        fileName += event['file_name']
        
        item_count=0
        stmt1 = 'select * from req_register where phoneNo="'+ event['phone_no']  + '" and fileName="' + fileName + '"'
        logger.info(stmt1)
        with conn.cursor() as cur:
            cur.execute(stmt1)
            for row in cur:
                logger.info(row)
                item_count += 1
        
        if item_count>0:
            pubSharable = row[8]
            if pubSharable == "Y":
                
                fnameLst = row[2].split("|")
                fname = fnameLst[len(fnameLst) - 1].replace(" ", "+")
                shareK = str(row[0]) + "&fileName=" + fname + "&fileType=" + row[4] 
                sharableLink = "http://www.m.smartsave.com/loadSharedFile?fileId=" + shareK
            
            else:
                return {
                        'statusCode': 200,
                        'headers' : {
                            'Content-Type': 'application/json', 
                            'Access-Control-Allow-Origin': '*',
                        },
                        'body': {
                            'status' : 211,
                            'error_msg' : 'Non Sharable File'
                        }
                    }                
    
        else:
            return {
                    'statusCode': 200,
                    'headers' : {
                        'Content-Type': 'application/json', 
                        'Access-Control-Allow-Origin': '*',
                    },
                    'body': {
                        'status' : 210,
                        'error_msg' : 'File Not Found'
                    }
                }
        logger.info(stmt1)
        
    except Exception as e:
        logger.info(e)
        return {
                    'statusCode': 200,
                    'headers' : {
                        'Content-Type': 'application/json', 
                        'Access-Control-Allow-Origin': '*',
                    },
                    'body': {
                        'status' : 210,
                        'error_msg' : 'Upload Failed!'
                    }
                }
        raise IOError(e)
    
    return {
        'statusCode': 200,
        'headers' : {
            'Content-Type': 'application/json', 
            'Access-Control-Allow-Origin': '*',
        },
        'body': {
            'status' : 200,
            'sharableLink' : sharableLink
        }
    }
        
        
def updateSharedFileDtls(event, context):
    logger.info("In the S3Connect Method!")
    
    
    try:
        event["otptype"] = ""
        otpResp = LoginOrc.validateOTP(event, context)
        logger.info("OTP Val Response")
        logger.info(otpResp)
        oValResp = otpResp["otp"]
    except Exception as e:
        logger.info(e)
        return otpResp
    
    # logger.info(file_path)
    s3 = boto3.client('s3')
    
    try:
        
        targetDir = ""
        fileName = ""
        if event['target_Dir'].strip(" ") != "":
            targetDir = event['target_Dir'].strip(" ").replace("|", "/")
            fileName = event['target_Dir'].strip(" ")+ "|" 
            targetDir = targetDir + '/'
        file_path = 'data/' + event['phone_no'] + "/" + targetDir + event['file_name']
        
        file_type = event['file_name'][event['file_name'].rfind("."):]
        
        fileName += event['file_name']
        
        item_count=0
        stmt1 = 'select * from req_register where phoneNo="'+ event['phone_no']  + '" and fileName="' + fileName + '"'
        logger.info(stmt1)
        with conn.cursor() as cur:
            cur.execute(stmt1)
            for row in cur:
                logger.info(row)
                item_count += 1
        
        if item_count>0:
            fileData = row[0]
            file_path = row[3]
            pubStat = event['shared_Folds']
            
            if row[4] == "Folder":
                makeFolSharable(file_path, pubStat)    
            
            timestamp = datetime.now()
            stmt1 = 'update req_register set Timestamp="'+ timestamp.strftime("%Y-%m-%d, %H:%M:%S") +'", sharedNos = "' + pubStat + '" where fileName="'+ fileName +'" and phoneNo="' + event['phone_no'] +'"'
        else:
            return {
                    'statusCode': 200,
                    'headers' : {
                        'Content-Type': 'application/json', 
                        'Access-Control-Allow-Origin': '*',
                    },
                    'body': {
                        'status' : 210,
                        'error_msg' : 'File Not Found'
                    }
                }
        logger.info(stmt1)
        with conn.cursor() as cur:
            cur.execute(stmt1)
            conn.commit()
        
    except Exception as e:
        logger.info(e)
        return {
                'statusCode': 200,
                'headers' : {
                    'Content-Type': 'application/json', 
                    'Access-Control-Allow-Origin': '*',
                },
                'body': {
                    'status' : 210,
                    'error_msg' : 'Upload Failed!'
                }
            }
        raise IOError(e)
    
    return {
        'statusCode': 200,
        'headers' : {
            'Content-Type': 'application/json', 
            'Access-Control-Allow-Origin': '*',
        },
        'body': {
            'status' : 200,
            'fileData' : fileData,
            'file_path': file_path
        }
    }
    
    
    

def makeFolSharable(file_path, pubStat):
    
    item_count = 0
    resultset = []
    stmt1 = 'select * from req_register where filePath like "'+ file_path  + '/%" and filePath  not like "' + file_path + '/%/%" order by Timestamp desc'
    logger.info(stmt1)
    with conn.cursor() as cur:
        cur.execute(stmt1)
        for row in cur:
            resultset.append(row)
            logger.info(resultset)
            item_count += 1
        logger.info(item_count)
    if(item_count ==0):
        return {
            'statusCode': 404,
            'headers' : {
                'Content-Type': 'application/json', 
                'Access-Control-Allow-Origin': '*',
            },
            'body': {
                'status': 210,
                'error_msg': 'No Files Found!!' 
            }
        }

    logger.info("File Found!")
    file_data = []
    file_count = 0
    for r_items in resultset:
        if r_items[4] == "Folder":
            makeFolSharable(r_items[3], pubStat)
        
        # s3 = boto3.resource('s3')  #boto3.client('s3')    
        # s3Obj = s3.Object(BUCKET_NAME, r_items[2])
        # s3_response = s3Obj.delete()
        timestamp = datetime.now()
        stmt1 = 'update req_register set Timestamp="'+ timestamp.strftime("%Y-%m-%d, %H:%M:%S") +'", sharedNos = "' + pubStat + '" where ID="'+ str(r_items[0]) +'"'
        logger.info(stmt1)
        with conn.cursor() as cur:
            cur.execute(stmt1)
            conn.commit()    
                    


def getSharedFile(event, context):
    logger.info("In the getSharedFile Method!")
    
    item_count = 0
    resultset = []
    logger.info(event)
    try:
        event["otptype"] = ""
        otpResp = LoginOrc.validateOTP(event, context)
        logger.info("OTP Val Response")
        logger.info(otpResp)
        oValResp = otpResp["otp"]
    except Exception as e:
        logger.info(e)
        return otpResp
    
    try:
        logger.info(event['fileId'])
        fileId = event['fileId']
        stmt1 = 'select * from req_register where ID = "' + fileId + '" order by Timestamp desc'
        logger.info(stmt1)
        with conn.cursor() as cur:
            cur.execute(stmt1)
            for row in cur:
                logger.info(row)
                resultset.append(row)
                item_count += 1
            logger.info(item_count)
        if(item_count ==0):
            return {
                'statusCode': 404,
                'headers' : {
                    'Content-Type': 'application/json', 
                    'Access-Control-Allow-Origin': '*',
                },
                'body': {
                    'status': 210,
                    'error_msg': 'No Files Found!!' 
                }
            }
    except Exception as e:
        logger.info(e)
        return {
                    'statusCode': 200,
                    'headers' : {
                        'Content-Type': 'application/json', 
                        'Access-Control-Allow-Origin': '*',
                    },
                    'body': {
                        'status' : 210,
                        'error_msg' : 'Upload Failed!'
                    }
                }
        raise IOError(e)
    
    file_data = []
    file_count = 0
    for r_items in resultset:
        # logger.info(r_items)
        # logger.info(r_items[5].strftime("%m-%d-%Y, %H:%M:%S"))
        access = False
        filePubOrSharable = 'N'
        isPublic = ""
        isSharedDtls = ""
        if r_items[1] == event['phone_no']:
            access = True
            isPublic = r_items[8]
            isSharedDtls = r_items[9]
        elif r_items[8] == 'Y':
            access = True
            filePubOrSharable = 'P'
        elif r_items[9].strip() != "":
            auth_Lst = r_items[9].split(",")
            for auth in auth_Lst:
                if event['phone_no'] == auth.strip():
                    access = True
                    filePubOrSharable = 'S'
                    break
        
        if access:        
            f_item = []
            f_item.append(r_items[2])
            f_item.append(r_items[4])
            f_item.append(r_items[5].strftime("%m-%d-%Y, %H:%M:%S"))
            f_item.append(r_items[6])
            f_item.append(isPublic)
            f_item.append(isSharedDtls)
            f_item.append(filePubOrSharable)
            file_data.append(f_item)
            file_count += 1
        
    return {
        'statusCode': 200,
        'headers' : {
            'Content-Type': 'application/json', 
            'Access-Control-Allow-Origin': '*',
        },
        'body': {
            'status' : 200,
            'file_count' : file_count,
            'files': file_data
        }
    }
    
    
def getSharedFileList(event, context):
    
    try:
        
        targetDir = ""
        if event['target_Dir'].strip(" ") != "":
            targetDir = event['target_Dir'].strip(" ").replace("|", "/")
            targetDir = targetDir + '/'
        # file_path = 'data/' + event['phone_no'] + "/" + targetDir
        
        sharedFileId = event['fileId']
        
        resultset = []
        item_count=0
        stmt1 = 'select * from req_register where ID = "'+ sharedFileId  + '" order by Timestamp desc'
        logger.info(stmt1)
        with conn.cursor() as cur:
            cur.execute(stmt1)
            for row in cur:
                logger.info(row)
                # resultset.append(row)
                item_count += 1
            logger.info(item_count)
        
        if(item_count ==0):
            return {
                'statusCode': 404,
                'headers' : {
                    'Content-Type': 'application/json', 
                    'Access-Control-Allow-Origin': '*',
                },
                'body': {
                    'status': 210,
                    'error_msg': 'No Files Found!!' 
                }
            }
        
        file_path = 'data/' + str(row[1]) + "/" + targetDir
            
        item_count=0    
        stmt1 = 'select * from req_register where filePath like "'+ file_path  + '%" and filePath  not like "' + file_path + '%/%" order by Timestamp desc'
        logger.info(stmt1)
        with conn.cursor() as cur:
            cur.execute(stmt1)
            for row in cur:
                logger.info(row)
                resultset.append(row)
                item_count += 1
            logger.info(item_count)
            
        file_data = []
        file_count = 0
        for r_items in resultset:
            # logger.info(r_items)
            # logger.info(r_items[5].strftime("%m-%d-%Y, %H:%M:%S"))
            access = False
            filePubOrSharable = 'N'
            isPublic = ""
            isSharedDtls = ""
            if r_items[1] == event['phone_no']:
                access = True
                isPublic = r_items[8]
                isSharedDtls = r_items[9]
            elif r_items[8] == 'Y':
                access = True
                filePubOrSharable = 'P'
            elif r_items[9].strip() != "":
                auth_Lst = r_items[9].split(",")
                for auth in auth_Lst:
                    if event['phone_no'] == auth.strip():
                        access = True
                        filePubOrSharable = 'S'
                        break
            
            if access:        
                f_item = []
                f_item.append(r_items[2])
                f_item.append(r_items[4])
                f_item.append(r_items[5].strftime("%m-%d-%Y, %H:%M:%S"))
                f_item.append(r_items[6])
                f_item.append(isPublic)
                f_item.append(isSharedDtls)
                f_item.append(filePubOrSharable)
                file_data.append(f_item)
                file_count += 1
            
        return {
            'statusCode': 200,
            'headers' : {
                'Content-Type': 'application/json', 
                'Access-Control-Allow-Origin': '*',
            },
            'body': {
                'status' : 200,
                'file_count' : file_count,
                'files': file_data
            }
        }
    
    except Exception as e:
        logger.info(e)
        return {
                    'statusCode': 200,
                    'headers' : {
                        'Content-Type': 'application/json', 
                        'Access-Control-Allow-Origin': '*',
                    },
                    'body': {
                        'status' : 210,
                        'error_msg' : 'Upload Failed!'
                    }
                }
        raise IOError(e)    
        
        
def downloadSharedFile(event, context):        
    try:
        
        targetDir = ""
        if event['target_Dir'].strip(" ") != "":
            targetDir = event['target_Dir'].strip(" ").replace("|", "/")
            targetDir = targetDir + '/'
        # file_path = 'data/' + event['phone_no'] + "/" + targetDir
        
        sharedFileId = event['fileId']
        fileName = event['file_name']
        resultset = []
        item_count=0
        stmt1 = 'select * from req_register where ID = "'+ sharedFileId  + '" order by Timestamp desc'
        logger.info(stmt1)
        with conn.cursor() as cur:
            cur.execute(stmt1)
            for row in cur:
                logger.info(row)
                # resultset.append(row)
                item_count += 1
            logger.info(item_count)
        
        if(item_count ==0):
            return {
                'statusCode': 404,
                'headers' : {
                    'Content-Type': 'application/json', 
                    'Access-Control-Allow-Origin': '*',
                },
                'body': {
                    'status': 210,
                    'error_msg': 'No Files Found!!' 
                }
            }
        
        phNo = str(row[1])
        file_path = 'data/' + phNo + "/" + targetDir
        
        logger.info(file_path)
            
        item_count=0    
        stmt1 = 'select * from req_register where phoneNo = "' + phNo + '" and fileName= "' + fileName + '"'
        logger.info(stmt1)
        with conn.cursor() as cur:
            cur.execute(stmt1)
            for row in cur:
                item_count += 1
            logger.info(item_count)
        if(item_count ==0):
            return {
                'statusCode': 404,
                'headers' : {
                    'Content-Type': 'application/json', 
                    'Access-Control-Allow-Origin': '*',
                },
                'body': {
                    'status': 210,
                    'error_msg': 'No Files Found!!' 
                }
            }
        
        file_path = row[3]
        
        s3 = boto3.client('s3')
        
        presigned_url = s3.generate_presigned_url(
            ClientMethod='get_object',
            Params={
                'Bucket': BUCKET_NAME,
                'Key': file_path
            },
            ExpiresIn=100
        )
        
        return {
                'statusCode': 200,
                'headers' : {
                    'Content-Type': 'application/json', 
                    'Access-Control-Allow-Origin': '*',
                },
                'body': {
                    'status' : 200,
                    'download_url': presigned_url
                }
        }            
        
    except Exception as e:
        logger.info(e)
        return {
                    'statusCode': 200,
                    'headers' : {
                        'Content-Type': 'application/json', 
                        'Access-Control-Allow-Origin': '*',
                    },
                    'body': {
                        'status' : 210,
                        'error_msg' : 'Upload Failed!'
                    }
                }
        raise IOError(e)    
            