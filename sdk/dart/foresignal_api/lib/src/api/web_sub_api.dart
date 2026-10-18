//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

import 'dart:async';

// ignore: unused_import
import 'dart:convert';
import 'package:foresignal_api/src/deserialize.dart';
import 'package:dio/dio.dart';

import 'package:foresignal_api/src/model/problem.dart';

class WebSubApi {

  final Dio _dio;

  const WebSubApi(this._dio);

  /// WebSub content distribution
  /// Hub content delivery for a subscribed feed. &#x60;X-Hub-Signature&#x60; (&#x60;sha256&#x3D;&#x60; preferred, &#x60;sha1&#x3D;&#x60; accepted) is verified over the raw body with a constant-time compare; the body (≤ 2 MB) is parsed with the feed-kit &#x60;FeedParser&#x60; and handed to the source&#39;s sink. Per-source cap 60/min.  **Scope:** &#x60;open&#x60; (no token; the unguessable path token identifies the subscription).
  ///
  /// Parameters:
  /// * [sourceId] - Feed-kit source id with `websub: true`
  /// * [token] - Unguessable per-subscription callback token
  /// * [xHubSignature] - HMAC of the raw body with the subscription secret: `sha256=<hex>` or `sha1=<hex>`
  /// * [body] - The updated feed document as published by the hub
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future]
  /// Throws [DioException] if API call or serialization fails
  Future<Response<void>> receiveWebSubNotification({ 
    required String sourceId,
    required String token,
    required String xHubSignature,
    required String body,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/websub/{sourceId}/{token}'.replaceAll('{' r'sourceId' '}', sourceId.toString()).replaceAll('{' r'token' '}', token.toString());
    final _options = Options(
      method: r'POST',
      headers: <String, dynamic>{
        r'X-Hub-Signature': xHubSignature,
        ...?headers,
      },
      extra: <String, dynamic>{
        'secure': <Map<String, String>>[],
        ...?extra,
      },
      contentType: 'application/json',
      validateStatus: validateStatus,
    );

    dynamic _bodyData;

    try {
      _bodyData = jsonEncode(body);

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

    return _response;
  }

  /// WebSub intent verification
  /// Hub intent verification. Echoes &#x60;hub.challenge&#x60; as &#x60;text/plain&#x60; only when &#x60;hub.mode&#x3D;subscribe&#x60;, &#x60;hub.topic&#x60; and the token match a pending or active subscription; the lease (&#x60;hub.lease_seconds&#x60;, default 86400) starts then. &#x60;hub.mode&#x3D;denied&#x60; with a matching topic and token marks the subscription lost and answers 200 with an empty body. Everything else, &#x60;unsubscribe&#x60; included (Foresignal never unsubscribes), is 404. Returns 404 for everything while &#x60;foresignal.kits.websub.enabled&#x3D;false&#x60; (default).  **Scope:** &#x60;open&#x60; (no token; the unguessable path token identifies the subscription).
  ///
  /// Parameters:
  /// * [sourceId] - Feed-kit source id with `websub: true`
  /// * [token] - Unguessable per-subscription callback token
  /// * [hubPeriodMode] - Verification mode
  /// * [hubPeriodTopic] - Topic URL; must equal the subscription's `self` link
  /// * [hubPeriodChallenge] - Random string to echo (absent for `denied`)
  /// * [hubPeriodLeaseSeconds] - Granted lease in seconds
  /// * [hubPeriodReason] - Reason for a `denied` notification
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [String] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<String>> verifyWebSubSubscription({ 
    required String sourceId,
    required String token,
    required String hubPeriodMode,
    required String hubPeriodTopic,
    String? hubPeriodChallenge,
    int? hubPeriodLeaseSeconds,
    String? hubPeriodReason,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/websub/{sourceId}/{token}'.replaceAll('{' r'sourceId' '}', sourceId.toString()).replaceAll('{' r'token' '}', token.toString());
    final _options = Options(
      method: r'GET',
      headers: <String, dynamic>{
        ...?headers,
      },
      extra: <String, dynamic>{
        'secure': <Map<String, String>>[],
        ...?extra,
      },
      validateStatus: validateStatus,
    );

    final _queryParameters = <String, dynamic>{
      r'hub.mode': hubPeriodMode,
      r'hub.topic': hubPeriodTopic,
      if (hubPeriodChallenge != null) r'hub.challenge': hubPeriodChallenge,
      if (hubPeriodLeaseSeconds != null) r'hub.lease_seconds': hubPeriodLeaseSeconds,
      if (hubPeriodReason != null) r'hub.reason': hubPeriodReason,
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
