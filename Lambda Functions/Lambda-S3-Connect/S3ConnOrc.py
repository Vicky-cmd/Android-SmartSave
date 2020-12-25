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
import FileShareOrc

logger = logging.getLogger()
logger.setLevel(logging.INFO)

try:
    constants = Constants.Constants.getInstance()
except Exception as e:
    logger.error(e)
    sys.exit()
    
conn = constants.conn
BUCKET_NAME = constants.BUCKET_NAME

def s3ConnectionEndpoint(event, context):
    opType = event['opType']
    if(opType=="s3Connect"): 
        logger.info("Proceeding into s3 Connect")
        return s3Connect(event, context)
    if(opType=="getFilesList"): 
        logger.info("Proceeding into s3 getFilesList")
        return getFilesList(event, context)
    if(opType=="getFile"): 
        logger.info("Proceeding into s3 getFile")
        return getFile(event, context)
    if(opType=="deleteFile"): 
        logger.info("Proceeding into s3 deleteFile")
        return deleteFile(event, context)
    if(opType=="getThumbnailsData"): 
        logger.info("Proceeding into s3 getThumbnailData")
        return getThumbnailData(event, context)
    if(opType=="uploadFile"):
        logger.info("Proceeding into s3 uploadFile")
        return uploadFile(event, context)
    if(opType=="UpdateuploadDtls"):
        logger.info("Proceeding into s3 UpdateuploadDtls")
        return updateuploadDtls(event, context)
    if(opType=='downloadFile'):
        logger.info("Proceeding into s3 downloadFile")
        return downloadFile(event, context)
    if(opType=='createFolder'):
        logger.info("Proceeding into s3 downloadFile")
        return createFolder(event, context)
        
        
        
def s3Connect(event, context):    
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
    
    if event['isencode']=="Y":
        file_content = base64.b64decode(event['content'])
    else:
        file_content = event['content']
    
    
    targetDir = ""
    fileName = ""
    if event['target_Dir'].strip(" ") != "":
        targetDir = event['target_Dir'].strip(" ").replace("|", "/")
        fileName = event['target_Dir'].strip(" ") + "|"
        targetDir = targetDir + '/'
    file_path = 'data/' + event['phone_no'] + "/" + targetDir + event['file_name']
    
    file_type = event['file_name'][event['file_name'].rfind("."):]
    
    fileName += event['file_name']
    
    logger.info(file_path)
    s3 = boto3.client('s3')
    
    try:
        s3_response = s3.put_object(Bucket=BUCKET_NAME, Key=file_path, Body=file_content)
        
        if(event['is_Image'] == "Y"):
            img = Image.open(BytesIO(file_content))
            img = img.resize((int(constants.size[0]), int(constants.size[1])), Image.ANTIALIAS)
            buffer = BytesIO()
            img.save(buffer, 'JPEG')
            buffer.seek(0)
            img_file_path = 'thumbnails/' + event['phone_no'] + '/' + fileName
            s3_response = s3.put_object(Bucket=BUCKET_NAME, Key=img_file_path, Body=buffer, ContentType='image/jpeg')
            
        
        item_count=0
        stmt1 = 'select * from req_register where phoneNo="'+ event['phone_no']  + '" and fileName="' +  fileName + '"'
        logger.info(stmt1)
        with conn.cursor() as cur:
            cur.execute(stmt1)
            for row in cur:
                item_count += 1
        
        if item_count>0:
            timestamp = datetime.now()
            stmt1 = 'update req_register set Timestamp="'+ timestamp.strftime("%Y-%m-%d, %H:%M:%S") +'" where fileName="'+ fileName +'" and phoneNo="' + event['phone_no'] + '"'
        else:
            stmt1 = 'insert into req_register (email_id, phoneNo, fileName, filePath, fileType, isImg) values( "' +  event['emailid']+ '","' +  event['phone_no'] + '","' + fileName + '","' + file_path + '","' + file_type + '","' + event['is_Image'] + '" )'
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
            'file_path': file_path
        }
    }


