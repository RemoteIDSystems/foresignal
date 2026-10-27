//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

import 'dart:async';

// ignore: unused_import
import 'dart:convert';
import 'package:foresignal_api/src/deserialize.dart';
import 'package:dio/dio.dart';

import 'dart:typed_data';
import 'package:foresignal_api/src/model/feature_collection.dart';
import 'package:foresignal_api/src/model/problem.dart';
import 'package:foresignal_api/src/model/record_page.dart';
import 'package:foresignal_api/src/model/saved_view.dart';
import 'package:foresignal_api/src/model/saved_view_input.dart';
import 'package:foresignal_api/src/model/saved_view_page.dart';
import 'package:foresignal_api/src/model/stream_view200_response.dart';
import 'package:foresignal_api/src/model/tile_json.dart';

class ViewsApi {

  final Dio _dio;

  const ViewsApi(this._dio);

  /// Create a saved view
  /// Compiles the view to one &#x60;RecordPredicate&#x60; shared with rules (SQL and in-memory evaluation agree on the operator truth table). A signed-in user creating their own view is refused past their &#x60;maxSavedViews&#x60; quota (409 QUOTA_EXCEEDED, R2-026).  **Scope:** &#x60;agent&#x60;.  **v2.0 (per-user, R2-012):** a saved view is **shared** (&#x60;shared: true&#x60;, no owner; every view from before v2.0 and every view an API token creates) or **personal** (&#x60;ownerPrincipalId&#x60;, the owning user&#39;s principal). A signed-in user reaches the view operations with &#x60;self.views&#x60; (the reads also with &#x60;data.read&#x60;): they list and render their own views and the shared ones, and create, replace and delete their own; a view they create is theirs. Another user&#39;s view is 404 UNKNOWN_VIEW, like an unknown one; replacing or deleting a shared view needs &#x60;users.manage&#x60; (403 PERMISSION_DENIED otherwise). A session holding &#x60;users.manage&#x60; and every API token see and manage all views. Slugs stay unique across all views. The shapes change only by the additive read-only &#x60;shared&#x60; and &#x60;ownerPrincipalId&#x60;.
  ///
  /// Parameters:
  /// * [savedViewInput] 
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [SavedView] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<SavedView>> createView({ 
    required SavedViewInput savedViewInput,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/views';
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
      contentType: 'application/json',
      validateStatus: validateStatus,
    );

    dynamic _bodyData;

    try {
      _bodyData = jsonEncode(savedViewInput);

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

    SavedView? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<SavedView, SavedView>(rawData, 'SavedView', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<SavedView>(
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

  /// Delete a saved view
  /// Removes the definition.  **Scope:** &#x60;agent&#x60;.  **v2.0 (per-user, R2-012):** a saved view is **shared** (&#x60;shared: true&#x60;, no owner; every view from before v2.0 and every view an API token creates) or **personal** (&#x60;ownerPrincipalId&#x60;, the owning user&#39;s principal). A signed-in user reaches the view operations with &#x60;self.views&#x60; (the reads also with &#x60;data.read&#x60;): they list and render their own views and the shared ones, and create, replace and delete their own; a view they create is theirs. Another user&#39;s view is 404 UNKNOWN_VIEW, like an unknown one; replacing or deleting a shared view needs &#x60;users.manage&#x60; (403 PERMISSION_DENIED otherwise). A session holding &#x60;users.manage&#x60; and every API token see and manage all views. Slugs stay unique across all views. The shapes change only by the additive read-only &#x60;shared&#x60; and &#x60;ownerPrincipalId&#x60;.
  ///
  /// Parameters:
  /// * [viewId] - Saved view id (uuid) or slug
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future]
  /// Throws [DioException] if API call or serialization fails
  Future<Response<void>> deleteView({ 
    required String viewId,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/views/{viewId}'.replaceAll('{' r'viewId' '}', viewId.toString());
    final _options = Options(
      method: r'DELETE',
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

    return _response;
  }

  /// Get a saved view
  /// 404 UNKNOWN_VIEW.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).  **v2.0 (per-user, R2-012):** a saved view is **shared** (&#x60;shared: true&#x60;, no owner; every view from before v2.0 and every view an API token creates) or **personal** (&#x60;ownerPrincipalId&#x60;, the owning user&#39;s principal). A signed-in user reaches the view operations with &#x60;self.views&#x60; (the reads also with &#x60;data.read&#x60;): they list and render their own views and the shared ones, and create, replace and delete their own; a view they create is theirs. Another user&#39;s view is 404 UNKNOWN_VIEW, like an unknown one; replacing or deleting a shared view needs &#x60;users.manage&#x60; (403 PERMISSION_DENIED otherwise). A session holding &#x60;users.manage&#x60; and every API token see and manage all views. Slugs stay unique across all views. The shapes change only by the additive read-only &#x60;shared&#x60; and &#x60;ownerPrincipalId&#x60;.
  ///
  /// Parameters:
  /// * [viewId] - Saved view id (uuid) or slug
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [SavedView] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<SavedView>> getView({ 
    required String viewId,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/views/{viewId}'.replaceAll('{' r'viewId' '}', viewId.toString());
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

    SavedView? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<SavedView, SavedView>(rawData, 'SavedView', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<SavedView>(
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

  /// Records matching a saved view
  /// Current records matching the view&#39;s predicate, in every 014 format, keyset-paged, with the &#x60;fields&#x60; projection applied. View data follows normal read rules unless the view references a place or watchlist, in which case a token is required (401).  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).  **v2.0 (per-user, R2-012):** a saved view is **shared** (&#x60;shared: true&#x60;, no owner; every view from before v2.0 and every view an API token creates) or **personal** (&#x60;ownerPrincipalId&#x60;, the owning user&#39;s principal). A signed-in user reaches the view operations with &#x60;self.views&#x60; (the reads also with &#x60;data.read&#x60;): they list and render their own views and the shared ones, and create, replace and delete their own; a view they create is theirs. Another user&#39;s view is 404 UNKNOWN_VIEW, like an unknown one; replacing or deleting a shared view needs &#x60;users.manage&#x60; (403 PERMISSION_DENIED otherwise). A session holding &#x60;users.manage&#x60; and every API token see and manage all views. Slugs stay unique across all views. The shapes change only by the additive read-only &#x60;shared&#x60; and &#x60;ownerPrincipalId&#x60;.
  ///
  /// Parameters:
  /// * [viewId] - Saved view id (uuid) or slug
  /// * [format] - Alternative to the Accept header
  /// * [cursor] - Opaque cursor from the previous page's `nextCursor`
  /// * [limit] - Page size
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [RecordPage] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<RecordPage>> getViewRecords({ 
    required String viewId,
    String? format,
    String? cursor,
    int? limit = 100,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/views/{viewId}/records'.replaceAll('{' r'viewId' '}', viewId.toString());
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
      if (format != null) r'format': format,
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

    RecordPage? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<RecordPage, RecordPage>(rawData, 'RecordPage', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<RecordPage>(
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

  /// Vector tile of a saved view
  /// Mapbox Vector Tile of the view&#39;s current records (016 properties); the cache key includes the view &#x60;version&#x60;. View data follows normal read rules unless the view references a place or watchlist, in which case a token is required (401).  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).  **v2.0 (per-user, R2-012):** a saved view is **shared** (&#x60;shared: true&#x60;, no owner; every view from before v2.0 and every view an API token creates) or **personal** (&#x60;ownerPrincipalId&#x60;, the owning user&#39;s principal). A signed-in user reaches the view operations with &#x60;self.views&#x60; (the reads also with &#x60;data.read&#x60;): they list and render their own views and the shared ones, and create, replace and delete their own; a view they create is theirs. Another user&#39;s view is 404 UNKNOWN_VIEW, like an unknown one; replacing or deleting a shared view needs &#x60;users.manage&#x60; (403 PERMISSION_DENIED otherwise). A session holding &#x60;users.manage&#x60; and every API token see and manage all views. Slugs stay unique across all views. The shapes change only by the additive read-only &#x60;shared&#x60; and &#x60;ownerPrincipalId&#x60;.
  ///
  /// Parameters:
  /// * [viewId] - Saved view id (uuid) or slug
  /// * [z] - Zoom
  /// * [x] - Tile column
  /// * [y] - Tile row
  /// * [ticket] - Stream or tile ticket from `POST /v1/stream-tickets` (023), for clients that cannot send an `Authorization` header. Accepted only for the ticket's purpose and never in a header; stripped from logs
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [Uint8List] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<Uint8List>> getViewTile({ 
    required String viewId,
    required int z,
    required int x,
    required int y,
    String? ticket,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/views/{viewId}/tiles/{z}/{x}/{y}.mvt'.replaceAll('{' r'viewId' '}', viewId.toString()).replaceAll('{' r'z' '}', z.toString()).replaceAll('{' r'x' '}', x.toString()).replaceAll('{' r'y' '}', y.toString());
    final _options = Options(
      method: r'GET',
      responseType: ResponseType.bytes,
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

    Uint8List? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : rawData as Uint8List;

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<Uint8List>(
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

  /// TileJSON for a saved view
  /// TileJSON 3.0.0 document whose tile template carries the view version. View data follows normal read rules unless the view references a place or watchlist, in which case a token is required (401).  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).  **v2.0 (per-user, R2-012):** a saved view is **shared** (&#x60;shared: true&#x60;, no owner; every view from before v2.0 and every view an API token creates) or **personal** (&#x60;ownerPrincipalId&#x60;, the owning user&#39;s principal). A signed-in user reaches the view operations with &#x60;self.views&#x60; (the reads also with &#x60;data.read&#x60;): they list and render their own views and the shared ones, and create, replace and delete their own; a view they create is theirs. Another user&#39;s view is 404 UNKNOWN_VIEW, like an unknown one; replacing or deleting a shared view needs &#x60;users.manage&#x60; (403 PERMISSION_DENIED otherwise). A session holding &#x60;users.manage&#x60; and every API token see and manage all views. Slugs stay unique across all views. The shapes change only by the additive read-only &#x60;shared&#x60; and &#x60;ownerPrincipalId&#x60;.
  ///
  /// Parameters:
  /// * [viewId] - Saved view id (uuid) or slug
  /// * [ticket] - Stream or tile ticket from `POST /v1/stream-tickets` (023), for clients that cannot send an `Authorization` header. Accepted only for the ticket's purpose and never in a header; stripped from logs
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [TileJson] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<TileJson>> getViewTileJson({ 
    required String viewId,
    String? ticket,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/views/{viewId}/tiles.json'.replaceAll('{' r'viewId' '}', viewId.toString());
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

    TileJson? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<TileJson, TileJson>(rawData, 'TileJson', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<TileJson>(
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

  /// List saved views
  /// View definitions.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).  **v2.0 (per-user, R2-012):** a saved view is **shared** (&#x60;shared: true&#x60;, no owner; every view from before v2.0 and every view an API token creates) or **personal** (&#x60;ownerPrincipalId&#x60;, the owning user&#39;s principal). A signed-in user reaches the view operations with &#x60;self.views&#x60; (the reads also with &#x60;data.read&#x60;): they list and render their own views and the shared ones, and create, replace and delete their own; a view they create is theirs. Another user&#39;s view is 404 UNKNOWN_VIEW, like an unknown one; replacing or deleting a shared view needs &#x60;users.manage&#x60; (403 PERMISSION_DENIED otherwise). A session holding &#x60;users.manage&#x60; and every API token see and manage all views. Slugs stay unique across all views. The shapes change only by the additive read-only &#x60;shared&#x60; and &#x60;ownerPrincipalId&#x60;.
  ///
  /// Parameters:
  /// * [cursor] - Opaque cursor from the previous page's `nextCursor`
  /// * [limit] - Page size
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [SavedViewPage] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<SavedViewPage>> listViews({ 
    String? cursor,
    int? limit = 100,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/views';
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

    SavedViewPage? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<SavedViewPage, SavedViewPage>(rawData, 'SavedViewPage', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<SavedViewPage>(
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

  /// Change stream of a saved view (SSE)
  /// 015 SSE semantics (replay from &#x60;Last-Event-ID&#x60;/&#x60;after&#x60;, live and touch lanes, resync and reset; &#x60;leftFilter&#x60; unchanged) filtered by the view&#39;s predicate. View data follows normal read rules unless the view references a place or watchlist, in which case a token is required (401).  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).  **v2.0 (per-user, R2-012):** a saved view is **shared** (&#x60;shared: true&#x60;, no owner; every view from before v2.0 and every view an API token creates) or **personal** (&#x60;ownerPrincipalId&#x60;, the owning user&#39;s principal). A signed-in user reaches the view operations with &#x60;self.views&#x60; (the reads also with &#x60;data.read&#x60;): they list and render their own views and the shared ones, and create, replace and delete their own; a view they create is theirs. Another user&#39;s view is 404 UNKNOWN_VIEW, like an unknown one; replacing or deleting a shared view needs &#x60;users.manage&#x60; (403 PERMISSION_DENIED otherwise). A session holding &#x60;users.manage&#x60; and every API token see and manage all views. Slugs stay unique across all views. The shapes change only by the additive read-only &#x60;shared&#x60; and &#x60;ownerPrincipalId&#x60;.
  ///
  /// Parameters:
  /// * [viewId] - Saved view id (uuid) or slug
  /// * [lastEventID] - SSE resume point (the last `id:` received)
  /// * [after] - Alternative to Last-Event-ID
  /// * [ticket] - Stream or tile ticket from `POST /v1/stream-tickets` (023), for clients that cannot send an `Authorization` header. Accepted only for the ticket's purpose and never in a header; stripped from logs
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [StreamView200Response] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<StreamView200Response>> streamView({ 
    required String viewId,
    String? lastEventID,
    int? after,
    String? ticket,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/views/{viewId}/stream'.replaceAll('{' r'viewId' '}', viewId.toString());
    final _options = Options(
      method: r'GET',
      headers: <String, dynamic>{
        if (lastEventID != null) r'Last-Event-ID': lastEventID,
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

    StreamView200Response? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<StreamView200Response, StreamView200Response>(rawData, 'StreamView200Response', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<StreamView200Response>(
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

  /// Replace a saved view
  /// Bumps &#x60;version&#x60; (tile cache keys include it).  **Scope:** &#x60;agent&#x60;.  **v2.0 (per-user, R2-012):** a saved view is **shared** (&#x60;shared: true&#x60;, no owner; every view from before v2.0 and every view an API token creates) or **personal** (&#x60;ownerPrincipalId&#x60;, the owning user&#39;s principal). A signed-in user reaches the view operations with &#x60;self.views&#x60; (the reads also with &#x60;data.read&#x60;): they list and render their own views and the shared ones, and create, replace and delete their own; a view they create is theirs. Another user&#39;s view is 404 UNKNOWN_VIEW, like an unknown one; replacing or deleting a shared view needs &#x60;users.manage&#x60; (403 PERMISSION_DENIED otherwise). A session holding &#x60;users.manage&#x60; and every API token see and manage all views. Slugs stay unique across all views. The shapes change only by the additive read-only &#x60;shared&#x60; and &#x60;ownerPrincipalId&#x60;.
  ///
  /// Parameters:
  /// * [viewId] - Saved view id (uuid) or slug
  /// * [savedViewInput] 
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [SavedView] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<SavedView>> updateView({ 
    required String viewId,
    required SavedViewInput savedViewInput,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/views/{viewId}'.replaceAll('{' r'viewId' '}', viewId.toString());
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
      _bodyData = jsonEncode(savedViewInput);

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

    SavedView? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<SavedView, SavedView>(rawData, 'SavedView', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<SavedView>(
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
