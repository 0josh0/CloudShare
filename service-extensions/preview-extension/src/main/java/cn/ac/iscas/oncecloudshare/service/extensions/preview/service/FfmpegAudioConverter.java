package cn.ac.iscas.oncecloudshare.service.extensions.preview.service;

import it.sauronsoftware.jave.AudioAttributes;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncodingAttributes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import cn.ac.iscas.oncecloudshare.service.extensions.preview.FileConverter;

import com.google.common.io.Files;
import com.google.common.io.InputSupplier;

@Component
public class FfmpegAudioConverter implements FileConverter {
	@Override
	public String[] getSupports() {
		return new String[] { "wav", "wma" };
	}

	@Override
	public String getName() {
		return "ffmpeg audio converter";
	}

	@Override
	public String convert(final InputStream input, String inputExtension, OutputStream output) throws Exception {
		File source = File.createTempFile(UUID.randomUUID().toString(), inputExtension);
		File target = File.createTempFile(UUID.randomUUID().toString(), "mp3");
		try {
			Files.copy(new InputSupplier<InputStream>() {
				@Override
				public InputStream getInput() throws IOException {
					return input;
				}
			}, source);
			if ("wav".equalsIgnoreCase(inputExtension)) {
			}

			AudioAttributes audio = new AudioAttributes();
			audio.setCodec("libmp3lame");
			audio.setBitRate(new Integer(128000));
			audio.setChannels(new Integer(2));
			audio.setSamplingRate(new Integer(44100));
			EncodingAttributes attrs = new EncodingAttributes();
			attrs.setFormat("mp3");
			attrs.setAudioAttributes(audio);
			Encoder encoder = new Encoder();
			encoder.encode(source, target, attrs);
			Files.copy(target, output);
		} finally {
			FileUtils.deleteQuietly(source);
			FileUtils.deleteQuietly(target);
		}
		return "mp3";
	}
}
