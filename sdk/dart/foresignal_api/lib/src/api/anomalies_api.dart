//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

import 'dart:async';

// ignore: unused_import
import 'dart:convert';
import 'package:foresignal_api/src/deserialize.dart';
import 'package:dio/dio.dart';

import 'package:foresignal_api/src/model/anomaly_observation_page.dart';
import 'package:foresignal_api/src/model/problem.dart';

class AnomaliesApi {

  final Dio _dio;

  const AnomaliesApi(this._dio);

  /// List anomaly results
  /// Evaluated anomaly buckets, newest first (keyset on id). p-values are descriptive under the stated count model.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [ruleId] - Filter by rule
  /// * [groupKey] - Filter by group
  /// * [classification] - Filter by classification
  /// * [since] - bucketStart ≥ since
  /// * [until] - bucketStart < until
  /// * [cursor] - Opaque pagination cursor from a previous response's nextCursor
  /// * [limit] - Page size
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [AnomalyObservationPage] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<AnomalyObservationPage>> listAnomalies({ 
    String? ruleId,
    String? groupKey,
    List<String>? classification,
    DateTime? since,
    DateTime? until,
    String? cursor,
    int? limit = 100,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/anomalies';
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
      if (ruleId != null) r'ruleId': ruleId,
      if (groupKey != null) r'groupKey': groupKey,
      if (classification != null) r'classification': classification,
      if (since != null) r'since': since,
      if (until != null) r'until': until,
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

    AnomalyObservationPage? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<AnomalyObservationPage, AnomalyObservationPage>(rawData, 'AnomalyObservationPage', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<AnomalyObservationPage>(
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
