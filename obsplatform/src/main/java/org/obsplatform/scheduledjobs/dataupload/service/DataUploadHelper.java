package org.obsplatform.scheduledjobs.dataupload.service;

import java.io.File;
import java.io.FileWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.obsplatform.finance.adjustment.data.AdjustmentData;
import org.obsplatform.finance.adjustment.exception.AdjustmentCodeNotFoundException;
import org.obsplatform.finance.adjustment.service.AdjustmentReadPlatformService;
import org.obsplatform.finance.payments.data.McodeData;
import org.obsplatform.finance.payments.exception.PaymentCodeNotFoundException;
import org.obsplatform.finance.payments.service.PaymentReadPlatformService;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.organisation.address.data.CityDetailsData;
import org.obsplatform.organisation.address.service.AddressReadPlatformService;
import org.obsplatform.organisation.mcodevalues.api.CodeNameConstants;
import org.obsplatform.organisation.mcodevalues.data.MCodeData;
import org.obsplatform.organisation.mcodevalues.service.MCodeReadPlatformService;
import org.obsplatform.portfolio.property.data.PropertyDefinationData;
import org.obsplatform.portfolio.property.service.PropertyReadPlatformService;
import org.obsplatform.scheduledjobs.dataupload.data.MRNErrorData;
import org.obsplatform.scheduledjobs.dataupload.domain.DataUpload;
import org.obsplatform.scheduledjobs.dataupload.domain.DataUploadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


@Service
public class DataUploadHelper {
	
	private final DataUploadRepository dataUploadRepository;
	private final AdjustmentReadPlatformService adjustmentReadPlatformService;
	private final PaymentReadPlatformService paymodeReadPlatformService;
	private final MCodeReadPlatformService mCodeReadPlatformService;
	private final PropertyReadPlatformService propertyReadPlatformService;
	private final AddressReadPlatformService addressReadPlatformService;
	
	@Autowired
	public DataUploadHelper(final DataUploadRepository dataUploadRepository,final PaymentReadPlatformService paymodeReadPlatformService,
			final AdjustmentReadPlatformService adjustmentReadPlatformService,final MCodeReadPlatformService mCodeReadPlatformService,
		final PropertyReadPlatformService propertyReadPlatformService,final AddressReadPlatformService addressReadPlatformService) {
		this.dataUploadRepository=dataUploadRepository;
		this.paymodeReadPlatformService=paymodeReadPlatformService;
		this.adjustmentReadPlatformService=adjustmentReadPlatformService;
		this.mCodeReadPlatformService=mCodeReadPlatformService;
		this.propertyReadPlatformService=propertyReadPlatformService;
		this.addressReadPlatformService=addressReadPlatformService;
		
	}
	
	

	
	
	public String buildJsonForHardwareItems(String[] currentLineData, ArrayList<MRNErrorData> errorData, int i)  {
		
		if(currentLineData.length>=8){
			final HashMap<String, String> map = new HashMap<>();
			map.put("itemMasterId",currentLineData[0]);
			map.put("serialNumber",currentLineData[1]);
			map.put("grnId",currentLineData[2]);
			map.put("provisioningSerialNumber",currentLineData[3]);
			map.put("quality", currentLineData[4]);
			map.put("status",currentLineData[5]);
			map.put("warranty", currentLineData[6]);
			map.put("remarks", currentLineData[7]);
			map.put("itemModel", currentLineData[8]);
			map.put("locale", "en");
			return new Gson().toJson(map);
		}else{
			errorData.add(new MRNErrorData((long)i, "Improper Data in this line"));
			return null;
			
		}
	}


	public String buildJsonForMrn(String[] currentLineData, ArrayList<MRNErrorData> errorData, int i) {
		
		if(currentLineData.length>=2){
			final HashMap<String, String> map = new HashMap<>();
			map.put("mrnId",currentLineData[0]);
			map.put("serialNumber",currentLineData[1]);
			map.put("type",currentLineData[2]);
			map.put("locale","en");
			return new Gson().toJson(map);
		}else{
			errorData.add(new MRNErrorData((long)i, "Improper Data in this line"));
			return null;
		}
		
	}


	public String buildJsonForMoveItems(String[] currentLineData, ArrayList<MRNErrorData> errorData, int i) {
		
		if(currentLineData.length>=2){
			final HashMap<String, String> map = new HashMap<>();
			map.put("itemId",currentLineData[0]);
			map.put("serialNumber",currentLineData[1]);
			map.put("type",currentLineData[2]);
			map.put("locale","en");
			return new Gson().toJson(map);
		}else{
			errorData.add(new MRNErrorData((long)i, "Improper Data in this line"));
			return null;
		}
	}


