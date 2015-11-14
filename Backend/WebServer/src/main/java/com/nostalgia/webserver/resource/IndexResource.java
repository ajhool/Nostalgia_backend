package com.nostalgia.webserver.resource;

import static com.google.common.base.Preconditions.checkArgument;
import io.dropwizard.auth.Auth;
import io.dropwizard.servlets.assets.ByteRange;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.net.MediaType;
import com.nostalgia.webserver.CachedAsset;
import com.nostalgia.webserver.auth.BasicUser;
import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.hash.Hashing;
import com.google.common.net.HttpHeaders;

@Path("/")
public class IndexResource {

	File webRoot; 

	@Context
	HttpServletResponse resp; 

	private static final MediaType DEFAULT_MEDIA_TYPE = MediaType.HTML_UTF_8;

	private final Charset defaultCharset;

	public IndexResource(File webRoot, Charset defaultCharset) {
		super();
		this.webRoot = webRoot;
		this.defaultCharset = defaultCharset;
	}
	private boolean isCachedClientSide(HttpServletRequest req, CachedAsset cachedAsset) {
		return cachedAsset.getETag().equals(req.getHeader(HttpHeaders.IF_NONE_MATCH)) ||
				(req.getDateHeader(HttpHeaders.IF_MODIFIED_SINCE) >= cachedAsset.getLastModifiedTime());
	}

	private CachedAsset loadAsset(String key) throws URISyntaxException, IOException {
		//		checkArgument(key.startsWith(uriPath));
		//		final String requestedResourcePath = SLASHES.trimFrom(key.substring(uriPath.length()));
		//		String currentDir = System.getProperty("user.dir");

		if(key == null || key.equals("/")){
			key = "index.html";
		}
		final String absoluteRequestedResourcePath = webRoot.getAbsolutePath() + "/" + key;

		long lastModified = new File(absoluteRequestedResourcePath).lastModified();
		if (lastModified < 1) {
			// Something went wrong trying to get the last modified time: just use the current time
			lastModified = System.currentTimeMillis();
		}

		// zero out the millis since the date we get back from If-Modified-Since will not have them
		lastModified = (lastModified / 1000) * 1000;

		File requested = new File(absoluteRequestedResourcePath);
		if(!requested.exists()){
			return null;
		}
		return new CachedAsset(org.apache.commons.io.FileUtils.readFileToByteArray(requested), lastModified);
	}
	
	@GET
	@Timed
	public void serveIndex(@Auth(required = false) BasicUser user, @Context HttpServletRequest req) throws Exception{

		try {

			//since this is the index servlet, only serve files from the root

			java.nio.file.Path reqPath = Paths.get(req.getPathInfo());


			final CachedAsset cachedAsset = loadAsset(reqPath.getRoot().toString());
			if (cachedAsset == null) {
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}

			if (isCachedClientSide(req, cachedAsset)) {
				resp.sendError(HttpServletResponse.SC_NOT_MODIFIED);
				return;
			}

			final String rangeHeader = req.getHeader(HttpHeaders.RANGE);

			final int resourceLength = cachedAsset.getResource().length;
			ImmutableList<ByteRange> ranges = ImmutableList.of();

			boolean usingRanges = false;
			// Support for HTTP Byte Ranges
			// http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
			if (rangeHeader != null) {

				final String ifRange = req.getHeader(HttpHeaders.IF_RANGE);

				if (ifRange == null || cachedAsset.getETag().equals(ifRange)) {

					try {
						ranges = parseRangeHeader(rangeHeader, resourceLength);
					} catch (NumberFormatException e) {
						resp.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
						return ;
					}

					if (ranges.isEmpty()) {
						resp.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
						return;
					}

					resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
					usingRanges = true;

					resp.addHeader(HttpHeaders.CONTENT_RANGE, "bytes "
							+ Joiner.on(",").join(ranges) + "/" + resourceLength);
				}
			}

			resp.setDateHeader(HttpHeaders.LAST_MODIFIED, cachedAsset.getLastModifiedTime());
			resp.setHeader(HttpHeaders.ETAG, cachedAsset.getETag());

			final String mimeTypeOfExtension = req.getServletContext()
					.getMimeType(req.getRequestURI());
			MediaType mediaType = DEFAULT_MEDIA_TYPE;

			if (mimeTypeOfExtension != null) {
				try {
					mediaType = MediaType.parse(mimeTypeOfExtension);
					if (defaultCharset != null && mediaType.is(MediaType.ANY_TEXT_TYPE)) {
						mediaType = mediaType.withCharset(defaultCharset);
					}
				} catch (IllegalArgumentException ignore) {}
			}

			if (mediaType.is(MediaType.ANY_VIDEO_TYPE)
					|| mediaType.is(MediaType.ANY_AUDIO_TYPE) || usingRanges) {
				resp.addHeader(HttpHeaders.ACCEPT_RANGES, "bytes");
			}

			resp.setContentType(mediaType.type() + '/' + mediaType.subtype());

			if (mediaType.charset().isPresent()) {
				resp.setCharacterEncoding(mediaType.charset().get().toString());
			}

			try (ServletOutputStream output = resp.getOutputStream()) {
				if (usingRanges) {
					for (final ByteRange range : ranges) {
						output.write(cachedAsset.getResource(), range.getStart(),
								range.getEnd() - range.getStart() + 1);
					}
				}
				else {
					output.write(cachedAsset.getResource());
				}
			}
		} catch (RuntimeException | URISyntaxException ignored) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}


	}




	/**
	 * Parses a given Range header for one or more byte ranges.
	 *
	 * @param rangeHeader Range header to parse
	 * @param resourceLength Length of the resource in bytes
	 * @return List of parsed ranges
	 */
	private ImmutableList<ByteRange> parseRangeHeader(final String rangeHeader,
			final int resourceLength) {
		final ImmutableList.Builder<ByteRange> builder = ImmutableList.builder();
		if (rangeHeader.indexOf("=") != -1) {
			final String[] parts = rangeHeader.split("=");
			if (parts.length > 1) {
				final List<String> ranges = Splitter.on(",").trimResults().splitToList(parts[1]);
				for (final String range : ranges) {
					builder.add(ByteRange.parse(range, resourceLength));
				}
			}
		}
		return builder.build();
	}

}
