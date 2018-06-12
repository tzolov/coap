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

package org.springframework.cloud.stream.app.coap.server.source;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;
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

/**
 * @author Christian Tzolov
 */
@EnableBinding(Source.class)
@EnableConfigurationProperties({ CoapServerSourceProperties.class, CoapDtlsProperties.class })
public class CoapServerSourceConfiguration {

	private static final Log logger = LogFactory.getLog(CoapServerSourceConfiguration.class);

	@Autowired
	private CoapServerSourceProperties serverProperties;

	@Autowired
	private CoapDtlsProperties dtlsProperties;

	@Autowired
	private CoapServer coapServer;

	@StreamEmitter
	@Output(Source.OUTPUT)
	public Flux<Message<byte[]>> emit() {

		return Flux.create(emitter -> {
			CoapResource resource = new CoapResource(this.serverProperties.getPath()) {

				private AtomicLong postCounter = new AtomicLong();
				private AtomicLong putCounter = new AtomicLong();
				private AtomicLong deleteCounter = new AtomicLong();

				@Override
				public void handleGET(CoapExchange exchange) {
					exchange.respond(CoAP.ResponseCode.CONTENT, String.format("POST: %d, PUT: %d, DELETE: %d",
							postCounter.get(), putCounter.get(), deleteCounter.get()));
				}

				@Override
				public void handlePOST(CoapExchange exchange) {
					this.postCounter.incrementAndGet();
					this.emitMessage(exchange);
					exchange.respond(CoAP.ResponseCode.CREATED);
				}

				@Override
				public void handlePUT(CoapExchange exchange) {
					this.putCounter.incrementAndGet();
					emitMessage(exchange);
					exchange.respond(CoAP.ResponseCode.CHANGED);
				}

				@Override
				public void handleDELETE(CoapExchange exchange) {
					this.deleteCounter.incrementAndGet();
					emitMessage(exchange);
					exchange.respond(CoAP.ResponseCode.DELETED);
				}

				private void emitMessage(CoapExchange exchange) {
					emitter.next(MessageBuilder
							.withPayload((exchange.getRequestPayload()))
							.setHeader("contentType", CaopUtility.contentTypeName(exchange.getRequestOptions().getContentFormat()))
							.setHeader("coap_method", exchange.getRequestCode().name())
							.setHeader("coap_remoteIp", exchange.getSourceAddress().getHostAddress())
							.setHeader("coap_remotePort", exchange.getSourcePort())
							.build());
				}
			};

			coapServer.add(resource);
			coapServer.start();

			emitter.onDispose(() -> {
				logger.debug("Emitter cancellation, stop Coap Server");
				coapServer.stop();
				coapServer.remove(resource);
			});
		});
	}

	@Bean
	public CoapServer coapServer() {
		return new CaopUtility().createCoapServer(this.serverProperties.getPort(), this.dtlsProperties);
	}
}
