package com.nostalgia.webserver.resource;


import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.hash.Hashing;
import com.google.common.io.Resources;
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import com.nostalgia.webserver.auth.BasicUser;

import io.dropwizard.auth.Auth;
import io.dropwizard.servlets.assets.ByteRange;
import io.dropwizard.servlets.assets.ResourceURL;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Hashtable;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

public class WebServlet extends HttpServlet {
    private static final long serialVersionUID = 6393345594784987909L;
    private static final CharMatcher SLASHES = CharMatcher.is('/');
    
	private static final Object CONTENT_SIGNATURE = "deadbeef";
	private static final Object CONTENT_KEY = "tok12";
	
    private static class CachedAsset {
        private final byte[] resource;
        private final String eTag;
        private final long lastModifiedTime;

        private CachedAsset(byte[] resource, long lastModifiedTime) {
            this.resource = resource;
            this.eTag = '"' + Hashing.murmur3_128().hashBytes(resource).toString() + '"';
            this.lastModifiedTime = lastModifiedTime;
        }

        public byte[] getResource() {
            return resource;
        }

        public String getETag() {
            return eTag;
        }

        public long getLastModifiedTime() {
            return lastModifiedTime;
        }
    }

    private static final MediaType DEFAULT_MEDIA_TYPE = MediaType.HTML_UTF_8;


    private final String resourcePath;
    private final String uriPath;
    private final Charset defaultCharset;

    /**
     * Creates a new {@code AssetServlet} that serves static assets loaded from {@code resourceURL}
     * (typically a file: or jar: URL). The assets are served at URIs rooted at {@code uriPath}. For
     * example, given a {@code resourceURL} of {@code "file:/data/assets"} and a {@code uriPath} of
     * {@code "/js"}, an {@code AssetServlet} would serve the contents of {@code
     * /data/assets/example.js} in response to a request for {@code /js/example.js}. If a directory
     * is requested and {@code indexFile} is defined, then {@code AssetServlet} will attempt to
     * serve a file with that name in that directory. If a directory is requested and {@code
     * indexFile} is null, it will serve a 404.
     *
     * @param resourcePath   the base URL from which assets are loaded
     * @param uriPath        the URI path fragment in which all requests are rooted
     * @param defaultCharset the default character set
     */
    public WebServlet(String resourcePath,
                        String uriPath,
                        Charset defaultCharset) {
        final String trimmedPath = SLASHES.trimFrom(resourcePath);
        this.resourcePath = trimmedPath.isEmpty() ? trimmedPath : trimmedPath + '/';
        final String trimmedUri = SLASHES.trimTrailingFrom(uriPath);
        this.uriPath = trimmedUri.isEmpty() ? "/" : trimmedUri;
        this.defaultCharset = defaultCharset;
    
        
        // ie this user has no password
          
//        validUsers.put(authorized.getName() + ":" + authorized.getPassword(),"authorized");
    }

//    Hashtable validUsers = new Hashtable();
    
    public URL getResourceURL() {
        return Resources.getResource(resourcePath);
    }

    public String getUriPath() {
        return uriPath;
    }

//    public String getIndexFile() {
//        return indexFile;
//    }
    
    // This method checks the user information sent in the Authorization
    // header against the database of users maintained in the users Hashtable.
    protected boolean allowUser(String auth) throws IOException {
         
        if (auth == null) {
            return false;  // no auth
        }
        if (!auth.toUpperCase().startsWith("BASIC ")) { 
            return false;  // we only do BASIC
        }
        // Get encoded user and password, comes after "BASIC "
        String userpassEncoded = auth.substring(6);
        // Decode it, using any base 64 decoder
        sun.misc.BASE64Decoder dec = new sun.misc.BASE64Decoder();
        String userpassDecoded = new String(dec.decodeBuffer(userpassEncoded));
     
//        // Check our user list to see if that user and password are "allowed"
//        if ("authorized".equals(validUsers.get(userpassDecoded))) {
//            return true;
//        } else {
//            return false;
//        }
        return true; 
    }
    
    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException {
    	
    	   resp.setContentType("text/html");
           
    	   Cookie[] cookies = req.getCookies();
    	   
    	   if(cookies == null){
    		   resp.sendError(401, "Cookies required to access content");
    		   return;
    	   }
    	  
    	   String key = this.extractKey(cookies);
    	   
    	   if(key == null){
    		   resp.sendError(401, "Invalid key specified");
    		   return;
    	   }
    	  
    	   String signature = this.extractSignature(cookies);
    	   
    	   if(signature == null){
    		   resp.sendError(401, "Invalid sigature specified");
    		   return;
    	   }
    	   
    	   	
    	   boolean valid = this.validate(key, signature);
           
    	   if(!valid){
    		   resp.sendError(401, "Unauthorized.");
    		   return ;
    	   }
           
        try {
            final StringBuilder builder = new StringBuilder(req.getServletPath());
            if (req.getPathInfo() != null) {
                builder.append(req.getPathInfo());
            }
            final CachedAsset cachedAsset = loadAsset(builder.toString());
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
                        return;
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

    private boolean validate(String key, String signature) {
		if(!key.equals(CONTENT_KEY) || !signature.equals(CONTENT_SIGNATURE)){
		return false;
		} else return true;
	}

	private String extractSignature(Cookie[] cookies) {
		if (cookies == null){
			return null;
		}
		
		for(Cookie cook : cookies){
			if(cook.getName().equals("nossig")){
				return cook.getValue();
			}
		}
		return null;
	}

	private String extractKey(Cookie[] cookies) {
		if (cookies == null){
			return null;
		}
		
		for(Cookie cook : cookies){
			if(cook.getName().equals("nostok")){
				return cook.getValue();
			}
		}
		return null;
	}

	private CachedAsset loadAsset(String key) throws URISyntaxException, IOException {
        checkArgument(key.startsWith(uriPath));
        final String requestedResourcePath = SLASHES.trimFrom(key.substring(uriPath.length()));
        String currentDir = System.getProperty("user.dir");
        String absoluteRequestedResourcePath = "/"+ this.resourcePath + requestedResourcePath;

//        URL requestedResourceURL = new URL(absoluteRequestedResourcePath);
        if (new File(absoluteRequestedResourcePath).isDirectory()) {
            
                // directory requested but no index file defined
                return null;
            
        }

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

    private boolean isCachedClientSide(HttpServletRequest req, CachedAsset cachedAsset) {
        return cachedAsset.getETag().equals(req.getHeader(HttpHeaders.IF_NONE_MATCH)) ||
                (req.getDateHeader(HttpHeaders.IF_MODIFIED_SINCE) >= cachedAsset.getLastModifiedTime());
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
