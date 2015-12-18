package batch;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.couchbase.client.java.document.JsonDocument;
import com.nostalgia.persistence.model.Video;

public class NonAwsVideoJsonGetter extends VideoBatchClass{

	

	@Override
	public String getName() {
		return "Non-AwsVideoJsonGetter"; 
	}


	@Override
	public Set<JsonDocument> execute(Collection<JsonDocument> input) {
		// TODO Auto-generated method stub
		return null;
	}

}
