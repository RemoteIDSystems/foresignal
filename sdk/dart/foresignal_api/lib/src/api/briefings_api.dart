//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

import 'dart:async';

// ignore: unused_import
import 'dart:convert';
import 'package:foresignal_api/src/deserialize.dart';
import 'package:dio/dio.dart';

import 'package:foresignal_api/src/model/briefing.dart';
import 'package:foresignal_api/src/model/briefing_input.dart';
import 'package:foresignal_api/src/model/briefing_run.dart';
import 'package:foresignal_api/src/model/briefing_run_page.dart';
import 'package:foresignal_api/src/model/digest.dart';
import 'package:foresignal_api/src/model/problem.dart';

class BriefingsApi {

  final Dio _dio;

  const BriefingsApi(this._dio);

  /// Create a scheduled briefing
  /// Creates a scheduled briefing. Validation: 5-field cron no more frequent than every 15 minutes, valid IANA &#x60;timezone&#x60;, existing scope ids (a private source the caller may not see is reported as unknown), &#x60;lookback&#x60; 1h–7d; scope members of later prompts (&#x60;placeIds&#x60;, &#x60;viewId&#x60;, &#x60;country&#x60;, &#x60;admin1&#x60;, &#x60;county&#x60;, &#x60;places&#x60;, &#x60;weatherProducts&#x60;) are refused with 400 until their prompt while non-empty, ; &#x60;audio&#x60; (141) is null or &#x60;{voice, format}&#x60; with a voice of 1–100 characters and &#x60;format&#x60; &#x60;mp3&#x60; (default) or &#x60;opus&#x60;, accepted without a &#x60;tts&#x60; provider (runs then record &#x60;audio.status&#x60; &#x60;skipped&#x60;); channels that do not exist, are disabled or are not subscribed to &#x60;briefing&#x60; produce &#x60;warnings&#x60;, not failures; &#x60;mode&#x3D;llm&#x60; is accepted without a provider (checked at run time). The creating token&#39;s scope is the briefing&#39;s owner scope: only an &#x60;admin&#x60;-owned briefing lists records of private sources, any other counts them only.  **Scope:** &#x60;agent&#x60;.
  ///
  /// Parameters:
  /// * [briefingInput] 
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [Briefing] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<Briefing>> createBriefing({ 
    required BriefingInput briefingInput,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/briefings';
    final _options = Options(
      method: r'POST',
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
      contentType: 'application/json',
      validateStatus: validateStatus,
    );

    dynamic _bodyData;

    try {
      _bodyData = jsonEncode(briefingInput);

    } catch(error, stackTrace) {
      throw DioException(
         requestOptions: _options.compose(
          _dio.options,
          _path,
        ),
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    final _response = await _dio.request<Object>(
      _path,
      data: _bodyData,
      options: _options,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    Briefing? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<Briefing, Briefing>(rawData, 'Briefing', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<Briefing>(
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

  /// Delete a briefing
  /// Past runs are kept until retention removes them.  **Scope:** &#x60;agent&#x60; or &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [briefingId] - Briefing id
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future]
  /// Throws [DioException] if API call or serialization fails
  Future<Response<void>> deleteBriefing({ 
    required String briefingId,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/briefings/{briefingId}'.replaceAll('{' r'briefingId' '}', briefingId.toString());
    final _options = Options(
      method: r'DELETE',
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

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    return _response;
  }

  /// Get a briefing
  /// Get a briefing  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [briefingId] - Briefing id
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [Briefing] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<Briefing>> getBriefing({ 
    required String briefingId,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/briefings/{briefingId}'.replaceAll('{' r'briefingId' '}', briefingId.toString());
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

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    Briefing? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<Briefing, Briefing>(rawData, 'Briefing', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<Briefing>(
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

  /// Briefing audio podcast feed
  /// RSS 2.0 feed (058 feed writer family) of the briefing&#39;s runs that have audio (&#x60;audio.status&#x60; &#x60;ok&#x60; and a media row that still exists), newest first, at most 50 items. Each &#x60;&lt;item&gt;&#x60; has a non-permalink &#x60;&lt;guid&gt;&#x60;, the run&#39;s HTML rendition as &#x60;&lt;link&gt;&#x60;, its plain text as &#x60;&lt;description&gt;&#x60;, and an &#x60;&lt;enclosure url length type&gt;&#x60; to the 043 media (&#x60;audio/mpeg&#x60; or &#x60;audio/ogg&#x60;), whose detached JWS is the run&#39;s &#x60;audio.signature&#x60; (109). Feed keys work through &#x60;/v1/k/{key}/briefings/{briefingId}/podcast.rss&#x60;; the enclosure URLs then carry the same key, so create the key with prefixes covering both the feed and &#x60;/v1/media/&#x60;. Same visibility as the run feeds: 404 unless the briefing is visible to the caller, runs citing private sources only behind the private gate. Strong ETag and &#x60;Last-Modified&#x60;; 304 on a match.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [briefingId] - Briefing id
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [String] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<String>> getBriefingPodcast({ 
    required String briefingId,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/briefings/{briefingId}/podcast.rss'.replaceAll('{' r'briefingId' '}', briefingId.toString());
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

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
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

  /// Get a briefing run
  /// JSON by default; sanitized HTML (allowlisted headings, lists, tables, links with rel&#x3D;\&quot;noopener nofollow\&quot;, code, emphasis; light/dark stylesheet; strict CSP) with &#x60;Accept: text/html&#x60; or &#x60;?format&#x3D;html&#x60;. ETag per run.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [briefingId] - Briefing id
  /// * [runId] - Briefing run id
  /// * [format] - Alternative to the Accept header
  /// * [ifNoneMatch] - ETag from a previous response; 304 when unchanged
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [BriefingRun] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<BriefingRun>> getBriefingRun({ 
    required String briefingId,
    required String runId,
    String? format,
    String? ifNoneMatch,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/briefings/{briefingId}/runs/{runId}'.replaceAll('{' r'briefingId' '}', briefingId.toString()).replaceAll('{' r'runId' '}', runId.toString());
    final _options = Options(
      method: r'GET',
      headers: <String, dynamic>{
        if (ifNoneMatch != null) r'If-None-Match': ifNoneMatch,
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
      if (format != null) r'format': format,
    };

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    BriefingRun? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<BriefingRun, BriefingRun>(rawData, 'BriefingRun', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<BriefingRun>(
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

  /// Catch-up digest since a cursor
  /// Counts by persist reason since &#x60;sinceSeq&#x60; (new, changed, removed, edges; routine reported separately), entities active from &#x60;record_live&#x60;, alerts since &#x60;sinceAlertId&#x60;, notable records with persisted seqs and source health. &#x60;gap&#x3D;true&#x60; when &#x60;sinceSeq&#x60; is below the retention floor (counts incomplete). Pass &#x60;next.sinceSeq&#x60;/&#x60;next.sinceAlertId&#x60; on the following call. ETag on (toSeq, untilAlertId, params); 304 support. Private sources appear only as counts unless the caller may see them; naming one the caller may not see is a 400 (unknown source), as is an unknown kind. &#x60;places&#x60; (056) adds the \&quot;Weather at places\&quot; section (&#x60;weatherAtPlaces&#x60;, also under &#x60;sections.weatherAtPlaces&#x60;): each place must exist (400 otherwise) and the caller needs a token (401 without one; places are never anonymous).  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [sinceSeq] - Start cursor (default cutoffSeq(now − 24h))
  /// * [sinceAlertId] - Alerts after this id (default: first alert after now − 24h)
  /// * [source_] - Source ids (repeatable or comma-separated)
  /// * [kind] - Kinds (repeatable or comma-separated)
  /// * [bbox] - minLon,minLat,maxLon,maxLat (WGS84)
  /// * [limit] - Notable records per kind
  /// * [includeHealth] - Include source health
  /// * [ifNoneMatch] - ETag from a previous response; 304 when unchanged
  /// * [places] - Place ids: adds the \"Weather at places\" section (`Digest.weatherAtPlaces`, prompt 056); unknown ids are 400, and a caller without a token is 401
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [Digest] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<Digest>> getDigest({ 
    int? sinceSeq,
    int? sinceAlertId,
    List<String>? source_,
    List<String>? kind,
    String? bbox,
    int? limit = 5,
    bool? includeHealth = true,
    String? ifNoneMatch,
    List<String>? places,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/digest';
    final _options = Options(
      method: r'GET',
      headers: <String, dynamic>{
        if (ifNoneMatch != null) r'If-None-Match': ifNoneMatch,
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
      if (sinceSeq != null) r'sinceSeq': sinceSeq,
      if (sinceAlertId != null) r'sinceAlertId': sinceAlertId,
      if (source_ != null) r'source': source_,
      if (kind != null) r'kind': kind,
      if (bbox != null) r'bbox': bbox,
      if (limit != null) r'limit': limit,
      if (includeHealth != null) r'includeHealth': includeHealth,
      if (places != null) r'places': places,
    };

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    Digest? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<Digest, Digest>(rawData, 'Digest', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<Digest>(
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

  /// Get the latest run of a briefing
  /// The most recent completed run (not one still &#x60;running&#x60;), as JSON or sanitized HTML (&#x60;Accept: text/html&#x60; or &#x60;?format&#x3D;html&#x60;), with the same CSP and ETag rules as &#x60;getBriefingRun&#x60;. 404 when the briefing has no runs yet.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [briefingId] - Briefing id
  /// * [format] - Alternative to the Accept header
  /// * [ifNoneMatch] - ETag from a previous response; 304 when unchanged
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [BriefingRun] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<BriefingRun>> getLatestBriefingRun({ 
    required String briefingId,
    String? format,
    String? ifNoneMatch,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/briefings/{briefingId}/runs/latest'.replaceAll('{' r'briefingId' '}', briefingId.toString());
    final _options = Options(
      method: r'GET',
      headers: <String, dynamic>{
        if (ifNoneMatch != null) r'If-None-Match': ifNoneMatch,
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
      if (format != null) r'format': format,
    };

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    BriefingRun? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<BriefingRun, BriefingRun>(rawData, 'BriefingRun', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<BriefingRun>(
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

  /// List runs of a briefing
  /// List runs of a briefing, newest first. The runs of a deleted briefing stay listed until retention removes them.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [briefingId] - Briefing id
  /// * [cursor] - Opaque pagination cursor from a previous response's nextCursor
  /// * [limit] - Page size
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [BriefingRunPage] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<BriefingRunPage>> listBriefingRuns({ 
    required String briefingId,
    String? cursor,
    int? limit = 500,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/briefings/{briefingId}/runs'.replaceAll('{' r'briefingId' '}', briefingId.toString());
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
      if (cursor != null) r'cursor': cursor,
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

    BriefingRunPage? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<BriefingRunPage, BriefingRunPage>(rawData, 'BriefingRunPage', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<BriefingRunPage>(
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

  /// List briefings
  /// List briefings  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [List<Briefing>] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<List<Briefing>>> listBriefings({ 
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/briefings';
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

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    List<Briefing>? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<List<Briefing>, Briefing>(rawData, 'List<Briefing>', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<List<Briefing>>(
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

  /// Render a briefing without storing or delivering it
  /// Render a briefing without storing or delivering it and without changing any schedule (&#x60;id&#x60; and &#x60;briefingId&#x60; are null). The period is &#x60;lookback&#x60; back, or 24 h. LLM failures, refusals and budget exhaustion produce a &#x60;fallback&#x60; rendition, not an error. Private records are listed only for an &#x60;admin&#x60; caller.  **Scope:** &#x60;agent&#x60; or &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [briefingInput] 
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [BriefingRun] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<BriefingRun>> previewBriefing({ 
    required BriefingInput briefingInput,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/briefings/preview';
    final _options = Options(
      method: r'POST',
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
      contentType: 'application/json',
      validateStatus: validateStatus,
    );

    dynamic _bodyData;

    try {
      _bodyData = jsonEncode(briefingInput);

    } catch(error, stackTrace) {
      throw DioException(
         requestOptions: _options.compose(
          _dio.options,
          _path,
        ),
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    final _response = await _dio.request<Object>(
      _path,
      data: _bodyData,
      options: _options,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    BriefingRun? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<BriefingRun, BriefingRun>(rawData, 'BriefingRun', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<BriefingRun>(
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

  /// Run a briefing now
  /// Runs synchronously (up to 120 s), stores the run and delivers it to the briefing&#39;s channels. A run still going after 120 s is returned with status &#x60;running&#x60; and completes in the background. In LLM mode any model failure, refusal or exhausted LLM budget produces a &#x60;fallback&#x60; run (the digest, with the reason in &#x60;error&#x60; and &#x60;warnings&#x60;), not an error.  **Scope:** &#x60;agent&#x60; or &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [briefingId] - Briefing id
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [BriefingRun] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<BriefingRun>> runBriefing({ 
    required String briefingId,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/briefings/{briefingId}/run'.replaceAll('{' r'briefingId' '}', briefingId.toString());
    final _options = Options(
      method: r'POST',
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

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    BriefingRun? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<BriefingRun, BriefingRun>(rawData, 'BriefingRun', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<BriefingRun>(
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

  /// Replace a briefing
  /// Replace a briefing whole (validated as &#x60;createBriefing&#x60;; output-only &#x60;Briefing&#x60; members are ignored, so a read briefing can be sent back). &#x60;next_run_at&#x60; is recomputed; past runs are kept. The replacing token becomes the owner.  **Scope:** &#x60;agent&#x60; or &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [briefingId] - Briefing id
  /// * [briefingInput] 
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [Briefing] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<Briefing>> updateBriefing({ 
    required String briefingId,
    required BriefingInput briefingInput,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/briefings/{briefingId}'.replaceAll('{' r'briefingId' '}', briefingId.toString());
    final _options = Options(
      method: r'PUT',
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
      contentType: 'application/json',
      validateStatus: validateStatus,
    );

    dynamic _bodyData;

    try {
      _bodyData = jsonEncode(briefingInput);

    } catch(error, stackTrace) {
      throw DioException(
         requestOptions: _options.compose(
          _dio.options,
          _path,
        ),
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    final _response = await _dio.request<Object>(
      _path,
      data: _bodyData,
      options: _options,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    Briefing? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<Briefing, Briefing>(rawData, 'Briefing', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<Briefing>(
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
