package edu.cvsu.labissues.exception;

import java.util.Map;

/** The JSON body of every error: {"status":400,"error":"Bad Request","message":"...","fieldErrors":{...}} */
public record ApiError(int status, String error, String message, Map<String, String> fieldErrors) {
}