	public String buildJsonForEpg(String[] currentLineData, ArrayList<MRNErrorData> errorData, int i) throws ParseException {
		
		if(currentLineData.length >=11){
			final HashMap<String, String> map = new HashMap<>();
			map.put("channelName",currentLineData[0]);
			map.put("channelIcon",currentLineData[1]);
			map.put("programDate",new SimpleDateFormat("dd/MM/yyyy").parse(currentLineData[2]).toString());
			map.put("startTime",currentLineData[3]);
			map.put("stopTime",currentLineData[4]);
			map.put("programTitle",currentLineData[5]);
			map.put("programDescription",currentLineData[6]);
			map.put("type",currentLineData[7]);
			map.put("genre",currentLineData[8]);
			map.put("locale",currentLineData[9]);
			map.put("dateFormat",currentLineData[10]);
			map.put("locale","en");
				return new Gson().toJson(map);
		}else{
			errorData.add(new MRNErrorData((long)i, "Improper Data in this line"));
			return null;
		}


	}


	public String buildJsonForAdjustment(String[] currentLineData, ArrayList<MRNErrorData> errorData, int i) {

		if(currentLineData.length >= 6){
			final HashMap<String, String> map = new HashMap<>();
			List<AdjustmentData> adjustmentDataList=this.adjustmentReadPlatformService.retrieveAllAdjustmentsCodes();
			if(!adjustmentDataList.isEmpty()){
				 for(AdjustmentData adjustmentData:adjustmentDataList){
					   if( adjustmentData.getAdjustmentCode().equalsIgnoreCase(currentLineData[2].toString())){ 
					        	 
						   map.put("adjustment_code", adjustmentData.getId().toString());
						   break;
					   }else{
						   map.put("adjustment_code", String.valueOf(-1));	
					   }
				    }
				 String adjustmentCode = map.get("adjustment_code");
				   if(adjustmentCode!=null && Long.valueOf(adjustmentCode)<=0){
				    	
				    	throw new AdjustmentCodeNotFoundException(currentLineData[2].toString());
				    }
				map.put("adjustment_date", currentLineData[1]);
				map.put("adjustment_type",currentLineData[3]);
				map.put("amount_paid",currentLineData[4]);
				map.put("Remarks",currentLineData[5]);
				map.put("locale", "en");
				map.put("dateFormat","dd MMMM yyyy");
				return new Gson().toJson(map);	
			}else{
				errorData.add(new MRNErrorData((long)i, "Adjustment Type list is empty"));
				return null;
			}
		}else{
			errorData.add(new MRNErrorData((long)i, "Improper Data in this line"));
			return null;
		}
	}
	
	//datauplod for refund

	public String buildJsonForRefund(String[] currentLineData, ArrayList<MRNErrorData> errorData, int i) {

		if(currentLineData.length>=3){
					final HashMap<String, String> map = new HashMap<>();
				map.put("refundAmount", currentLineData[1]);
				map.put("refundMode", currentLineData[2]);
				map.put("locale", "en");
				
				return new Gson().toJson(map);	
			
		}else{
			errorData.add(new MRNErrorData((long)i, "Improper Data in this line"));
			return null;
		}
	}


	public String buildjsonForPayments(String[] currentLineData, ArrayList<MRNErrorData> errorData, int i) {
		
	
		if(currentLineData.length >=6){
			final HashMap<String, String> map = new HashMap<>();
			Collection<McodeData> paymodeDataList = this.paymodeReadPlatformService.retrievemCodeDetails("Payment Mode");
				if(!paymodeDataList.isEmpty()){
					for(McodeData paymodeData:paymodeDataList){
						if(paymodeData.getPaymodeCode().equalsIgnoreCase(currentLineData[2].toString())){
							map.put("paymentCode",paymodeData.getId().toString());
							break;
						}else{
							map.put("paymentCode","-1");
						}
					}
					String paymentCode = map.get("paymentCode");
					if(paymentCode!=null && Long.valueOf(paymentCode) <=0){
						throw new PaymentCodeNotFoundException(currentLineData[2].toString());
					}
					map.put("clientId", currentLineData[0]);
					map.put("paymentDate",currentLineData[1]);
					map.put("amountPaid", currentLineData[3]);
					map.put("remarks",  currentLineData[4]);
					map.put("locale", "en");
					map.put("dateFormat","dd MMMM yyyy");
					map.put("receiptNo",currentLineData[4]);
        	return new Gson().toJson(map);
        }else{
        	errorData.add(new MRNErrorData((long)i, "Paymode type list empty"));
			return null;
        }
		 
	}else{
		errorData.add(new MRNErrorData((long)i, "Improper Data in this line"));
		return null;
	}
	}


