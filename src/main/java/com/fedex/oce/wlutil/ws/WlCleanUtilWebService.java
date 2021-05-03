package com.fedex.oce.wlutil.ws;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fedex.oce.vo.ExecutionEnum.ExecutionCd;
import com.fedex.oce.vo.ResultInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@RestController("/tmp")
public class WlCleanUtilWebService {

	@Value("${wl.adminserver.lockfie}")
	private String lokFile;

	@Value("${wl.adminserver.tmp}")
	private String wlServerTmpFolder;

	@Value("${wl.adminserver.logs}")
	private String wlServerLogsFolder;

	private final Logger LOG = LoggerFactory.getLogger(WlCleanUtilWebService.class);

	private final GsonBuilder gsonBuilderObj = new GsonBuilder();
	private final Gson gsonObj = gsonBuilderObj.create();

	@GetMapping("/clean")
	public ResponseEntity<Map<String, ResultInfo>> clearTempFolder(@RequestParam("clearLogs") boolean clearLogs,
			@RequestParam("delTempFiles") boolean delTempFiles) {
		LOG.info("wlServer Tmp Folder =>" + wlServerTmpFolder);
		LOG.info("wlServer Logs Folder =>" + wlServerLogsFolder);
		LOG.info("Clear Logs =>" + clearLogs + ", Delete Tmp Folder =>" + delTempFiles);

		Map<String, ResultInfo> results = cleanFolders(clearLogs, delTempFiles);

		return new ResponseEntity<>(results, HttpStatus.OK);
	}

	private Map<String, ResultInfo> cleanFolders(boolean clearLogs, boolean delTempFiles) {
		Map<String, ResultInfo> resultsMap = new HashMap<>();

		if (clearLogs || delTempFiles) {
			ResultInfo resultInfo = null;
			if (clearLogs) {
				resultInfo = executeCommand("rm -rf " + wlServerLogsFolder);
				resultsMap.put("clearLogs", resultInfo);
			}

			if (delTempFiles) {
				resultInfo = executeCommand("rm -rf " + wlServerTmpFolder);
				resultsMap.put("delTempFiles", resultInfo);
			}
			LOG.debug(gsonObj.toJson(resultInfo));

		}
		return resultsMap;
	}

	private ResultInfo executeCommand(String command) {
		LOG.debug("command =>" + command);

		String errorStr = null;
		String outputStr = null;

		ResultInfo resultInfo = null;

		if (null != command && command.length() > 0) {
			try {
				Runtime run = Runtime.getRuntime();
				Process procObj = run.exec(command);

				InputStream stdInp = procObj.getInputStream();
				InputStreamReader isrInp = new InputStreamReader(stdInp);
				BufferedReader brInp = new BufferedReader(isrInp);

				InputStream stderr = procObj.getErrorStream();
				InputStreamReader isrErr = new InputStreamReader(stderr);
				BufferedReader brErr = new BufferedReader(isrErr);

				if (null != brErr.readLine()) {
					StringBuffer errSb = new StringBuffer("<ERROR>");
					String line;
					while ((line = brErr.readLine()) != null) {
						errSb.append(line);
					}
					errSb.append("</ERROR>");
					errorStr = errSb.toString();
				}

				if (null != brInp.readLine()) {
					StringBuffer outSb = new StringBuffer("<OUTPUT>");
					String line = null;
					while ((line = brInp.readLine()) != null) {
						outSb.append(line);
					}
					outSb.append("</OUTPUT>");
					outputStr = outSb.toString();
				}

				int exitVal = procObj.waitFor();
				LOG.info("Process exitValue: " + exitVal);
				if (exitVal == 0) {
					//LOG.info(outputStr);
					resultInfo = new ResultInfo(ExecutionCd.SUCCESS, outputStr != null ? outputStr.toString() : "");
				} else {
					LOG.info(errorStr);
					resultInfo = new ResultInfo(ExecutionCd.FAILURE, errorStr.toString());
				}

			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
		return resultInfo;
	}

}
