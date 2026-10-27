//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

import 'dart:async';

// ignore: unused_import
import 'dart:convert';
import 'package:foresignal_api/src/deserialize.dart';
import 'package:dio/dio.dart';

import 'package:foresignal_api/src/model/problem.dart';
import 'package:foresignal_api/src/model/user_preferences.dart';

class PreferencesApi {

  final Dio _dio;

  const PreferencesApi(this._dio);

  /// Get my preferences
  /// Returns the calling user&#39;s preferences with the defaults filled in: an unset &#x60;timezone&#x60; reads as the server&#39;s (getServerSettings), an unset &#x60;theme&#x60; as &#x60;system&#x60;, an unset &#x60;locale&#x60; as the instance notification language (&#x60;notify.defaultLanguage&#x60;). &#x60;notifications&#x60; summarizes the caller&#39;s notification preferences (listMyNotificationPreferences): &#x60;emailEnabled&#x60; is false only when the caller has email channels and every one is off, &#x60;pushEnabled&#x60; likewise for push channels (ntfy, gotify, pushover, webpush), and &#x60;quietHours&#x60; is the window every preference shares, or null when they differ or have none. Needs a signed-in session (an API token is 401). Requires &#x60;self.preferences&#x60;.  **Scope:** &#x60;read&#x60;.
  ///
  /// Parameters:
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [UserPreferences] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<UserPreferences>> getMyPreferences({ 
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/me/preferences';
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

    UserPreferences? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<UserPreferences, UserPreferences>(rawData, 'UserPreferences', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<UserPreferences>(
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

  /// Update my preferences
  /// Replaces the calling user&#39;s preferences. &#x60;timezone&#x60; (an IANA zone the server knows), &#x60;locale&#x60; (a BCP 47 tag naming an ISO 639 language) and &#x60;theme&#x60; (&#x60;system&#x60;, &#x60;light&#x60; or &#x60;dark&#x60;) are replaced; absent or null resets each to its default, and an invalid value is 400 VALIDATION_FAILED. &#x60;ui&#x60; is replaced whole (at most 64 KiB of JSON). The timezone is the user&#39;s own (&#x60;Principal.timezone&#x60;): the user&#39;s personal notification channels render their quiet hours in it, and the Ask assistant writes the user&#39;s local time in it; stored times stay UTC. &#x60;notifications&#x60; writes through to the caller&#39;s notification preferences rather than a second copy: &#x60;emailEnabled&#x60; and &#x60;pushEnabled&#x60; turn every email or push channel on or off, &#x60;quietHours&#x60; (&#x60;HH:MM-HH:MM&#x60;, or null to clear) sets the window on every preference; a member left out, or the whole object, leaves those preferences as they are. Needs a signed-in session (an API token is 401). Requires &#x60;self.preferences&#x60;.  **Scope:** &#x60;read&#x60;.
  ///
  /// Parameters:
  /// * [userPreferences] 
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [UserPreferences] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<UserPreferences>> updateMyPreferences({ 
    required UserPreferences userPreferences,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/me/preferences';
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
      _bodyData = jsonEncode(userPreferences);

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

    UserPreferences? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<UserPreferences, UserPreferences>(rawData, 'UserPreferences', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<UserPreferences>(
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
