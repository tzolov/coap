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

package org.springframework.cloud.stream.app.coap.observe.source;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import reactor.core.publisher.Flux;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.app.coap.CaopUtility;
import org.springframework.cloud.stream.app.coap.CoapDtlsProperties;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.reactive.StreamEmitter;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

/**
 * Creates a CoAP MessageProducer
 *
 * @author Christian Tzolov
 */
@EnableBinding(Source.class)
@EnableConfigurationProperties({ CoapObserveSourceProperties.class, CoapDtlsProperties.class })
public class CoapObserveSourceConfiguration {

	private static final Log logger = LogFactory.getLog(CoapObserveSourceConfiguration.class);

	@Autowired
	private CoapDtlsProperties dtlsProperties;

	@Autowired
	private CoapObserveSourceProperties observeSourceProperties;

	@Autowired
	private CoapClient coapClient;

	@StreamEmitter
	@Output(Source.OUTPUT)
	public Flux<Message<byte[]>> emit() {

		return Flux.create(emitter -> {
			CoapObserveRelation observe = this.coapClient.observe(new CoapHandler() {

				@Override
				public void onLoad(CoapResponse response) {
					if (response.isSuccess()) {
						Message<byte[]> message = MessageBuilder
								.withPayload(response.getPayload())
								.setHeader(MessageHeaders.CONTENT_TYPE, CaopUtility.contentTypeName(response.getOptions().getContentFormat()))
								.build();
						emitter.next(message);
					}
					else {
						emitter.error(new RuntimeException("Error Response: " + Utils.prettyPrint(response)));
					}
				}

				@Override
				public void onError() {
					// request either timeout'd or was rejected, complete stream
					emitter.error(new RuntimeException("Request for url [" +
							observeSourceProperties.getUrl() + "] either timeout'd or was rejected"));
				}
			});

			emitter.onDispose(() -> {
				logger.debug("Emitter cancellation, proactive cancel for coap observer");
				observe.proactiveCancel();
				if (this.dtlsProperties.isDtlsEnabled()) {
					this.coapClient.getEndpoint().destroy();
					this.coapClient.shutdown();
				}
			});
		});
	}

	@Bean
	public CoapClient coapClient() {
		return new CaopUtility().createCoapClient(
				this.observeSourceProperties.getUrl(), this.dtlsProperties);
	}
}
