package batch;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.couchbase.client.java.document.JsonDocument;
import com.nostalgia.persistence.model.Video;

public class AwsUrlFixer extends VideoBatchClass{

	

	@Override
	public String getName() {
		return "AwsUrlFixer"; 
	}


	@Override
	public Set<JsonDocument> execute(Collection<JsonDocument> input) {
		HashSet<JsonDocument> toSave = new HashSet<JsonDocument>();
		
		//if video has /data in paths
		
		//remove + save
		
		//else just add to output, no changes needed
		
		
		
		
		return toSave;
	}

}
