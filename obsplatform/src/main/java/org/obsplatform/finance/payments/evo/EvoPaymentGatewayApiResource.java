package org.obsplatform.finance.payments.evo;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.core.serialization.ToApiJsonSerializer;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.portfolio.client.data.ClientAdditionalData;
import org.obsplatform.portfolio.client.data.ClientData;
import org.obsplatform.portfolio.client.exception.ClientNotFoundException;
import org.obsplatform.portfolio.client.service.ClientReadPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/evo")
@Component
@Scope("singleton")

/**
 * 
 * @author raghu
 *
 */
public class EvoPaymentGatewayApiResource {
	
	 private final static Logger logger = LoggerFactory.getLogger(EvoPaymentGatewayApiResource.class);
	
	private static PlatformSecurityContext context;
	private static ToApiJsonSerializer<ClientAdditionalData> jsonSerializer;
	private final ClientReadPlatformService clientReadPlatformService;

	@Autowired
	public EvoPaymentGatewayApiResource(final PlatformSecurityContext securitycontext, final ToApiJsonSerializer<ClientAdditionalData> toApiJsonSerializer,
			final ClientReadPlatformService clientReadPlatformService) {
		context = securitycontext;
		jsonSerializer = toApiJsonSerializer;
		this.clientReadPlatformService = clientReadPlatformService;
	}
	
	public static String blowfishProcess(final String apiRequestBodyAsJson,@PathParam("method") final String method) {
		
	     try{	    	
	    	 JSONObject jsonData  = new JSONObject(apiRequestBodyAsJson);
	    	     Blowfish bf	= new Blowfish("9b_JY3m=2t*Pa)T8",new HexCoder());//--live
	    	    //Blowfish bf   = new Blowfish("yK(9[t8LdW*42!aA",new HexCoder());//--test
	    	 String encrypt_decrypt_String = "";
	    	 if(method.equalsIgnoreCase("encrypt")){
	    		 encrypt_decrypt_String = bf.encrypt(jsonData.getString("text"), jsonData.getInt("length"));
	    		 return encrypt_decrypt_String;
	    	 }else if(method.equalsIgnoreCase("decrypt")){
	    		 encrypt_decrypt_String = bf.decrypt(jsonData.getString("text"), jsonData.getInt("length"));
	    	 }
	    	 JSONObject jsonObj = new JSONObject();
	    	 jsonObj.put("blowfishData", encrypt_decrypt_String);
	    	 return jsonSerializer.serialize(jsonObj);
		}catch (Exception e) {
		       // return e.getMessage();
			throw new PlatformDataIntegrityException(e.getMessage(),e.getLocalizedMessage(),"length",e.getStackTrace().getClass());
	    }
	
	}

	@POST
	@Path("{method}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public static String blowfishEncrpt(final String apiRequestBodyAsJson,@PathParam("method") final String method){
		 context.authenticatedUser();
		 return blowfishProcess(apiRequestBodyAsJson, method);
	}
	
