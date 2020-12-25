import json
import base64
import boto3
import os
import logging
import Constants
import random
from datetime import datetime

logger = logging.getLogger()
logger.setLevel(logging.INFO)

lambda_client = boto3.client('lambda')
sns_client = boto3.client('sns')


try:
    constants = Constants.Constants.getInstance()
except Exception as e:
    logger.error(e)
    sys.exit()
    
conn = constants.conn


def loginSignupEndpoint(event, context):
    opType = event['opType']
    if(opType=="Login"):
        logger.info("Proceeding into Login Action")
        return loginAction(event, context)
    elif(opType=="SignUp"):
        logger.info("Proceeding into SignUp Action")
        return signUpAction(event, context)
    elif(opType=="confirmLogin"):
        logger.info("Proceeding into Confirm Login Action")
        return confirmLogin(event, context)
    elif(opType=="Logout"):
        logger.info("Proceeding into LogOut Action")
        return logoutAction(event, context)
    elif(opType=="ResetPassword"):
        logger.info("Proceeding into Reset Password Action")
        return resetPassword(event, context)
    elif(opType=="checkUsrEmail"):
        logger.info("Proceeding into Usermane Email Check Action")
        return checkUsrEmailAvailability(event, context)
    elif(opType=="sendOTP"):
        logger.info("Proceeding into Send Email Action")
        return sendOTP(event, context)
    elif(opType=="validateOTP"):
        logger.info("Proceeding into validate OTP Action")
        return validateOTP(event, context)
    
    

def loginAction(event, context):
    #emailid = event['emailid']
    password = event['password']
    phoneNo = event['phone_no']
    authKey = ""
    try:
        authKey = event['auth_key']
    except Exception as e:
        logger.error(e)
        
    item_count = 0
    with conn.cursor() as cur:
        cur.execute("select * from registered_users where phone_no = \"" + phoneNo + "\"")
        for row in cur:
            item_count += 1
            logger.info(row)
            
    if(item_count >1): 
        return {
            'statusCode': 404,
            'headers' : {
                'Content-Type': 'application/json', 
                'Access-Control-Allow-Origin': '*',
            },
            'body': {
                'status' : 209,
                'error_msg' : 'Data Inconsistancy Detected!' 
            }
        }
    elif(item_count ==0):
        return {
            'statusCode': 404,
            'headers' : {
                'Content-Type': 'application/json', 
                'Access-Control-Allow-Origin': '*',
            },
            'body': {
                'status': 210,
                'error_msg': 'No Users Found!' 
            }
        }
    else:
        logger.info("Validating Password")
        if password==row[3]:
            logger.info("Password Correct")
            if row[7]=="Y":
                logger.info("Phone no verified")
                authKeyVal = "Y"
                logger.info("Checking User Login")
                if row[8]=="Y" and (authKey!=None and authKey.strip()!=""):
                    logger.info("User Login Detected")
                    oValResp=""
                    try:
                        event["otp"] = authKey
                        event["otptype"] = "login"
                        event['username'] = row[1]
                        logger.info("Going to Validate OTP")
                        otpResp = validateOTP(event, context)
                        logger.info(otpResp)
                        oValResp = otpResp["otp"]
                        oValResp = str(oValResp).strip()
                        logger.info(oValResp)
                        if oValResp!="T":
                            authKeyVal="N"
                    except Exception as e:
                        logger.info(e)
                        authKeyVal="N"
                        return otpResp
                        
                if row[8]=="N" or (row[8]=="Y" and authKeyVal=="Y"):
                    otp=""
                    try:    
                        event["otptype"] = "login"
                        otpResp = genOTP(event, context)
                        logger.info(otpResp)
                        otp = otpResp["otp"];
                    except Exception as e:
                        logger.error(e)        
                        return {
                                    'statusCode': 400,
                                    'headers' : {
                                        'Content-Type': 'application/json', 
                                        'Access-Control-Allow-Origin': '*',
                                    },
                                    'body': {
                                        'status' : 400,
                                        'error_msg': 'Internal Server Error' 
                                    }
                                }
                    return {
                        'statusCode': 200,
                        'headers' : {
                            'Content-Type': 'application/json', 
                            'Access-Control-Allow-Origin': '*',
                        },
                        'body': {
                            'status' : 200,
                            'id' : row[0],
                            'username': row[1],
                            'emailid' : row[2],
                            'occupation' : row[4],
                            'loginTime' : row[5],
                            'loginAttempt' : row[6],
                            'phoneVerified' : row[7],
                            'active_login' : row[8],
                            'phoneNo' : row[9],
                            'otp' : otp
                        }
                    }
                else:
                    event["otptype"] = "smsLogin"
                    otpgented = sendOTP(event, context)
                    return {
                                'statusCode': 200,
                                'headers' : {
                                    'Content-Type': 'application/json', 
                                    'Access-Control-Allow-Origin': '*',
                                },
                                'body': {
                                    'status' : 201,
                                    'error_msg': 'Need password Authentication!', 
                                    'otp' : otpgented['otp']
                                }
                            }    
            else:
                event["otptype"] = "smsLogin"
                otpgented = sendOTP(event, context)
                return {
                                'statusCode': 200,
                                'headers' : {
                                    'Content-Type': 'application/json', 
                                    'Access-Control-Allow-Origin': '*',
                                },
                                'body': {
                                    'status' : 401,
                                    'error_msg': 'Need To Confirm Phone Number!', 
                                    'otp' : otpgented['otp']
                                }
                            }   
        else:
            try:
                with conn.cursor() as cur:
                    stmt1="update registered_users set login_attempts = \"" + str(int(row[6])+1) + "\" where user_id= \""+ str(row[0]) + "\" and phone_no = \"" + row[9] + "\""
                    logger.info(stmt1)
                    cur.execute(stmt1)
                    conn.commit()   
            except Exception as e:
                logger.error(e)
                raise IOError(e)        
            return {
                        'statusCode': 200,
                        'headers' : {
                            'Content-Type': 'application/json', 
                            'Access-Control-Allow-Origin': '*',
                        },
                        'body': {
                            'status' : 209,
                            'error_msg': 'Password Incorrect!' 
                        }
                    }        
    
