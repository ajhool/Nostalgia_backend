package batch;

import java.util.Collection;
import java.util.HashSet;
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
		HashSet<JsonDocument> toSave = new HashSet<JsonDocument>();
		
		//if video has exoatmospherics in links
		
		//create video object from document 
		
		//write video object to metadata folder
		
		//download video data + save in local folder
		
		//else, just add video to output, so that it is saved
		
		
		return toSave;
	}

}
