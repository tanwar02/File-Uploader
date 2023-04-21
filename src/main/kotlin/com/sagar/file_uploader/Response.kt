package com.sagar.file_uploader

import org.springframework.http.HttpStatusCode

class Response(var message: String?, var statusCode: HttpStatusCode, var code: Int) {
}