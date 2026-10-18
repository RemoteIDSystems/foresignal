/// The minimal HTTP layer of the hand-written SDK code: dio (the client the generated package uses), a base URL,
/// a bearer token and JSON. It does not use the generated types.
library;

import 'dart:convert';

import 'package:dio/dio.dart';

import 'errors.dart';

/// A parsed JSON response with the raw response for its headers.
class JsonResponse {
  /// Creates the response.
  JsonResponse(this.data, this.statusCode, this.headers);

  /// The decoded body (null for an empty body).
  final Object? data;

  /// HTTP status.
  final int statusCode;

  /// Response headers.
  final Headers headers;

  /// The body as a JSON object (empty when it is not one).
  Map<String, Object?> get json => data is Map<String, Object?> ? data as Map<String, Object?> : const {};
}

/// Builds query parameters for [Uri]: lists become repeated parameters, nulls are left out.
Map<String, Object>? encodeQuery(Map<String, Object?>? query) {
  if (query == null) {
    return null;
  }
  final out = <String, Object>{};
  query.forEach((key, value) {
    if (value == null) {
      return;
    }
    if (value is Iterable) {
      out[key] = value.map((v) => '$v').toList();
    } else {
      out[key] = '$value';
    }
  });
  return out.isEmpty ? null : out;
}

/// Sends requests to one Foresignal instance.
class HttpTransport {
  /// Creates a transport. [baseUrl] is the Foresignal base URL without `/v1`; [token] is sent as
  /// `Authorization: Bearer …`; pass [dio] to share the generated client's instance.
  HttpTransport({required String baseUrl, this.token, Map<String, String>? headers, Dio? dio})
      : baseUrl = baseUrl.replaceAll(RegExp(r'/+$'), ''),
        extraHeaders = Map.unmodifiable(headers ?? const {}),
        dio = dio ?? Dio();

  /// The base URL without a trailing slash.
  final String baseUrl;

  /// The API token, if any.
  final String? token;

  /// Extra headers sent with every request.
  final Map<String, String> extraHeaders;

  /// The dio instance used for every request.
  final Dio dio;

  /// The absolute URI of [path] (starting with `/`) with [query].
  Uri uri(String path, [Map<String, Object?>? query]) {
    final base = Uri.parse(baseUrl + path);
    final params = encodeQuery(query);
    return params == null ? base : base.replace(queryParameters: params);
  }

  /// The headers of a request: the extra headers, the token (when [auth]), then [extra].
  Map<String, String> headers([Map<String, String>? extra, bool auth = true]) {
    final out = <String, String>{...extraHeaders};
    if (auth && token != null && token!.isNotEmpty) {
      out['Authorization'] = 'Bearer $token';
    }
    out.addAll(extra ?? const {});
    return out;
  }

  /// Sends a request and returns the raw text response, whatever its status.
  Future<Response<String>> send(String method, String path,
      {Map<String, Object?>? query,
      Object? body,
      String contentType = 'application/json',
      Map<String, String>? headers,
      bool auth = true,
      CancelToken? cancelToken}) {
    return dio.requestUri<String>(
      uri(path, query),
      data: body == null ? null : jsonEncode(body),
      cancelToken: cancelToken,
      options: Options(
        method: method,
        headers: this.headers({'Accept': 'application/json', ...?headers}, auth),
        contentType: body == null ? null : contentType,
        responseType: ResponseType.plain,
        validateStatus: (_) => true,
      ),
    );
  }

  /// Sends a request and parses the JSON answer.
  ///
  /// Throws [ApiException] (or [ResyncRequiredException]) for a non-2xx status.
  Future<JsonResponse> request(String method, String path,
      {Map<String, Object?>? query,
      Object? body,
      String contentType = 'application/json',
      Map<String, String>? headers,
      bool auth = true,
      CancelToken? cancelToken}) async {
    final response = await send(method, path,
        query: query, body: body, contentType: contentType, headers: headers, auth: auth, cancelToken: cancelToken);
    final status = response.statusCode ?? 0;
    if (status < 200 || status >= 300) {
      throw apiExceptionFrom(status, response.data, response.headers.value('retry-after'));
    }
    final text = response.data;
    return JsonResponse(text == null || text.isEmpty ? null : jsonDecode(text), status, response.headers);
  }

  /// `GET` returning JSON.
  Future<JsonResponse> getJson(String path, [Map<String, Object?>? query]) => request('GET', path, query: query);

  /// `POST` with a JSON body returning JSON.
  Future<JsonResponse> postJson(String path, Object body) => request('POST', path, body: body);

  /// Opens a streamed `GET` (any status; the caller reads or drains the body).
  Future<Response<ResponseBody>> openStream(String path,
      {Map<String, Object?>? query, Map<String, String>? headers, bool auth = true, CancelToken? cancelToken}) {
    return dio.requestUri<ResponseBody>(
      uri(path, query),
      cancelToken: cancelToken,
      options: Options(
        method: 'GET',
        headers: this.headers(headers, auth),
        responseType: ResponseType.stream,
        validateStatus: (_) => true,
      ),
    );
  }
}
