import groovy.json.JsonSlurper

// Test payload. 3311/5850 == 1 -> On , 3311/5850 == 0 -> OFF
//def payload = '{' +
//        '  "3311" : [ { "5850" : 1, "5851" : 203, "9003" : 0} ], ' +
//        '  "9001" : "GU10 WC", "9002" : 1528124737, "9020" : 1528746166, "9003" : 65539, "9054" : 0, "5750" : 2, "9019" : 1, ' +
//        '  "3" : { "0" : "IKEA of Sweden", "1" : "TRADFRI bulb GU10 W 400lm", "2" : "", "3" : "1.2.214",  "6" : 1 } ' +
//        '}'

def jsonPayload = new JsonSlurper().parseText(payload)
def inputState = jsonPayload."3311"[0]["5850"]
def outputJson = '{"3311": [ {"5850": ' + (1 - inputState) + '} ]}'

println "in: " + inputState + "\nout: " + outputJson

//sleep(1000)

return outputJson

