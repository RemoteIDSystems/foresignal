//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

import 'dart:async';

// ignore: unused_import
import 'dart:convert';
import 'package:foresignal_api/src/deserialize.dart';
import 'package:dio/dio.dart';

import 'package:foresignal_api/src/model/problem.dart';

class KMLApi {

  final Dio _dio;

  const KMLApi(this._dio);

  /// Self-refreshing KML NetworkLink document
  /// Document with one &#x60;NetworkLink&#x60; per layer pointing at &#x60;/v1/kml/records.kml?layer&#x3D;{layer}&#x60; (feed-key prefixed when one is used), &#x60;refreshMode&#x3D;onInterval&#x60;, &#x60;viewRefreshMode&#x3D;onStop&#x60; (2 s) and &#x60;viewFormat&#x3D;bbox&#x3D;[bboxWest],[bboxSouth],[bboxEast],[bboxNorth]&#x60;. For Google Earth and Cesium &#x60;KmlDataSource&#x60;.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [layers] - Comma-separated tile layer ids; default every source layer the caller may read. A layer the caller cannot see is left out.
  /// * [refresh] - Refresh interval in seconds (30 to 86400)
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [String] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<String>> getKmlNetworkLink({ 
    String? layers,
    int? refresh = 60,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/kml/networklink.kml';
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
      if (layers != null) r'layers': layers,
      if (refresh != null) r'refresh': refresh,
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

  /// Current records as KML or KMZ
  /// One &#x60;Style&#x60; per kind (icon href &#x60;&lt;base&gt;/v1/map/icons/{name}.png&#x60;, relative inside KMZ; colour as &#x60;aabbggrr&#x60;; heading from &#x60;rotateField&#x60;) and one &#x60;Placemark&#x60; per record (&#x60;name&#x60; &#x3D; title, &#x60;TimeStamp/when&#x60; &#x3D; observedAt, &#x60;ExtendedData&#x60; for fieldSpecs/tileProperties fields, plain escaped &#x60;description&#x60;). Capped at 5,000 Placemarks, newest observedAt first; when truncated the Document carries &#x60;Snippet&#x60; \&quot;truncated: showing 5000 of N\&quot; and &#x60;ExtendedData truncated&#x3D;true&#x60;. An antimeridian bbox (&#x60;west &gt; east&#x60;) is split. One of &#x60;layer&#x60;, &#x60;source&#x60; or &#x60;kind&#x60; is required. All upstream strings are escaped for XML and HTML (Google Earth renders balloons as HTML); no CDATA with upstream text. The Document &#x60;description&#x60; carries the included sources&#39; attributions. Private filtering and &#x60;EgressPolicy.allows(FEED)&#x60; apply; keys in URLs are redacted from logs.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [layer] - Tile layer id (016)
  /// * [source_] - Filter by source id (repeatable or comma-separated)
  /// * [kind] - Filter by kind (repeatable or comma-separated)
  /// * [bbox] - minLon,minLat,maxLon,maxLat (WGS84); Google Earth fills it from `viewFormat`
  /// * [asOf] - Transaction-time snapshot (014 asOf)
  /// * [format] - `kml` (default, `application/vnd.google-earth.kml+xml`) or `kmz` (zip of `doc.kml` plus the icons used, `application/vnd.google-earth.kmz`)
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [String] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<String>> getKmlRecords({ 
    String? layer,
    List<String>? source_,
    List<String>? kind,
    String? bbox,
    DateTime? asOf,
    String? format,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/kml/records.kml';
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
      if (layer != null) r'layer': layer,
      if (source_ != null) r'source': source_,
      if (kind != null) r'kind': kind,
      if (bbox != null) r'bbox': bbox,
      if (asOf != null) r'asOf': asOf,
      if (format != null) r'format': format,
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

  /// Entity tracks as KML gx:Track
  /// One &#x60;Placemark&#x60; with &#x60;gx:Track&#x60; per entity (&#x60;when&#x60; plus &#x60;gx:coord \&quot;lon lat alt\&quot;&#x60;; altitude from the payload altitude field, else 0 with &#x60;clampToGround&#x60;), from 037&#39;s TrackService unsimplified. Select one entity with &#x60;sourceId&#x60;+&#x60;externalId&#x60;, or many with &#x60;kind&#x60;+&#x60;bbox&#x60;. Limits: ≤ 100 entities and ≤ 5,000 points per track, with truncation flags in &#x60;ExtendedData&#x60;. All upstream strings are escaped for XML and HTML (Google Earth renders balloons as HTML); no CDATA with upstream text. The Document &#x60;description&#x60; carries the included sources&#39; attributions. Private filtering and &#x60;EgressPolicy.allows(FEED)&#x60; apply; keys in URLs are redacted from logs.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [sourceId] - Source id of a single entity
  /// * [externalId] - External id of a single entity
  /// * [kind] - Kind, for a multi-entity export
  /// * [bbox] - minLon,minLat,maxLon,maxLat (WGS84)
  /// * [from] - Track start (observedAt >= from)
  /// * [to] - Track end (observedAt < to)
  /// * [format] - `kml` (default, `application/vnd.google-earth.kml+xml`) or `kmz` (zip of `doc.kml` plus the icons used, `application/vnd.google-earth.kmz`)
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [String] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<String>> getKmlTracks({ 
    String? sourceId,
    String? externalId,
    String? kind,
    String? bbox,
    DateTime? from,
    DateTime? to,
    String? format,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/kml/tracks.kml';
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
      if (sourceId != null) r'sourceId': sourceId,
      if (externalId != null) r'externalId': externalId,
      if (kind != null) r'kind': kind,
      if (bbox != null) r'bbox': bbox,
      if (from != null) r'from': from,
      if (to != null) r'to': to,
      if (format != null) r'format': format,
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

  /// Trigger geofence as KML
  /// The rule&#39;s geofence as a &#x60;Polygon&#x60;, or a &#x60;LineString&#x60; plus its buffered &#x60;Polygon&#x60;, or the union of its 040 &#x60;geofenceRef&#x60;, simplified to ≤ 10,000 vertices. The literal &#x60;.kml&#x60; suffix distinguishes this path from &#x60;getTrigger&#x60;.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [ruleId] - Trigger rule id
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [String] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<String>> getTriggerKml({ 
    required String ruleId,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/triggers/{ruleId}.kml'.replaceAll('{' r'ruleId' '}', ruleId.toString());
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
