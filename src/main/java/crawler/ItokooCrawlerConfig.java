/*
 * Copyright 2014 NAKANO Hideo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package crawler;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@ConfigurationProperties
/**
 * Download configuration for crawler...
 */
public class ItokooCrawlerConfig {
	private static final String DEFAULT_URL = "http://www.itokoo.com/read.php?tid=188";
	private static final long DEFAULT_DOWNLOAD_INTERVAL = TimeUnit.HOURS.toMillis(1);
	private String url = DEFAULT_URL;
	private long downloadInterval = DEFAULT_DOWNLOAD_INTERVAL;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public long getDownloadInterval() {
		return downloadInterval;
	}

	public void setDownloadInterval(long downloadInterval) {
		this.downloadInterval = downloadInterval;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ItokooCrawlerConfig that = (ItokooCrawlerConfig) o;

		if (downloadInterval != that.downloadInterval) return false;
		if (!url.equals(that.url)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = url.hashCode();
		result = 31 * result + (int) (downloadInterval ^ (downloadInterval >>> 32));
		return result;
	}

	@Override
	public String toString() {
		return "CrawlerConfig{" +
				"url='" + url + '\'' +
				", downloadInterval=" + downloadInterval +
				'}';
	}
}
