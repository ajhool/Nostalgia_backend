package batch;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.couchbase.client.java.document.JsonDocument;
import com.nostalgia.persistence.model.Video;

public class User_DeletedVideoIdFixer2 extends LocationBatchClass{

	

	@Override
	public String getName() {
		return "Location video id fixer"; 
	}


	@Override
	public Set<JsonDocument> execute(Collection<JsonDocument> input) {
		HashSet<JsonDocument> toSave = new HashSet<JsonDocument>();
		
		//for all fields with video ids, check that video actually exists 
		
		//if not, remove
		
		//add to set so that updates get saved
		return toSave;
	}

}
