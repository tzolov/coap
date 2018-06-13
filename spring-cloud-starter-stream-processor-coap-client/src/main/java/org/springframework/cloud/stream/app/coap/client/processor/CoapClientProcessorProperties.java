/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this coap except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.app.coap.client.processor;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

import org.eclipse.californium.core.coap.CoAP;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.expression.Expression;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.validation.annotation.Validated;

/**
 * Properties for the CoAP client processor.
 *
 * @author Christian Tzolov
 */
@ConfigurationProperties("coap.client")
@Validated
public class CoapClientProcessorProperties {

	private static final CoAP.Code DEFAULT_COAP_METHOD = CoAP.Code.GET;

	/**
	 * The URL to issue an CoAP request to, as a static value.
	 */
	private String url;

	/**
	 * A SpEL expression against incoming message to determine the URL to use.
	 */
	private Expression urlExpression;

	/**
	 * The kind of coap method to use.
	 */
	private CoAP.Code method = DEFAULT_COAP_METHOD;

	/**
	 * Request's body content type
	 */
	private String formatType = "text/plain";

	/**
	 * Request's expected response type
	 */
	private String acceptType = "text/plain";

	/**
	 * The (static) request body; if neither this nor bodyExpression is provided, the payload will be used.
	 */
	private Object body;

	/**
	 * A SpEL expression to derive the request body from the incoming message.
	 */
	private Expression bodyExpression;

	/**
	 * A SpEL expression used to derive the CoAP headers map to use.
	 */
	private Expression headersExpression;

	/**
	 * A SpEL expression used to compute the final result, applied against the whole CoAP response.
	 */
	private Expression replyExpression = new SpelExpressionParser().parseExpression("payload");

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return this.url;
	}

	public Expression getUrlExpression() {
		return urlExpression != null ? urlExpression
				: new LiteralExpression(this.url);
	}

	public void setUrlExpression(Expression urlExpression) {
		this.urlExpression = urlExpression;
	}

	@NotNull
	public CoAP.Code getMethod() {
		return method;
	}

	public void setMethod(CoAP.Code method) {
		this.method = method;
	}

	public String getFormatType() {
		return formatType;
	}

	public void setFormatType(String formatType) {
		this.formatType = formatType;
	}

	public String getAcceptType() {
		return acceptType;
	}

	public void setAcceptType(String acceptType) {
		this.acceptType = acceptType;
	}

	public Object getBody() {
		return body;
	}

	public void setBody(Object body) {
		this.body = body;
	}

	public Expression getBodyExpression() {
		return bodyExpression;
	}

	public void setBodyExpression(Expression bodyExpression) {
		this.bodyExpression = bodyExpression;
	}

	public Expression getHeadersExpression() {
		return headersExpression;
	}

	public void setHeadersExpression(Expression headersExpression) {
		this.headersExpression = headersExpression;
	}

	@NotNull
	public Expression getReplyExpression() {
		return replyExpression;
	}

	public void setReplyExpression(Expression replyExpression) {
		this.replyExpression = replyExpression;
	}

	@AssertTrue(message = "Exactly one of 'url' or 'urlExpression' is required")
	public boolean isExactlyOneUrl() {
		return url == null ^ urlExpression == null;
	}

	@AssertTrue(message = "At most one of 'body' or 'bodyExpression' is allowed")
	public boolean isAtMostOneBody() {
		return body == null || bodyExpression == null;
	}
}
