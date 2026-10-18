//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

import 'dart:async';

// ignore: unused_import
import 'dart:convert';
import 'package:foresignal_api/src/deserialize.dart';
import 'package:dio/dio.dart';

import 'package:foresignal_api/src/model/problem.dart';
import 'package:foresignal_api/src/model/series_page.dart';
import 'package:foresignal_api/src/model/series_point_page.dart';

class SeriesApi {

  final Dio _dio;

  const SeriesApi(this._dio);

  /// Get points of one series
  /// &#x60;series-point&#x60; records of one series ordered by &#x60;periodStart&#x60; (served from the partial expression index on &#x60;(source_id, payload-&gt;&gt;&#39;series&#39;, event_time)&#x60;), as JSON or CSV. Revisions are record versions; the current value per period is returned. 404 &#x60;SERIES_NOT_FOUND&#x60;.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [sourceId] - Source id
  /// * [seriesId] - Series id as in the `series` payload field, e.g. `eia930:ERCO:D`
  /// * [from] - periodStart >= from
  /// * [to] - periodStart < to
  /// * [limit] - Maximum points
  /// * [format] - Response format (alternatively Accept: text/csv)
  /// * [cursor] - Opaque pagination cursor from a previous response's nextCursor
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [SeriesPointPage] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<SeriesPointPage>> getSeriesPoints({ 
    required String sourceId,
    required String seriesId,
    DateTime? from,
    DateTime? to,
    int? limit = 1000,
    String? format,
    String? cursor,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/series/{sourceId}/{seriesId}'.replaceAll('{' r'sourceId' '}', sourceId.toString()).replaceAll('{' r'seriesId' '}', seriesId.toString());
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
      if (from != null) r'from': from,
      if (to != null) r'to': to,
      if (limit != null) r'limit': limit,
      if (format != null) r'format': format,
      if (cursor != null) r'cursor': cursor,
    };

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    SeriesPointPage? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<SeriesPointPage, SeriesPointPage>(rawData, 'SeriesPointPage', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<SeriesPointPage>(
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

  /// List time series
  /// Series catalog built from &#x60;series-latest&#x60; records (093 energy sources and later series sources): one entry per series with its newest non-forecast point. Each entry carries the series metadata of 117 (family, transform, releaseLagDays, egress, regionCode, topicId, attribution). LOCAL_ONLY sources&#39; series are served only to callers allowed local egress (EgressPolicy on API_READ).  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [source_] - Filter by source id (repeatable or comma-separated)
  /// * [q] - Case-insensitive match on series id, seriesName and area
  /// * [cursor] - Opaque pagination cursor from a previous response's nextCursor
  /// * [limit] - Page size
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [SeriesPage] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<SeriesPage>> listSeries({ 
    List<String>? source_,
    String? q,
    String? cursor,
    int? limit = 100,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/series';
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
      if (q != null) r'q': q,
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

    SeriesPage? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<SeriesPage, SeriesPage>(rawData, 'SeriesPage', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<SeriesPage>(
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