def signUpAction(event, context):
    userid = str(random.randint(0, 10))
    username = event['username']
    emailid = event['emailid']
    password = event['password']
    occupation = event['occupation']
    # logintime = event['logintime']
    phoneNo = event['phone_no']
    contCode = event["country_code"]
    loginattempts = 0
    emailverified="N"
    tier = "1"
    otp = ""
    logger.info("Start Select Query!")
    try:
        item_count = 0
        with conn.cursor() as cur:
            cur.execute("select * from registered_users where phone_no = \"" + phoneNo +"\"")
            for row in cur:
                item_count += 1
                logger.info(row)

        if(item_count >1): 
                return {
                    'statusCode': 404,
                    'headers' : {
                        'Content-Type': 'application/json', 
                        'Access-Control-Allow-Origin': '*',
                    },
                    'body': {
                        'status' : 209,
                        'error_msg' : 'Data Inconsistancy Detected!' 
                    }
                }
        elif(item_count==1):
            if password == row[3]:
                # event["otptype"] = "login"
                # otpResp = genOTP(event, context)
                # logger.info(otpResp)
                # otp = otpResp["otp"];
                return {
                            'statusCode': 200,
                            'headers' : {
                                'Content-Type': 'application/json', 
                                'Access-Control-Allow-Origin': '*',
                            },
                            'body': {
                                'status' : 201,
                                'error_msg': 'Users Already exists!',
                                # 'id' : row[0],
                                # 'username': row[1],
                                # 'emailid' : row[2],
                                # 'occupation' : row[4],
                                # 'loginTime' : row[5],
                                # 'loginAttempt' : row[6],
                                # 'phoneVerified' : row[7],
                                # 'active_login' : row[8],
                                # 'phoneNo' : row[9],
                                # 'otp' : otp
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
                                'status' : 208,
                                'statusMsg' : 'Failure',
                                'error_msg' : 'Phone Number Already exists!'
                            }
                        }
        else:
            logger.info("Creating a new user...")
        
            item_count = 0
            with conn.cursor() as cur:
                stmt1="select * from registered_users where username = \""+ username + "\""
                logger.info(stmt1)
                cur.execute(stmt1)
                item_count=0
                for row in cur:
                    item_count += 1
                conn.commit()
            if item_count==0:        
                
                with conn.cursor() as cur:
                    cur.execute("select number from no_generator where req_type = 'cust_id' and tier=\"" + tier + "\"")
                    for row in cur:
                        logger.info(row)
                    userid = userid + str(row[0] +1)
                    cur.execute("update infotrends_in.no_generator set number = \""+ str(row[0] + 1) + "\" where req_type = 'cust_id' and tier=\"" + tier + "\"")
                    logger.info(userid)
                    stmt1 = 'insert into registered_users (user_id, username, email_id, password, occupation, login_time, login_attempts, phone_verified, phone_no, country_code) values( "' +  userid + '","' + username + '","' + emailid + '","' + password + '","' + occupation + '"," ","' + str(loginattempts) + '","' + emailverified + '","'  + str(phoneNo) + '","'  + contCode + '" )'
                    logger.info(stmt1)
                    cur.execute(stmt1)
                    conn.commit()            
                    cur.execute("select * from registered_users where phone_no = \"" + phoneNo + "\" and password = \"" + password +"\"")
                    for row in cur:
                        item_count += 1

                event["otptype"] = "validateMob"
                otpResp = sendOTP(event, context)
                logger.info(userid)
                # event["otptype"] = "login"
                # otpResp = genOTP(event, context)
                # logger.info(otpResp)
                # otp = otpResp["otp"]
                
                return {
                            'statusCode': 200,
                            'headers' : {
                                'Content-Type': 'application/json', 
                                'Access-Control-Allow-Origin': '*',
                            },
                            'body': {
                                'status': 200,
                                'statusMsg':'Success!'
                                # 'id' : row[0],
                                # 'username': row[1],
                                # 'emailid' : row[2],
                                # 'occupation' : row[4],
                                # 'loginTime' : row[5],
                                # 'loginAttempt' : row[6],
                                # 'phoneVerified' : row[7],
                                # 'active_login' : row[8],
                                # 'phoneNo' : row[9],
                                # 'otp' : otp
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
                                'status' : 207,
                                'statusMsg' : 'Failure',
                                'error_msg' : 'Usermame Already exists!'
                            }
                        }
    except Exception as e:
        logger.error(e)
        if otp == "":
            return otpResp
        else:
            return {
                        'statusCode': 400,
                        'headers' : {
                        'Content-Type': 'application/json', 
                        'Access-Control-Allow-Origin': '*',
                        },
                        'body': {
                            'status' : 210,
                            'error_msg': 'IO ERROR! in SignUp Action'
                        }
                    }
        raise IOError(e)

    
