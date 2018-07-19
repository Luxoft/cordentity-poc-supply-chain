# Manufacture API Reference

## Request to list all packages
After package gets prepared by TC it goes to the MF

 [PACKAGE_DATA_OBJECT](DataObjects.md)

### Method and url
`GET` */api/mf/request/list*

### Request

### Response
#### Code 200
```
[
    ${PACKAGE_DATA_OBJECT}
]
```


## Request to process package request
MF turns request into an absolutely powerful cure
After this package is shipped back

### Method and url
`POST` */api/mf/request/process*

### Request
```
{
    serial: string;
}
```

### Response
#### Code 200
```
Ok
```


## Request to start QP release process
Qualified Person is performing quality control of produced cure

### Method and url
`POST` */api/mf/qp/start*

### Request
```
{
    serial: string;
}
```

### Response
#### Code 200
```
Ok
```


## Request to finish QP release process
When QP is done it calls this endpoint

### Method and url
`POST` */api/mf/qp/finish*

### Request
```
{
    serial: string;
    status: boolean;
}
```

### Response
#### Code 200
```
Ok
```