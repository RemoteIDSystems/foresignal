//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

import 'dart:async';

// ignore: unused_import
import 'dart:convert';
import 'package:foresignal_api/src/deserialize.dart';
import 'package:dio/dio.dart';

import 'package:foresignal_api/src/model/aggregator.dart';
import 'package:foresignal_api/src/model/aggregator_config.dart';
import 'package:foresignal_api/src/model/aggregator_list.dart';
import 'package:foresignal_api/src/model/aggregator_test_result.dart';
import 'package:foresignal_api/src/model/problem.dart';

class AggregatorsApi {

  final Dio _dio;

  const AggregatorsApi(this._dio);

  /// Get a domain aggregator
  /// Returns the aggregator for one domain (adsb, ais, datalink, satellite, ground-sensors). Requires &#x60;network.manage&#x60;.  **Scope:** &#x60;admin&#x60;.  **R2-019:** 404 UNKNOWN_AGGREGATOR for any other domain.
  ///
  /// Parameters:
  /// * [domain] - Aggregator domain
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [Aggregator] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<Aggregator>> getAggregator({ 
    required String domain,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/aggregators/{domain}'.replaceAll('{' r'domain' '}', domain.toString());
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

    Aggregator? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<Aggregator, Aggregator>(rawData, 'Aggregator', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<Aggregator>(
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

  /// List first-party aggregators
  /// Lists the first-party aggregators across RF/sensor domains with status and the public sources each supersedes (C3). Requires &#x60;network.manage&#x60;.  **Scope:** &#x60;admin&#x60;.  **R2-019:** every domain is listed. adsb is implemented, ais since R2-020, datalink since R2-021, satellite since R2-022 and ground-sensors since R2-023, so every domain reads &#x60;implemented: true&#x60;; a domain whose backend a server does not have reads &#x60;implemented: false&#x60; and &#x60;status: unconfigured&#x60;.
  ///
  /// Parameters:
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [AggregatorList] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<AggregatorList>> listAggregators({ 
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/aggregators';
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

    AggregatorList? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<AggregatorList, AggregatorList>(rawData, 'AggregatorList', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<AggregatorList>(
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

  /// Test a domain aggregator
  /// Probes the aggregator&#39;s local endpoint and reports reachability and a sample object count; nothing is persisted. Requires &#x60;network.manage&#x60;.  **Scope:** &#x60;admin&#x60;.  **R2-019:** reads the stored endpoint once through the aggregator&#39;s source (its budget and pinning apply). Always 200; &#x60;ok&#x60; false with a &#x60;message&#x60; when nothing is configured, the endpoint cannot be reached, or the answer is not readsb aircraft.json. &#x60;sampleCount&#x60; is the number of aircraft in it.  **R2-020:** for ais, listens to the stored NMEA endpoint for 10 s through &#x60;local-ais-aggregator&#x60; and reports the vessel records decoded (at most 100); a live UDP listener, which cannot be bound twice, is reported from its statistics (distinct MMSIs in the last 10 minutes).  **R2-021:** for datalink, listens to every selected read (ACARS/VDLM2/HFDL and APRS) at once for 10 s and sums the messages decoded; &#x60;reachable&#x60; is false only when no selected read could be connected.  **R2-022:** for satellite, runs the satdump-aggregator station for 10 s (its /api read and inbox scan) and counts the records it produced: station state, passes and products.  **R2-023:** for ground-sensors, probes every enabled sensor&#39;s polling or streaming read at once for 10 s and sums the records; a push source (seismic, same, the MeshCore bridge) or a live local-sonde UDP listener is reachable once it has received data. A failing sensor is named in &#x60;message&#x60;; &#x60;reachable&#x60; is false only when every sensor fails.
  ///
  /// Parameters:
  /// * [domain] - Aggregator domain
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [AggregatorTestResult] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<AggregatorTestResult>> testAggregator({ 
    required String domain,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/aggregators/{domain}/test'.replaceAll('{' r'domain' '}', domain.toString());
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

    AggregatorTestResult? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<AggregatorTestResult, AggregatorTestResult>(rawData, 'AggregatorTestResult', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<AggregatorTestResult>(
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

  /// Configure a domain aggregator
  /// Enables and configures a domain&#39;s first-party aggregator and, by default, defaults the superseded public/mirror sources off (they stay in the catalog as opt-in fallbacks). Requires &#x60;network.manage&#x60;.  **Scope:** &#x60;admin&#x60;.  **R2-019:** the configuration is replaced whole. adsb writes &#x60;endpoint&#x60; (and &#x60;config.band&#x60;) to the &#x60;local-adsb-aggregator&#x60; source and turns it on while the aggregator is live (enabled with an endpoint), off otherwise. While live with &#x60;disableSupersededSources&#x60;, every registered &#x60;adsblol-*&#x60;, &#x60;adsbfi-*&#x60; and &#x60;airplaneslive-*&#x60; source and &#x60;gpsjam-daily&#x60; that is on is turned off, and seeding and &#x60;enable-core&#x60;/&#x60;enable-free&#x60; leave them off; when the aggregator stops being live, or &#x60;disableSupersededSources&#x60; is false, the sources it turned off are turned back on. 400 for a bad member, an endpoint that is not an http(s) aircraft.json URL (adsb) or a tcp:// or udp:// NMEA endpoint (ais, R2-020; udp:// needs config.allowSenders) or a tcp://host decoder host with at least one decoder selected (datalink, R2-021) or an http(s) SatDump URL without config.location (satellite, R2-022) or any endpoint, an unknown sensor or member, or no sensor enabled (ground-sensors, R2-023, where a source&#39;s own parameter error is reported at the sensor&#39;s member) or does not resolve to a local address, or &#x60;enabled: true&#x60; on a domain this server has no backend for. A live aggregator whose source starts failing raises &#x60;system.aggregator_down&#x60; (and &#x60;system.aggregator_recovered&#x60; when it is healthy again).  **R2-023:** ground-sensors is live while enabled with a sensor enabled. Each enabled sensor&#39;s members are written into its local read, which is turned on while live; a read a previous configuration drove and this one does not is turned off, and one never driven is left alone. Only the enabled sensors&#39; mirrors are superseded, and a configuration that supersedes less turns the rest back on.
  ///
  /// Parameters:
  /// * [domain] - Aggregator domain
  /// * [aggregatorConfig] 
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [Aggregator] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<Aggregator>> updateAggregator({ 
    required String domain,
    required AggregatorConfig aggregatorConfig,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/aggregators/{domain}'.replaceAll('{' r'domain' '}', domain.toString());
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
      _bodyData = jsonEncode(aggregatorConfig);

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

    Aggregator? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<Aggregator, Aggregator>(rawData, 'Aggregator', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<Aggregator>(
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
