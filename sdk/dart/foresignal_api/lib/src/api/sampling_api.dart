//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

import 'dart:async';

// ignore: unused_import
import 'dart:convert';
import 'package:foresignal_api/src/deserialize.dart';
import 'package:dio/dio.dart';

import 'package:foresignal_api/src/model/problem.dart';
import 'package:foresignal_api/src/model/sample_batch_response.dart';
import 'package:foresignal_api/src/model/sample_request.dart';
import 'package:foresignal_api/src/model/sample_response.dart';
import 'package:foresignal_api/src/model/sample_track_request.dart';
import 'package:foresignal_api/src/model/sample_track_response.dart';
import 'package:foresignal_api/src/model/series.dart';
import 'package:foresignal_api/src/model/stats_request.dart';
import 'package:foresignal_api/src/model/stats_response.dart';

class SamplingApi {

  final Dio _dio;

  const SamplingApi(this._dio);

  /// Get a point time series
  /// Observed past plus forecast future at a point, each step with status, frame key and run (one run, never mixed). At most 500 steps; larger ranges → 400 VALIDATION_FAILED. Observation products: complete frames in &#x60;from&#x60;..&#x60;to&#x60; (default the last 6 h, never after now), &#x60;kind: observed&#x60;. Forecast products: the steps of one run (newest complete, or &#x60;run&#x60;) in &#x60;from&#x60;..&#x60;to&#x60;, &#x60;kind: forecast&#x60;. A disabled product&#39;s steps are &#x60;unavailable&#x60;.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [productId] - Product id (kebab-case; `timeline` and `loop` are reserved)
  /// * [lon] - Longitude (WGS84); with `lat`, or use `place`
  /// * [lat] - Latitude (WGS84); with `lon`, or use `place`
  /// * [place] - Place id (036). Using `place` requires a token even while `openReads` is on (places are not anonymous). Unknown → 404 UNKNOWN_PLACE
  /// * [from] - Series start
  /// * [to] - Series end
  /// * [run] - Forecast run: `latest`, `previous`, or the run's reference time (ISO-8601, e.g. `2026-10-10T12:00:00Z`). Required for a concrete forecast time (400 RUN_REQUIRED); only a pinned run is immutable
  /// * [units] - Unit system for values and labels
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [Series] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<Series>> getProductSeries({ 
    required String productId,
    num? lon,
    num? lat,
    String? place,
    DateTime? from,
    DateTime? to,
    String? run,
    String? units,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/products/{productId}/series'.replaceAll('{' r'productId' '}', productId.toString());
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
      if (lon != null) r'lon': lon,
      if (lat != null) r'lat': lat,
      if (place != null) r'place': place,
      if (from != null) r'from': from,
      if (to != null) r'to': to,
      if (run != null) r'run': run,
      if (units != null) r'units': units,
    };

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    Series? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<Series, Series>(rawData, 'Series', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<Series>(
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

  /// Sample products at a point
  /// Per product: value (or u/v with derived speed and direction), units, label, frame key, valid time, local time, age, run, method, status and attribution. Non-ok statuses carry &#x60;statusText&#x60;; no-coverage reads \&quot;outside product coverage; this is not evidence of clear weather\&quot;. Never triggers an upstream fetch. Time: &#x60;latest&#x60; is the newest frame (judged stale against now); &#x60;now&#x60;, an ISO time and a past offset choose the observation frame valid at or before the time; a future offset (and any time on a forecast product) uses the newest complete forecast run, taking the step valid at or before the time. Status order: unavailable (product disabled, source failing, no frame, frame expired), no-coverage, no-data, stale, ok; a non-ok value has &#x60;value: null&#x60;.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [products] - Products to sample (at most 20)
  /// * [lon] - Longitude (WGS84); with `lat`, or use `place`
  /// * [lat] - Latitude (WGS84); with `lon`, or use `place`
  /// * [place] - Place id (036). Using `place` requires a token even while `openReads` is on (places are not anonymous). Unknown → 404 UNKNOWN_PLACE
  /// * [time] - `latest`, `now`, ISO-8601, or a relative offset (`-PT1H` picks observation frames, `+PT6H` the newest complete forecast run)
  /// * [units] - Unit system for values and labels
  /// * [record] - Sample at this record's current live position: `{sourceId}/{externalId}` (instead of lon/lat or place). Arrives with prompt 106; until then a request with `record` is 400 VALIDATION_FAILED
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [SampleResponse] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<SampleResponse>> getSample({ 
    required List<String> products,
    num? lon,
    num? lat,
    String? place,
    String? time = 'latest',
    String? units,
    String? record,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/sample';
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
      if (lon != null) r'lon': lon,
      if (lat != null) r'lat': lat,
      if (place != null) r'place': place,
      r'products': products,
      if (time != null) r'time': time,
      if (units != null) r'units': units,
      if (record != null) r'record': record,
    };

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    SampleResponse? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<SampleResponse, SampleResponse>(rawData, 'SampleResponse', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<SampleResponse>(
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

  /// Compute area statistics
  /// min, max, mean, p95, coverage and the area above each threshold (spherical cell areas per grid type) over a geometry, place or bbox. Geometry ≤ 10,000 vertices and area ≤ 2,000,000 km² (400 VALIDATION_FAILED beyond). Thresholds are given in the requested unit system. A cell belongs to the area when its center lies inside; &#x60;coveragePct&#x60; is the share of those cells holding a value. A non-ok status (unavailable, no-coverage, stale) carries no statistics (&#x60;min&#x60;…&#x60;p95&#x60; null, &#x60;thresholds&#x60; empty).  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [productId] - Product id (kebab-case; `timeline` and `loop` are reserved)
  /// * [statsRequest] 
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [StatsResponse] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<StatsResponse>> postProductStats({ 
    required String productId,
    required StatsRequest statsRequest,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/products/{productId}/stats'.replaceAll('{' r'productId' '}', productId.toString());
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
      _bodyData = jsonEncode(statsRequest);

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

    StatsResponse? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<StatsResponse, StatsResponse>(rawData, 'StatsResponse', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<StatsResponse>(
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

  /// Sample products at many points
  /// Batch form of &#x60;GET /v1/sample&#x60; for up to 100 points (400 VALIDATION_FAILED beyond); results are in request order.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [sampleRequest] 
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [SampleBatchResponse] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<SampleBatchResponse>> postSample({ 
    required SampleRequest sampleRequest,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/sample';
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
      _bodyData = jsonEncode(sampleRequest);

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

    SampleBatchResponse? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<SampleBatchResponse, SampleBatchResponse>(rawData, 'SampleBatchResponse', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<SampleBatchResponse>(
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

  /// Sample products along a track
  /// Samples each point at its time using the nearest frame within &#x60;maxGap&#x60;, or forecast frames for future times with &#x60;run&#x60;. With &#x60;record&#x60;, points come from the 037 track simplification. Private sources and observer gating apply.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [sampleTrackRequest] 
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [SampleTrackResponse] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<SampleTrackResponse>> sampleTrack({ 
    required SampleTrackRequest sampleTrackRequest,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/sample/track';
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
      _bodyData = jsonEncode(sampleTrackRequest);

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

    SampleTrackResponse? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<SampleTrackResponse, SampleTrackResponse>(rawData, 'SampleTrackResponse', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<SampleTrackResponse>(
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