	public String buildForMediaAsset(Row mediaRow) throws JSONException {
		//final HashMap<String, Object> map = new HashMap<>();
				JsonObject map = new JsonObject();
				map.addProperty("mediaTitle",mediaRow.getCell(0).getStringCellValue());//-
				map.addProperty("mediatype",mediaRow.getCell(1).getStringCellValue());//-
				map.addProperty("catageoryId",(int)mediaRow.getCell(2).getNumericCellValue());//-
				map.addProperty("mediaImage",mediaRow.getCell(3).getStringCellValue());//- 
				map.addProperty("duration",(int)mediaRow.getCell(4).getNumericCellValue());//-
				map.addProperty("cpShareValue",(int)mediaRow.getCell(5).getNumericCellValue());//-
				/*map.put("subject",mediaRow.getCell(6).getStringCellValue());//-
				map.put("overview",mediaRow.getCell(7).getStringCellValue());//-
		*/		map.addProperty("contentProvider",(int)mediaRow.getCell(6).getNumericCellValue());//-
				/*map.put("rated",mediaRow.getCell(9).getStringCellValue());//-
		*/		//map.put("rating",mediaRow.getCell(10).getNumericCellValue());//-
				map.addProperty("mediaRating",(int)mediaRow.getCell(7).getNumericCellValue());//-
				map.addProperty("status",mediaRow.getCell(8).getStringCellValue());//-
				SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");
				map.addProperty("releaseDate",formatter.format(mediaRow.getCell(9).getDateCellValue()));
				map.addProperty("dateFormat",mediaRow.getCell(10).getStringCellValue());
				map.addProperty("locale",mediaRow.getCell(11).getStringCellValue());
				map.addProperty("mediaTypeCheck", "ADVANCEMEDIA");
						
					//JSONArray a = new JSONArray();
					JsonArray a = new JsonArray();
					//JsonArray ab = new JsonArray();
					//Map<String, Object> m = new LinkedHashMap<String, Object>();
					JsonObject m = new JsonObject();
					m.addProperty("attributeType",mediaRow.getCell(12).getStringCellValue());
					m.addProperty("attributeName", (int)mediaRow.getCell(13).getNumericCellValue());
					m.addProperty("attributevalue", mediaRow.getCell(14).getStringCellValue());
					m.addProperty("attributeNickname", mediaRow.getCell(15).getStringCellValue());
					m.addProperty("attributeImage", mediaRow.getCell(16).getStringCellValue());
					
					a.add(m);
					//a.put(m);
					map.add("mediaassetAttributes",a);
					
					
					JsonArray b = new JsonArray();
					//Map<String, Object> n = new LinkedHashMap<String, Object>();
					JsonObject n = new JsonObject();
					n.addProperty("languageId",(int)mediaRow.getCell(17).getNumericCellValue());	
					n.addProperty("formatType",mediaRow.getCell(18).getStringCellValue());
					n.addProperty("location",mediaRow.getCell(19).getStringCellValue());
					b.add(n);
					
					map.add("mediaAssetLocations",b);
					System.out.println(map);
					System.out.println(map.toString());
					return map.toString();
	}


	public String buildforitemSale(String[] currentLineData,ArrayList<MRNErrorData> errorData, int i)  throws ParseException {
		final HashMap<String, String> map = new HashMap<>();
		if(currentLineData.length>=6){
		map.put("agentId",currentLineData[0]);
		map.put("itemId",currentLineData[1]);
		map.put("orderQuantity",currentLineData[2]);
		map.put("chargeAmount",currentLineData[3]);
		map.put("taxPercantage", currentLineData[4]);
		
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MMMM-yy");
    	Date date=formatter.parse(currentLineData[5]);
    	SimpleDateFormat formatter1 = new SimpleDateFormat("dd MMMM yyyy");
    	   
    	map.put("locale", "en");
    	map.put("dateFormat","dd MMMM yyyy");
    	map.put("purchaseDate",formatter1.format(date));
    	return new Gson().toJson(map);
		}else{
			errorData.add(new MRNErrorData((long)i, "Improper Data in this line"));
		return null;
		}
	}

