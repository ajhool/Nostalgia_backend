package batch;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.couchbase.client.java.document.JsonDocument;
import com.nostalgia.persistence.model.Video;

public class DeletedVideoIdFixer extends BatchClass implements UserBatchClass, LocationBatchClass{

	

	@Override
	public String getName() {
		return "User video id fixer"; 
	}


	@Override
	public Set<JsonDocument> execute(Collection<JsonDocument> input) {
		HashSet<JsonDocument> toSave = new HashSet<JsonDocument>();
		
		for(JsonDocument original : input){
		//for all fields with video ids, check that video actually exists 
		
		//if not, remove
		
		//add to set so that updates get saved
			toSave.add(original);
		}
		return toSave;
	}

}
