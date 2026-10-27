//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

import 'dart:async';

// ignore: unused_import
import 'dart:convert';
import 'package:foresignal_api/src/deserialize.dart';
import 'package:dio/dio.dart';

import 'package:foresignal_api/src/model/problem.dart';
import 'package:foresignal_api/src/model/resource_status.dart';
import 'package:foresignal_api/src/model/resource_thresholds.dart';

class ResourcesApi {

  final Dio _dio;

  const ResourcesApi(this._dio);

  /// Get host resource status
  /// Live host disk free space, memory and database size with an overall ok/warn/critical status (177), rated against &#x60;GET /v1/settings/thresholds&#x60;. Readings are cached for up to 30 s. A &#x60;warn&#x60; or &#x60;critical&#x60; gauge also appears in &#x60;/v1/system.warnings&#x60; as &#x60;DISK_LOW&#x60;, &#x60;DISK_CRITICAL&#x60;, &#x60;MEMORY_LOW&#x60; or &#x60;MEMORY_CRITICAL&#x60;. Requires &#x60;storage.manage&#x60;.  **Scope:** &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [ResourceStatus] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<ResourceStatus>> getResourceStatus({ 
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/system/resources';
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

    ResourceStatus? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<ResourceStatus, ResourceStatus>(rawData, 'ResourceStatus', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<ResourceStatus>(
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

  /// Get resource thresholds
  /// Settings key &#x60;thresholds&#x60;: the warn/critical disk and memory levels, the alert channels, and the source-storm and collector-backlog bounds; the defaults when never set. Requires &#x60;storage.manage&#x60;.  **Scope:** &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [ResourceThresholds] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<ResourceThresholds>> getResourceThresholds({ 
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/settings/thresholds';
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

    ResourceThresholds? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<ResourceThresholds, ResourceThresholds>(rawData, 'ResourceThresholds', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<ResourceThresholds>(
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

  /// Update resource thresholds
  /// Replaces the thresholds; a breach raises /v1/system warnings and fires alerts through the channel system. Each alert is edge-triggered: entering &#x60;warn&#x60; or &#x60;critical&#x60; publishes one &#x60;system&#x60; notification (&#x60;system.disk_low&#x60;, &#x60;system.disk_critical&#x60;, &#x60;system.memory_low&#x60;, &#x60;system.memory_critical&#x60;, &#x60;system.source_storm&#x60;, &#x60;system.collector_backlog&#x60;, &#x60;system.user_rate_limited&#x60;) and returning to normal publishes one recovery (&#x60;system.disk_recovered&#x60;, &#x60;system.memory_recovered&#x60;, &#x60;system.source_storm_cleared&#x60;, &#x60;system.collector_backlog_cleared&#x60;, &#x60;system.user_rate_limit_cleared&#x60;). Requires &#x60;storage.manage&#x60;.  **Scope:** &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [resourceThresholds] 
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [ResourceThresholds] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<ResourceThresholds>> updateResourceThresholds({ 
    required ResourceThresholds resourceThresholds,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/settings/thresholds';
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
      _bodyData = jsonEncode(resourceThresholds);

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

    ResourceThresholds? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<ResourceThresholds, ResourceThresholds>(rawData, 'ResourceThresholds', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<ResourceThresholds>(
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
