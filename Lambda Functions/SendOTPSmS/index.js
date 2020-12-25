const aws =  require("aws-sdk");
var snsmessage = "";
const sns = new aws.SNS({
   region:'ap-southeast-1'
});
exports.handler = function(event, context, callback) {
   console.log("AWS lambda and SNS trigger ");
   console.log(event);
   var smsattrs = {
        'AWS.SNS.SMS.SenderID': { 'DataType': 'String', 'StringValue': 'TestSender' },
        'AWS.SNS.SMS.SMSType': { 'DataType': 'String', 'StringValue': 'Transactional'}
    };
   if(event.type == 'smsLogin') {
       snsmessage = "Hi " + event.username + ",\nYour OTP for Login is:" + event.otp ;
   } else if(event.type == 'smsReset') {
       snsmessage = "Hi " + event.username + ",\nYour OTP to Rest your Password is:" + event.otp ;
   } else if(event.type == 'validateMob') {
       snsmessage = "Hi " + event.username + ",\nThank You for for Using SmartSave! Your OTP to Validate Your Number is:" + event.otp ;
   } else if(event.type == 'CustomMsgs') {
       snsmessage = event.CustomMsgs;
   } else {
      snsmessage="";
      return {
         "ResponseMetadata" : {
           "Error_msg": "Unable to send message due ro Internal Server Error!"
         }
      };
   }
   console.log(snsmessage);
   sns.publish({
      Message: snsmessage,
      PhoneNumber: event.country_code + event.phone_no,
      MessageAttributes : smsattrs
   }, function (err, data) {
      if (err) {
         console.log(err);
         callback(err, null);
      } else {
         console.log(data);
         callback(null, data);
      }	
   });
};