def confirmLogin(event, context):
    #emailid = event['emailid']
    phoneNo = event['phone_no']
    username = event['username']
    otpval = ""
    try:
        event["otptype"] = ""
        otpvalresp = validateOTP(event, context)
        logger.info("OTP Val Response")
        logger.info(otpvalresp)
        otpval = otpvalresp['otp']
        userid = event['userid']
        if otpval=="T":
            with conn.cursor() as cur:
                stmt1="update registered_users set active_login = \"Y\" where user_id= \""+ str(userid) + "\" and phone_no = \"" + phoneNo + "\""
                logger.info(stmt1)
                cur.execute(stmt1)
                conn.commit()
            return {
                'statusCode': 200,
                'headers' : {
                    'Content-Type': 'application/json', 
                    'Access-Control-Allow-Origin': '*',
                },
                'body': {
                    'status' : 200,
                    'statusMsg' : 'Success'
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
                    'statusMsg' : 'Failure',
                    'error_msg' : 'OTP Authentication Failed'
                }
            }
    except Exception as e:
        logger.error(e)
        if otpval == "":
            return otpvalresp
        else:
            return {
                    'statusCode': 400,
                    'headers' : {
                        'Content-Type': 'application/json', 
                        'Access-Control-Allow-Origin': '*',
                    },
                    'body': {
                        'status' : 210,
                        'statusMsg' : 'Failure'
                    }
                }
            

