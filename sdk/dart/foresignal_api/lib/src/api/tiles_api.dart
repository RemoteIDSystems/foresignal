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
import 'package:foresignal_api/src/model/tile_json.dart';
import 'package:foresignal_api/src/model/tile_layer_index.dart';

class TilesApi {

  final Dio _dio;

  const TilesApi(this._dio);

  /// Vector tile of current records
  /// Mapbox Vector Tile of current live state (record_live ⋈ record_head): geometry and observedAt come from record_live, so a live position shows before any new version is persisted. &#x60;layer&#x60; is a source id (wins) or a kind declared by a visible source; anything else is 404 UNKNOWN_LAYER. Raw features carry &#x60;source&#x60;, &#x60;externalId&#x60;, &#x60;kind&#x60;, &#x60;title&#x60; (record_head.title), &#x60;observedAt&#x60; (epoch seconds, number), &#x60;version&#x60; (number), &#x60;live&#x60; (boolean, record_live.dirty) plus the layer sources&#39; valid &#x60;tileProperties&#x60; (names matching &#x60;^[A-Za-z0-9_]{1,64}$&#x60;) read from the merged payload with their native JSON types; null values are omitted. Raw tiles are capped at foresignal.tiles.maxFeatures (default 50,000) newest-first, with &#x60;Foresignal-Tile-Truncated: true&#x60; when the cap was hit. Aggregated tiles (&#x60;agg&#x3D;grid|hex&#x60;, or &#x60;auto&#x60; below the layer&#39;s aggBelowZoom, which is the lowest value its visible sources declare) carry one polygon per non-empty cell with &#x60;count&#x60;, &#x60;latestObservedAt&#x60; (epoch seconds), &#x60;kinds&#x60; (a JSON string, kind → count), &#x60;agg&#x60; (&#x60;grid&#x60; or &#x60;hex&#x60;) and &#x60;maxRank&#x60; only when every source of the layer declares a numeric rankField. Cells are built in EPSG:3857 with foresignal.tiles.cellsPerTile (default 32) cells across a tile; grid cells line up with tile edges. Each record is reduced to ST_PointOnSurface (a polygon counts once) and counted in exactly one tile (half-open tile envelope) and one cell; a hex cell that straddles a tile edge is drawn by both tiles, each counting the records on its own side. Private sources never appear in features or counts for anonymous callers. An empty tile is 200 with an empty body. With &#x60;asOf&#x60; (037) the tile shows the layer as known at that instant (transaction axis, record_version): per entity the newest version with seq in [L, S(asOf)] that is not a tombstone, still inside the tile and valid at asOf (an entity that moved out is not drawn at its old position); raw features carry &#x60;source&#x60;, &#x60;externalId&#x60;, &#x60;kind&#x60;, &#x60;title&#x60; (from the version payload), &#x60;observedAt&#x60;, &#x60;version&#x60; and the tile properties, without &#x60;live&#x60;; the same cap and aggregation apply.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [layer] - Kind or source id
  /// * [z] - Zoom
  /// * [x] - Tile column
  /// * [y] - Tile row
  /// * [agg] - auto aggregates with hex below the layer's aggBelowZoom; none forces raw; grid/hex force aggregation
  /// * [ifNoneMatch] - Conditional GET: an ETag from a previous response; 304 when unchanged
  /// * [ticket] - Stream or tile ticket from `POST /v1/stream-tickets` (023), for clients that cannot send an `Authorization` header. Accepted only for the ticket's purpose and never in a header; stripped from logs
  /// * [place] - Place id(s) (036), resolved to their areas and intersected with the other filters; requires a token (places are never anonymous)
  /// * [radiusKm] - Extra buffer around `place` areas (036)
  /// * [view] - Saved view id or slug (036), intersected with the other filters; a view that references a place or watchlist needs a token
  /// * [inWatchlist] - Only entities whose identity is on this watchlist (036); token required (401 for anonymous and feed keys)
  /// * [asOf] - Render the tile as known at this instant (transaction axis, 037); normalised to whole seconds (other forms 301 to the canonical URL)
  /// * [licencePolicy] - Licence policy flags (comma-separated): `no-nc` excludes records whose commercial use is not ALLOWED, `no-sa` excludes share-alike records, `redistributable` excludes records whose redistribution is not ALLOWED. Adds to the token's `licencePolicy` floor; it can never remove a token flag.
  /// * [foresignalLicencePolicy] - Header form of `licencePolicy` (comma-separated flags `no-nc`, `no-sa`, `redistributable`)
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [Uint8List] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<Uint8List>> getTile({ 
    required String layer,
    required int z,
    required int x,
    required int y,
    String? agg,
    String? ifNoneMatch,
    String? ticket,
    List<String>? place,
    num? radiusKm,
    String? view,
    String? inWatchlist,
    DateTime? asOf,
    List<String>? licencePolicy,
    String? foresignalLicencePolicy,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/tiles/{layer}/{z}/{x}/{y}.mvt'.replaceAll('{' r'layer' '}', layer.toString()).replaceAll('{' r'z' '}', z.toString()).replaceAll('{' r'x' '}', x.toString()).replaceAll('{' r'y' '}', y.toString());
    final _options = Options(
      method: r'GET',
      responseType: ResponseType.bytes,
      headers: <String, dynamic>{
        if (ifNoneMatch != null) r'If-None-Match': ifNoneMatch,
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
      if (agg != null) r'agg': agg,
      if (ticket != null) r'ticket': ticket,
      if (place != null) r'place': place,
      if (radiusKm != null) r'radiusKm': radiusKm,
      if (view != null) r'view': view,
      if (inWatchlist != null) r'inWatchlist': inWatchlist,
      if (asOf != null) r'asOf': asOf,
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

  /// TileJSON for one layer
  /// TileJSON 3.0.0 for a source id or kind: &#x60;tilejson&#x60;, &#x60;name&#x60;, &#x60;description&#x60;, &#x60;version&#x60; (Foresignal version), &#x60;attribution&#x60; (distinct descriptor attributions joined with \&quot; · \&quot;), &#x60;scheme: xyz&#x60;, &#x60;tiles&#x60; under &#x60;server.publicBaseUrl&#x60; (else the request&#39;s scheme, host and port; X-Forwarded headers are not trusted until 023), &#x60;minzoom&#x60; 0, &#x60;maxzoom&#x60; 16, &#x60;bounds&#x60;, and one &#x60;vector_layers&#x60; entry whose &#x60;fields&#x60; describe every feature property (fieldSpecs label and unit, else String/Number/Boolean), plus the extension keys &#x60;foresignal:aggBelowZoom&#x60; (null &#x3D; never) and &#x60;foresignal:properties&#x60;. Strong ETag; &#x60;Cache-Control: public, max-age&#x3D;300&#x60;. Unknown layer, or a private source asked anonymously → 404 UNKNOWN_LAYER. With &#x60;asOf&#x60; (037) the document is the layer as known at that instant: the &#x60;tiles&#x60; template is concrete with the canonical &#x60;asOf&#x60; baked in, &#x60;live&#x60; is not among the properties, and &#x60;foresignal:asOf&#x60; echoes the instant.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [layer] - Source id or kind
  /// * [ifNoneMatch] - Conditional GET: an ETag from a previous response; 304 when unchanged
  /// * [ticket] - Stream or tile ticket from `POST /v1/stream-tickets` (023), for clients that cannot send an `Authorization` header. Accepted only for the ticket's purpose and never in a header; stripped from logs
  /// * [asOf] - Render the tile as known at this instant (transaction axis, 037); normalised to whole seconds (other forms 301 to the canonical URL)
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [TileJson] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<TileJson>> getTileJson({ 
    required String layer,
    String? ifNoneMatch,
    String? ticket,
    DateTime? asOf,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/tiles/{layer}.json'.replaceAll('{' r'layer' '}', layer.toString());
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
      if (ticket != null) r'ticket': ticket,
      if (asOf != null) r'asOf': asOf,
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

  /// Vector-tile layer index
  /// Every registered source id and every kind with at least one source the caller can see (private sources only for token callers; a kind spelled like a source id is left out because the name resolves to the source), each with its TileJSON URL (under &#x60;server.publicBaseUrl&#x60;, else the request&#39;s scheme, host and port) and aggBelowZoom (null &#x3D; never aggregate). Strong ETag; &#x60;Cache-Control: public, max-age&#x3D;300&#x60; (&#x60;private&#x60; when openReads is off). With &#x60;asOf&#x60; (037) every TileJSON URL carries the canonical &#x60;asOf&#x60;.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [ifNoneMatch] - Conditional GET: an ETag from a previous response; 304 when unchanged
  /// * [ticket] - Stream or tile ticket from `POST /v1/stream-tickets` (023), for clients that cannot send an `Authorization` header. Accepted only for the ticket's purpose and never in a header; stripped from logs
  /// * [asOf] - Render the tile as known at this instant (transaction axis, 037); normalised to whole seconds (other forms 301 to the canonical URL)
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [TileLayerIndex] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<TileLayerIndex>> listTileLayers({ 
    String? ifNoneMatch,
    String? ticket,
    DateTime? asOf,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/tiles.json';
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
      if (ticket != null) r'ticket': ticket,
      if (asOf != null) r'asOf': asOf,
    };

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    TileLayerIndex? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<TileLayerIndex, TileLayerIndex>(rawData, 'TileLayerIndex', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<TileLayerIndex>(
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
