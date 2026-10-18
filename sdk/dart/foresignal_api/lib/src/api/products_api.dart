//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

import 'dart:async';

// ignore: unused_import
import 'dart:convert';
import 'package:foresignal_api/src/deserialize.dart';
import 'package:dio/dio.dart';

import 'dart:typed_data';
import 'package:foresignal_api/src/model/field_manifest.dart';
import 'package:foresignal_api/src/model/legend.dart';
import 'package:foresignal_api/src/model/problem.dart';
import 'package:foresignal_api/src/model/product.dart';
import 'package:foresignal_api/src/model/product_loop.dart';
import 'package:foresignal_api/src/model/product_page.dart';
import 'package:foresignal_api/src/model/product_timeline.dart';
import 'package:foresignal_api/src/model/raster_frame.dart';
import 'package:foresignal_api/src/model/raster_frame_page.dart';
import 'package:foresignal_api/src/model/style.dart';
import 'package:foresignal_api/src/model/style_fragment.dart';
import 'package:foresignal_api/src/model/style_page.dart';
import 'package:foresignal_api/src/model/tile_json.dart';
import 'package:foresignal_api/src/model/velocity_json_component.dart';

class ProductsApi {

  final Dio _dio;

  const ProductsApi(this._dio);

  /// Get the latest frame
  /// The newest complete observed frame, or for forecasts the newest complete run&#39;s step nearest now. Short max-age (&#x60;min(cadence/2, 60s)&#x60;) with an ETag; &#x60;public&#x60; only when &#x60;openReads&#x60; covers the path. 404 UNKNOWN_FRAME when no frame exists yet.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [productId] - Product id (kebab-case; `timeline` and `loop` are reserved)
  /// * [run] - Forecast run: `latest`, `previous`, or the run's reference time (ISO-8601, e.g. `2026-10-10T12:00:00Z`). Required for a concrete forecast time (400 RUN_REQUIRED); only a pinned run is immutable
  /// * [includePartial] - Include `partial` frames (excluded from aliases and lists by default)
  /// * [ifNoneMatch] - Conditional GET; 304 when the ETag matches
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [RasterFrame] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<RasterFrame>> getLatestProductFrame({ 
    required String productId,
    String? run,
    bool? includePartial = false,
    String? ifNoneMatch,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/products/{productId}/frames/latest'.replaceAll('{' r'productId' '}', productId.toString());
    final _options = Options(
      method: r'GET',
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
      if (run != null) r'run': run,
      if (includePartial != null) r'includePartial': includePartial,
    };

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    RasterFrame? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<RasterFrame, RasterFrame>(rawData, 'RasterFrame', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<RasterFrame>(
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

  /// Get a product
  /// One product with every catalogue field. Conditional GET via ETag. 404 UNKNOWN_PRODUCT.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [productId] - Product id (kebab-case; `timeline` and `loop` are reserved)
  /// * [ifNoneMatch] - Conditional GET; 304 when the ETag matches
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [Product] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<Product>> getProduct({ 
    required String productId,
    String? ifNoneMatch,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/products/{productId}'.replaceAll('{' r'productId' '}', productId.toString());
    final _options = Options(
      method: r'GET',
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

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    Product? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<Product, Product>(rawData, 'Product', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<Product>(
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

  /// Data tile carrying physical values
  /// Terrain-RGB style: v &#x3D; (R·65536 + G·256 + B) · scale + offset, no-data alpha 0, nearest resampling only. &#x60;terrarium&#x60; only for products whose range fits it.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [productId] - Product id
  /// * [frameKey] - Frame key `{timeKey}~{sha8}` or an alias (latest, now)
  /// * [z] - Zoom
  /// * [x] - Tile column
  /// * [y] - Tile row
  /// * [encoding] - Value encoding
  /// * [tms] - Tile matrix set
  /// * [run] - Forecast run: latest | previous | <runIso> (required for concrete forecast times)
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [Uint8List] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<Uint8List>> getProductDataTile({ 
    required String productId,
    required String frameKey,
    required int z,
    required int x,
    required int y,
    String? encoding,
    String? tms,
    String? run,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/products/{productId}/data/{frameKey}/{z}/{x}/{y}.png'.replaceAll('{' r'productId' '}', productId.toString()).replaceAll('{' r'frameKey' '}', frameKey.toString()).replaceAll('{' r'z' '}', z.toString()).replaceAll('{' r'x' '}', x.toString()).replaceAll('{' r'y' '}', y.toString());
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
      if (encoding != null) r'encoding': encoding,
      if (tms != null) r'tms': tms,
      if (run != null) r'run': run,
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

  /// Download wind field data
  /// Planar little-endian Float32, u then v, rows north→south, longitudes 0..359 (1°: 360×181, 521,280 bytes raw; 0.25°: 1440×721, 8,305,920 bytes raw), written once at ingest and served precompressed with &#x60;Content-Encoding: gzip&#x60;. 406 NOT_ACCEPTABLE only when &#x60;Accept-Encoding&#x60; refuses gzip (&#x60;gzip;q&#x3D;0&#x60;, or only &#x60;identity&#x60;); no header accepts it. &#x60;bbox&#x60; is snapped outward to grid points, wraps across 0°, and is cut on the fly from the file of the requested resolution (cached in the tile cache). The ETag is derived from the raw data&#39;s sha256.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [productId] - Product id (kebab-case; `timeline` and `loop` are reserved)
  /// * [frameKey] - Frame key: content-addressed `{timeKey}~{sha8}` (immutable), bare `timeKey` (e.g. `20261010T143240Z`), or alias `latest` / `now`. An ISO-extended time is redirected (301) to its `timeKey`
  /// * [run] - Forecast run: `latest`, `previous`, or the run's reference time (ISO-8601, e.g. `2026-10-10T12:00:00Z`). Required for a concrete forecast time (400 RUN_REQUIRED); only a pinned run is immutable
  /// * [res] - Grid resolution in degrees
  /// * [bbox] - Subset (recommended for 0.25°); snapped outward to grid cells, wraps across 0°
  /// * [acceptEncoding] - Must allow `gzip`
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
  Future<Response<Uint8List>> getProductFieldData({ 
    required String productId,
    required String frameKey,
    String? run,
    String? res,
    String? bbox,
    String? acceptEncoding,
    String? ifNoneMatch,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/products/{productId}/fields/{frameKey}.f32'.replaceAll('{' r'productId' '}', productId.toString()).replaceAll('{' r'frameKey' '}', frameKey.toString());
    final _options = Options(
      method: r'GET',
      responseType: ResponseType.bytes,
      headers: <String, dynamic>{
        if (acceptEncoding != null) r'Accept-Encoding': acceptEncoding,
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
      if (run != null) r'run': run,
      if (res != null) r'res': res,
      if (bbox != null) r'bbox': bbox,
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

  /// Wind field as int16
  /// Planar little-endian int16 u, v with &#x60;scale&#x60;/&#x60;offset&#x60; in the field manifest; precompressed like &#x60;.f32&#x60;.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [productId] - Product id
  /// * [frameKey] - Frame key `{timeKey}~{sha8}` or an alias (latest, now)
  /// * [run] - Forecast run: latest | previous | <runIso> (required for concrete forecast times)
  /// * [res] - Grid resolution in degrees
  /// * [bbox] - minLon,minLat,maxLon,maxLat (WGS84)
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [Uint8List] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<Uint8List>> getProductFieldI16({ 
    required String productId,
    required String frameKey,
    String? run,
    num? res,
    String? bbox,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/products/{productId}/fields/{frameKey}.i16'.replaceAll('{' r'productId' '}', productId.toString()).replaceAll('{' r'frameKey' '}', frameKey.toString());
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
      if (run != null) r'run': run,
      if (res != null) r'res': res,
      if (bbox != null) r'bbox': bbox,
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

  /// Get a wind field manifest
  /// GEV &#x60;schemaVersion: 1&#x60; manifest for the planar u/v Float32 field (&#x60;gfs&#x60; only in v1: a &#x60;kind&#x3D;field&#x60; product with two bands and global coverage), with cycle, grid, component offsets, byte length, sha256 of the raw (uncompressed) data, attribution and a content-addressed &#x60;dataUrl&#x60; (&#x60;…/fields/{frameKey}.f32?run&#x3D;&lt;runIso&gt;&amp;res&#x3D;&lt;res&gt;[&amp;bbox&#x3D;&lt;snapped&gt;]&#x60;; the run is always pinned). &#x60;stale&#x60; is true when the frame&#39;s run is not the newest complete run; a disabled product answers 503 rather than an &#x60;unavailable&#x60; manifest. With &#x60;bbox&#x60;, &#x60;grid&#x60; describes the snapped subset (&#x60;lo1&#x60; in 0..360, so &#x60;nx·dx&#x60; is less than 360). A concrete forecast time without &#x60;run&#x60; → 400 RUN_REQUIRED.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [productId] - Product id (kebab-case; `timeline` and `loop` are reserved)
  /// * [frameKey] - Frame key: content-addressed `{timeKey}~{sha8}` (immutable), bare `timeKey` (e.g. `20261010T143240Z`), or alias `latest` / `now`. An ISO-extended time is redirected (301) to its `timeKey`
  /// * [run] - Forecast run: `latest`, `previous`, or the run's reference time (ISO-8601, e.g. `2026-10-10T12:00:00Z`). Required for a concrete forecast time (400 RUN_REQUIRED); only a pinned run is immutable
  /// * [res] - Grid resolution in degrees
  /// * [bbox] - Subset (recommended for 0.25°); snapped outward to grid cells, wraps across 0°
  /// * [ifNoneMatch] - Conditional GET; 304 when the ETag matches
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [FieldManifest] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<FieldManifest>> getProductFieldManifest({ 
    required String productId,
    required String frameKey,
    String? run,
    String? res,
    String? bbox,
    String? ifNoneMatch,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/products/{productId}/fields/{frameKey}.json'.replaceAll('{' r'productId' '}', productId.toString()).replaceAll('{' r'frameKey' '}', frameKey.toString());
    final _options = Options(
      method: r'GET',
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
      if (run != null) r'run': run,
      if (res != null) r'res': res,
      if (bbox != null) r'bbox': bbox,
    };

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    FieldManifest? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<FieldManifest, FieldManifest>(rawData, 'FieldManifest', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<FieldManifest>(
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

  /// Wind field as an RGB PNG
  /// R &#x3D; u and G &#x3D; v scaled linearly over the manifest&#39;s [uMin, uMax] and [vMin, vMax]; B &#x3D; 0. The format WebGL particle layers expect.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [productId] - Product id
  /// * [frameKey] - Frame key `{timeKey}~{sha8}` or an alias (latest, now)
  /// * [run] - Forecast run: latest | previous | <runIso> (required for concrete forecast times)
  /// * [res] - Grid resolution in degrees
  /// * [bbox] - minLon,minLat,maxLon,maxLat (WGS84)
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [String] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<String>> getProductFieldPngUv({ 
    required String productId,
    required String frameKey,
    String? run,
    num? res,
    String? bbox,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/products/{productId}/fields/{frameKey}.png-uv'.replaceAll('{' r'productId' '}', productId.toString()).replaceAll('{' r'frameKey' '}', frameKey.toString());
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
      if (run != null) r'run': run,
      if (res != null) r'res': res,
      if (bbox != null) r'bbox': bbox,
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

  /// Tiled png-uv wind field
  /// png-uv encoding per tile (R &#x3D; u, G &#x3D; v) with min/max in the field manifest.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [productId] - Product id
  /// * [frameKey] - Frame key `{timeKey}~{sha8}` or an alias (latest, now)
  /// * [z] - Zoom
  /// * [x] - Tile column
  /// * [y] - Tile row
  /// * [tms] - Tile matrix set
  /// * [run] - Forecast run: latest | previous | <runIso> (required for concrete forecast times)
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [Uint8List] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<Uint8List>> getProductFieldTile({ 
    required String productId,
    required String frameKey,
    required int z,
    required int x,
    required int y,
    String? tms,
    String? run,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/products/{productId}/fields/{frameKey}/{z}/{x}/{y}.png'.replaceAll('{' r'productId' '}', productId.toString()).replaceAll('{' r'frameKey' '}', frameKey.toString()).replaceAll('{' r'z' '}', z.toString()).replaceAll('{' r'x' '}', x.toString()).replaceAll('{' r'y' '}', y.toString());
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
      if (tms != null) r'tms': tms,
      if (run != null) r'run': run,
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

  /// Wind field as velocity JSON
  /// leaflet-velocity / wind-js format: [U, V]. Requires &#x60;res&#x3D;1&#x60; or a bbox (≤ 300k points per component, else 400 FIELD_TOO_LARGE). Gzip; cached like &#x60;.f32&#x60;.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [productId] - Product id
  /// * [frameKey] - Frame key `{timeKey}~{sha8}` or an alias (latest, now)
  /// * [run] - Forecast run: latest | previous | <runIso> (required for concrete forecast times)
  /// * [res] - Grid resolution in degrees
  /// * [bbox] - minLon,minLat,maxLon,maxLat (WGS84)
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [List<VelocityJsonComponent>] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<List<VelocityJsonComponent>>> getProductFieldVelocityJson({ 
    required String productId,
    required String frameKey,
    String? run,
    num? res,
    String? bbox,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/products/{productId}/fields/{frameKey}.velocity-json'.replaceAll('{' r'productId' '}', productId.toString()).replaceAll('{' r'frameKey' '}', frameKey.toString());
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
      if (run != null) r'run': run,
      if (res != null) r'res': res,
      if (bbox != null) r'bbox': bbox,
    };

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    List<VelocityJsonComponent>? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<List<VelocityJsonComponent>, VelocityJsonComponent>(rawData, 'List<VelocityJsonComponent>', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<List<VelocityJsonComponent>>(
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

  /// Render a single product image
  /// Renders one frame for a bbox, up to 2048 px a side, in EPSG:3857 or EPSG:4326, and returns the snapped bounds in &#x60;Foresignal-Image-Bounds&#x60;. Follows the same cache rule as tiles.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [productId] - Product id (kebab-case; `timeline` and `loop` are reserved)
  /// * [frameKey] - Frame key: content-addressed `{timeKey}~{sha8}` (immutable), bare `timeKey` (e.g. `20261010T143240Z`), or alias `latest` / `now`. An ISO-extended time is redirected (301) to its `timeKey`
  /// * [format] - Image format
  /// * [bbox] - minLon,minLat,maxLon,maxLat (WGS84)
  /// * [width] - Width in px
  /// * [height] - Height in px (default keeps the bbox aspect)
  /// * [crs] - Output projection
  /// * [style] - Registered style, `name@v` (content-addressed) or `name` (resolves to the current version; not immutable). Default: the product's default style. Unknown → 400 UNKNOWN_STYLE
  /// * [run] - Forecast run: `latest`, `previous`, or the run's reference time (ISO-8601, e.g. `2026-10-10T12:00:00Z`). Required for a concrete forecast time (400 RUN_REQUIRED); only a pinned run is immutable
  /// * [prefetch] - `1` marks a prefetch request (lowest render priority) without the `Foresignal-Prefetch` header, avoiding a CORS preflight
  /// * [foresignalPrefetch] - `1` marks a prefetch request (lowest render priority)
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
  Future<Response<Uint8List>> getProductImage({ 
    required String productId,
    required String frameKey,
    required String format,
    required String bbox,
    int? width,
    int? height,
    String? crs,
    String? style,
    String? run,
    int? prefetch,
    String? foresignalPrefetch,
    String? ifNoneMatch,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/products/{productId}/image/{frameKey}.{format}'.replaceAll('{' r'productId' '}', productId.toString()).replaceAll('{' r'frameKey' '}', frameKey.toString()).replaceAll('{' r'format' '}', format.toString());
    final _options = Options(
      method: r'GET',
      responseType: ResponseType.bytes,
      headers: <String, dynamic>{
        if (foresignalPrefetch != null) r'Foresignal-Prefetch': foresignalPrefetch,
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
      r'bbox': bbox,
      if (width != null) r'width': width,
      if (height != null) r'height': height,
      if (crs != null) r'crs': crs,
      if (style != null) r'style': style,
      if (run != null) r'run': run,
      if (prefetch != null) r'prefetch': prefetch,
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

  /// Get a product legend (JSON)
  /// Legend for native clients: stops, ticks, categories, specials and plain-language meanings (e.g. \&quot;≥ 50 dBZ: heavy rain, hail possible\&quot;), converted to the requested unit system (mm/h↔in/h, K→°C/°F, m/s↔kt/mph, mm↔in).  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [productId] - Product id (kebab-case; `timeline` and `loop` are reserved)
  /// * [style] - Registered style, `name@v` (content-addressed) or `name` (resolves to the current version; not immutable). Default: the product's default style. Unknown → 400 UNKNOWN_STYLE
  /// * [units] - Unit system for values and labels
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [Legend] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<Legend>> getProductLegend({ 
    required String productId,
    String? style,
    String? units,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/products/{productId}/legend'.replaceAll('{' r'productId' '}', productId.toString());
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
      if (style != null) r'style': style,
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

    Legend? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<Legend, Legend>(rawData, 'Legend', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<Legend>(
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

  /// Get a product legend (PNG)
  /// Legend rendered with Java2D (JDK &#x60;SansSerif&#x60;), horizontal or vertical, for dark or light themes.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [productId] - Product id (kebab-case; `timeline` and `loop` are reserved)
  /// * [style] - Registered style, `name@v` (content-addressed) or `name` (resolves to the current version; not immutable). Default: the product's default style. Unknown → 400 UNKNOWN_STYLE
  /// * [units] - Unit system for values and labels
  /// * [width] - Width in px
  /// * [orientation] - Layout
  /// * [theme] - Colour theme
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [Uint8List] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<Uint8List>> getProductLegendPng({ 
    required String productId,
    String? style,
    String? units,
    int? width,
    String? orientation,
    String? theme,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/products/{productId}/legend.png'.replaceAll('{' r'productId' '}', productId.toString());
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
      if (style != null) r'style': style,
      if (units != null) r'units': units,
      if (width != null) r'width': width,
      if (orientation != null) r'orientation': orientation,
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

  /// Build an animation loop
  /// Snaps the bbox once and returns corner coordinates plus up to 24 frame URLs pointing at &#x60;/image&#x60;, for mobile and animation clients. &#x60;products&#x60; lists one or two products, or &#x60;past&#x60; and &#x60;future&#x60; name an observed and a forecast product for a seamless loop; forecast runs are never mixed. &#x60;place&#x60; resolves through places (404 UNKNOWN_PLACE).  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [products] - Products (one or two); alternative to `past`/`future`
  /// * [past] - Observed product for the past part of a seamless loop
  /// * [future] - Forecast product for the future part of a seamless loop
  /// * [bbox] - minLon,minLat,maxLon,maxLat (WGS84)
  /// * [place] - Place id (036). Using `place` requires a token even while `openReads` is on (places are not anonymous). Unknown → 404 UNKNOWN_PLACE
  /// * [radiusKm] - Radius around `place`
  /// * [frames] - Number of frames (≤ 24)
  /// * [from] - Window start
  /// * [to] - Window end
  /// * [width] - Image width in px (≤ 1024)
  /// * [crs] - Image projection
  /// * [style] - Registered style, `name@v` (content-addressed) or `name` (resolves to the current version; not immutable). Default: the product's default style. Unknown → 400 UNKNOWN_STYLE
  /// * [run] - Forecast run: `latest`, `previous`, or the run's reference time (ISO-8601, e.g. `2026-10-10T12:00:00Z`). Required for a concrete forecast time (400 RUN_REQUIRED); only a pinned run is immutable
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [ProductLoop] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<ProductLoop>> getProductLoop({ 
    List<String>? products,
    String? past,
    String? future,
    String? bbox,
    String? place,
    num? radiusKm,
    int? frames,
    DateTime? from,
    DateTime? to,
    int? width,
    String? crs,
    String? style,
    String? run,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/products/loop';
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
      if (products != null) r'products': products,
      if (past != null) r'past': past,
      if (future != null) r'future': future,
      if (bbox != null) r'bbox': bbox,
      if (place != null) r'place': place,
      if (radiusKm != null) r'radiusKm': radiusKm,
      if (frames != null) r'frames': frames,
      if (from != null) r'from': from,
      if (to != null) r'to': to,
      if (width != null) r'width': width,
      if (crs != null) r'crs': crs,
      if (style != null) r'style': style,
      if (run != null) r'run': run,
    };

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    ProductLoop? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<ProductLoop, ProductLoop>(rawData, 'ProductLoop', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<ProductLoop>(
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

  /// Animated GIF loop
  /// Takes the &#x60;/loop&#x60; parameters; palette from the style LUT; rendered at prefetch priority (503 RENDER_BUSY applies). Cached only for content-addressed frame sets.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [products] - Product ids (comma-separated) or `past`
  /// * [past] - Observed product for the seamless past
  /// * [future] - Forecast product for the seamless future
  /// * [bbox] - minLon,minLat,maxLon,maxLat (WGS84)
  /// * [place] - Place id
  /// * [radiusKm] - Radius around the place
  /// * [frames] - Frame count
  /// * [from] - Start
  /// * [to] - End
  /// * [width] - Pixels
  /// * [crs] - CRS
  /// * [style] - Versioned style `name@v` (400 UNKNOWN_STYLE)
  /// * [delay] - Frame delay in ms
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [Uint8List] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<Uint8List>> getProductLoopGif({ 
    List<String>? products,
    String? past,
    String? future,
    String? bbox,
    String? place,
    num? radiusKm,
    int? frames = 12,
    DateTime? from,
    DateTime? to,
    int? width,
    String? crs,
    String? style,
    int? delay = 500,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/products/loop.gif';
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
      if (products != null) r'products': products,
      if (past != null) r'past': past,
      if (future != null) r'future': future,
      if (bbox != null) r'bbox': bbox,
      if (place != null) r'place': place,
      if (radiusKm != null) r'radiusKm': radiusKm,
      if (frames != null) r'frames': frames,
      if (from != null) r'from': from,
      if (to != null) r'to': to,
      if (width != null) r'width': width,
      if (crs != null) r'crs': crs,
      if (style != null) r'style': style,
      if (delay != null) r'delay': delay,
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

  /// ATAK MOBAC custom map source
  /// &#x60;&lt;customMapSource&gt;&#x60; with native zooms, tileType png, tileUpdate None and a &#x60;{$z}/{$x}/{$y}&#x60; template on the &#x60;latest&#x60; alias with the caller&#39;s key prefix. Meant to be fetched through a tile-key path.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [productId] - Product id
  /// * [style] - Versioned style `name@v` (400 UNKNOWN_STYLE)
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [String] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<String>> getProductMobacXml({ 
    required String productId,
    String? style,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/products/{productId}/mobac.xml'.replaceAll('{' r'productId' '}', productId.toString());
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
      if (style != null) r'style': style,
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

  /// MapLibre style fragment for a product
  /// &#x60;{version: 8, sources, layers}&#x60; with concrete content-addressed URLs, zooms, bounds, attribution and legend links. The server never fetches a basemap style.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [productId] - Product id
  /// * [frameKey] - Frame key or alias (resolved when generated)
  /// * [style] - Versioned style `name@v` (400 UNKNOWN_STYLE)
  /// * [tms] - Tile matrix set
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [StyleFragment] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<StyleFragment>> getProductStyleFragment({ 
    required String productId,
    String? frameKey,
    String? style,
    String? tms,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/products/{productId}/style-fragment.json'.replaceAll('{' r'productId' '}', productId.toString());
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
      if (frameKey != null) r'frameKey': frameKey,
      if (style != null) r'style': style,
      if (tms != null) r'tms': tms,
    };

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    StyleFragment? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<StyleFragment, StyleFragment>(rawData, 'StyleFragment', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<StyleFragment>(
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

  /// Get a raster product tile
  /// WebMercatorQuad PNG-8 tile rendered on the fly from the native-grid frame (256 px, or 512 px for &#x60;{y}@2x&#x60;). Content-addressed URLs (&#x60;~sha8&#x60;, &#x60;name@v&#x60; style, pinned &#x60;run&#x60;, complete frame) are immutable; anything else gets a short max-age, ETag and &#x60;Foresignal-Frame-Time&#x60;. Empty or out-of-bounds tiles return one shared transparent PNG (fixed ETag) unless &#x60;empty&#x3D;204&#x60;; above native max zoom or below min zoom → 400 TILE_OUT_OF_RANGE; external products → 400 VALIDATION_FAILED; render queue overload → 503 RENDER_BUSY with &#x60;Retry-After: 1&#x60;.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [productId] - Product id (kebab-case; `timeline` and `loop` are reserved)
  /// * [frameKey] - Frame key: content-addressed `{timeKey}~{sha8}` (immutable), bare `timeKey` (e.g. `20261010T143240Z`), or alias `latest` / `now`. An ISO-extended time is redirected (301) to its `timeKey`
  /// * [z] - Zoom (WebMercatorQuad)
  /// * [x] - Tile column
  /// * [y] - Tile row, optionally suffixed `@2x` for a 512 px tile covering the same extent
  /// * [style] - Registered style, `name@v` (content-addressed) or `name` (resolves to the current version; not immutable). Default: the product's default style. Unknown → 400 UNKNOWN_STYLE
  /// * [run] - Forecast run: `latest`, `previous`, or the run's reference time (ISO-8601, e.g. `2026-10-10T12:00:00Z`). Required for a concrete forecast time (400 RUN_REQUIRED); only a pinned run is immutable
  /// * [empty] - `204` returns 204 instead of the shared transparent tile
  /// * [onExpired] - Serve the transparent tile with `Foresignal-Frame-Expired: true` instead of 410
  /// * [prefetch] - `1` marks a prefetch request (lowest render priority) without the `Foresignal-Prefetch` header, avoiding a CORS preflight
  /// * [foresignalPrefetch] - `1` marks a prefetch request (lowest render priority)
  /// * [ifNoneMatch] - Conditional GET; 304 when the ETag matches
  /// * [tileSize] - 512 renders natively at 512 px (z is one less than 256 for the same detail); distinct from @2x
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [Uint8List] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<Uint8List>> getProductTile({ 
    required String productId,
    required String frameKey,
    required int z,
    required int x,
    required String y,
    String? style,
    String? run,
    String? empty,
    String? onExpired,
    int? prefetch,
    String? foresignalPrefetch,
    String? ifNoneMatch,
    int? tileSize,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/products/{productId}/tiles/{frameKey}/{z}/{x}/{y}.png'.replaceAll('{' r'productId' '}', productId.toString()).replaceAll('{' r'frameKey' '}', frameKey.toString()).replaceAll('{' r'z' '}', z.toString()).replaceAll('{' r'x' '}', x.toString()).replaceAll('{' r'y' '}', y.toString());
    final _options = Options(
      method: r'GET',
      responseType: ResponseType.bytes,
      headers: <String, dynamic>{
        if (foresignalPrefetch != null) r'Foresignal-Prefetch': foresignalPrefetch,
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
      if (style != null) r'style': style,
      if (run != null) r'run': run,
      if (empty != null) r'empty': empty,
      if (onExpired != null) r'onExpired': onExpired,
      if (prefetch != null) r'prefetch': prefetch,
      if (tileSize != null) r'tileSize': tileSize,
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

  /// Get TileJSON for a product frame
  /// TileJSON 3.0.0 with a concrete content-addressed template (aliases resolved at generation), &#x60;bounds&#x60;, native &#x60;minzoom&#x60;/&#x60;maxzoom&#x60;, &#x60;center&#x60;, &#x60;tileSize: 256&#x60;, the &#x60;@2x&#x60; variant and attribution text. The document itself has a short max-age.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [productId] - Product id (kebab-case; `timeline` and `loop` are reserved)
  /// * [frameKey] - Frame key or alias (`latest`, `now`)
  /// * [style] - Registered style, `name@v` (content-addressed) or `name` (resolves to the current version; not immutable). Default: the product's default style. Unknown → 400 UNKNOWN_STYLE
  /// * [run] - Forecast run: `latest`, `previous`, or the run's reference time (ISO-8601, e.g. `2026-10-10T12:00:00Z`). Required for a concrete forecast time (400 RUN_REQUIRED); only a pinned run is immutable
  /// * [includePartial] - Include `partial` frames (excluded from aliases and lists by default)
  /// * [ifNoneMatch] - Conditional GET; 304 when the ETag matches
  /// * [tms] - Tile matrix set
  /// * [tileSize] - 512 renders natively at 512 px (z is one less than 256 for the same detail); distinct from @2x
  /// * [encoding] - Return TileJSON for data tiles (`…/data/{frameKey}/{z}/{x}/{y}.png`) with raster-dem factors
  /// * [format] - Tile format in the template
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [TileJson] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<TileJson>> getProductTileJson({ 
    required String productId,
    String? frameKey = 'latest',
    String? style,
    String? run,
    bool? includePartial = false,
    String? ifNoneMatch,
    String? tms,
    int? tileSize,
    String? encoding,
    String? format,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/products/{productId}/tilejson'.replaceAll('{' r'productId' '}', productId.toString());
    final _options = Options(
      method: r'GET',
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
      if (frameKey != null) r'frameKey': frameKey,
      if (style != null) r'style': style,
      if (run != null) r'run': run,
      if (includePartial != null) r'includePartial': includePartial,
      if (tms != null) r'tms': tms,
      if (tileSize != null) r'tileSize': tileSize,
      if (encoding != null) r'encoding': encoding,
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

  /// Product tile in a tile matrix set
  /// Renders a tile in WebMercatorQuad or WorldCRS84Quad (EPSG:4326, 2×1 tiles at z0) as PNG or WebP. Same caching as tiles: content-addressed URLs are immutable.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [productId] - Product id
  /// * [frameKey] - Frame key `{timeKey}~{sha8}` or an alias (latest, now)
  /// * [tileMatrixSetId] - Tile matrix set (OGC 17-083r4)
  /// * [z] - Zoom
  /// * [x] - Tile column
  /// * [y] - Tile row, optionally suffixed `@2x` for a 512 px tile covering the same extent
  /// * [format] - Image format
  /// * [style] - Versioned style `name@v` (400 UNKNOWN_STYLE)
  /// * [run] - Forecast run: latest | previous | <runIso> (required for concrete forecast times)
  /// * [tileSize] - 512 renders natively at 512 px (z is one less than 256 for the same detail); distinct from @2x
  /// * [empty] - `204` returns 204 for empty tiles instead of the shared transparent PNG
  /// * [onExpired] - `transparent` serves the transparent tile for an expired frame with `Foresignal-Frame-Expired: true`
  /// * [prefetch] - `1` marks a prefetch request (lowest render priority)
  /// * [foresignalPrefetch] - `1` marks a prefetch request
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
  Future<Response<Uint8List>> getProductTileTms({ 
    required String productId,
    required String frameKey,
    required String tileMatrixSetId,
    required int z,
    required int x,
    required String y,
    required String format,
    String? style,
    String? run,
    int? tileSize,
    String? empty,
    String? onExpired,
    int? prefetch,
    String? foresignalPrefetch,
    String? ifNoneMatch,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/products/{productId}/tiles/{frameKey}/{tileMatrixSetId}/{z}/{x}/{y}.{format}'.replaceAll('{' r'productId' '}', productId.toString()).replaceAll('{' r'frameKey' '}', frameKey.toString()).replaceAll('{' r'tileMatrixSetId' '}', tileMatrixSetId.toString()).replaceAll('{' r'z' '}', z.toString()).replaceAll('{' r'x' '}', x.toString()).replaceAll('{' r'y' '}', y.toString()).replaceAll('{' r'format' '}', format.toString());
    final _options = Options(
      method: r'GET',
      responseType: ResponseType.bytes,
      headers: <String, dynamic>{
        if (foresignalPrefetch != null) r'Foresignal-Prefetch': foresignalPrefetch,
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
      if (style != null) r'style': style,
      if (run != null) r'run': run,
      if (tileSize != null) r'tileSize': tileSize,
      if (empty != null) r'empty': empty,
      if (onExpired != null) r'onExpired': onExpired,
      if (prefetch != null) r'prefetch': prefetch,
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

  /// Product tile as WebP
  /// WebP variant of the WebMercatorQuad tile: lossless for categorical and indexed styles, quality 80 for rgb imagery. 406 FORMAT_UNAVAILABLE when the startup WebP check failed.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [productId] - Product id
  /// * [frameKey] - Frame key `{timeKey}~{sha8}` or an alias (latest, now)
  /// * [z] - Zoom
  /// * [x] - Tile column
  /// * [y] - Tile row, optionally suffixed `@2x` for a 512 px tile covering the same extent
  /// * [style] - Versioned style `name@v` (400 UNKNOWN_STYLE)
  /// * [run] - Forecast run: latest | previous | <runIso> (required for concrete forecast times)
  /// * [tileSize] - 512 renders natively at 512 px (z is one less than 256 for the same detail); distinct from @2x
  /// * [empty] - `204` returns 204 for empty tiles instead of the shared transparent PNG
  /// * [onExpired] - `transparent` serves the transparent tile for an expired frame with `Foresignal-Frame-Expired: true`
  /// * [prefetch] - `1` marks a prefetch request (lowest render priority)
  /// * [foresignalPrefetch] - `1` marks a prefetch request
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
  Future<Response<Uint8List>> getProductTileWebp({ 
    required String productId,
    required String frameKey,
    required int z,
    required int x,
    required String y,
    String? style,
    String? run,
    int? tileSize,
    String? empty,
    String? onExpired,
    int? prefetch,
    String? foresignalPrefetch,
    String? ifNoneMatch,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/products/{productId}/tiles/{frameKey}/{z}/{x}/{y}.webp'.replaceAll('{' r'productId' '}', productId.toString()).replaceAll('{' r'frameKey' '}', frameKey.toString()).replaceAll('{' r'z' '}', z.toString()).replaceAll('{' r'x' '}', x.toString()).replaceAll('{' r'y' '}', y.toString());
    final _options = Options(
      method: r'GET',
      responseType: ResponseType.bytes,
      headers: <String, dynamic>{
        if (foresignalPrefetch != null) r'Foresignal-Prefetch': foresignalPrefetch,
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
      if (style != null) r'style': style,
      if (run != null) r'run': run,
      if (tileSize != null) r'tileSize': tileSize,
      if (empty != null) r'empty': empty,
      if (onExpired != null) r'onExpired': onExpired,
      if (prefetch != null) r'prefetch': prefetch,
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

  /// Get the cross-product timeline
  /// Ticks marked &#x60;observed|forecast&#x60;: the union of the products&#39; frame valid times in the window, with the frame chosen per product per tick (at or before the tick with the smallest gap, honoring &#x60;maxGap&#x60;; a product with none is absent at that tick), the single run used per forecast product (its newest complete run, never mixed; &#x60;runs&#x60;), &#x60;nowIndex&#x60; (the tick nearest now) and expanded, content-addressed tile templates. &#x60;mode&#x3D;seamless&#x60; joins a &#x60;past&#x60; observed product (ticks up to now) to a &#x60;future&#x60; forecast product (ticks after now). External catalog entries get synthesized ticks every &#x60;timeDimension.period&#x60; up to &#x60;now − defaultLag&#x60;, with &#x60;availability: unverified&#x60; and the external template&#39;s &#x60;{time}&#x60; filled. Default window: now − 6 h to now (+ 48 h when a forecast product is named); seamless now − 3 h to now + 18 h. At most 10 products. More than 500 ticks → 400 VALIDATION_FAILED.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [products] - Products (comma-separated or repeated); omit with `mode=seamless`
  /// * [from] - Window start
  /// * [to] - Window end
  /// * [mode] - Seamless observed→forecast timeline
  /// * [past] - Observed product (seamless mode)
  /// * [future] - Forecast product (seamless mode)
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [ProductTimeline] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<ProductTimeline>> getProductTimeline({ 
    List<String>? products,
    DateTime? from,
    DateTime? to,
    String? mode,
    String? past,
    String? future,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/products/timeline';
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
      if (products != null) r'products': products,
      if (from != null) r'from': from,
      if (to != null) r'to': to,
      if (mode != null) r'mode': mode,
      if (past != null) r'past': past,
      if (future != null) r'future': future,
    };

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    ProductTimeline? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<ProductTimeline, ProductTimeline>(rawData, 'ProductTimeline', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<ProductTimeline>(
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

  /// Get a wind barb vector tile
  /// Mapbox Vector Tile (layer &#x60;wind&#x60;, extent 4096) of wind points for the u/v products (&#x60;gfs&#x60;, &#x60;hrrr&#x60;): the frame sampled bilinearly on a 32 px lattice inside the 256 px tile (8×8 points at pixel centers, so the on-screen density is the same at every zoom), each with &#x60;speedMs&#x60; (m/s, 0.1), &#x60;fromDeg&#x60; (the direction the wind blows from, &#x60;(270 − atan2(v,u)·180/π) mod 360&#x60;, integer) and &#x60;barbKt&#x60; (knots rounded to a 5-kt bucket). HRRR&#39;s grid-relative winds are rotated to earth-relative at ingest. Built by PostGIS &#x60;ST_AsMVT&#x60;; an empty tile is an empty body. Cached and render-queued like raster tiles, with the same cache rule; zooms above the native max zoom are 400 TILE_OUT_OF_RANGE (clients overzoom).  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [productId] - Product id (kebab-case; `timeline` and `loop` are reserved)
  /// * [frameKey] - Frame key: content-addressed `{timeKey}~{sha8}` (immutable), bare `timeKey` (e.g. `20261010T143240Z`), or alias `latest` / `now`. An ISO-extended time is redirected (301) to its `timeKey`
  /// * [z] - Zoom (WebMercatorQuad)
  /// * [x] - Tile column
  /// * [y] - Tile row
  /// * [run] - Forecast run: `latest`, `previous`, or the run's reference time (ISO-8601, e.g. `2026-10-10T12:00:00Z`). Required for a concrete forecast time (400 RUN_REQUIRED); only a pinned run is immutable
  /// * [prefetch] - `1` marks a prefetch request (lowest render priority) without the `Foresignal-Prefetch` header, avoiding a CORS preflight
  /// * [foresignalPrefetch] - `1` marks a prefetch request (lowest render priority)
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
  Future<Response<Uint8List>> getProductVectorTile({ 
    required String productId,
    required String frameKey,
    required int z,
    required int x,
    required int y,
    String? run,
    int? prefetch,
    String? foresignalPrefetch,
    String? ifNoneMatch,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/products/{productId}/vectors/{frameKey}/{z}/{x}/{y}.mvt'.replaceAll('{' r'productId' '}', productId.toString()).replaceAll('{' r'frameKey' '}', frameKey.toString()).replaceAll('{' r'z' '}', z.toString()).replaceAll('{' r'x' '}', x.toString()).replaceAll('{' r'y' '}', y.toString());
    final _options = Options(
      method: r'GET',
      responseType: ResponseType.bytes,
      headers: <String, dynamic>{
        if (foresignalPrefetch != null) r'Foresignal-Prefetch': foresignalPrefetch,
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
      if (run != null) r'run': run,
      if (prefetch != null) r'prefetch': prefetch,
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

  /// Style fragment for several products
  /// One fragment with a namespaced source and layer per product.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [products] - Product ids
  /// * [frameKey] - Frame key or alias applied to every product
  /// * [style] - Versioned style `name@v` (400 UNKNOWN_STYLE)
  /// * [tms] - Tile matrix set
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [StyleFragment] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<StyleFragment>> getProductsStyleFragment({ 
    required List<String> products,
    String? frameKey,
    String? style,
    String? tms,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/products/style-fragment.json';
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
      r'products': products,
      if (frameKey != null) r'frameKey': frameKey,
      if (style != null) r'style': style,
      if (tms != null) r'tms': tms,
    };

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    StyleFragment? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<StyleFragment, StyleFragment>(rawData, 'StyleFragment', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<StyleFragment>(
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

  /// Get a style
  /// One style version; an unversioned name resolves to the current version. Unknown → 400 UNKNOWN_STYLE.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [styleId] - Style `name@v`, or `name` for the current version
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [Style] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<Style>> getStyle({ 
    required String styleId,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/styles/{styleId}'.replaceAll('{' r'styleId' '}', styleId.toString());
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

    Style? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<Style, Style>(rawData, 'Style', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<Style>(
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

  /// List frames of a product
  /// Cursor page of frames, newest first, each with &#x60;time&#x60;, &#x60;timeKey&#x60;, &#x60;frameKey&#x60;, &#x60;expiresAt&#x60;, &#x60;status&#x60;, &#x60;run&#x60;/&#x60;step&#x60; for forecasts and fully expanded &#x60;tiles&#x60; and &#x60;image&#x60; templates, plus a prefetch hint, &#x60;serverTime&#x60; and &#x60;earliest&#x60;. Partial frames are excluded unless &#x60;includePartial&#x3D;true&#x60;. Clients never build time strings.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [productId] - Product id (kebab-case; `timeline` and `loop` are reserved)
  /// * [from] - Valid time ≥ from
  /// * [to] - Valid time ≤ to
  /// * [run] - Forecast products: only frames of this run (`latest`, `previous` or the run's reference time)
  /// * [includePartial] - Include `partial` frames (excluded from aliases and lists by default)
  /// * [cursor] - Opaque cursor from `nextCursor`
  /// * [limit] - Max items
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [RasterFramePage] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<RasterFramePage>> listProductFrames({ 
    required String productId,
    DateTime? from,
    DateTime? to,
    String? run,
    bool? includePartial = false,
    String? cursor,
    int? limit = 100,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/products/{productId}/frames'.replaceAll('{' r'productId' '}', productId.toString());
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
      if (from != null) r'from': from,
      if (to != null) r'to': to,
      if (run != null) r'run': run,
      if (includePartial != null) r'includePartial': includePartial,
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

    RasterFramePage? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<RasterFramePage, RasterFramePage>(rawData, 'RasterFramePage', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<RasterFramePage>(
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

  /// List raster, field and external products
  /// The product catalogue with coverage meaning, temporal and spatial facts, rendering defaults, legend links, structured attribution and current status. Supports &#x60;compact&#x3D;true&#x60; and conditional GET (ETag); &#x60;kind&#x3D;external&#x60; lists client-direct catalogue entries.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [category] - Filter by category
  /// * [kind] - Filter by product kind
  /// * [bbox] - minLon,minLat,maxLon,maxLat (WGS84)
  /// * [source_] - Filter by owning source id
  /// * [q] - Free-text match on id and title
  /// * [compact] - Compact items
  /// * [cursor] - Opaque cursor from `nextCursor`
  /// * [limit] - Max items
  /// * [ifNoneMatch] - Conditional GET; 304 when the ETag matches
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [ProductPage] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<ProductPage>> listProducts({ 
    String? category,
    String? kind,
    String? bbox,
    String? source_,
    String? q,
    bool? compact = false,
    String? cursor,
    int? limit = 100,
    String? ifNoneMatch,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/products';
    final _options = Options(
      method: r'GET',
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
      if (category != null) r'category': category,
      if (kind != null) r'kind': kind,
      if (bbox != null) r'bbox': bbox,
      if (source_ != null) r'source': source_,
      if (q != null) r'q': q,
      if (compact != null) r'compact': compact,
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

    ProductPage? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<ProductPage, ProductPage>(rawData, 'ProductPage', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<ProductPage>(
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

  /// List registered styles
  /// Every registered style version (&#x60;nws-reflectivity@1&#x60;, &#x60;ir-gray@1&#x60;, …) with stops, categories, specials and meanings. Only registered names are accepted elsewhere (400 UNKNOWN_STYLE).  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [cursor] - Opaque cursor from `nextCursor`
  /// * [limit] - Max items
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [StylePage] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<StylePage>> listStyles({ 
    String? cursor,
    int? limit = 100,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/styles';
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

    StylePage? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<StylePage, StylePage>(rawData, 'StylePage', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<StylePage>(
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
