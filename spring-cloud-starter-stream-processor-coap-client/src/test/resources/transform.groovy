import groovy.json.JsonSlurper


def jsonPayload = new JsonSlurper().parseText(payload)
def inputState = jsonPayload."3311"[0]["5850"]
def outputJson = '{"3311": [ {"5850": ' + (1 - inputState) + '} ]}'

println "in: " + inputState + "\nout: " + outputJson

sleep(1000)

return outputJson


//def payload = '{\n' +
//        '  "3311" : [ {\n' +
//        '    "5850" : 1,\n' +
//        '    "5851" : 203,\n' +
//        '    "9003" : 0\n' +
//        '  } ],\n' +
//        '  "9001" : "GU10 WC",\n' +
//        '  "9002" : 1528124737,\n' +
//        '  "9020" : 1528746166,\n' +
//        '  "9003" : 65539,\n' +
//        '  "9054" : 0,\n' +
//        '  "5750" : 2,\n' +
//        '  "9019" : 1,\n' +
//        '  "3" : {\n' +
//        '    "0" : "IKEA of Sweden",\n' +
//        '    "1" : "TRADFRI bulb GU10 W 400lm",\n' +
//        '    "2" : "",\n' +
//        '    "3" : "1.2.214",\n' +
//        '    "6" : 1\n' +
//        '  }\n' +
//        '}'