def getFilesList(event, context):
    logger.info("In the S3Connect Method!")
    # if event['isencode']=="Y":
    #     file_content = base64.b64decode(event['content'])
    # else:
    #     file_content = event['content']
        
    # file_path = 'data/' + event['name']
    
    # file_type = event['name'][event['name'].rfind("."):]
    
    # if file_type==".txt":
    #     compFlg="N"
    # else:
    #     compFlg="Y"
    
    # logger.info(file_path)
    # s3 = boto3.client('s3') 
    item_count = 0
    resultset = []
    
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
        
        if event['isFetchSharable'] == 'S' or event['isFetchSharable'] == 'P':
            return FileShareOrc.getSharedFileList(event, context)
        
        targetDir = ""
        if event['target_Dir'].strip(" ") != "":
            targetDir = event['target_Dir'].strip(" ").replace("|", "/")
            targetDir = targetDir + '/'
        file_path = 'data/' + event['phone_no'] + "/" + targetDir
        # s3_response = s3.put_object(Bucket=BUCKET_NAME, Key=file_path, Body=file_content)
        
        # stmt1 = 'insert into req_register (email_id, phoneNo, fileName, filePath, fileType, compFlg) values( "' +  event['emailid']+  event['phoneNo'] + '","' + '","' + event['name'] + '","' + file_path + '","' + file_type + '","' + compFlg + '" )'
        stmt1 = 'select * from req_register where filePath like "'+ file_path  + '%" and filePath  not like "' + file_path + '%/%" order by Timestamp desc'
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
        f_item = []
        f_item.append(r_items[2])
        f_item.append(r_items[4])
        f_item.append(r_items[5].strftime("%m-%d-%Y, %H:%M:%S"))
        f_item.append(r_items[6])
        f_item.append(r_items[8])
        f_item.append(r_items[9])
        f_item.append("N")
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
    
    
def getFile(event, context):
    logger.info("In the getFile Method!")
    
    
    try:
        event["otptype"] = ""
        otpResp = LoginOrc.validateOTP(event, context)
        logger.info("OTP Val Response")
        logger.info(otpResp)
        oValResp = otpResp["otp"]
    except Exception as e:
        logger.info(e)
        return otpResp
    
    item_count = 0
    try:
        stmt1 = 'select * from req_register where phoneNo = "' + event['phone_no'] + '" and fileName= "' + event['file_name'] + '"'
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
        
        logger.info(file_path)
        s3 = boto3.client('s3')    
        
        s3_response = s3.get_object(Bucket=BUCKET_NAME, Key=file_path)
        logger.info(s3_response)
        bodyObj = s3_response.get("Body").read()
        logger.info(bodyObj)
            
        if event['isencode']=="Y":
            file_content = base64.b64encode(bodyObj).decode("UTF-8")
        else:
            file_content = bodyObj
            
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
            'file_content': file_content,
            'fileName': row[2],
            'is_Image': row[6]
        }
    }
    
    
def deleteFile(event, context):
    logger.info("In the S3Connect Method!")
    
    targetDir = ""
    fileName = ""
    if event['target_Dir'].strip(" ") != "":
        targetDir = event['target_Dir'].strip(" ").replace("|", "/")
        fileName = event['target_Dir'].strip(" ")+ "|" 
        targetDir = targetDir + '/'
    file_path = 'data/' + event['phone_no'] + "/" + targetDir + event['file_name']
    
    file_type = event['file_name'][event['file_name'].rfind("."):]
    
    fileName += event['file_name']
    
    
    item_count = 0
    try:
        s3 = boto3.resource('s3')  #boto3.client('s3')    
        stmt1 = 'select * from req_register where phoneNo = "' + event['phone_no'] + '" and fileName= "' + fileName + '"'
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
        file_type = row[4]
        logger.info(file_path)
        if file_type == "Folder":
            deleteFolders(file_path)
            s3_bucket_for_del = s3.Bucket(BUCKET_NAME)
            for key in s3_bucket_for_del.objects.filter(Prefix = file_path):
                key.delete()
            
        else:
            s3Obj = s3.Object(BUCKET_NAME, file_path)
            s3_response = s3Obj.delete()
            logger.info(s3_response)
        
        stmt1 = 'delete from req_register where phoneNo = "' + event['phone_no'] + '" and fileName= "' + row[2] + '"'
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
            'status' : 200
        }
    }    
    

def deleteFolders(file_path):
    
    item_count = 0
    resultset = []
    stmt1 = 'select * from req_register where filePath like "'+ file_path  + '/%" and filePath  not like "' + file_path + '/%/%" order by Timestamp desc'
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

    file_data = []
    file_count = 0
    for r_items in resultset:
        if r_items[4] == "Folder":
            deleteFolders(r_items[3])
        
        # s3 = boto3.resource('s3')  #boto3.client('s3')    
        # s3Obj = s3.Object(BUCKET_NAME, r_items[2])
        # s3_response = s3Obj.delete()
        stmt1 = 'delete from req_register where phoneNo = "' + r_items[1] + '" and fileName= "' + r_items[2] + '"'
        logger.info(stmt1)
        with conn.cursor() as cur:
            cur.execute(stmt1)
            conn.commit()    
                
        
    
    