def logoutAction(event, context):
    userid = event['userid']
    # emailid = event['emailid']
    phoneNo = event['phone_no']
    otpval = ""
    try:
        otpvalresp = validateOTP(event, context)
        logger.info("OTP Val Response")
        logger.info(otpvalresp)
        otpval = otpvalresp['otp']
        userid = event['userid']
        if otpval=="T":
            with conn.cursor() as cur:
                stmt1="update registered_users set active_login = \"N\" where user_id= \""+ str(userid) + "\" and phone_no = \"" + phoneNo + "\""
                logger.info(stmt1)
                cur.execute(stmt1)
                conn.commit()
                return {
                    'statusCode': 200,
                    'headers' : {
                        'Content-Type': 'application/json', 
                        'Access-Control-Allow-Origin': '*',
                    },
                    'body': {
                        'status' : 200,
                        'statusMsg' : 'Success'
                    }
                }
    except Exception as e:
        logger.error(e)
        if otpval == "":
            return otpvalresp
        else:
            return {
                    'statusCode': 400,
                    'headers' : {
                        'Content-Type': 'application/json', 
                        'Access-Control-Allow-Origin': '*',
                    },
                    'body': {
                        'status' : 210,
                        'statusMsg' : 'Failure'
                    }
                }
            
 
def resetPassword(event, context):
    userid = event['userid']
    emailid = event['emailid']
    password = event['password']
    phoneNo = event['phone_no']
    otpval = ""
    try:
        otpvalresp = validateOTP(event, context)
        logger.info("OTP Val Response")
        logger.info(otpvalresp)
        otpval = otpvalresp['otp']
        userid = event['userid']
        if otpval=="T":
            with conn.cursor() as cur:
                stmt1="update registered_users set password = \""+ password +"\", login_attempts =\"0\" where user_id= \""+ str(userid) + "\" and phone_no = \"" + phoneNo + "\""
                logger.info(stmt1)
                cur.execute(stmt1)
                conn.commit()
                return {
                    'statusCode': 200,
                    'headers' : {
                        'Content-Type': 'application/json', 
                        'Access-Control-Allow-Origin': '*',
                    },
                    'body': {
                        'status' : 200,
                        'statusMsg' : 'Success'
                    }
                }
    except Exception as e:
        logger.error(e)
        if otpval == "":
            return otpvalresp
        else:
            return {
                    'statusCode': 400,
                    'headers' : {
                        'Content-Type': 'application/json', 
                        'Access-Control-Allow-Origin': '*',
                    },
                    'body': {
                        'status' : 210,
                        'statusMsg' : 'Failure'
                    }
                }
            

def checkUsrEmailAvailability(event, context):
    username = event['username']
    emailid = event['emailid']
    phoneNo = event['phone_no']
    try:
        with conn.cursor() as cur:
            stmt1="select * from registered_users where username = \""+ username + "\""
            logger.info(stmt1)
            cur.execute(stmt1)
            item_count=0
            for row in cur:
                item_count += 1
            conn.commit()
        if item_count==0:
            with conn.cursor() as cur:
                stmt1="select * from registered_users where phone_no = \""+ phoneNo + "\""
                logger.info(stmt1)
                cur.execute(stmt1)
                item_count=0
                for row in cur:
                    item_count += 1
                conn.commit()
            if item_count==0:
                return {
                    'statusCode': 200,
                    'headers' : {
                        'Content-Type': 'application/json', 
                        'Access-Control-Allow-Origin': '*',
                    },
                    'body': {
                        'status' : 200,
                        'statusMsg' : 'Success'
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
                        'status' : 208,
                        'statusMsg' : 'Failure',
                        'error_msg' : 'Phone Number Already exists!'
                    }
                }
        else:
            with conn.cursor() as cur:
                stmt1="select * from registered_users where phone_no = \""+ phoneNo + "\""
                logger.info(stmt1)
                cur.execute(stmt1)
                item_count=0
                for row in cur:
                    item_count += 1
                conn.commit()
            if item_count==0:            
                return {
                    'statusCode': 200,
                    'headers' : {
                        'Content-Type': 'application/json', 
                        'Access-Control-Allow-Origin': '*',
                    },
                    'body': {
                        'status' : 207,
                        'statusMsg' : 'Failure',
                        'error_msg' : 'Usermame Already exists!'
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
                        'status' : 209,
                        'statusMsg' : 'Failure',
                        'error_msg' : 'Both Phone Number And Usermame Already exist!'
                    }
                }
    except Exception as e:
        logger.error(e)
        return {
                'statusCode': 400,
                'headers' : {
                    'Content-Type': 'application/json', 
                    'Access-Control-Allow-Origin': '*',
                },
                'body': {
                    'status' : 210,
                    'statusMsg' : 'Failure'
                }
            }
            

