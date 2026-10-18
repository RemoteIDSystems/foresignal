//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

import 'dart:async';

// ignore: unused_import
import 'dart:convert';
import 'package:foresignal_api/src/deserialize.dart';
import 'package:dio/dio.dart';

import 'package:foresignal_api/src/model/problem.dart';

class CalendarsApi {

  final Dio _dio;

  const CalendarsApi(this._dio);

  /// Subscribe to an iCalendar feed
  /// RFC 5545 feed: &#x60;launches&#x60; (064), &#x60;tfr&#x60; (087), &#x60;nws&#x60; (047), &#x60;briefings&#x60; (next 30 days of occurrences) or &#x60;custom&#x60; (any kind whose descriptor declares startField/endField; &#x60;source&#x60; and &#x60;kind&#x60; required). UID &#x60;{sourceId}:{externalId}@{instanceId}&#x60;, SEQUENCE &#x3D; record version; tombstoned or expired records stay 7 days as CANCELLED; at most 2,000 events. ETag and conditional GET; feed keys via &#x60;/v1/k/{key}/…&#x60;. Errors: &#x60;400 ICAL_FIELDS_MISSING&#x60;.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [feed] - Feed name
  /// * [bbox] - minLon,minLat,maxLon,maxLat (WGS84)
  /// * [placeId] - Only events within this place
  /// * [source_] - Filter by source id (repeatable or comma-separated)
  /// * [kind] - Filter by kind (repeatable or comma-separated)
  /// * [ifNoneMatch] - ETag
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [String] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<String>> getCalendar({ 
    required String feed,
    String? bbox,
    String? placeId,
    List<String>? source_,
    List<String>? kind,
    String? ifNoneMatch,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/calendars/{feed}.ics'.replaceAll('{' r'feed' '}', feed.toString());
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
      if (bbox != null) r'bbox': bbox,
      if (placeId != null) r'placeId': placeId,
      if (source_ != null) r'source': source_,
      if (kind != null) r'kind': kind,
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

}
