package oauth;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Wither;
import org.joda.time.DateTime;

import javax.validation.constraints.NotNull;

import java.io.Serializable;
import java.security.Principal;
import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode
@Wither
public class AccessToken implements Serializable, Principal {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3569465691710328516L;

	@JsonProperty("access_token_id")
	@NotNull
	private UUID access_token_id;

	@JsonProperty("user_id")
	@NotNull
	private String user_id;

	public AccessToken(UUID access_token_id, String user_id, DateTime last_access_utc) {
		super();
		this.access_token_id = access_token_id;
		this.user_id = user_id;
		this.last_access_utc = last_access_utc;
	}

	public UUID getAccess_token_id() {
		return access_token_id;
	}

	public void setAccess_token_id(UUID access_token_id) {
		this.access_token_id = access_token_id;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public DateTime getLast_access_utc() {
		return last_access_utc;
	}

	public AccessToken setLast_access_utc(DateTime last_access_utc) {
		this.last_access_utc = last_access_utc;
		return this; 
	}

	@JsonProperty("last_access_utc")
	@NotNull
	private DateTime last_access_utc;

	@Override
	public String getName() {

		return access_token_id.toString(); 
	}
}
