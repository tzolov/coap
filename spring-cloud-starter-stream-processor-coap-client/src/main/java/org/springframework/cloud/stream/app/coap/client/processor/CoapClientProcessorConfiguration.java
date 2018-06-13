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

import java.io.IOException;
import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.app.coap.CaopUtility;
import org.springframework.cloud.stream.app.coap.CoapDtlsProperties;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

/**
 *
 * @author Christian Tzolov
 */
@EnableBinding(Processor.class)
@EnableConfigurationProperties({ CoapClientProcessorProperties.class, CoapDtlsProperties.class })
public class CoapClientProcessorConfiguration {

	@Bean
	public CoapClient coapClient(CoapClientProcessorProperties clientProperties,
			CoapDtlsProperties dtlsProperties) {
		return new CaopUtility().createCoapClient(clientProperties.getUrl(), dtlsProperties);
	}

	@Bean
	public CoapClientProcessor coapClientProcessor() {
		return new CoapClientProcessor();
	}

	@MessageEndpoint
	public static class CoapClientProcessor {

		private static final Log logger = LogFactory.getLog(CoapClientProcessor.class);

		@Autowired
		private CoapClientProcessorProperties clientProperties;

		@Autowired
		private CoapClient coapClient;

		@ServiceActivator(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
		public Object process(Message<?> message) {
			try {
				/* construct headers */
				// TODO
				//HttpHeaders headers = new HttpHeaders();
				//
				//if (clientProperties.getHeadersExpression() != null) {
				//	Map<?, ?> headersMap = clientProperties.getHeadersExpression().getValue(message, Map.class);
				//	for (Map.Entry<?, ?> header : headersMap.entrySet()) {
				//		if (header.getKey() != null && header.getValue() != null) {
				//			headers.add(header.getKey().toString(),
				//					header.getValue().toString());
				//		}
				//	}
				//}

				CoAP.Code method = this.clientProperties.getMethod();
				String url = this.clientProperties.getUrlExpression().getValue(message, String.class);
				Object body = null;
				if (this.clientProperties.getBody() != null) {
					body = this.clientProperties.getBody();
				}
				else if (this.clientProperties.getBodyExpression() != null) {
					body = this.clientProperties.getBodyExpression().getValue(message);
				}
				else {
					body = message.getPayload();
				}
				URI uri = new URI(url);

				Request request = new Request(method);
				request.setType(CoAP.Type.CON);
				request.setURI(uri);
				request.setPayload(toByteArray(body));

				int format = MediaTypeRegistry.parse(this.clientProperties.getFormatType());
				Assert.isTrue(format != -1, "Unsupported Format Type: " + this.clientProperties.getFormatType());
				request.getOptions().setContentFormat(format);

				int accept = MediaTypeRegistry.parse(this.clientProperties.getAcceptType());
				Assert.isTrue(accept != -1, "Unsupported Accept Type: " + this.clientProperties.getAcceptType());
				request.getOptions().setAccept(accept);

				logger.debug("CoAP Request: " + Utils.prettyPrint(request));

				CoapResponse response = this.coapClient.advanced(request);

				return this.clientProperties.getReplyExpression().getValue(response);
			}
			catch (Exception e) {
				logger.warn("Error in HTTP request", e);
				return null;
			}
		}

		public static byte[] toByteArray(Object object) throws IOException {
			if (object == null) {
				return new byte[0];
			}
			else if (object instanceof byte[]) {
				return (byte[]) object;
			}
			return object.toString().getBytes();
		}
	}
}
