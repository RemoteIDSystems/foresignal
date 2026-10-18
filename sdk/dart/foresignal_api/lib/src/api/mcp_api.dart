//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

import 'dart:async';

// ignore: unused_import
import 'dart:convert';
import 'package:foresignal_api/src/deserialize.dart';
import 'package:dio/dio.dart';

import 'package:foresignal_api/src/model/problem.dart';
import 'package:foresignal_api/src/model/protected_resource_metadata.dart';

class MCPApi {

  final Dio _dio;

  const MCPApi(this._dio);

  /// MCP protected resource metadata
  /// RFC 9728 metadata at the path-suffixed location for the &#x60;/mcp&#x60; resource.  **Scope:** &#x60;open&#x60; (no token).
  ///
  /// Parameters:
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [ProtectedResourceMetadata] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<ProtectedResourceMetadata>> getMcpProtectedResourceMetadata({ 
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/.well-known/oauth-protected-resource/mcp';
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

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    ProtectedResourceMetadata? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<ProtectedResourceMetadata, ProtectedResourceMetadata>(rawData, 'ProtectedResourceMetadata', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<ProtectedResourceMetadata>(
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

  /// Protected resource metadata
  /// RFC 9728 metadata for the MCP resource (same document as the &#x60;/mcp&#x60; path variant). Clients find it through &#x60;WWW-Authenticate: Bearer resource_metadata&#x3D;…&#x60; on 401 from &#x60;/mcp&#x60;.  While &#x60;mcp.auth.mode&#x60; is &#x60;oauth&#x60; or &#x60;both&#x60;, &#x60;/mcp&#x60; always needs a credential (open reads do not apply there): a request without one, or with an invalid token, gets &#x60;401&#x60; with &#x60;WWW-Authenticate: Bearer resource_metadata&#x3D;\&quot;{base}/.well-known/oauth-protected-resource/mcp\&quot;, scope&#x3D;\&quot;foresignal:read\&quot;&#x60; (plus &#x60;error&#x3D;\&quot;invalid_token\&quot;&#x60; when a token was presented); a valid token whose scopes map to nothing, or an identity-provider token calling a tool above its mapped scope, gets &#x60;403&#x60; with &#x60;WWW-Authenticate: Bearer error&#x3D;\&quot;insufficient_scope\&quot;, scope&#x3D;\&quot;…\&quot;, resource_metadata&#x3D;\&quot;…\&quot;&#x60;. The &#x60;scope&#x60; values are the identity-provider scopes of &#x60;mcp.auth.scopeMap&#x60; that reach the needed Foresignal scope. REST paths never accept identity-provider tokens.  **Scope:** &#x60;open&#x60; (no token).
  ///
  /// Parameters:
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [ProtectedResourceMetadata] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<ProtectedResourceMetadata>> getProtectedResourceMetadata({ 
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/.well-known/oauth-protected-resource';
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

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    ProtectedResourceMetadata? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<ProtectedResourceMetadata, ProtectedResourceMetadata>(rawData, 'ProtectedResourceMetadata', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<ProtectedResourceMetadata>(
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
