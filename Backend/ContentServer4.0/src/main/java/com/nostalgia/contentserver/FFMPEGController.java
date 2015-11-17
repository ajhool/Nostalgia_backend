package com.nostalgia.contentserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.nostalgia.contentserver.model.dash.jaxb.RepresentationType;


/*
 * Here's what we are trying to replace here:
 * 	final Process p = Runtime
				.getRuntime()
				.exec("ffmpeg -i http://odna.octoshape.net/f3f5m2v4/cds/ch4_320p/chunklist.m3u8 -g 52 -acodec libfdk_aac "
						+ "-ab 64k -profile:a aac_he -frag_size 4096000 -frag_duration 100000 -vn -vcodec libx264 -vb 448k -f "
						+ "mp4 -movflags frag_keyframe+empty_moov test_audio.mp4 "
						+ "-vcodec libx264 -vb 448k -an -g 52 -f mp4 -movflags frag_keyframe+empty_moov test_video.mp4");
 * 
 */




public class FFMPEGController {

	//public static final String mffmpegBin = "bin/ffmpeg";
	public static final String mffmpegBin = "ffmpeg";

	public class Argument
	{
		String key;
		String value;

		public static final String VIDEOCODEC = "-vcodec";
		public static final String AUDIOCODEC = "-acodec";

		public static final String VIDEOBITSTREAMFILTER = "-vbsf";
		public static final String AUDIOBITSTREAMFILTER = "-absf";

		public static final String VERBOSITY = "-v";
		public static final String FILE_INPUT = "-i";
		public static final String ENABLE_REALTIME = "-re";
		public static final String KEYFRAME_INTERVAL = "-g";
		public static final String AUDIO_BITRATE = "-ab";
		public static final String AUDIO_PROFILE = "-profile:a";
		public static final String FRAGMENT_SIZE = "-frag_size";
		public static final String FRAGMENT_DUR = "-frag_duration";
		public static final String DISABLE_VIDEO = "-vn";
		public static final String VIDEO_BITRATE = "-vb";
		public static final String MOVFLAGS = "-movflags";
		public static final String CONSTANT_RATE_FACTOR = "-crf";
		public static final String SPEED_PRESET = "-preset";
		
		public static final String DISABLE_AUDIO = "-an";
		public static final String ENABLE_OUTFILE_OVERWRITE = "-y";
		public static final String SIZE = "-s";
		public static final String FRAMERATE = "-r";
		public static final String FORMAT = "-f";
		public static final String BITRATE_VIDEO = "-b:v";

		public static final String BITRATE_AUDIO = "-b:a";
		public static final String CHANNELS_AUDIO = "-ac";
		public static final String FREQ_AUDIO = "-ar";

		public static final String STARTTIME = "-ss";
		public static final String DURATION = "-t";


	}

