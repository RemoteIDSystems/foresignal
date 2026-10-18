//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

import 'dart:async';

// ignore: unused_import
import 'dart:convert';
import 'package:foresignal_api/src/deserialize.dart';
import 'package:dio/dio.dart';

import 'dart:typed_data';
import 'package:foresignal_api/src/model/map_sprite_index_value.dart';
import 'package:foresignal_api/src/model/map_style.dart';
import 'package:foresignal_api/src/model/problem.dart';

class MapStylesApi {

  final Dio _dio;

  const MapStylesApi(this._dio);

  /// Get a single 32 px icon
  /// One 32 px PNG per sprite icon, used by KML &#x60;IconStyle&#x60; hrefs (bundled inside KMZ). Long-lived cache headers.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [name] - Icon name from the sprite catalogue (e.g. `airport`, `volcano`)
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [Uint8List] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<Uint8List>> getMapIcon({ 
    required String name,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/map/icons/{name}.png'.replaceAll('{' r'name' '}', name.toString());
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

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
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

  /// Get the SDF sprite sheet or index
  /// Content-addressed sprite generated from Maki and Temaki (CC0) with &#x60;spreet --sdf&#x60;: &#x60;foresignal-{sha8}.json|png&#x60; at 1x and &#x60;foresignal-{sha8}@2x.json|png&#x60;. Responses are &#x60;max-age&#x3D;31536000, immutable&#x60; (&#x60;public&#x60; or &#x60;private&#x60; per the openReads cache rule). An unknown &#x60;sha8&#x60; returns 404 with a &#x60;Link&#x60; header naming the current sprite.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [spriteFile] - Sprite file name: `foresignal-{sha8}[@2x].{json|png}`
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [Map<String, MapSpriteIndexValue>] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<Map<String, MapSpriteIndexValue>>> getMapSprite({ 
    required String spriteFile,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/map/sprites/{spriteFile}'.replaceAll('{' r'spriteFile' '}', spriteFile.toString());
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

    Map<String, MapSpriteIndexValue>? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<Map<String, MapSpriteIndexValue>, MapSpriteIndexValue>(rawData, 'Map<String, MapSpriteIndexValue>', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<Map<String, MapSpriteIndexValue>>(
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

  /// MapLibre overlay style for record layers
  /// Version-8 MapLibre style generated from each kind&#39;s &#x60;kindDisplay&#x60; hints: one vector source per requested layer (016 per-layer TileJSON, prefixed &#x60;/v1/k/{key}/&#x60; when the request came through a tile key), symbol/circle/line/fill layers per kind, hex-aggregation sources below &#x60;aggBelowZoom&#x60;, and &#x60;sprite&#x60; pointing at the content-addressed sprite. Overlay only: no &#x60;glyphs&#x60;, no text layers, no basemap. Private sources are omitted unless the caller passes the private gate. Strong ETag, &#x60;max-age&#x3D;300&#x60;; &#x60;If-None-Match&#x60; → 304. Generated URLs use &#x60;publicBaseUrl&#x60;.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [layers] - Comma-separated tile layer ids (016); default every source layer the caller may read (kind layers repeat the same records, so they are included only when named). A layer the caller cannot see, or that does not exist, is left out.
  /// * [theme] - Colour theme for line and fill defaults
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [MapStyle] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<MapStyle>> getMapStyle({ 
    String? layers,
    String? theme,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/map/style.json';
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
      if (theme != null) r'theme': theme,
    };

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    MapStyle? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<MapStyle, MapStyle>(rawData, 'MapStyle', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<MapStyle>(
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
