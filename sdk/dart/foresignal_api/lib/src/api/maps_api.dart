//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

import 'dart:async';

// ignore: unused_import
import 'dart:convert';
import 'package:foresignal_api/src/deserialize.dart';
import 'package:dio/dio.dart';

import 'dart:typed_data';
import 'package:foresignal_api/src/model/problem.dart';

class MapsApi {

  final Dio _dio;

  const MapsApi(this._dio);

  /// Render a static map
  /// Basemap-free server-rendered PNG: Natural Earth land, water and boundaries, product frames, alert geometry, place marker and range rings, scale bar, frame valid times (UTC plus the place&#39;s local time), legend, attribution and an \&quot;Automated — not an official warning\&quot; footer. The box is &#x60;bbox&#x60;, else &#x60;center&#x60; with &#x60;radiusKm&#x60; (default 50 km), else the alert&#39;s record geometry, else the place; it is snapped outwards to 0.01° once and widened to the map area&#39;s aspect, and &#x60;Foresignal-Image-Bounds&#x60; gives the result. Missing or stale frames are drawn as a labelled note (\&quot;… unavailable — not evidence of clear weather\&quot;), never omitted; a concrete ISO time at which an enabled product has no frame is 404 UNKNOWN_FRAME. Nothing fetches third-party tiles (an operator&#39;s LAN tile server, &#x60;FORESIGNAL_MAPS_BASEMAP_URL_TEMPLATE&#x60;, may replace the Natural Earth fills). Immutable only when every frame is pinned: a concrete ISO &#x60;time&#x60;, complete current frames, a pinned run for forecasts, a versioned style for every product and no &#x60;place&#x60;; otherwise a short max-age with an ETag checked before rendering. Rendered on the render pool within 2 s, else 503 RENDER_BUSY.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [products] - Products to draw, in order (at most 4)
  /// * [center] - `lon,lat` (with `radiusKm`)
  /// * [radiusKm] - Radius around `center`, `place` or the alert (alert default 50 km)
  /// * [bbox] - minLon,minLat,maxLon,maxLat (WGS84)
  /// * [time] - `latest`, `now`, ISO-8601, or a relative offset (`-PT1H` picks observation frames, `+PT6H` the newest complete forecast run)
  /// * [run] - Forecast run: `latest`, `previous`, or the run's reference time (ISO-8601, e.g. `2026-10-10T12:00:00Z`). Required for a concrete forecast time (400 RUN_REQUIRED); only a pinned run is immutable
  /// * [width] - Width in px (≤ 1280, default 800)
  /// * [height] - Height in px (default width × 0.75)
  /// * [alert] - Alert id: draws and centers on the alert's record geometry (an alert the caller may not read is 404 NOT_FOUND)
  /// * [place] - Place id (036). Using `place` requires a token even while `openReads` is on (places are not anonymous). Unknown → 404 UNKNOWN_PLACE
  /// * [style] - Style per product, in `products` order
  /// * [theme] - Color theme (default light)
  /// * [units] - Unit system for values and labels
  /// * [ifNoneMatch] - Conditional GET; 304 when the ETag matches
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [Uint8List] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<Uint8List>> getStaticMap({ 
    List<String>? products,
    String? center,
    num? radiusKm,
    String? bbox,
    String? time = 'latest',
    String? run,
    int? width,
    int? height,
    int? alert,
    String? place,
    List<String>? style,
    String? theme,
    String? units,
    String? ifNoneMatch,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/maps/static';
    final _options = Options(
      method: r'GET',
      responseType: ResponseType.bytes,
      headers: <String, dynamic>{
        if (ifNoneMatch != null) r'If-None-Match': ifNoneMatch,
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
      if (products != null) r'products': products,
      if (center != null) r'center': center,
      if (radiusKm != null) r'radiusKm': radiusKm,
      if (bbox != null) r'bbox': bbox,
      if (time != null) r'time': time,
      if (run != null) r'run': run,
      if (width != null) r'width': width,
      if (height != null) r'height': height,
      if (alert != null) r'alert': alert,
      if (place != null) r'place': place,
      if (style != null) r'style': style,
      if (theme != null) r'theme': theme,
      if (units != null) r'units': units,
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

}