def genOTP(event, context):
    phoneNo = event["phone_no"]
    event["type"] = event["otptype"]
    try:
        otp = ""
        if (event["type"]=="login" or event["type"]=="resetpass"):
            for i in range(0,6):
                otp = otp + str(random.randint(1,9))
        else:
            for i in range(0,4):
                otp = otp + str(random.randint(1,9))
            
        logger.info("OTP :")
        logger.info(otp)
        event["otp"] = str(otp)
        
        item_count = 0
        with conn.cursor() as cur:
            cur.execute("select * from registered_users where phone_no = \"" + phoneNo + "\"")
            for row in cur:
                item_count += 1
                logger.info(row)
                
        if(item_count >1): 
            return {
                'statusCode': 404,
                'headers' : {
                    'Content-Type': 'application/json', 
                    'Access-Control-Allow-Origin': '*',
                },
                'body': {
                    'status' : 209,
                    'error_msg' : 'Data Inconsistancy Detected!' 
                }
            }
        elif(item_count ==0):
            return {
                'statusCode': 404,
                'headers' : {
                    'Content-Type': 'application/json', 
                    'Access-Control-Allow-Origin': '*',
                },
                'body': {
                    'status': 210,
                    'error_msg': 'No Users Found!' 
                }
            }
        else:
            event["username"] = row[1].upper()
            event["country_code"] = row[10]
            contCode = event["country_code"]
            userid = row[0]
            timestamp = datetime.now()
            with conn.cursor() as cur:
                stmt1='insert into otp_validator (otp, user_id, timestamp) values( "' + str(otp)  + '","'  + str(userid)  + '","'  + timestamp.strftime("%m-%d-%Y, %H:%M:%S")+ '" )' 
                logger.info(stmt1)
                cur.execute(stmt1)
                conn.commit()
        
        logger.info(event)

        return {
            "otp" : otp
        };
    except Exception as e:
        logger.error(e)
    
            
def sendOTP(event, context):
    logger.info("Inside send OTP")
    phoneNo = event["phone_no"]
    event["type"] = event["otptype"]
    otp = ""
    try:
        logger.info("Generate OTP Start")
        otpgen=genOTP(event, context)
        logger.info("Generate OTP SEnding----->")
        otp = otpgen["otp"]
    except Exception as e:
        logger.error(e)
        return otpgen

    try:
        
        logger.info("Abt to send OTP Start")
        if otp != "":
            
            #To be commented until in Live use to prevent Excess OTP Generation.
            logger.info("Invoking the mail function!")
            invoke_response = lambda_client.invoke(FunctionName="SendOTPSmS",
                                               InvocationType='Event',
                                               Payload=json.dumps(event))
            logger.info("Response Received.")
            # invoke_response = {}
            # invoke_response['otp'] = otp
            return invoke_response['ResponseMetadata']
    except Exception as e:
        logger.error(e)
        
        
