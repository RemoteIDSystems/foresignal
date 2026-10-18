//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

import 'dart:async';

// ignore: unused_import
import 'dart:convert';
import 'package:foresignal_api/src/deserialize.dart';
import 'package:dio/dio.dart';

import 'package:foresignal_api/src/model/history_frames.dart';
import 'package:foresignal_api/src/model/problem.dart';
import 'package:foresignal_api/src/model/timeline.dart';
import 'package:foresignal_api/src/model/track.dart';

class HistoryApi {

  final Dio _dio;

  const HistoryApi(this._dio);

  /// Keyframe and delta frames for playback
  /// Keyframe &#x3D; asOf state at &#x60;from&#x60;; each frame holds upserts and removals (tombstone, expiry, leaving the bbox) for its interval, built in one ordered pass on the transaction axis. More than 20,000 entities or 2,000,000 scanned rows → 400 FRAMES_TOO_LARGE (narrow bbox or kind). ndjson sends the keyframe line first (the envelope without &#x60;frames&#x60;), then one line per frame. Frame ends are t_i &#x3D; min(from + i·step, to); (to − from)/step frames must not exceed maxFrames. With a bbox only entities with a version inside it in the range are read; the caller&#39;s restriction and &#x60;place&#x60; judge each version, and an entity whose newest version leaves them is a &#x60;left&#x60; removal.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [from] - Start
  /// * [to] - End
  /// * [step] - Frame step
  /// * [source_] - Source ids (source or kind required) (repeatable or comma-separated)
  /// * [kind] - Kinds (repeatable or comma-separated)
  /// * [bbox] - minLon,minLat,maxLon,maxLat (WGS84)
  /// * [place] - Place id(s) (036), resolved to their areas and intersected with the other filters; requires a token (places are never anonymous)
  /// * [maxFrames] - Frame cap
  /// * [geometry] - Centroid or full geometry
  /// * [format] - Output format
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [HistoryFrames] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<HistoryFrames>> getHistoryFrames({ 
    required DateTime from,
    required DateTime to,
    required String step,
    List<String>? source_,
    List<String>? kind,
    String? bbox,
    List<String>? place,
    int? maxFrames = 240,
    String? geometry,
    String? format,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/history/frames';
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
      if (source_ != null) r'source': source_,
      if (kind != null) r'kind': kind,
      if (bbox != null) r'bbox': bbox,
      if (place != null) r'place': place,
      r'from': from,
      r'to': to,
      r'step': step,
      if (maxFrames != null) r'maxFrames': maxFrames,
      if (geometry != null) r'geometry': geometry,
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

    HistoryFrames? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<HistoryFrames, HistoryFrames>(rawData, 'HistoryFrames', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<HistoryFrames>(
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

  /// Simplified track of one entity
  /// Versions in [from, to] by seq (≤ 200,000 rows scanned, else &#x60;truncated&#x60;), POSITION_STALE carry-forwards skipped, new segment after a tombstone or a gap &gt; &#x60;gap&#x60;, the live position appended when newer, Douglas–Peucker simplification keeping endpoints and FIRST/REAPPEAR/SIGNIFICANT/TRIGGER_EDGE points, split at the antimeridian. Window larger than the source&#39;s retention → 400 TRACK_WINDOW_TOO_LARGE. Caching as asOf tiles, keyed on &#x60;to&#x60;; a response with an appended live point is never immutable. Simplification is Douglas–Peucker with the synchronized Euclidean distance (each point against the time-interpolated position on the simplified segment, in meters on a local equirectangular projection); versions outside the caller&#39;s restriction break the track. A segment with a single point repeats it, so every segment is a valid LineString.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [sourceId] - Source id
  /// * [externalId] - External id (percent-encoded)
  /// * [from] - Default: to − 6h
  /// * [to] - Default: now
  /// * [tolerance] - Metres or `auto` (bbox diagonal / 2000, clamped 5 m–2 km)
  /// * [maxPoints] - Point cap
  /// * [gap] - Gap that starts a new segment
  /// * [includeLive] - Append the current live position
  /// * [fields] - ≤ 4 numeric payload fields (e.g. alt_baro,gs)
  /// * [ifNoneMatch] - ETag from a previous response; 304 when unchanged
  /// * [format] - Response format; `czml` returns a CZML document (prompt 141)
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [Track] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<Track>> getRecordTrack({ 
    required String sourceId,
    required String externalId,
    DateTime? from,
    DateTime? to,
    String? tolerance = 'auto',
    int? maxPoints = 2000,
    String? gap,
    bool? includeLive = true,
    List<String>? fields,
    String? ifNoneMatch,
    String? format,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/records/{sourceId}/{externalId}/track'.replaceAll('{' r'sourceId' '}', sourceId.toString()).replaceAll('{' r'externalId' '}', externalId.toString());
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
      if (from != null) r'from': from,
      if (to != null) r'to': to,
      if (tolerance != null) r'tolerance': tolerance,
      if (maxPoints != null) r'maxPoints': maxPoints,
      if (gap != null) r'gap': gap,
      if (includeLive != null) r'includeLive': includeLive,
      if (fields != null) r'fields': fields,
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

    Track? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<Track, Track>(rawData, 'Track', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<Track>(
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

  /// Event-density timeline
  /// &#x60;date_bin&#x60; histogram over record versions (HEARTBEAT and KEYFRAME excluded unless &#x60;includeKeyframes&#x60;), optionally grouped and with alert counts. ≤ 1,000 buckets (400 TIMELINE_TOO_LARGE); 10 s timeout (503 QUERY_TIMEOUT). &#x60;bucket&#x3D;auto&#x60; picks the smallest of 1m, 5m, 10m, 15m, 30m, 1h, 2h, 3h, 6h, 12h, 1d, 2d, 7d, 14d, 30d giving at most 200 buckets; buckets start at &#x60;from&#x60; and every bucket in range is returned, empty ones included. Without &#x60;source&#x60; or &#x60;kind&#x60;, every source the caller may see is counted. &#x60;timeField&#x3D;ingested&#x60; is translated to seq bounds through seq_clock. &#x60;retainedFrom&#x60; is the latest of now − window over the involved sources. Not the same as /v1/products/timeline (051).  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [from] - Start
  /// * [to] - End
  /// * [source_] - Source ids (repeatable or comma-separated)
  /// * [kind] - Kinds (repeatable or comma-separated)
  /// * [bbox] - minLon,minLat,maxLon,maxLat (WGS84)
  /// * [place] - Place id(s) (036), resolved to their areas and intersected with the other filters; requires a token (places are never anonymous)
  /// * [bucket] - `auto` or a duration
  /// * [timeField] - Time axis
  /// * [measure] - Measure
  /// * [groupBy] - Grouping
  /// * [includeKeyframes] - Count HEARTBEAT and KEYFRAME versions
  /// * [includeAlerts] - Add alert counts per severity per bucket
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [Timeline] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<Timeline>> getTimeline({ 
    required DateTime from,
    required DateTime to,
    List<String>? source_,
    List<String>? kind,
    String? bbox,
    List<String>? place,
    String? bucket = 'auto',
    String? timeField,
    String? measure,
    String? groupBy,
    bool? includeKeyframes = false,
    bool? includeAlerts = false,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/timeline';
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
      if (source_ != null) r'source': source_,
      if (kind != null) r'kind': kind,
      if (bbox != null) r'bbox': bbox,
      if (place != null) r'place': place,
      r'from': from,
      r'to': to,
      if (bucket != null) r'bucket': bucket,
      if (timeField != null) r'timeField': timeField,
      if (measure != null) r'measure': measure,
      if (groupBy != null) r'groupBy': groupBy,
      if (includeKeyframes != null) r'includeKeyframes': includeKeyframes,
      if (includeAlerts != null) r'includeAlerts': includeAlerts,
    };

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    Timeline? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<Timeline, Timeline>(rawData, 'Timeline', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<Timeline>(
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
