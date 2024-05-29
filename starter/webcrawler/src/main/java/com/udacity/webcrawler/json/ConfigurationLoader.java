package com.udacity.webcrawler.json;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A static utility class that loads a JSON configuration file.
 */
public final class ConfigurationLoader {

	private final Path path;

	/**
	 * Create a {@link ConfigurationLoader} that loads configuration from the given
	 * {@link Path}.
	 */
	public ConfigurationLoader(Path path) {
		this.path = Objects.requireNonNull(path);
	}

	/**
	 * Loads configuration from this {@link ConfigurationLoader}'s path
	 *
	 * @return the loaded {@link CrawlerConfiguration}.
	 * @throws IOException
	 */
	public CrawlerConfiguration load() throws IOException {
		// Fill in this method.
		try (Reader reader = Files.newBufferedReader(path)) {
			return read(reader);
		} catch (IOException e) {
			throw new IOException("Error reading from file: " + path, e);
		}
	}

	/**
	 * Loads crawler configuration from the given reader.
	 *
	 * @param reader a Reader pointing to a JSON string that contains crawler
	 *               configuration.
	 * @return a crawler configuration
	 * @throws IOException
	 */
	public static CrawlerConfiguration read(Reader reader) throws IOException {
		// This is here to get rid of the unused variable warning.
		Objects.requireNonNull(reader);
		// : Fill in this method

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
			// read the JSON input and parse it into a CrawlerConfiguration using the
			// Jackson JSON library.
			return objectMapper.readValue(reader, CrawlerConfiguration.Builder.class).build();
		} catch (IOException e) {
			throw new IOException("Error reading from reader: " + reader, e);
		}
	}
}
