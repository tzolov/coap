package org.springframework.cloud.stream.app.coap.client.processor;

import com.jayway.jsonpath.Predicate;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.integration.json.JsonPathUtils;

/**
 * @author Christian Tzolov
 */
public class Main {
	public static class PayloadHolder {
		public PayloadHolder(String payload) {
			this.payload = payload;
		}

		private String payload;

		public String getPayload() {
			return payload;
		}

		public void setPayload(String payload) {
			this.payload = payload;
		}
	}

	static String jsonTest = "{\n" +
			"  \"3311\" : [ {\n" +
			"    \"5850\" : 1,\n" +
			"    \"5851\" : 203,\n" +
			"    \"9003\" : 0\n" +
			"  } ],\n" +
			"  \"9001\" : \"GU10 WC\",\n" +
			"  \"9002\" : 1528124737,\n" +
			"  \"9020\" : 1528746166,\n" +
			"  \"9003\" : 65539,\n" +
			"  \"9054\" : 0,\n" +
			"  \"5750\" : 2,\n" +
			"  \"9019\" : 1,\n" +
			"  \"3\" : {\n" +
			"    \"0\" : \"IKEA of Sweden\",\n" +
			"    \"1\" : \"TRADFRI bulb GU10 W 400lm\",\n" +
			"    \"2\" : \"\",\n" +
			"    \"3\" : \"1.2.214\",\n" +
			"    \"6\" : 1\n" +
			"  }\n" +
			"}";

	public static void main(String[] args) throws NoSuchMethodException {
		System.out.println(test("{\"boza\":1}", "1 - #jsonPath(payload,'$.boza')"));
		System.out.println(test(jsonTest, "{\"k\" :  1 - #jsonPath(payload,'$.3311[0].5850')}"));
		//System.out.println(test(jsonTest, " {\"foo\": { \"bar\": 1-#jsonPath(payload,'$.3311[0].5850') } } }"));

		System.out.println(test("{ \"bar\": 1} ", "new Object[]{payload}"));
		System.out.println(test("[{ \"bar\": 1 } ]", " {\"foo\": payload  } "));
	}

	public static Object test(String json, String jsonPathExpression) throws NoSuchMethodException {
		final ExpressionParser parser = new SpelExpressionParser();

		StandardEvaluationContext context = new StandardEvaluationContext(new PayloadHolder(json));

		context.registerFunction("jsonPath",
				JsonPathUtils.class.getMethod("evaluate", Object.class, String.class, Predicate[].class));

		return parser.parseExpression(jsonPathExpression).getValue(context);
	}

}
