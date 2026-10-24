//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

import 'dart:async';

// ignore: unused_import
import 'dart:convert';
import 'package:foresignal_api/src/deserialize.dart';
import 'package:dio/dio.dart';

import 'package:foresignal_api/src/model/glance.dart';
import 'package:foresignal_api/src/model/problem.dart';
import 'package:foresignal_api/src/model/stats_grid.dart';
import 'package:foresignal_api/src/model/stats_rollup_rebuild.dart';
import 'package:foresignal_api/src/model/stats_rollup_rebuild_request.dart';
import 'package:foresignal_api/src/model/stats_series.dart';
import 'package:foresignal_api/src/model/stats_top.dart';

class StatsApi {

  final Dio _dio;

  const StatsApi(this._dio);

  /// Compact status for widgets
  /// ≤ 2,048 bytes of JSON, or ≤ 8 lines × 40 ASCII chars with &#x60;format&#x3D;text&#x60; / &#x60;Accept: text/plain&#x60;. Anonymous callers get no counts by default. &#x60;place&#x60; scopes alerts to a place (token required). 15 s server cache; strong ETag; &#x60;Cache-Control: private, max-age&#x3D;15&#x60;.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [place] - Scope alerts to a place (036)
  /// * [format] - Alternative to Accept
  /// * [ifNoneMatch] - ETag from a previous response; 304 when unchanged
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [Glance] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<Glance>> getGlance({ 
    String? place,
    String? format,
    String? ifNoneMatch,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/glance';
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
      if (place != null) r'place': place,
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

    Glance? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<Glance, Glance>(rawData, 'Glance', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<Glance>(
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

  /// Prometheus metrics of projected data
  /// Values of the configured metric projections with &#x60;foresignal_data_&lt;name&gt;_observed_timestamp_seconds&#x60; companions, from a dedicated registry (never on /actuator/prometheus). Text format 0.0.4, or OpenMetrics when &#x60;Accept&#x60; asks for it. Global cap 10,000 series.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [String] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<String>> getMetrics({ 
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/metrics';
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

  /// Spatial activity grid
  /// Rollup mode (hourly rows for ranges up to 7 days, daily rows beyond; closed buckets only): H3 cells aggregated to &#x60;res&#x60; (≤ cellRes) with boundaries split at the antimeridian. Raw mode (&#x60;mode&#x3D;raw&amp;sizeKm&#x3D;&#x60;, range ≤ 7d): hexagons in EPSG:3857. ≤ 10,000 cells (400 STATS_TOO_LARGE).  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [bbox] - minLon,minLat,maxLon,maxLat (WGS84)
  /// * [from] - Start
  /// * [to] - End
  /// * [source_] - Source ids (repeatable or comma-separated)
  /// * [kind] - Kinds (repeatable or comma-separated)
  /// * [measure] - Measure
  /// * [res] - H3 resolution (≤ cellRes, default cellRes)
  /// * [mode] - Backend
  /// * [sizeKm] - Raw-mode hexagon size in ground kilometers at the bbox's centre latitude (required with mode=raw)
  /// * [country] - ISO 3166-1 alpha-2 codes (040); unknown → 400 UNKNOWN_REGION
  /// * [admin1] - ISO 3166-2 codes, e.g. US-TX (040)
  /// * [county] - US county FIPS5 codes (040)
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [StatsGrid] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<StatsGrid>> getStatsGrid({ 
    required String bbox,
    required DateTime from,
    required DateTime to,
    List<String>? source_,
    List<String>? kind,
    String? measure,
    int? res,
    String? mode,
    num? sizeKm,
    List<String>? country,
    List<String>? admin1,
    List<String>? county,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/stats/grid';
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
      r'bbox': bbox,
      r'from': from,
      r'to': to,
      if (measure != null) r'measure': measure,
      if (res != null) r'res': res,
      if (mode != null) r'mode': mode,
      if (sizeKm != null) r'sizeKm': sizeKm,
      if (country != null) r'country': country,
      if (admin1 != null) r'admin1': admin1,
      if (county != null) r'county': county,
    };

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    StatsGrid? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<StatsGrid, StatsGrid>(rawData, 'StatsGrid', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<StatsGrid>(
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

  /// Activity time series
  /// &#x60;timeField&#x3D;ingested&#x60; reads the rollup (plus raw rows for the open hour); other time fields read raw history and need &#x60;from ≥ retainedFrom&#x60;; &#x60;alerts&#x60; reads alert fire times. ≤ 2,000 buckets. 10 s timeout (503 QUERY_TIMEOUT). &#x60;measure&#x3D;entities&#x60; with &#x60;bucket&#x3D;1d&#x60; over a day the daily rollup does not hold yet (the current UTC day) is 422 STATS_MEASURE_UNSUPPORTED: distinct entities cannot be summed from hours, so use &#x60;bucket&#x3D;1h&#x60; or end the range at that day&#39;s start.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [from] - Start
  /// * [to] - End
  /// * [source_] - Source ids (repeatable or comma-separated)
  /// * [kind] - Kinds (repeatable or comma-separated)
  /// * [bbox] - minLon,minLat,maxLon,maxLat (WGS84)
  /// * [place] - Place id(s) (036), resolved to their areas and intersected with the other filters; requires a token (places are never anonymous)
  /// * [bucket] - Bucket
  /// * [measure] - Measure
  /// * [groupBy] - Group series by source, kind or region
  /// * [timeField] - Time axis
  /// * [country] - ISO 3166-1 alpha-2 codes (040); unknown → 400 UNKNOWN_REGION
  /// * [admin1] - ISO 3166-2 codes, e.g. US-TX (040)
  /// * [county] - US county FIPS5 codes (040)
  /// * [region] - Filter by 040 region code (repeatable or comma-separated); served from region rollups (resolutions 2 and 3)
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [StatsSeries] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<StatsSeries>> getStatsTimeseries({ 
    required DateTime from,
    required DateTime to,
    List<String>? source_,
    List<String>? kind,
    String? bbox,
    List<String>? place,
    String? bucket,
    String? measure,
    String? groupBy,
    String? timeField,
    List<String>? country,
    List<String>? admin1,
    List<String>? county,
    List<String>? region,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/stats/timeseries';
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
      if (measure != null) r'measure': measure,
      if (groupBy != null) r'groupBy': groupBy,
      if (timeField != null) r'timeField': timeField,
      if (country != null) r'country': country,
      if (admin1 != null) r'admin1': admin1,
      if (county != null) r'county': county,
      if (region != null) r'region': region,
    };

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    StatsSeries? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<StatsSeries, StatsSeries>(rawData, 'StatsSeries', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<StatsSeries>(
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

  /// Top sources, kinds, cells or entities
  /// &#x60;by&#x3D;entity&#x60; reads raw history inside retention and returns titles.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [from] - Start
  /// * [to] - End
  /// * [by] - Rank by source, kind, H3 cell, entity or region
  /// * [measure] - Measure
  /// * [limit] - Items
  /// * [source_] - Source ids (repeatable or comma-separated)
  /// * [kind] - Kinds (repeatable or comma-separated)
  /// * [country] - ISO 3166-1 alpha-2 codes (040); unknown → 400 UNKNOWN_REGION
  /// * [admin1] - ISO 3166-2 codes, e.g. US-TX (040)
  /// * [county] - US county FIPS5 codes (040)
  /// * [region] - Filter by 040 region code (repeatable or comma-separated)
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [StatsTop] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<StatsTop>> getStatsTop({ 
    required DateTime from,
    required DateTime to,
    String? by,
    String? measure,
    int? limit = 10,
    List<String>? source_,
    List<String>? kind,
    List<String>? country,
    List<String>? admin1,
    List<String>? county,
    List<String>? region,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/stats/top';
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
      if (by != null) r'by': by,
      if (measure != null) r'measure': measure,
      r'from': from,
      r'to': to,
      if (limit != null) r'limit': limit,
      if (source_ != null) r'source': source_,
      if (kind != null) r'kind': kind,
      if (country != null) r'country': country,
      if (admin1 != null) r'admin1': admin1,
      if (county != null) r'county': county,
      if (region != null) r'region': region,
    };

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    StatsTop? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<StatsTop, StatsTop>(rawData, 'StatsTop', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<StatsTop>(
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

  /// Rebuild activity rollups
  /// Recomputes closed hourly and daily buckets in the range that still have raw data.  **Scope:** &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [statsRollupRebuildRequest] 
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [StatsRollupRebuild] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<StatsRollupRebuild>> rebuildStatsRollup({ 
    required StatsRollupRebuildRequest statsRollupRebuildRequest,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/stats/rollup/rebuild';
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
      _bodyData = jsonEncode(statsRollupRebuildRequest);

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

    StatsRollupRebuild? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<StatsRollupRebuild, StatsRollupRebuild>(rawData, 'StatsRollupRebuild', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<StatsRollupRebuild>(
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