	@POST
	@Path("/encriptmode")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String amountCal(final String apiRequestBodyAsJson){
	     try{
	    	/* context.authenticatedUser();pg_57966
	    	 JSONObject jsonData  = new JSONObject(apiRequestBodyAsJson);
	    	 double amount = jsonData.getDouble("amount");
	    	 Long clientId = jsonData.getLong("clientId");
	    	 String merchantId = jsonData.getString("merchantId");
	    	 String currencyType = jsonData.getString("currencyType");
	    	 
	    	 
	    	// String data = "*174*pg_57966*100*GBP";
	    	 
	    	 String HmacKey = "f]7C8bW[c!4ET9x?5j)XH=6e2Fo*Gw(3";
	    	 
	    	 JSONObject jsonObj = new JSONObject();
	    	 jsonObj.put("amount", amount*100);
	    	 
	    	 String data = "*"+clientId+"*"+merchantId+"*"+jsonObj.getInt("amount")+"*"+currencyType; 
	    	
	    	 Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
	    	 SecretKeySpec secret_key = new SecretKeySpec(HmacKey.getBytes("UTF-8"), "HmacSHA256");
	    	 sha256_HMAC.init(secret_key);
	    	 jsonObj.put("macValue", Hex.encodeHexString(sha256_HMAC.doFinal(data.getBytes("UTF-8"))));
	    	 
	    	 return this.jsonSerializer.serialize(jsonObj);*/
	    	 
	    	 context.authenticatedUser();
	      	 JSONObject jsonData  = new JSONObject(apiRequestBodyAsJson);
	      	 
	      	 Long clientId 		 = jsonData.getLong("clientId");
	      	 
	      	 ClientData clientData = this.clientReadPlatformService.retrieveOne(clientId);
	      	 if(clientData == null){ throw new ClientNotFoundException(clientId);}
	      	 
	      	 Long transID		 = clientId;
	      	 String refNr 		 = jsonData.getString("RefNr");
	      	 Double amount 		 = jsonData.getDouble("amount");
	      	 String orderDesc	 = jsonData.getString("OrderDesc");
	      	 String merchantId 	 = jsonData.getString("merchantId");
	      	 String userData 	 = jsonData.getString("UserData");
	      	 String firstName 	 = clientData.getFirstname();
	      	 String lastName 	 = clientData.getLastname();
	      	 String city 		 = clientData.getCity();
	      	 String state 		 = clientData.getState();
	      	 String phone 		 = clientData.getPhone();
	      	 String eMail 		 = clientData.getEmail();
	      	 String addrStreet 	 = clientData.getAddressNo();
	      	 String addrZip 	 = clientData.getZip();
	      	 String reqId		 = refNr;
	      	 String currency	 = "GBP";
	      	 String response	 = "encrypt";
	      	 String urlBack		 = jsonData.getString("origin");
	      	 String urlSuccess	 = urlBack+"#/evosuccess";
	      	 String urlFailure	 = urlBack+"#/evosuccess";
	      	 String obsCl 		 = jsonData.getString("obsCl");
	      	 String urlNotify	 = obsCl+"/evowar/login";
	      	 String rtf 	     = jsonData.getString("RTF");
	      	 String AccVerify    = jsonData.getString("AccVerify");
	      	 
	      	 String capture = null;
	      	 if(AccVerify.equalsIgnoreCase("yes") && (rtf.equalsIgnoreCase("I") || rtf.equalsIgnoreCase("R"))){
	      		capture	= "MANUAL";
	      	 }else{
	      		capture = "AUTO";
	      	 }
	      	 
	      	 logger.info(userData + "\r\n");
	      	 
	      	String HmacKey = "f]7C8bW[c!4ET9x?5j)XH=6e2Fo*Gw(3";//-----live
	      	//String HmacKey = "Ny6_(2LaR!g37mW)tD=45?kHrS*89fT]";//-----test
	      	 
	      	JSONObject amountjsonObj = new JSONObject();
	      	amountjsonObj.put("multiplyAmount", amount*100);
	      	
	      	logger.info(amount*100 + "\r\n");
	      	logger.info(amountjsonObj.getInt("multiplyAmount") + "\r\n");
	    	 
	      	 //String data = "*"+clientId+"*"+merchantId+"*"+jsonObj.getInt("amount")+"*"+currencyType; 
	      	 String macDataString = "*"+clientId+"*"+merchantId+"*"+amountjsonObj.getInt("multiplyAmount")+"*"+currency; 
	       	 logger.info(macDataString + "\r\n");
	      	 Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
	      	 SecretKeySpec secret_key = new SecretKeySpec(HmacKey.getBytes("UTF-8"), "HmacSHA256");
	      	 sha256_HMAC.init(secret_key);
	      	 String MAC = Hex.encodeHexString(sha256_HMAC.doFinal(macDataString.getBytes("UTF-8")));
	      	 
	      	 String dataString = "TransID="+transID+"&RefNr="+refNr+"&amount="+amountjsonObj.getInt("multiplyAmount")+"&FirstName="+firstName+"&" +
	    					"LastName="+lastName+"&AddrCity="+city+"&AddrState="+state+"&" +
	    					"phone="+phone+"&E-Mail="+eMail+"&Currency="+currency+"&OrderDesc="+orderDesc+"&" +
	    					"Response="+response+"&MAC="+MAC+"&" +
	    					"URLSuccess="+urlSuccess+"&" +
	    					"URLFailure="+urlFailure+"&" +
	    					"URLNotify="+urlNotify+"&" +
	    					"UserData="+userData+"&ReqId="+reqId+"&URLBack="+urlBack+"&Capture="+capture;
	      	
	      	logger.info("Evo dataString : " + dataString);
	      	 
	    	if(!"".equalsIgnoreCase(rtf)){ 
	    		dataString = dataString+"&RTF="+rtf;
	    	}
	    	
	    	if(!"".equalsIgnoreCase(AccVerify)){ 
	    		dataString = dataString+"&AccVerify="+AccVerify;
	    	}
	      	
	    	logger.info("Evo dataString : " + dataString);
	    	
	      	 if(addrStreet != null|| addrStreet !=""){
	      		 dataString = dataString+"&AddrStreet="+addrStreet;
	      	 }
	      	 if(addrZip != null|| addrZip !=""){
	      		 dataString = dataString+"&AddrZip="+addrZip;
	      	 }
	      	 
	      	 int len = dataString.length();
	      	 
	      	 JSONObject postObj = new JSONObject();
	      	 postObj.put("text",dataString);
	      	 postObj.put("length",len);
	      	 
	      	 String blowfishData = blowfishEncrpt(postObj.toString(), "encrypt");
	      	 
	      	 JSONObject returnjsonObj = new JSONObject();
	      	 returnjsonObj.put("blowfishData", blowfishData);
	      	 returnjsonObj.put("len", len);
	      	 
	      	 return jsonSerializer.serialize(returnjsonObj);
	    	 
		}catch (Exception e) {
		        return e.getMessage();
	    }
	}

}