def getThumbnailData(event, context):
    logger.info("In the getThumbnailData Method!")
    
    
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
        s3 = boto3.client('s3')
        file_data = []
        logger.info(event)
        fList = int(event["fListNo"])
        logger.info(fList)
        for fNo in range(fList):
            fEntry = event["fListE" + str(fNo)]
            logger.info("Insisde For Loop")
            logger.info(fEntry)
            logger.info(fEntry[0] + "   " + fEntry[1])
            fName = fEntry
            # fid = fEntry[0]
            fcontent = {}
            file_path = 'thumbnails/' + event['phone_no'] + '/' + fName
            logger.info(file_path)
            try:
                s3_response = s3.get_object(Bucket=BUCKET_NAME, Key=file_path)
                logger.info(s3_response)
                bodyObj = s3_response.get("Body").read()
                # logger.info(bodyObj)
                logger.info("Received Response Body")
                if event['isencode']=="Y":
                    file_content = base64.b64encode(bodyObj).decode("UTF-8")
                else:
                    file_content = bodyObj
                
                logger.info("Encoded File Conent")
                
                # fcontent["id"] = fid
                fcontent["Name"] = fName
                fcontent["FileContent"] = file_content
                file_data.append(fcontent)
                logger.info("Appended File Data")

            except ClientError as e:
                if e.response['Error']['Code'] == 'NoSuchKey':
                    logger.info("No Such Key:  " + fName)
                else:
                    raise
        
        return {
            'statusCode': 200,
            'headers' : {
                'Content-Type': 'application/json', 
                'Access-Control-Allow-Origin': '*',
            },
            'body': {
                'status' : 200,
                'filesLst': file_data,
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
                        'error_msg' : 'Upload Failed!',
                        'Exception' : e
                    }
                }
        raise IOError(e)    
        
        
        
def uploadFile(event, context):
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
    
    
    s3 = boto3.client('s3')
    
    targetDir = ""
    if event['target_Dir'].strip(" ") != "":
        targetDir = event['target_Dir'].strip(" ").replace("|", "/")
        targetDir = targetDir + '/'
    upload_key = 'data/' + event['phone_no'] + "/" + targetDir + event['file_name']
        
    presigned_url = s3.generate_presigned_url(
        ClientMethod='put_object',
        Params={
            'Bucket': BUCKET_NAME,
            'Key': upload_key
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
                'upload_url': presigned_url
            }
    }    



def downloadFile(event, context):
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
    
    try:
        
        
        # targetDir = ""
        # if event['target_Dir'].strip(" ") != "":
        #     targetDir = event['target_Dir'].strip(" ").replace("|", "/")
        #     targetDir = targetDir + '/'
        # upload_key = 'data/' + event['phone_no'] + '/' + targetDir + event['file_name']
        
                
        if event['isFetchSharable'] == 'S' or event['isFetchSharable'] == 'P':
            return FileShareOrc.downloadSharedFile(event, context)
        
        fileName = event['file_name']
    
        item_count = 0
        stmt1 = 'select * from req_register where phoneNo = "' + event['phone_no'] + '" and fileName= "' + fileName + '"'
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
    
        # file_path = 'data/' + event['phone_no'] + '/' + event['file_name']
        
        presigned_url = s3.generate_presigned_url(
            ClientMethod='get_object',
            Params={
                'Bucket': BUCKET_NAME,
                'Key': file_path
            },
            ExpiresIn=100
        )
    except Exception as e:
        logger.info("This is the error!!!!")
        logger.info(e)
        try:
            o = otpResp['otp']
        except Exception as e:
            return otpResp
            
        return {
                    'statusCode': 200,
                    'headers' : {
                        'Content-Type': 'application/json', 
                        'Access-Control-Allow-Origin': '*',
                    },
                    'body': {
                        'status' : 210,
                        'error_msg' : 'Url Generation Failed!'
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
                'download_url': presigned_url
            }
    }    
    
    
