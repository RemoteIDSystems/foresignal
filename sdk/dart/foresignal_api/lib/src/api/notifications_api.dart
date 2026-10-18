//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

import 'dart:async';

// ignore: unused_import
import 'dart:convert';
import 'package:foresignal_api/src/deserialize.dart';
import 'package:dio/dio.dart';

import 'package:foresignal_api/src/model/action_result.dart';
import 'package:foresignal_api/src/model/client_presence.dart';
import 'package:foresignal_api/src/model/client_presence_input.dart';
import 'package:foresignal_api/src/model/notification_badge.dart';
import 'package:foresignal_api/src/model/notification_detail.dart';
import 'package:foresignal_api/src/model/notification_page.dart';
import 'package:foresignal_api/src/model/notification_read_request.dart';
import 'package:foresignal_api/src/model/problem.dart';
import 'package:foresignal_api/src/model/stream_notifications200_response.dart';

class NotificationsApi {

  final Dio _dio;

  const NotificationsApi(this._dio);

  /// Execute a notification action
  /// Executes the capability once (single use, in one transaction): ack sets &#x60;acknowledged_by&#x3D;action:&lt;channelType&gt;:&lt;channelName&gt;&#x60;, snooze snoozes the alert, mute mutes the rule. An already acknowledged or resolved alert returns 200 with &#x60;result: already-&lt;state&gt;&#x60;. &#x60;Accept: application/json&#x60; → JSON, otherwise an HTML result page. An &#x60;Origin&#x60; other than the &#x60;publicBaseUrl&#x60; origin or the request&#39;s own origin (scheme and Host, behind trusted proxies) → 403. Audited; 30 requests/min per client IP.  **Scope:** open (no token; the credential is in the request).
  ///
  /// Parameters:
  /// * [token] - Action capability (`eac_…`); redacted in logs
  /// * [origin] - Must be absent, the publicBaseUrl origin, or the request's own origin
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [ActionResult] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<ActionResult>> executeAction({ 
    required String token,
    String? origin,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/actions/{token}'.replaceAll('{' r'token' '}', token.toString());
    final _options = Options(
      method: r'POST',
      headers: <String, dynamic>{
        if (origin != null) r'Origin': origin,
        ...?headers,
      },
      extra: <String, dynamic>{
        'secure': <Map<String, String>>[],
        ...?extra,
      },
      contentType: 'application/json',
      validateStatus: validateStatus,
    );

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    ActionResult? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<ActionResult, ActionResult>(rawData, 'ActionResult', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<ActionResult>(
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

  /// Show an action confirm page
  /// Never acts. Returns a minimal server-rendered HTML page (alert title, severity, fired time, action and duration, one POST button; no JavaScript or external assets) or a 410 page. The MAC is verified before any lookup. Limited to 30 requests/min per client IP.  **Scope:** open (no token; the credential is in the request).
  ///
  /// Parameters:
  /// * [token] - Action capability (`eac_…`); redacted in logs
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [String] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<String>> getActionConfirm({ 
    required String token,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/actions/{token}'.replaceAll('{' r'token' '}', token.toString());
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

  /// Get a notification with actions
  /// Adds &#x60;links&#x60; and &#x60;actions&#x60;: capability URLs minted for the calling device (12 h), so native apps never receive capabilities through Apple or Google.  **Scope:** &#x60;read&#x60; (always authenticated: a token is required even when &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [notificationId] - Notification id
  /// * [foresignalDeviceId] - Client-chosen device id (UUID). Used as the device key when the token carries no device metadata (033); combined with the token id
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [NotificationDetail] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<NotificationDetail>> getNotification({ 
    required int notificationId,
    String? foresignalDeviceId,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/notifications/{notificationId}'.replaceAll('{' r'notificationId' '}', notificationId.toString());
    final _options = Options(
      method: r'GET',
      headers: <String, dynamic>{
        if (foresignalDeviceId != null) r'Foresignal-Device-Id': foresignalDeviceId,
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

    NotificationDetail? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<NotificationDetail, NotificationDetail>(rawData, 'NotificationDetail', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<NotificationDetail>(
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

  /// Unread badge counts
  /// Unread alert and system notifications for this device, counting alerts that are still firing. The same count feeds native &#x60;aps.badge&#x60;.  **Scope:** &#x60;read&#x60; (always authenticated: a token is required even when &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [foresignalDeviceId] - Client-chosen device id (UUID). Used as the device key when the token carries no device metadata (033); combined with the token id
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [NotificationBadge] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<NotificationBadge>> getNotificationBadge({ 
    String? foresignalDeviceId,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/notifications/badge';
    final _options = Options(
      method: r'GET',
      headers: <String, dynamic>{
        if (foresignalDeviceId != null) r'Foresignal-Device-Id': foresignalDeviceId,
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

    NotificationBadge? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<NotificationBadge, NotificationBadge>(rawData, 'NotificationBadge', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<NotificationBadge>(
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

  /// List client presence
  /// Current presence rows of all devices.  **Scope:** &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [List<ClientPresence>] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<List<ClientPresence>>> listClients({ 
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/clients';
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

    List<ClientPresence>? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<List<ClientPresence>, ClientPresence>(rawData, 'List<ClientPresence>', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<List<ClientPresence>>(
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

  /// List inbox notifications
  /// Newest first, keyset on id: &#x60;before&#x60; pages to older items, &#x60;after&#x60; returns only newer ones. Read state is per device (token device metadata, else the &#x60;Foresignal-Device-Id&#x60; header combined with the token id; else 400 DEVICE_ID_REQUIRED). Includes system events. Reading never acknowledges an alert.  **Scope:** &#x60;read&#x60; (always authenticated: a token is required even when &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [after] - Only ids greater than this
  /// * [before] - Only ids smaller than this (paging)
  /// * [events] - Event filter
  /// * [minSeverity] - Minimum severity
  /// * [unread] - Only unread on this device
  /// * [cursor] - Opaque cursor from the previous page's `nextCursor`
  /// * [limit] - Page size
  /// * [foresignalDeviceId] - Client-chosen device id (UUID). Used as the device key when the token carries no device metadata (033); combined with the token id
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [NotificationPage] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<NotificationPage>> listNotifications({ 
    int? after,
    int? before,
    List<String>? events,
    String? minSeverity,
    bool? unread,
    String? cursor,
    int? limit = 100,
    String? foresignalDeviceId,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/notifications';
    final _options = Options(
      method: r'GET',
      headers: <String, dynamic>{
        if (foresignalDeviceId != null) r'Foresignal-Device-Id': foresignalDeviceId,
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
      if (after != null) r'after': after,
      if (before != null) r'before': before,
      if (events != null) r'events': events,
      if (minSeverity != null) r'minSeverity': minSeverity,
      if (unread != null) r'unread': unread,
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

    NotificationPage? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<NotificationPage, NotificationPage>(rawData, 'NotificationPage', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<NotificationPage>(
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

  /// Mark notifications read
  /// Moves this device&#39;s read cursor (&#x60;upToId&#x60;) or records sparse reads (&#x60;ids&#x60;). Reading never acknowledges an alert. Other windows of the same device get a &#x60;badge&#x60; event.  **Scope:** &#x60;read&#x60; (always authenticated: a token is required even when &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [notificationReadRequest] 
  /// * [foresignalDeviceId] - Client-chosen device id (UUID). Used as the device key when the token carries no device metadata (033); combined with the token id
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [NotificationBadge] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<NotificationBadge>> markNotificationsRead({ 
    required NotificationReadRequest notificationReadRequest,
    String? foresignalDeviceId,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/notifications/read';
    final _options = Options(
      method: r'POST',
      headers: <String, dynamic>{
        if (foresignalDeviceId != null) r'Foresignal-Device-Id': foresignalDeviceId,
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
      _bodyData = jsonEncode(notificationReadRequest);

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

    NotificationBadge? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<NotificationBadge, NotificationBadge>(rawData, 'NotificationBadge', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<NotificationBadge>(
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

  /// Inbox event stream (SSE)
  /// Server-Sent Events through the 015 channel: &#x60;notification&#x60; (id &#x3D; notification id; replayed from the table after &#x60;Last-Event-ID&#x60;) and &#x60;badge&#x60; (when this device&#39;s cursor moves). An authenticated open stream counts as &#x60;background&#x60; presence.  **Scope:** &#x60;read&#x60; (always authenticated: a token is required even when &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [lastEventID] - SSE resume point (the last `id:` received)
  /// * [foresignalDeviceId] - Client-chosen device id (UUID). Used as the device key when the token carries no device metadata (033); combined with the token id
  /// * [ticket] - Stream or tile ticket from `POST /v1/stream-tickets` (023), for clients that cannot send an `Authorization` header. Accepted only for the ticket's purpose and never in a header; stripped from logs
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [StreamNotifications200Response] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<StreamNotifications200Response>> streamNotifications({ 
    String? lastEventID,
    String? foresignalDeviceId,
    String? ticket,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/notifications/stream';
    final _options = Options(
      method: r'GET',
      headers: <String, dynamic>{
        if (lastEventID != null) r'Last-Event-ID': lastEventID,
        if (foresignalDeviceId != null) r'Foresignal-Device-Id': foresignalDeviceId,
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
      if (ticket != null) r'ticket': ticket,
    };

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    StreamNotifications200Response? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<StreamNotifications200Response, StreamNotifications200Response>(rawData, 'StreamNotifications200Response', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<StreamNotifications200Response>(
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

  /// Report client presence
  /// Upserts this device&#39;s presence (&#x60;ttlSeconds&#x60; ≤ 300). While any desktop is &#x60;active&#x60;, non-critical alerts on channels with &#x60;deferWhileActiveSeconds&#x60; are deferred and suppressed if read or acknowledged meanwhile. Critical alerts and system events are never deferred; stale presence fails open.  **Scope:** &#x60;read&#x60; (always authenticated: a token is required even when &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [clientPresenceInput] 
  /// * [foresignalDeviceId] - Client-chosen device id (UUID). Used as the device key when the token carries no device metadata (033); combined with the token id
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [ClientPresence] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<ClientPresence>> updatePresence({ 
    required ClientPresenceInput clientPresenceInput,
    String? foresignalDeviceId,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/clients/presence';
    final _options = Options(
      method: r'POST',
      headers: <String, dynamic>{
        if (foresignalDeviceId != null) r'Foresignal-Device-Id': foresignalDeviceId,
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
      _bodyData = jsonEncode(clientPresenceInput);

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

    ClientPresence? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<ClientPresence, ClientPresence>(rawData, 'ClientPresence', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<ClientPresence>(
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
