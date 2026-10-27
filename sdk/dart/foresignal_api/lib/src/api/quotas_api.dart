//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

import 'dart:async';

// ignore: unused_import
import 'dart:convert';
import 'package:foresignal_api/src/deserialize.dart';
import 'package:dio/dio.dart';

import 'package:foresignal_api/src/model/problem.dart';
import 'package:foresignal_api/src/model/quota.dart';
import 'package:foresignal_api/src/model/quota_usage.dart';

class QuotasApi {

  final Dio _dio;

  const QuotasApi(this._dio);

  /// Get my quota and usage
  /// Returns the calling user&#39;s effective quota (their own limits over the instance default; null is unlimited) and current usage: &#x60;alerts&#x60;, &#x60;channels&#x60; and &#x60;savedViews&#x60; (the trigger rules, notification channels and saved views they own), &#x60;storageBytes&#x60; (their uploaded attachments plus the JSON of the workspaces they own) and &#x60;apiRequestsThisMinute&#x60; (requests in the current clock minute across their sessions and own API tokens) (D5/189, R2-026). A session or a user&#39;s own API token (R2-025); an API token with no user has no quota and gets 401.  **Scope:** &#x60;read&#x60;.
  ///
  /// Parameters:
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [QuotaUsage] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<QuotaUsage>> getMyQuota({ 
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/me/quota';
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

    QuotaUsage? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<QuotaUsage, QuotaUsage>(rawData, 'QuotaUsage', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<QuotaUsage>(
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

  /// Get a user&#39;s quota
  /// Returns a user&#39;s own quota (D5/189, R2-026): the limits set for that user, null where the instance default applies. The effective quota is what &#x60;getMyQuota&#x60; reports to the user. Requires &#x60;users.manage&#x60;.  **Scope:** &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [id] - User id
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [Quota] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<Quota>> getUserQuota({ 
    required String id,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/users/{id}/quota'.replaceAll('{' r'id' '}', id.toString());
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

    Quota? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<Quota, Quota>(rawData, 'Quota', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<Quota>(
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

  /// Set a user&#39;s quota
  /// Replaces a user&#39;s own quota whole (R2-026): every member is a non-negative integer or null, and null (or absent) uses the instance default; an unknown member is 400. A quota with every member null removes the user&#39;s own limits. &#x60;0&#x60; allows nothing: no API requests, or no further alert rules, channels, views or stored bytes. &#x60;apiRateLimitPerMinute&#x60; limits the user&#39;s sessions and own API tokens together (429 RATE_LIMITED with Retry-After, &#x60;class: user&#x60;); when a user is refused in every minute for 5 minutes an operational alert &#x60;system.user_rate_limited&#x60; is raised, and &#x60;system.user_rate_limit_cleared&#x60; once a minute passes without a refusal. The count and storage limits apply when the user creates (409 QUOTA_EXCEEDED). Requires &#x60;users.manage&#x60;.  **Scope:** &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [id] - User id
  /// * [quota] 
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [Quota] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<Quota>> updateUserQuota({ 
    required String id,
    required Quota quota,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/users/{id}/quota'.replaceAll('{' r'id' '}', id.toString());
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
      _bodyData = jsonEncode(quota);

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

    Quota? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<Quota, Quota>(rawData, 'Quota', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<Quota>(
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
