/*
 * Copyright (c) 2017 com2m GmbH.
 * All rights reserved.
 */

package de.com2m.payload;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.RandomStringUtils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class PayloadSample {

	public static void main(String[] args) throws IOException {
		writeSingleLight();
		writeCompactSingleLight();
		writeLights((short) 10, "lights.random.10");
		writeLightsWithEqualValues((short) 10, "lights.equal.10");
		writeLightsWithEqualValues((short) 50, "lights.equal.50");
	}

	private static void writeLights(short numberOfLights, String filename) throws IOException {
		File bytesFile = new File(filename + ".bytes");
		File jsonFile = new File(filename + ".json");
		List<File> files = Lists.newArrayList(bytesFile, jsonFile);
		files.forEach(File::delete);
		List<Light> lights = Lists.newArrayList();
		Random random = new Random();
		// multiple lights
		for (int i = 0; i < numberOfLights; i++) {
			lights.add(new Light(
					"LXA34-" + RandomStringUtils.randomAlphanumeric(6),
					random.nextFloat() * 100,
					random.nextFloat() * 100,
					(short) random.nextInt(100),
					random.nextBoolean()));
		}
		JsonArray jsonElements = new JsonArray();
		List<Byte> bytes = Lists.newArrayList();
		bytes.addAll(Lists.newArrayList(ArrayUtils.toObject(ByteBuffer.allocate(2).putShort(numberOfLights).array())));
		lights.forEach((Light oneLight) -> {
			jsonElements.add(oneLight.toJsonObject());
			bytes.addAll(Arrays.asList(ArrayUtils.toObject(oneLight.toBytes())));
		});
		String lightsJson = new Gson().toJson(jsonElements);
		Byte[] byteArray = new Byte[0];
		FileUtils.writeByteArrayToFile(bytesFile, ArrayUtils.toPrimitive(bytes.toArray(byteArray)));
		FileUtils.writeStringToFile(jsonFile, lightsJson, Charset.forName("UTF-8"));
		files.forEach(PayloadSample::gzip);
	}

	private static void writeLightsWithEqualValues(short numberOfLights, final String filename) throws IOException {
		File bytesFile = new File(filename + ".bytes");
		File jsonFile = new File(filename + ".json");
		List<File> files = Lists.newArrayList(bytesFile, jsonFile);
		files.forEach(File::delete);
		List<Light> lights = Lists.newArrayList();
		Random random = new Random();

		for (int i = 0; i < numberOfLights; i++) {
			lights.add(new Light(
					"LXA34-691E9" + String.valueOf(i),
					random.nextFloat(),
					0f,
					(short) 0,
					false));
		}
		JsonArray jsonElements = new JsonArray();
		List<Byte> bytes = Lists.newArrayList();
		bytes.addAll(Lists.newArrayList(ArrayUtils.toObject(ByteBuffer.allocate(2).putShort(numberOfLights).array())));
		lights.forEach((Light oneLight) -> {
			jsonElements.add(oneLight.toJsonObject());
			bytes.addAll(Arrays.asList(ArrayUtils.toObject(oneLight.toBytes())));
		});
		String lightsJson = new Gson().toJson(jsonElements);
		Byte[] byteArray = new Byte[0];
		FileUtils.writeByteArrayToFile(bytesFile, ArrayUtils.toPrimitive(bytes.toArray(byteArray)));
		FileUtils.writeStringToFile(jsonFile, lightsJson, Charset.forName("UTF-8"));
		files.forEach(PayloadSample::gzip);
	}

	private static void writeSingleLight() throws IOException {
		Light light = new Light(
				"LXA34-691E90",
				23.5f,
				50.5f,
				(short) 75,
				true);
		File bytesFile = new File("light.bytes");
		File bytesHexFile = new File("light.bytes.hex");
		File jsonFile = new File("light.json");
		List<File> files = Lists.newArrayList(bytesFile, bytesHexFile, jsonFile);
		files.forEach(File::delete);


		byte[] bytes = light.toBytes();
		FileUtils.writeByteArrayToFile(bytesFile, bytes);
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(String.format("%02X ", b));
		}
		FileUtils.writeStringToFile(bytesHexFile, sb.toString(), Charset.forName("UTF-8"));
		FileUtils.writeStringToFile(jsonFile, light.toJson(), Charset.forName("UTF-8"));
		files.forEach(PayloadSample::gzip);
	}

	private static void writeCompactSingleLight() throws IOException {
		Light light = new Light(
				"LXA34-691E90",
				23.5f,
				50.5f,
				(short) 75,
				true);
		File jsonFile = new File("light.compact.json");
		List<File> files = Lists.newArrayList(jsonFile);
		files.forEach(File::delete);

		FileUtils.writeStringToFile(jsonFile, light.toCompactJson(), Charset.forName("UTF-8"));
		files.forEach(PayloadSample::gzip);
	}

	public static void gzip(File file) {
		try {
			InputStream in = Files.newInputStream(file.toPath());
			File outputFile = new File(file.getPath().toString() + ".gzip");
			OutputStream fout = Files.newOutputStream(outputFile.toPath());
			BufferedOutputStream out = new BufferedOutputStream(fout);
			GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(out);
			final byte[] buffer = new byte[8];
			int n = 0;
			while (-1 != (n = in.read(buffer))) {
				gzOut.write(buffer, 0, n);
			}
			gzOut.close();
			in.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	static class Light {

		String serialNumber;
		Float temperature;
		Float power;
		Short dimLevel;
		Boolean on;

		public Light(String serialNumber, Float temperature, Float power, Short dimLevel, Boolean on) {
			this.serialNumber = serialNumber;
			this.temperature = temperature;
			this.power = power;
			this.dimLevel = dimLevel;
			this.on = on;
		}

		public byte[] toBytes() {
			try {
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				byteArrayOutputStream.write(this.serialNumber.getBytes());
				byteArrayOutputStream.write(ByteBuffer.allocate(4).putFloat(this.temperature.byteValue()).array());
				byteArrayOutputStream.write(ByteBuffer.allocate(4).putFloat(this.power.byteValue()).array());
				byteArrayOutputStream.write(ByteBuffer.allocate(2).putShort(this.dimLevel.byteValue()).array());
				byteArrayOutputStream.write(ByteBuffer.allocate(1).put((byte) (this.on ? 1 : 0)).array());
				return byteArrayOutputStream.toByteArray();
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		}

		public String toJson() {
			return new Gson().toJson(this.toJsonObject());
		}

		public JsonObject toJsonObject() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("serialNumber", this.serialNumber);
			jsonObject.addProperty("temperature", Math.round(this.temperature * 100.0) / 100.0);
			jsonObject.addProperty("power", Math.round(this.power * 100.0) / 100.0);
			jsonObject.addProperty("dimLevel", this.dimLevel);
			jsonObject.addProperty("on", this.on);
			return jsonObject;
		}

		public String toCompactJson() {
			return new Gson().toJson(this.toCompactJsonObject());
		}

		public JsonObject toCompactJsonObject() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("sn", this.serialNumber);
			jsonObject.addProperty("temp", Math.round(this.temperature * 100.0) / 100.0);
			jsonObject.addProperty("w", Math.round(this.power * 100.0) / 100.0);
			jsonObject.addProperty("dim", this.dimLevel);
			jsonObject.addProperty("on", this.on);
			return jsonObject;
		}
	}
}
