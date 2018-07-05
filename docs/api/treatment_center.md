# Treatment Center API Reference


## Request to list all packages

 [PACKAGE_DATA_OBJECT](data_objects.md)

### Method and url
`GET` */api/tc/package/list*

### Request

### Response
#### Code 200
```
    ${PACKAGE_DATA_OBJECT}
```


#### Code 500 (writing it once, but for each other request it will be the same)
```
{
    error: {
        type: string;
        message: string;
    }
}
```

##### Example
```
{
    "error": {
        "type": "DatabaseError",
        "message": "Out of space"
    }
}
```
