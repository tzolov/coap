package org.springframework.cloud.stream.app.coap.client.processor

import groovy.json.JsonSlurper

def jsonSlurper = new JsonSlurper()
def object = jsonSlurper.parseText('{\n' +
        '  "3311" : [ {\n' +
        '    "5850" : 1,\n' +
        '    "5851" : 203,\n' +
        '    "9003" : 0\n' +
        '  } ],\n' +
        '  "9001" : "GU10 WC",\n' +
        '  "9002" : 1528124737,\n' +
        '  "9020" : 1528746166,\n' +
        '  "9003" : 65539,\n' +
        '  "9054" : 0,\n' +
        '  "5750" : 2,\n' +
        '  "9019" : 1,\n' +
        '  "3" : {\n' +
        '    "0" : "IKEA of Sweden",\n' +
        '    "1" : "TRADFRI bulb GU10 W 400lm",\n' +
        '    "2" : "",\n' +
        '    "3" : "1.2.214",\n' +
        '    "6" : 1\n' +
        '  }\n' +
        '}')

assert object instanceof Map
assert object."3311" instanceof List
assert object."3311"[0] instanceof Map
println object."3311"[0]["5850"]
def value = 1 - object."3311"[0]["5850"]

def p = (65541 - (object."9003" + 1)) % 5
println ((object."9003" + 1) % 65538)

println '{"3311": [{"5850":' + (1 - object."3311"[0]["5850"]) + '}]}'

