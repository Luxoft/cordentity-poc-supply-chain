#  API data objects Reference

###  Package data object (used also as request)

Available statuses:
* 0 -  Insurer approved your prescription (only exists on mobile for a moment)
* 1 - Package requested
* 2 - Manufactured and sent back
* 3 - Arrived to TC
* 4 - Approved by QP
* 5 - Collected by patient


```
[
    {
        serial: string;
        
        status: number; 
        
        manufacturer: string;

     	patientDid: string;
        patientDiagnosis: string;
        
        medicineName: string;
        medicineDescription: string;
        
        treatmentCenterName: string;
        treatmentCenterAddress: string;
        
        issuedAt: number;
        issuedBy: string;
        
        // manufactured and sent back
        processedAt?: number;                
        processedBy?: string;
        
        // arrived back to tc
        deliveredAt?: number;                
        deliveredBy?: string;
        
        qp?: boolean;
        
        // picked up by patient
        collectedAt?: number;                
        collectedBy?: string;
    }
]
```

##### Example 1
```
[
	{
		"serial": "5d9b00b1-8604-4447-ba83-f39826154513",
		
		"status": 5
		
		"patientDid": "did:sov:NzMsMTEsMjM0LDkzLDEzNw",
		"patientDiagnosis": "diabetes (type 2)",
		
		
		"medicineName": "Teoxalin",
		"medicineDescription": "Sulphonylureas",
		
		"treatmentCenterName": "MARINA BAY Hosp",
		"treatmentCenterAddress": "Marina Sands 117, Singapore",
		
		"manufacturer": "Medicare",
		
		"qp": true,
		
		"issuedAt: 1516755243143,
		"issuedBy": "did:sov:MzMsMTU2LDE4MSwzMSwyND",
		
        "processedAt": 1519230380054,
        "processedBy": "did:sov:MTMzLDExNSw1MiwwLDE0Ng",
        
        "deliveredAt": 1519507724784,
        "deliveredBy": "did:sov:MTQyLDQwLDEyMiwyMjIsMj",
        
        "collectedAt": 1518201032008,
        "collectedBy": "did:sov:MTcyLDIwMCwxMjQsMTM5LDI0OA"
	}
]
```
