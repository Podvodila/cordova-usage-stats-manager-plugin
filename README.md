# cordova-usage-stats-manager
Cordova Android plugin for accessing UsageStatsManager API

# Usage
To get time in foreground for each app in the system
```javascript
var success = function(success){
	console.log("Sucess :: " + success);
};

var error = function(error){
	console.log("Error :: " + error);
};

UsageStatistics.getTimeInForeground(fromInMs, toInMs, success, error);
```
To access to device usage history and statistics
```javascript
var success = function(success){
	console.log("Sucess :: " + success);
};

var error = function(error){
	console.log("Error :: " + error);
};

UsageStatistics.getEvents(fromInMs, toInMs, success, error);
```
If you need to prompt the user for permission
```javascript
var success = function(success){
	console.log("Sucess :: " + success);
};

var error = function(error){
	console.log("Error :: " + error);
};

UsageStatistics.openPermissionSettings(success, error);

```