	public ArrayList<String> generateFFMPEGCommand(RepresentationType toTranscode, File original, File outputParent){
		ArrayList<String> cmd = new ArrayList<String>();



		cmd.add(mffmpegBin);
		//cmd.add(Argument.ENABLE_OUTFILE_OVERWRITE);

		//add streaming input
		cmd.add(Argument.FILE_INPUT);
		//cmd.add("http://odna.octoshape.net/f3f5m2v4/cds/ch4_320p/chunklist.m3u8");
		cmd.add(original.getName());


		if(toTranscode.getAudioChannelConfiguration().size() > 0){
			//need to create audio representation

			cmd.add(Argument.DISABLE_VIDEO);

			cmd.add(Argument.CHANNELS_AUDIO);
			cmd.add("2");

			cmd.add("-keyint_min");
			cmd.add("24");

			cmd.add(Argument.KEYFRAME_INTERVAL);
			cmd.add("72");

			cmd.add(Argument.AUDIOCODEC);
			cmd.add("libfdk_aac");

//			cmd.add(Argument.AUDIO_PROFILE);
//			cmd.add("aac_he");

			cmd.add(Argument.AUDIO_BITRATE);
			cmd.add("64k");


		} else {


			//create video representation
			cmd.add(Argument.DISABLE_AUDIO);

			cmd.add(Argument.VIDEO_BITRATE);
			cmd.add(Long.toString(toTranscode.getBandwidth()));


			cmd.add("-keyint_min");
			cmd.add("24");

			cmd.add(Argument.KEYFRAME_INTERVAL);
			cmd.add("72");

			cmd.add(Argument.VIDEOCODEC);
			cmd.add("libx264");
		}

		//cmd.add(Argument.MOVFLAGS);
		//cmd.add("frag_keyframe+empty_moov");


		//cmd.add("-preset");
		//cmd.add("superfast");
		//add audio output file
		cmd.add( outputParent.getAbsolutePath() + "/" + toTranscode.getId() + ".mp4");



		//		cmd.add(mffmpegBin);
		//		cmd.add(Argument.ENABLE_OUTFILE_OVERWRITE);
		//
		//		//add streaming inout
		//		cmd.add(Argument.FILE_INPUT);
		//		cmd.add("http://odna.octoshape.net/f3f5m2v4/cds/ch4_320p/chunklist.m3u8");
		//
		//		//add audio output
		//		cmd.add(Argument.KEYFRAME_INTERVAL);
		//		cmd.add("52");
		//
		//		cmd.add(Argument.AUDIOCODEC);
		//		cmd.add("libfdk_aac");
		//
		//		cmd.add(Argument.AUDIO_BITRATE);
		//		cmd.add("64k");
		//
		//		cmd.add(Argument.AUDIO_PROFILE);
		//		cmd.add("aac_he");
		//
		//		cmd.add(Argument.FRAGMENT_SIZE);
		//		cmd.add("4096000");
		//
		//		cmd.add(Argument.FRAGMENT_DUR);
		//		cmd.add("100000");
		//
		//		cmd.add(Argument.DISABLE_VIDEO);
		//

		//
		//		cmd.add(Argument.VIDEO_BITRATE);
		//		cmd.add("448k");
		//
		//		cmd.add(Argument.FORMAT);
		//		cmd.add("mp4");
		//
		//		cmd.add(Argument.MOVFLAGS);
		//		cmd.add("frag_keyframe+empty_moov");
		//
		//		//add audio output file
		//		cmd.add("test_audio.mp4");
		//
		//		//part II: video output
		//		cmd.add(Argument.VIDEOCODEC);
		//		cmd.add("libx264");
		//
		//		cmd.add(Argument.VIDEO_BITRATE);
		//		cmd.add("448k");
		//
		//		cmd.add(Argument.DISABLE_AUDIO);
		//
		//		cmd.add(Argument.KEYFRAME_INTERVAL);
		//		cmd.add("52");
		//
		//		cmd.add(Argument.FORMAT);
		//		cmd.add("mp4");
		//
		//		cmd.add(Argument.MOVFLAGS);
		//		cmd.add("frag_keyframe+empty_moov");
		//
		//		//add audio output file
		//		cmd.add("test_video.mp4");

		return cmd;

	}

