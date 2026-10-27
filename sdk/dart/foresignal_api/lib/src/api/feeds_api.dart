//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

import 'dart:async';

// ignore: unused_import
import 'dart:convert';
import 'package:foresignal_api/src/deserialize.dart';
import 'package:dio/dio.dart';

import 'package:foresignal_api/src/model/json_feed.dart';
import 'package:foresignal_api/src/model/problem.dart';

class FeedsApi {

  final Dio _dio;

  const FeedsApi(this._dio);

  /// Alerts as an Atom feed
  /// RFC 4287 Atom feed of alerts with GeoRSS-Simple geometry (&#x60;georss:point&#x60;, or &#x60;georss:polygon&#x60; for single polygons ≤ 500 vertices, else the centroid). Entries are ordered newest first by last lifecycle change. Entry &#x60;id&#x60; is &#x60;tag:foresignal,2026:{installId}/alert/{id}&#x60;, title is &#x60;shortTitle&#x60; (else the title), the alternate link is the client deep link (else &#x60;publicBaseUrl/v1/alerts/{id}&#x60;), categories carry severity and rule tags. Content is markdown rendered with commonmark-java and sanitised by an allowlist (&#x60;p, br, strong, em, code, pre, ul, ol, li, blockquote, h3–h6, a[href]&#x60; http/https only). Private-source alerts are excluded unless the token or feed key passes the private gate and &#x60;EgressPolicy.allows(FEED)&#x60;. Conditional GET: ETag over (max lifecycle timestamp, max id, filters) plus &#x60;Last-Modified&#x60;. Feed readers without headers use a 035 feed key (&#x60;/v1/k/{key}/alerts/feed.atom&#x60;). RSS 2.0 and WebSub publishing are not offered.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).  **v2.0 (per-user, R2-008):** an alert carries its rule&#39;s owner from when it fired. A signed-in user sees, and acts on, the alerts of their own rules and of shared rules; another user&#39;s alert is left out of lists, feeds and streams and is 404 by id. A session holding &#x60;users.manage&#x60; and every API token see all alerts; anonymous callers and feed keys see the shared ones.
  ///
  /// Parameters:
  /// * [rule] - Filter by trigger rule id (repeatable or comma-separated)
  /// * [minSeverity] - Minimum severity
  /// * [source_] - Filter by source id (repeatable or comma-separated)
  /// * [kind] - Filter by kind (repeatable or comma-separated)
  /// * [state] - Filter by alert lifecycle state (017; repeatable or comma-separated)
  /// * [limit] - Number of entries, newest first by last lifecycle change
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [String] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<String>> getAlertsAtom({ 
    List<String>? rule,
    String? minSeverity,
    List<String>? source_,
    List<String>? kind,
    List<String>? state,
    int? limit = 50,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/alerts/feed.atom';
    final _options = Options(
      method: r'GET',
      headers: <String, dynamic>{
        ...?headers,
      },
      extra: <String, dynamic>{
        'secure': <Map<String, String>>[
          {
            'type': 'http',
            'scheme': 'bearer',
            'name': 'bearerToken',
          },
        ],
        ...?extra,
      },
      validateStatus: validateStatus,
    );

    final _queryParameters = <String, dynamic>{
      if (rule != null) r'rule': rule,
      if (minSeverity != null) r'minSeverity': minSeverity,
      if (source_ != null) r'source': source_,
      if (kind != null) r'kind': kind,
      if (state != null) r'state': state,
      if (limit != null) r'limit': limit,
    };

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    String? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<String, String>(rawData, 'String', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<String>(
      data: _responseData,
      headers: _response.headers,
      isRedirect: _response.isRedirect,
      requestOptions: _response.requestOptions,
      redirects: _response.redirects,
      statusCode: _response.statusCode,
      statusMessage: _response.statusMessage,
      extra: _response.extra,
    );
  }

  /// Alerts as a JSON Feed
  /// JSON Feed 1.1 of alerts; each item carries the &#x60;_foresignal&#x60; extension &#x60;{severity, state, sourceId, externalId, geometry}&#x60;. Entries are ordered newest first by last lifecycle change. Entry &#x60;id&#x60; is &#x60;tag:foresignal,2026:{installId}/alert/{id}&#x60;, title is &#x60;shortTitle&#x60; (else the title), the alternate link is the client deep link (else &#x60;publicBaseUrl/v1/alerts/{id}&#x60;), categories carry severity and rule tags. Content is markdown rendered with commonmark-java and sanitised by an allowlist (&#x60;p, br, strong, em, code, pre, ul, ol, li, blockquote, h3–h6, a[href]&#x60; http/https only). Private-source alerts are excluded unless the token or feed key passes the private gate and &#x60;EgressPolicy.allows(FEED)&#x60;. Conditional GET: ETag over (max lifecycle timestamp, max id, filters) plus &#x60;Last-Modified&#x60;. Feed readers without headers use a 035 feed key (&#x60;/v1/k/{key}/alerts/feed.atom&#x60;). RSS 2.0 and WebSub publishing are not offered.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).  **v2.0 (per-user, R2-008):** an alert carries its rule&#39;s owner from when it fired. A signed-in user sees, and acts on, the alerts of their own rules and of shared rules; another user&#39;s alert is left out of lists, feeds and streams and is 404 by id. A session holding &#x60;users.manage&#x60; and every API token see all alerts; anonymous callers and feed keys see the shared ones.
  ///
  /// Parameters:
  /// * [rule] - Filter by trigger rule id (repeatable or comma-separated)
  /// * [minSeverity] - Minimum severity
  /// * [source_] - Filter by source id (repeatable or comma-separated)
  /// * [kind] - Filter by kind (repeatable or comma-separated)
  /// * [state] - Filter by alert lifecycle state (017; repeatable or comma-separated)
  /// * [limit] - Number of entries, newest first by last lifecycle change
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [JsonFeed] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<JsonFeed>> getAlertsJsonFeed({ 
    List<String>? rule,
    String? minSeverity,
    List<String>? source_,
    List<String>? kind,
    List<String>? state,
    int? limit = 50,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/alerts/feed.json';
    final _options = Options(
      method: r'GET',
      headers: <String, dynamic>{
        ...?headers,
      },
      extra: <String, dynamic>{
        'secure': <Map<String, String>>[
          {
            'type': 'http',
            'scheme': 'bearer',
            'name': 'bearerToken',
          },
        ],
        ...?extra,
      },
      validateStatus: validateStatus,
    );

    final _queryParameters = <String, dynamic>{
      if (rule != null) r'rule': rule,
      if (minSeverity != null) r'minSeverity': minSeverity,
      if (source_ != null) r'source': source_,
      if (kind != null) r'kind': kind,
      if (state != null) r'state': state,
      if (limit != null) r'limit': limit,
    };

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    JsonFeed? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<JsonFeed, JsonFeed>(rawData, 'JsonFeed', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<JsonFeed>(
      data: _responseData,
      headers: _response.headers,
      isRedirect: _response.isRedirect,
      requestOptions: _response.requestOptions,
      redirects: _response.redirects,
      statusCode: _response.statusCode,
      statusMessage: _response.statusMessage,
      extra: _response.extra,
    );
  }

  /// Briefing runs as an Atom feed
  /// Runs of one briefing, newest first. Entry id &#x60;tag:foresignal,2026:{installId}/briefing-run/{id}&#x60;; content embeds the full run markdown, rendered and sanitised like alert entries. Conditional GET with ETag and &#x60;Last-Modified&#x60;; feed keys via &#x60;/v1/k/{key}/briefings/{briefingId}/runs/feed.atom&#x60;.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [briefingId] - Briefing id
  /// * [limit] - Number of entries
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [String] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<String>> getBriefingRunsAtom({ 
    required String briefingId,
    int? limit = 50,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/briefings/{briefingId}/runs/feed.atom'.replaceAll('{' r'briefingId' '}', briefingId.toString());
    final _options = Options(
      method: r'GET',
      headers: <String, dynamic>{
        ...?headers,
      },
      extra: <String, dynamic>{
        'secure': <Map<String, String>>[
          {
            'type': 'http',
            'scheme': 'bearer',
            'name': 'bearerToken',
          },
        ],
        ...?extra,
      },
      validateStatus: validateStatus,
    );

    final _queryParameters = <String, dynamic>{
      if (limit != null) r'limit': limit,
    };

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    String? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<String, String>(rawData, 'String', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<String>(
      data: _responseData,
      headers: _response.headers,
      isRedirect: _response.isRedirect,
      requestOptions: _response.requestOptions,
      redirects: _response.redirects,
      statusCode: _response.statusCode,
      statusMessage: _response.statusMessage,
      extra: _response.extra,
    );
  }

  /// Briefing runs as a JSON Feed
  /// Runs of one briefing, newest first. Entry id &#x60;tag:foresignal,2026:{installId}/briefing-run/{id}&#x60;; content embeds the full run markdown, rendered and sanitised like alert entries. Conditional GET with ETag and &#x60;Last-Modified&#x60;; feed keys via &#x60;/v1/k/{key}/briefings/{briefingId}/runs/feed.atom&#x60;.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [briefingId] - Briefing id
  /// * [limit] - Number of entries
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [JsonFeed] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<JsonFeed>> getBriefingRunsJsonFeed({ 
    required String briefingId,
    int? limit = 50,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/briefings/{briefingId}/runs/feed.json'.replaceAll('{' r'briefingId' '}', briefingId.toString());
    final _options = Options(
      method: r'GET',
      headers: <String, dynamic>{
        ...?headers,
      },
      extra: <String, dynamic>{
        'secure': <Map<String, String>>[
          {
            'type': 'http',
            'scheme': 'bearer',
            'name': 'bearerToken',
          },
        ],
        ...?extra,
      },
      validateStatus: validateStatus,
    );

    final _queryParameters = <String, dynamic>{
      if (limit != null) r'limit': limit,
    };

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    JsonFeed? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<JsonFeed, JsonFeed>(rawData, 'JsonFeed', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<JsonFeed>(
      data: _responseData,
      headers: _response.headers,
      isRedirect: _response.isRedirect,
      requestOptions: _response.requestOptions,
      redirects: _response.redirects,
      statusCode: _response.statusCode,
      statusMessage: _response.statusMessage,
      extra: _response.extra,
    );
  }

}
