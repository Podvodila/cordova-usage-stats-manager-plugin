var exec = require('cordova/exec');

window.UsageStatistics = {
    getTimeInForeground: function(startTimestamp, endTimestamp, success, error) {
      	console.log(`getTimeInForeground() :: ${startTimestamp} | ${endTimestamp}`);

    	exec(success, error, "MyUsageStatsManager", "getTimeInForeground", [startTimestamp, endTimestamp]);
    },

	openPermissionSettings: function(success, error) {
		console.log("openPermissionSettings() :: ");
		var array = []; // not needed but seems to throw exceptions on some cases if not included.
    	exec(success, error, "MyUsageStatsManager", "openPermissionSettings", array);
	}
};