def validateOTP(event, context):
    otp = event["otp"]
    username = event["username"]
    item_count = 0
    logger.info("Inside Validate OTP")
    try:
        with conn.cursor() as cur:
            stmt1="select * from otp_validator where otp = \"" + str(otp)  + '" and status="A" and user_id = ( select user_id from registered_users where username = \"' + username + "\")"
            logger.info(stmt1)
            cur.execute(stmt1)
            for row in cur:
                item_count += 1
                logger.info(row)
            if(item_count >1): 
                return {
                    'statusCode': 404,
                    'headers' : {
                        'Content-Type': 'application/json', 
                        'Access-Control-Allow-Origin': '*',
                    },
                    'body': {
                        'status' : 209,
                        'error_msg' : 'Data Inconsistancy Detected!' 
                    }
                }
            elif(item_count ==0):
                return {
                    'statusCode': 404,
                    'headers' : {
                        'Content-Type': 'application/json', 
                        'Access-Control-Allow-Origin': '*',
                    },
                    'body': {
                        'status': 710,
                        'error_msg': 'Invalid OTP' 
                    }
                }
            else:
                rotp = row[1]
                ruser = row[2]
                event['userid'] = ruser
                rtimestamp = row[3]
                rstatus = row[4]
                currtime = datetime.now()
                logger.info(currtime)
                logger.info(rtimestamp)
                timediff = (currtime - datetime.strptime(rtimestamp, "%m-%d-%Y, %H:%M:%S")).total_seconds()
                logger.info(timediff)
                
                if event["opType"]=="validateOTP":
                    tlimit = constants.otp_expiry_time 
                elif event["opType"]=="Logout":
                    tlimit = constants.auth_key_expiry_time_signOut     
                else:
                    tlimit = constants.auth_key_expiry_time 
                
                if timediff>tlimit:
                    with conn.cursor() as cur:
                        stmt1="update otp_validator set status=\"I\" where otp = \"" + str(otp)  + '" and user_id = "' + ruser + "\""
                        logger.info(stmt1)
                        cur.execute(stmt1)
                        if event["opType"]!="validateOTP":
                            phoneNo = event["phone_no"]
                            stmt1="update registered_users set active_login = \"N\" where user_id= \""+ str(ruser) + "\" and phone_no = \"" + phoneNo + "\""
                            logger.info(stmt1)
                            cur.execute(stmt1)
                        conn.commit()
                    return {
                        'statusCode': 404,
                        'headers' : {
                            'Content-Type': 'application/json', 
                            'Access-Control-Allow-Origin': '*',
                        },
                        'body': {
                            'status': 755,
                            'error_msg': 'OTP has expired!' 
                        }
                    }
                
                else:
                    if event["opType"]=="validateOTP":
                        with conn.cursor() as cur:
                            stmt1="update registered_users set phone_verified=\"Y\" where user_id = \"" + ruser + "\""
                            logger.info(stmt1)
                            cur.execute(stmt1)
                            stmt1="update otp_validator set status=\"I\" where otp = \"" + str(otp)  + '" and user_id = \"' + ruser + "\""
                            logger.info(stmt1)
                            cur.execute(stmt1)
                            conn.commit()
                        return {
                            'statusCode': 200,
                            'headers' : {
                                'Content-Type': 'application/json', 
                                'Access-Control-Allow-Origin': '*',
                            },
                            'body': {
                                'status': 200,
                                'error_msg': '',
                                'statusMsg' : "Success"
                            }
                        }
                    elif event["otptype"] == "login":
                        with conn.cursor() as cur:
                            stmt1="update otp_validator set status=\"I\" where otp = \"" + str(otp)  + '" and user_id = \"' + ruser + "\""
                            logger.info(stmt1)
                            cur.execute(stmt1)
                            conn.commit()
                        return {
                            'otp' : 'T'
                        }
                    else:
                        return {
                            'otp' : "T"
                        }
    except Exception as e:
        logger.error(e)
        return {
                'statusCode': 400,
                'headers' : {
                    'Content-Type': 'application/json', 
                    'Access-Control-Allow-Origin': '*',
                },
                'body': {
                    'status' : 210,
                    'statusMsg' : 'Failure'
                }
            }
    