	public int execProcess(List<String> cmds, ShellCallback sc, File fileExec) throws IOException, InterruptedException {		

		//ensure that the arguments are in the correct Locale format
		for (String cmd :cmds)
		{
			cmd = String.format(Locale.US, "%s", cmd);
		}//	public static void main(String[] args) throws IOException, InterruptedException{
		//
		//		if(args.length > 0 && args[0].equalsIgnoreCase("kill")){
		//			killVideoProcessor(false, true);
		//			return;
		//		}
		//
		//		ShellCallback sc = new StdoutCallback();
		//		FFMPEGController me = new FFMPEGController();
		//
		//
		//		List<String> cmds = me.generateFFMPEGCommand(null);
		//
		//		int exit = me.execProcess(cmds, sc, null);
		//		System.out.println("goodbye. Exit code: " + exit);
		//	}

		ProcessBuilder pb = new ProcessBuilder(cmds);
		pb.directory(fileExec);

		StringBuffer cmdlog = new StringBuffer();

		for (String cmd : cmds)
		{
			cmdlog.append(cmd);
			cmdlog.append(' ');
		}

		sc.shellOut(cmdlog.toString());

		//pb.redirectErrorStream(true);

		Process process = pb.start();    


		// any error message?
		StreamGobbler errorGobbler = new StreamGobbler(
				process.getErrorStream(), "ERROR", sc);

		// any output?
		StreamGobbler outputGobbler = new 
				StreamGobbler(process.getInputStream(), "OUTPUT", sc);

		errorGobbler.start();
		outputGobbler.start();

		int exitVal = process.waitFor();

		sc.processComplete(exitVal);

		return exitVal;

	}


	private class StreamGobbler extends Thread
	{
		InputStream is;
		String type;
		ShellCallback sc;

		StreamGobbler(InputStream is, String type, ShellCallback sc)
		{
			this.is = is;
			this.type = type;
			this.sc = sc;
		}

		public void run()
		{
			try
			{
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line=null;
				while ( (line = br.readLine()) != null)
					if (sc != null)
						sc.shellOut(line);

			} catch (IOException ioe)
			{
				//   Log.e(TAG,"error reading shell slog",ioe);
				ioe.printStackTrace();
			}
		}
	}


	public static int killVideoProcessor (boolean asRoot, boolean waitFor) throws IOException
	{
		int killDelayMs = 300;

		int result = -1;

		int procId = -1;

		while ((procId = ShellUtils.findProcessId(mffmpegBin)) != -1)
		{

			//	Log.d(TAG, "Found PID=" + procId + " - killing now...");

			String[] cmd = { ShellUtils.SHELL_CMD_KILL + ' ' + procId + "" };

			try { 
				result = ShellUtils.doShellCommand(cmd, new StdoutCallback(), asRoot, waitFor);
				Thread.sleep(killDelayMs); }
			catch (Exception e){}
		}

		return result;
	}

	public ArrayList<String> generateFFMPEGBaselineCommand(File sourceFile,
			File output) {
		
		ArrayList<String> cmd = new ArrayList<String>();
		
		cmd.add(mffmpegBin);
		//cmd.add(Argument.ENABLE_OUTFILE_OVERWRITE);

		//add streaming input
		cmd.add(Argument.FILE_INPUT);
		//cmd.add("http://odna.octoshape.net/f3f5m2v4/cds/ch4_320p/chunklist.m3u8");
		cmd.add(sourceFile.getName());
		
		cmd.add(Argument.VIDEOCODEC);
		cmd.add("libx264");
		
		cmd.add(Argument.AUDIOCODEC);
		cmd.add("libfdk_aac");

//		cmd.add(Argument.AUDIO_PROFILE);
//		cmd.add("aac_he");
		
		cmd.add(Argument.KEYFRAME_INTERVAL);
		cmd.add("90");
		
		cmd.add(Argument.CONSTANT_RATE_FACTOR);
		cmd.add("17");
		
		
		cmd.add(Argument.AUDIO_BITRATE);
		cmd.add("96k");
		
		//add output 
		cmd.add(output.getName());
		return cmd;
	}


	//	public static void main(String[] args) throws IOException, InterruptedException{
	//
	//		if(args.length > 0 && args[0].equalsIgnoreCase("kill")){
	//			killVideoProcessor(false, true);
	//			return;
	//		}
	//
	//		ShellCallback sc = new StdoutCallback();
	//		FFMPEGController me = new FFMPEGController();
	//
	//
	//		List<String> cmds = me.generateFFMPEGCommand(null);
	//
	//		int exit = me.execProcess(cmds, sc, null);
	//		System.out.println("goodbye. Exit code: " + exit);
	//	}


}
