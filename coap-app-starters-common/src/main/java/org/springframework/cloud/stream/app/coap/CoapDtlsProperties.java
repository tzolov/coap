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

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotEmpty;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

/**
 * @author Christian Tzolov
 */
@ConfigurationProperties("coap.dtls")
@Validated
public class CoapDtlsProperties {

	/**
	 * Enable or disable the DTLS transport encryption
	 */
	private boolean enabled = false;

	/**
	 * PSK identity if required to authenticate with teh CoAP Server
	 */
	private String identity;

	/**
	 * PSK password if required to authenticate with teh CoAP Server
	 */
	private String secret;

	/**
	 * DTLS Trust store location (supports file:// , classpath:/ or http:/)
	 */
	private String trustStoreLocation = "classpath:/cacerts";

	/**
	 * Trust store password
	 */
	private String trustStorePassword = "changeit";

	/**
	 * DTLS key store location. (supports file:// , classpath:/ or http:/)
	 */
	private String keyStoreLocation = "classpath:/clientKeyStore.jks";

	/**
	 * Keystore user alias
	 */
	private String keyStoreAlias = "client";

	/**
	 * Keystore password
	 */
	private String keyStorePassword = "clientPass";


	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	@NotEmpty
	public String getTrustStoreLocation() {
		return trustStoreLocation;
	}

	public void setTrustStoreLocation(String trustStoreLocation) {
		this.trustStoreLocation = trustStoreLocation;
	}

	@NotEmpty
	public String getTrustStorePassword() {
		return trustStorePassword;
	}

	public void setTrustStorePassword(String trustStorePassword) {
		this.trustStorePassword = trustStorePassword;
	}

	@NotEmpty
	public String getKeyStoreLocation() {
		return keyStoreLocation;
	}

	public void setKeyStoreLocation(String keyStoreLocation) {
		this.keyStoreLocation = keyStoreLocation;
	}

	@NotEmpty
	public String getKeyStorePassword() {
		return keyStorePassword;
	}

	public void setKeyStorePassword(String keyStorePassword) {
		this.keyStorePassword = keyStorePassword;
	}

	@NotEmpty
	public String getKeyStoreAlias() {
		return keyStoreAlias;
	}

	public void setKeyStoreAlias(String keyStoreAlias) {
		this.keyStoreAlias = keyStoreAlias;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@AssertTrue(message = "Enabled DTLS requires valid 'trustStoreLocation', 'trustStorePassword', 'keyStoreLocation'" +
			", 'keyStoreAlias' and 'keyStorePassword'")
	public boolean isDtlsEnabledRequiredProperties() {
		return (!this.enabled) ? true : StringUtils.hasText(this.trustStoreLocation)
				&& StringUtils.hasText(this.trustStorePassword) && StringUtils.hasText(this.keyStoreLocation)
				&& StringUtils.hasText(this.keyStoreAlias) && StringUtils.hasText(this.keyStorePassword);
	}
}
