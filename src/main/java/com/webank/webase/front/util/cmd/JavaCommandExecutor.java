/**
 * Copyright 2014-2021 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.webase.front.util.cmd;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * use java process to run command in bash -c
 */
@Slf4j
@ToString
public class JavaCommandExecutor {
    public static final long DEFAULT_EXEC_TIMEOUT = 60_60_1000L;

    /**
     * exec command
     * @param command
     * @param timeout unit: ms
     * @return
     */
    public static ExecuteResult executeCommand(String command, long timeout) {
        Process process = null;
        InputStream pIn = null;
        InputStream pErr = null;
        StreamGobbler outputGobbler = null;
        StreamGobbler errorGobbler = null;
        try {
            log.info("exec command:[{}]", command);
            String[] commandArray = { "/bin/bash", "-c", command };
            process = Runtime.getRuntime().exec(commandArray);
            final Process p = process;

            // close process's output stream.
            closeQuietly(p.getOutputStream());

            pIn = process.getInputStream();
            outputGobbler = new StreamGobbler(pIn, "OUTPUT");
            outputGobbler.start();

            pErr = process.getErrorStream();
            errorGobbler = new StreamGobbler(pErr, "ERROR");
            errorGobbler.start();
            long newTimeout = timeout <= 0 ? DEFAULT_EXEC_TIMEOUT : timeout;

            p.waitFor(newTimeout, TimeUnit.MILLISECONDS);
            int exitCode = p.exitValue();

            if (exitCode == 0) {
                log.info("Exec command success: code:[{}], OUTPUT:\n[{}]",
                        exitCode, outputGobbler.getContent());
            } else {
                log.warn("Exec command code not zero: code:[{}], OUTPUT:\n[{}],\nERROR:\n[{}]",
                        exitCode, outputGobbler.getContent(), errorGobbler.getContent());
            }

            return new ExecuteResult(exitCode, outputGobbler.getContent());
        } catch (IOException ex) {
            String errorMessage = "The command [" + command + "] execute failed.";
            log.error(errorMessage, errorMessage);
            return new ExecuteResult(-1, null);
        } catch (InterruptedException ex) {
            String errorMessage = "The command [" + command + "] did not complete due to an interrupted error.";
            log.error(errorMessage, ex);
            return new ExecuteResult(-1, errorMessage);
        } finally {
            if (pIn != null) {
                closeQuietly(pIn);
                if (outputGobbler != null && !outputGobbler.isInterrupted()) {
                    outputGobbler.interrupt();
                }
            }
            if (pErr != null) {
                closeQuietly(pErr);
                if (errorGobbler != null && !errorGobbler.isInterrupted()) {
                    errorGobbler.interrupt();
                }
            }
            if (process != null) {
                process.destroy();
            }
        }
    }

    private static void closeQuietly(Closeable c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (IOException e) {
            log.error("Exception occurred when closeQuietly!!! ", e);
        }
    }
}