	public CommandProcessingResult updateFile(DataUpload dataUpload,Long totalRecordCount, Long processRecordCount,
			ArrayList<MRNErrorData> errorData) {
		
		dataUpload.setProcessRecords(processRecordCount);
		dataUpload.setUnprocessedRecords(totalRecordCount-processRecordCount);
		dataUpload.setTotalRecords(totalRecordCount);
		writeCSVData(dataUpload.getUploadFilePath(), errorData,dataUpload);
		processRecordCount=0L;totalRecordCount=0L;
		this.dataUploadRepository.save(dataUpload);
		final String filelocation=dataUpload.getUploadFilePath();
		dataUpload=null;
		writeToFile(filelocation,errorData);
		return new CommandProcessingResult(Long.valueOf(1));
	}


	private void writeToFile(String uploadFilePath,ArrayList<MRNErrorData> errorData) {
		
			FileWriter fw = null;
			try{
				File f = new File(uploadFilePath.replace(".csv", ".log"));
				if(!f.exists()){
					f.createNewFile();
				}
				fw = new FileWriter(f,true);
				for(int k=0;k<errorData.size();k++){
					if(!errorData.get(k).getErrorMessage().equalsIgnoreCase("Success.")){
						fw.append("Data at row: "+errorData.get(k).getRowNumber()+", Message: "+errorData.get(k).getErrorMessage()+"\n");
					}
				}
				
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				try{
					if(fw!=null){
						fw.flush();
						fw.close();
					}
					
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		
	


	private void writeCSVData(String uploadFilePath,ArrayList<MRNErrorData> errorData,DataUpload uploadStatus) {
		

		
		try{
			
			long processRecords = uploadStatus.getProcessRecords();
			long totalRecords = uploadStatus.getTotalRecords();
			long unprocessRecords  = totalRecords-processRecords;
			if(unprocessRecords == 0){
				uploadStatus.setProcessStatus("Success");
				uploadStatus.setErrorMessage("Data successfully saved");
			}else if(unprocessRecords<totalRecords){
				uploadStatus.setProcessStatus("Completed");
				uploadStatus.setErrorMessage("Completed with some errors");
			}else if(unprocessRecords == totalRecords){
				uploadStatus.setProcessStatus("Failed");
				uploadStatus.setErrorMessage("Processing failed");
			}
			
			uploadStatus.setProcessDate(DateUtils.getDateOfTenant());
			this.dataUploadRepository.save(uploadStatus);
			uploadStatus = null;
		}catch(Exception  exception){
			exception.printStackTrace();
		}
		
		
	}

	public String buildjsonForPropertyDefinition(String[] currentLineData,ArrayList<MRNErrorData> errorData, int i) {

		if (currentLineData.length >= 10) {
			final HashMap<String, String> map = new HashMap<>();
			 map.put("propertyCode", currentLineData[0]);
			final Collection<MCodeData> propertyTypesList = this.mCodeReadPlatformService.getCodeValue(CodeNameConstants.CODE_PROPERTY_TYPE);
			if (!propertyTypesList.isEmpty()) {
				for (MCodeData mCodeData : propertyTypesList) {
					if (mCodeData.getmCodeValue().equalsIgnoreCase(currentLineData[1].toString().trim())) {
						map.put("propertyType", mCodeData.getId().toString());
						break;
					} 
				}

				final Collection<PropertyDefinationData> unitCodesList = this.propertyReadPlatformService.retrievPropertyType(CodeNameConstants.CODE_PROPERTY_UNIT,currentLineData[2].trim(),null);
				if (!unitCodesList.isEmpty()) {
					for (PropertyDefinationData unitData : unitCodesList) {
						if (unitData.getCode().equalsIgnoreCase(currentLineData[2].toString().trim())) {
							map.put("unitCode", currentLineData[2].trim());
							break;
						}
					}

					final Collection<PropertyDefinationData> floorList = this.propertyReadPlatformService.retrievPropertyType(CodeNameConstants.CODE_PROPERTY_FLOOR,currentLineData[3].trim(),null);
					if (!floorList.isEmpty()) {
						for (PropertyDefinationData floorData : floorList) {
							if (floorData.getCode().equalsIgnoreCase(currentLineData[3].toString().trim())) {
								map.put("floor", currentLineData[3].trim());
								break;
							}
						}

						final Collection<PropertyDefinationData> buildingCodeList = this.propertyReadPlatformService.retrievPropertyType(CodeNameConstants.CODE_PROPERTY_BUILDING,currentLineData[4].trim(),null);
						if (!buildingCodeList.isEmpty()) {
							for (PropertyDefinationData buildingCode : buildingCodeList) {
								if (buildingCode.getCode().equalsIgnoreCase(currentLineData[4].toString().trim())) {
									map.put("buildingCode", currentLineData[4].trim());
									break;
								}
							}

							final Collection<PropertyDefinationData> parcelList = this.propertyReadPlatformService.retrievPropertyType(CodeNameConstants.CODE_PROPERTY_PARCEL,currentLineData[5].trim(),null);
							if (!buildingCodeList.isEmpty()) {
								for (PropertyDefinationData parcel : parcelList) {
									if (parcel.getCode().equalsIgnoreCase(currentLineData[5].toString().trim())) {
										map.put("parcel", currentLineData[5].trim());
										break;
									}
								}
					         final List<CityDetailsData> cityDetailsList = this.addressReadPlatformService.retrieveAddressDetailsByCityName(currentLineData[6].trim());
					         if (!cityDetailsList.isEmpty()) {
									for (CityDetailsData cityDetail : cityDetailsList) {
										if (cityDetail.getCityName().equalsIgnoreCase(currentLineData[6].toString().trim())) {
											map.put("precinct", currentLineData[6].trim());
											break;
										}
									}
								map.put("poBox", currentLineData[7]);
								map.put("street", currentLineData[8]);
								map.put("state", currentLineData[9]);
								map.put("country", currentLineData[10]);
								return new Gson().toJson(map);
							  } else {
								errorData.add(new MRNErrorData((long) i,"Precinct list is empty"));
								return null;
							  }
							} else {
								errorData.add(new MRNErrorData((long) i,"Parcel list is empty"));
								return null;
							}
						} else {
							errorData.add(new MRNErrorData((long) i,"buildingCode list is empty"));
							return null;
						}
					} else {
						errorData.add(new MRNErrorData((long) i,"floor list is empty"));
						return null;
					}
				} else {
					errorData.add(new MRNErrorData((long) i,"unitCode list is empty"));
					return null;
				}
			} else {
				errorData.add(new MRNErrorData((long) i,"Property Types list is empty"));
				return null;
			}
		} else {
			errorData.add(new MRNErrorData((long) i,"Improper Data in this line"));
			return null;
		}
	}

	public String buildjsonForPropertyCodeMaster(String[] currentLineData, ArrayList<MRNErrorData> errorData, int i) {
		
		 if(currentLineData.length>=3){
			final HashMap<String, String> map = new HashMap<>();
		    final Collection<MCodeData> propertyCodeTypesList =this.mCodeReadPlatformService.getCodeValue(CodeNameConstants.PROPERTY_CODE_TYPE);
			if(!propertyCodeTypesList.isEmpty()){
				 for(MCodeData mCodeData:propertyCodeTypesList){
					   if(mCodeData.getmCodeValue().equalsIgnoreCase(currentLineData[0].toString())){ 
							map.put("propertyCodeType",mCodeData.getmCodeValue());
						   break;
					   }
				    }
				    map.put("code",currentLineData[1]);
					map.put("description",currentLineData[2]);
					if(currentLineData.length==4){
						map.put("referenceValue", currentLineData[3]);
					}
					return new Gson().toJson(map);	
			}else{
				errorData.add(new MRNErrorData((long)i, "Property Code Type list is empty"));
				return null;
			}
		}else{
			errorData.add(new MRNErrorData((long)i, "Improper Data in this line"));
			return null;
			
		}
	}
	/**
	 * @param currentLineData
	 * @param errorData
	 * @param i
	 * @return jsonString
	 */
	public String buildJsonForUsageCharge(String[] currentLineData,ArrayList<MRNErrorData> errorData, int i) {

		if (currentLineData.length >= 7) {
			final HashMap<String, String> map = new HashMap<>();
			map.put("clientId", currentLineData[0]);
			map.put("number", currentLineData[1]);
			map.put("time", currentLineData[2]);
			map.put("destinationNumber", currentLineData[3]);
			map.put("destinationLocation", currentLineData[4]);
			map.put("duration", currentLineData[5]);
			map.put("cost", currentLineData[6]);
			map.put("locale", "en");
			return new Gson().toJson(map);

		} else {
			errorData.add(new MRNErrorData((long) i,"Improper Data in this line"));
			return null;
		}
	}

}
	
	