def updateuploadDtls(event, context):
    
    logger.info("Enters Update Upload Dtls")
    # if event['isencode']=="Y":
    #     file_content = base64.b64decode(event['content'])
    # else:
    #     file_content = event['content']
        
    
    targetDir = ""
    fileName = ""
    if event['target_Dir'].strip(" ") != "":
        targetDir = event['target_Dir'].strip(" ").replace("|", "/")
        fileName = event['target_Dir'].strip(" ") + "|"
        targetDir = targetDir + '/'
    file_path = 'data/' + event['phone_no'] + "/" + targetDir + event['file_name']
    
    fileName += event['file_name']
       
    file_type = event['file_name'][event['file_name'].rfind("."):]
    
    
    logger.info(file_path)
    s3 = boto3.client('s3')
    
    try:
        
        # s3_response = s3.put_object(Bucket=BUCKET_NAME, Key=file_path, Body=file_content)
        event["otptype"] = ""
        otpResp = LoginOrc.validateOTP(event, context)
        logger.info("OTP Val Response")
        logger.info(otpResp)
        oValResp = otpResp["otp"]
        
        if(event['is_Image'] == "Y"):
            s3_response = s3.get_object(Bucket=BUCKET_NAME, Key=file_path)
            logger.info(s3_response)
            file_content = s3_response.get("Body").read()
            img = Image.open(BytesIO(file_content))
            img = img.resize((int(constants.size[0]), int(constants.size[1])), Image.ANTIALIAS)
            buffer = BytesIO()
            img.save(buffer, 'JPEG')
            buffer.seek(0)
            img_file_path = 'thumbnails/' + event['phone_no'] + '/' + fileName
            s3_response = s3.put_object(Bucket=BUCKET_NAME, Key=img_file_path, Body=buffer, ContentType='image/jpeg')
            
        
        item_count=0
        stmt1 = 'select * from req_register where phoneNo="'+ event['phone_no']  + '" and fileName="' + fileName + '"'
        logger.info(stmt1)
        with conn.cursor() as cur:
            cur.execute(stmt1)
            for row in cur:
                item_count += 1
        
        if item_count>0:
            timestamp = datetime.now()
            stmt1 = 'update req_register set Timestamp="'+ timestamp.strftime("%Y-%m-%d, %H:%M:%S") +'" where fileName="'+ fileName +'" and phoneNo="' + event['phone_no'] + '"'
        else:
            stmt1 = 'insert into req_register (email_id, phoneNo, fileName, filePath, fileType, isImg) values( "' +  event['emailid']+ '","' +  event['phone_no'] + '","' + fileName + '","' + file_path + '","' + file_type + '","' + event['is_Image'] + '" )'
        logger.info(stmt1)
        with conn.cursor() as cur:
            cur.execute(stmt1)
            conn.commit()
        
    except Exception as e:
        logger.info("This is the error!!!!")
        logger.info(e)
        try:
            o = otpResp['otp']
        except Exception as e:
            return otpResp
            
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
            'file_path': file_path
        }
    }
    
    
    
def createFolder(event, context):    
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
    
    
    
    
    targetDir = ""
    fileName = ""
    if event['target_Dir'].strip(" ") != "":
        targetDir = event['target_Dir'].strip(" ").replace("|", "/")
        fileName = event['target_Dir'].strip(" ") + "|"
        targetDir = targetDir + '/'
    file_path = 'data/' + event['phone_no'] + "/" + targetDir + event['file_name']
    
    logger.info(event['target_Dir'])
    logger.info(event['target_Dir'].strip(" "))
    fileName += event['file_name']
        
    # file_path = 'data/' + event['phone_no'] + '/' + event['file_name']
    
    file_type = "Folder"
    
    
    logger.info(file_path)
    try:
        
        item_count=0
        stmt1 = 'select * from req_register where phoneNo="'+ event['phone_no']  + '" and fileName="' + fileName + '"'
        logger.info(stmt1)
        with conn.cursor() as cur:
            cur.execute(stmt1)
            for row in cur:
                item_count += 1
        
        if item_count>0:
            return {
                        'statusCode': 200,
                        'headers' : {
                            'Content-Type': 'application/json', 
                            'Access-Control-Allow-Origin': '*',
                        },
                        'body': {
                            'status' : 210,
                            'error_msg' : 'Folder Already Exists!'
                        }
                    }
        else:
            item_count = 0
            with conn.cursor() as cur:
                cur.execute("select * from registered_users where phone_no = \"" + event['phone_no'] + "\"")
                for row in cur:
                    item_count += 1
                    logger.info(row)
            emailid = row[2]
            stmt1 = 'insert into req_register (email_id, phoneNo, fileName, filePath, fileType, isImg) values( "' +  emailid + '","' +  event['phone_no'] + '","' + fileName + '","' + file_path + '","' + file_type + '","N" )'
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
            'file_path': file_path
        }
    }    