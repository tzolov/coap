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

package org.springframework.cloud.stream.app.coap;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.pskstore.StaticPskStore;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author Christian Tzolov
 */
public class CaopUtility {

	public static final int DTLS_PORT = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_SECURE_PORT);

	public static String contentTypeName(int contentTypeId) {
		if (contentTypeId < 0) { // The undefined content type is set to -1
			contentTypeId = 0; // 0 -> TEXT/PLAIN
		}
		return MediaTypeRegistry.toString(contentTypeId);
	}

	public CoapClient createCoapClient(String serverUrl, CoapDtlsProperties dtlsProperties) {
		final CoapClient coapClient = new CoapClient(serverUrl);

		if (dtlsProperties.isEnabled()) {
			final DTLSConnector dtlsConnector = createDtlsConnector(0, dtlsProperties);

			CoapEndpoint coapEndpoint = new CoapEndpoint.CoapEndpointBuilder()
					.setNetworkConfig(NetworkConfig.getStandard())
					.setConnector(dtlsConnector).build();

			coapClient.setEndpoint(coapEndpoint);
		}

		return coapClient;
	}


	public CoapServer createCoapServer(int port, CoapDtlsProperties dtlsProperties) {

		if (dtlsProperties.isEnabled()) {
			Assert.hasText(dtlsProperties.getIdentity(), "PKS identity is required for Server");
			Assert.hasText(dtlsProperties.getSecret(), "PKS secret is required for Server");

			final DTLSConnector dtlsConnector = createDtlsConnector(port, dtlsProperties);

			CoapEndpoint coapEndpoint = new CoapEndpoint.CoapEndpointBuilder()
					.setNetworkConfig(NetworkConfig.getStandard())
					.setConnector(dtlsConnector).build();

			CoapServer server = new CoapServer();
			server.addEndpoint(coapEndpoint);
			return server;
		}
		else {
			return new CoapServer();
		}
	}

	public DTLSConnector createDtlsConnector(int port, CoapDtlsProperties properties) {

		DTLSConnector dtlsConnector = null;
		try {
			// load Java trust store
			Certificate[] trustedCertificates = loadTrustStore(properties.getTrustStoreLocation(), properties.getTrustStorePassword());

			// load client key store
			KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(getInputStream(properties.getKeyStoreLocation()),
					properties.getKeyStorePassword().toCharArray());

			// Build DTLS config
			DtlsConnectorConfig.Builder builder = new DtlsConnectorConfig.Builder()
					.setAddress(new InetSocketAddress(port))
					.setIdentity((PrivateKey) keyStore.getKey(properties.getKeyStoreAlias(),
							properties.getKeyStorePassword().toCharArray()),
							keyStore.getCertificateChain(properties.getKeyStoreAlias()), true)
					.setTrustStore(trustedCertificates)
					.setMaxConnections(100)
					.setStaleConnectionThreshold(60);

			if (StringUtils.hasText(properties.getIdentity()) && StringUtils.hasText(properties.getSecret())) {
				builder.setPskStore(new StaticPskStore(properties.getIdentity(), properties.getSecret().getBytes()));
			}

			// Create DTLS endpoint
			dtlsConnector = new DTLSConnector(builder.build());
			dtlsConnector.setRawDataReceiver(raw -> System.out.println("Received response: " + new String(raw.getBytes())));
		}
		catch (Exception e) {
			System.err.println("Error creating DTLS endpoint");
			e.printStackTrace();
		}

		return dtlsConnector;
	}

	private Certificate[] loadTrustStore(String trustStoreLocation, String trustSstorePassword) throws Exception {
		// load client key store
		KeyStore trustStore = KeyStore.getInstance("JKS");
		trustStore.load(getInputStream(trustStoreLocation), trustSstorePassword.toCharArray());

		// load trustStore
		TrustManagerFactory trustMgrFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustMgrFactory.init(trustStore);
		TrustManager trustManagers[] = trustMgrFactory.getTrustManagers();
		X509TrustManager defaultTrustManager = null;

		for (TrustManager trustManager : trustManagers) {
			if (trustManager instanceof X509TrustManager) {
				defaultTrustManager = (X509TrustManager) trustManager;
			}
		}

		return (defaultTrustManager == null) ? null : defaultTrustManager.getAcceptedIssuers();
	}

	private InputStream getInputStream(String resourceUri) throws IOException {
		return new DefaultResourceLoader().getResource(resourceUri).getInputStream();
	}
}
