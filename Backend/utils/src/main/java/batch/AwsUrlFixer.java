package batch;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.nostalgia.persistence.model.Video;

public class AwsUrlFixer extends BatchClass{



	@Override
	public String getName() {
		return "AwsUrlFixer"; 
	}


	@Override
	public Set<JsonDocument> execute(Collection<JsonDocument> input) {
		HashSet<JsonDocument> toSave = new HashSet<JsonDocument>();

		for(JsonDocument orig : input){

			JsonObject video = orig.content();


			String linkToVideo = video.getString("url");

			if(linkToVideo == null){
				linkToVideo = video.getString("mpd");
			}

			//if video has /data in paths
			if(linkToVideo.contains("cloudfront.net") && linkToVideo.contains("data")){

				linkToVideo = linkToVideo.replace("data/", "");
				//fix url
				video.put("url", linkToVideo);
			}

			toSave.add(orig);
		}



		return toSave;
	}

}
