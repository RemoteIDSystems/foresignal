//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

import 'dart:async';

// ignore: unused_import
import 'dart:convert';
import 'package:foresignal_api/src/deserialize.dart';
import 'package:dio/dio.dart';

import 'package:foresignal_api/src/model/change_page.dart';
import 'package:foresignal_api/src/model/problem.dart';
import 'package:foresignal_api/src/model/stream_changes200_response.dart';

class ChangesApi {

  final Dio _dio;

  const ChangesApi(this._dio);

  /// Change feed after a cursor
  /// Every new version in commit order (&#x60;seq &gt; after&#x60;); seq is gap-tolerant but strictly increasing in commit order, so &#x60;after&#x3D;&lt;lastSeq&gt;&#x60; never misses a change. Each change carries prevSeq, persistReason and validUntil; &#x60;raster-frame&#x60; changes always carry their payload. When &#x60;after&#x60; is below the purge horizon (heads purged without tombstones) the response is 410 RESYNC_REQUIRED and the client re-snapshots.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [after] - Return changes with seq > after
  /// * [source_] - Filter by source id (repeatable or comma-separated)
  /// * [kind] - Filter by kind (repeatable or comma-separated)
  /// * [bbox] - minLon,minLat,maxLon,maxLat (WGS84). minLon > maxLon crosses the antimeridian and is split into two boxes; minLat < maxLat is required.
  /// * [payload] - Include payloads
  /// * [limit] - Max changes
  /// * [externalId] - Filter by external id (≤ 100) (repeatable or comma-separated)
  /// * [fields] - Projection: comma list of top-level properties (source, externalId, kind, title, observedAt, geometry, …) and `payload.<dot.path>` entries; unknown top-level names → 400
  /// * [format] - Response format; streaming formats are capped at foresignal.api.stream.maxRows (400 RESULT_TOO_LARGE)
  /// * [place] - Place id(s) (036), resolved to their areas and intersected with the other filters; requires a token (places are never anonymous)
  /// * [radiusKm] - Extra buffer around `place` areas (036)
  /// * [view] - Saved view id or slug (036), intersected with the other filters; a view that references a place or watchlist needs a token
  /// * [inWatchlist] - Only entities whose identity is on this watchlist (036); token required (401 for anonymous and feed keys)
  /// * [licencePolicy] - Licence policy flags (comma-separated): `no-nc` excludes records whose commercial use is not ALLOWED, `no-sa` excludes share-alike records, `redistributable` excludes records whose redistribution is not ALLOWED. Adds to the token's `licencePolicy` floor; it can never remove a token flag.
  /// * [foresignalLicencePolicy] - Header form of `licencePolicy` (comma-separated flags `no-nc`, `no-sa`, `redistributable`)
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [ChangePage] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<ChangePage>> listChanges({ 
    int? after = 0,
    List<String>? source_,
    List<String>? kind,
    String? bbox,
    bool? payload = false,
    int? limit = 1000,
    List<String>? externalId,
    String? fields,
    String? format,
    List<String>? place,
    num? radiusKm,
    String? view,
    String? inWatchlist,
    List<String>? licencePolicy,
    String? foresignalLicencePolicy,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/changes';
    final _options = Options(
      method: r'GET',
      headers: <String, dynamic>{
        if (foresignalLicencePolicy != null) r'Foresignal-Licence-Policy': foresignalLicencePolicy,
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
      if (source_ != null) r'source': source_,
      if (kind != null) r'kind': kind,
      if (bbox != null) r'bbox': bbox,
      if (payload != null) r'payload': payload,
      if (limit != null) r'limit': limit,
      if (externalId != null) r'externalId': externalId,
      if (fields != null) r'fields': fields,
      if (format != null) r'format': format,
      if (place != null) r'place': place,
      if (radiusKm != null) r'radiusKm': radiusKm,
      if (view != null) r'view': view,
      if (inWatchlist != null) r'inWatchlist': inWatchlist,
      if (licencePolicy != null) r'licencePolicy': licencePolicy,
    };

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    ChangePage? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<ChangePage, ChangePage>(rawData, 'ChangePage', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<ChangePage>(
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

  /// Live change stream (SSE)
  /// Two lanes: lossless &#x60;change&#x60; events (&#x60;id:&#x60; &#x3D; seq; replayed from &#x60;Last-Event-ID&#x60;, else &#x60;after&#x60;, else live from now) and conflated, non-persisted &#x60;live&#x60;/&#x60;touch&#x60; updates without ids. A spatial filter matches bbox, prevPersistedBbox or prevLiveBbox; when only a previous box matches, &#x60;leftFilter: true&#x60; tells the client to remove the feature. On &#x60;resync&#x60; re-fetch current state (&#x60;/v1/records?order&#x3D;id&#x60; or tiles) and keep the stream open; on &#x60;reset&#x60; reconnect from &#x60;resumeAfter&#x60;, or re-snapshot when the reason is &#x60;resync_required&#x60;; apply an event only if its observedAt ≥ the stored one.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [lastEventID] - Last change seq received (preferred resume point)
  /// * [after] - Resume after this seq when Last-Event-ID is absent (default: live from now)
  /// * [source_] - Filter by source id (repeatable or comma-separated)
  /// * [kind] - Filter by kind (repeatable or comma-separated)
  /// * [bbox] - minLon,minLat,maxLon,maxLat (WGS84). minLon > maxLon crosses the antimeridian and is split into two boxes; minLat < maxLat is required.
  /// * [payload] - Include payloads on change events (kinds in inlinePayloadKinds, e.g. raster-frame, always carry them)
  /// * [externalId] - Filter by external id (≤ 100) (repeatable or comma-separated)
  /// * [near] - Centre point `lon,lat` (GeoJSON order); requires `radiusKm`
  /// * [radiusKm] - Radius in km around `near` (0 < r ≤ 2000); required with `near`
  /// * [live] - Send `live` events
  /// * [touches] - Send `touch` events
  /// * [ticket] - Stream or tile ticket from `POST /v1/stream-tickets` (023), for clients that cannot send an `Authorization` header. Accepted only for the ticket's purpose and never in a header; stripped from logs
  /// * [place] - Place id(s) (036), resolved to their areas and intersected with the other filters; requires a token (places are never anonymous)
  /// * [view] - Saved view id or slug (036), intersected with the other filters; a view that references a place or watchlist needs a token
  /// * [inWatchlist] - Only entities whose identity is on this watchlist (036); token required (401 for anonymous and feed keys)
  /// * [licencePolicy] - Licence policy flags (comma-separated): `no-nc` excludes records whose commercial use is not ALLOWED, `no-sa` excludes share-alike records, `redistributable` excludes records whose redistribution is not ALLOWED. Adds to the token's `licencePolicy` floor; it can never remove a token flag.
  /// * [foresignalLicencePolicy] - Header form of `licencePolicy` (comma-separated flags `no-nc`, `no-sa`, `redistributable`)
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [StreamChanges200Response] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<StreamChanges200Response>> streamChanges({ 
    String? lastEventID,
    int? after,
    List<String>? source_,
    List<String>? kind,
    String? bbox,
    bool? payload = false,
    List<String>? externalId,
    String? near,
    num? radiusKm,
    bool? live = true,
    bool? touches = false,
    String? ticket,
    List<String>? place,
    String? view,
    String? inWatchlist,
    List<String>? licencePolicy,
    String? foresignalLicencePolicy,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/changes/stream';
    final _options = Options(
      method: r'GET',
      headers: <String, dynamic>{
        if (lastEventID != null) r'Last-Event-ID': lastEventID,
        if (foresignalLicencePolicy != null) r'Foresignal-Licence-Policy': foresignalLicencePolicy,
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
      if (source_ != null) r'source': source_,
      if (kind != null) r'kind': kind,
      if (bbox != null) r'bbox': bbox,
      if (payload != null) r'payload': payload,
      if (externalId != null) r'externalId': externalId,
      if (near != null) r'near': near,
      if (radiusKm != null) r'radiusKm': radiusKm,
      if (live != null) r'live': live,
      if (touches != null) r'touches': touches,
      if (ticket != null) r'ticket': ticket,
      if (place != null) r'place': place,
      if (view != null) r'view': view,
      if (inWatchlist != null) r'inWatchlist': inWatchlist,
      if (licencePolicy != null) r'licencePolicy': licencePolicy,
    };

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    StreamChanges200Response? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<StreamChanges200Response, StreamChanges200Response>(rawData, 'StreamChanges200Response', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<StreamChanges200Response>(
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
