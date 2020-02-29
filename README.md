# KeyValuePair

##  I expect you to have two jars in you runtime for the test case
https://www.javatpoint.com/src/junit/junit4jars.zip


#### Please follow the the Test class for how to run
## SimpleInputTest.java
#### otherwise follow the following instructions

## JSON 
 I have assumed the JSON object for value is in string form 
```bash
 {name: "John", age: 31, city: "New York"}
```

I am not making any validation that the value you enter is JSON or not, because of external library dependency

## Starting the file mapper 

```bash
 MHashMap map = FileMapper.getMap();
```

```bash
 MHashMap mapWithLocation = FileMapper.getMap(location);
```

 The default location will be classpath location and file named 
####    keyValuePair.txt
 
## Putting into file mapper
```bash
 map.put(key,JSONValue)
```
## Get from the file
```bash
  map.get(key)
```
 it will return a string formed JSON, get only will do the expiry check and if found the key to be expired it will delete the key/value from the memory,  in the next flush it will get deleted from the file as well

## Delete from the file
```bash
  map.delete(key)
```
It will delete the key value pair from the file

## FLUSH operation
Every now and then we have to call the flush method, which will put the details into file, i have restrained from doing every read/write operation from the file due to memory issue(its time inefficient) so only when you call flush method , data will get persisted into the memory, and in the next get call data will be read from the file again

```bash  
map.flush()
```
If doing multiple thread operation, please call flush once you are done with all your thread operation
this is the only limitation of the program
Here is the list of requirement from Problem statement and their status

## I have created a jar named FileMapper.jar, if you want to import to use this program as external library

## It can be initialized using an optional file path. If one is not provided, it will reliably create itself in a reasonable location on the laptop -- Working

## A new key-value pair can be added to the data store using the Create operation. The key is always a string - capped at 32chars. The value is always a JSON object - capped at 16KB. ----Working

## If Create is invoked for an existing key, an appropriate error must be returned -- Working

## A Read operation on a key can be performed by providing the key, and receiving the value in response, as a JSON object --- Working

## A Delete operation can be performed by providing the key -- Done

## Time-To-Live property -- Done

## Appropriate error responses must always be returned to a client if it uses the data store in unexpected ways or breaches any limits -- Done, but not througly tested

## The size of the file storing data must never exceed 1GB -- Done

## More than one client process cannot be allowed to use the same file as a data store at any given time -- Done

## A client process is allowed to access the data store using multiple threads, if it desires to. The data store must therefore be thread-safe -- Done

## The client will bear as little memory costs as possible to use this data store, while deriving maximum performance with respect to response times for accessing the data store -- I have tried to make it efficient
