# Sovrin Agent API Reference


## Request to create package request
SA initiates communication with TC to create medicine request

### Method and url
`POST` */api/sa/request/create*

### Request
```
{
    tcName: string;
}
```

### Response
#### Code 200
```
Ok
```


## Request to withdraw package 
SA initiates communication with TC to pick up medicine

### Method and url
`POST` */api/sa/request/withdraw*

### Request
```
{
    serial: string;
}
```

### Response
#### Code 200
```
serial: string
```


## Request to list all packages

 [PACKAGE_DATA_OBJECT](DataObjects.md)

### Method and url
`GET` */api/sa/request/list*

### Request

### Response
#### Code 200
```
[
    ${PACKAGE_DATA_OBJECT}
]
```

## Request to list all claims


 [CLAIM_DATA_OBJECT](DataObjects.md)

### Method and url
`GET` */api/sa/claim/list*

### Request

### Response
#### Code 200
```
[
    ${CLAIM_DATA_OBJECT}
]
```