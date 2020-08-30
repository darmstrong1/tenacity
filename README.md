Tenacity is a library that allows developers to configure functions to be called again if it fails due to an error that
is considered recoverable.  
  
To configure how often and how frequently a function will be retried, create a
[WaitConfiguration](src/main/kotlin/co/da/tenacity/WaitConfiguration.kt) object.  
To configure how to determine if an error is recoverable, create a recoverable error predicate lambda with a
(Throwable) -> Boolean signature. If the error predicate returns true, the tenacious caller will retry if the
WaitConfiguration allows it. If the error predicate returns false, the tenacious caller will throw an
[UnrecoverableException](src/main/kotlin/co/da/tenacity/Tenacity.kt).  
  
######Example
You have a function that calls an external service. If the service is unavailable, it throws a
[SocketTimedOutException](https://docs.oracle.com/javase/8/docs/api/java/net/SocketTimeoutException.html). The function
is part of a backend process that does not interact with end-users. You want the application to keep trying the function
call until it becomes available or fails for another reason. 
You could create an error predicate like this:
```
val errorPredicate: (Throwable) -> Boolean = { it is SocketTimeoutException }
```
This would cause the tenacious caller to retry if the function throws a SocketTimedOutException. 
You could create a WaitConfiguration like this:  
```
val waitConfig = WaitConfiguration(TimeUnit.MINUTES, 1, 60, 30)
```
If the external service were unavailable, and the function threw a SocketTimedOutException, the tenacious caller would
retry the function call once per minute for 60 times, once per two minutes for 60 times, once per four minutes
for 60 times, once per 8 minutes for 60 times and once per 16 minutes for 60 times. Finally, it would try once every 30
minutes until the service becomes available, and the function returns successfully or the service becomes available and
fails for another reason.
```
import co.da.tenacity.WaitConfiguration
import co.da.tenacity.Tenacity.apply

val errorPredicate: (Throwable) -> Boolean = { it is SocketTimeoutException }
val waitConfig = WaitConfiguration(TimeUnit.MINUTES, 1, 60, 30)

try {
  val status = apply(waitConfig, errorPredicate, arg) { arg ->
    if(connect() == false) {
      throw SocketTimedOutException()
    }
  ...
  }
} catch(e: UnrecoverableException) {
  // Handle unrecoverable exception
}
```
Please see test cases for other examples.
   

  