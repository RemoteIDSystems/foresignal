//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

import 'dart:async';

// ignore: unused_import
import 'dart:convert';
import 'package:foresignal_api/src/deserialize.dart';
import 'package:dio/dio.dart';

import 'package:foresignal_api/src/model/feature.dart';
import 'package:foresignal_api/src/model/feature_collection.dart';
import 'package:foresignal_api/src/model/kind_schema.dart';
import 'package:foresignal_api/src/model/kind_summary.dart';
import 'package:foresignal_api/src/model/list_records200_response.dart';
import 'package:foresignal_api/src/model/problem.dart';
import 'package:foresignal_api/src/model/purge_report.dart';
import 'package:foresignal_api/src/model/purge_request.dart';
import 'package:foresignal_api/src/model/query_history200_response.dart';
import 'package:foresignal_api/src/model/record.dart';
import 'package:foresignal_api/src/model/record_version_page.dart';

class RecordsApi {

  final Dio _dio;

  const RecordsApi(this._dio);

  /// Export everything about one entity
  /// Streams the current state, every retained version (with observers), alerts referencing the entity and a manifest with counts and SHA-256; &#x60;gpx&#x60; writes a &#x60;trk&#x60; for kind tracker. Audited.  **Scope:** &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [sourceId] - Source id
  /// * [externalId] - External id (percent-encoded)
  /// * [format] - Output format
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [String] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<String>> exportRecord({ 
    required String sourceId,
    required String externalId,
    String? format,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/records/{sourceId}/{externalId}/export'.replaceAll('{' r'sourceId' '}', sourceId.toString()).replaceAll('{' r'externalId' '}', externalId.toString());
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

  /// JSON Schema of a kind&#39;s payload
  /// Merges the fieldSpecs of every source emitting the kind into a JSON Schema 2020-12 document (&#x60;x-unit&#x60;, &#x60;x-unitLabel&#x60;, &#x60;x-label&#x60;, &#x60;x-format&#x60;, &#x60;x-display&#x60; from kindDisplay); conflicting types become &#x60;oneOf&#x60; with &#x60;x-conflicts&#x60; listing the source ids. Strong ETag over the merged document.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [kind] - Record kind, e.g. earthquake
  /// * [ifNoneMatch] - Conditional GET: an ETag from a previous response; 304 when unchanged
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [KindSchema] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<KindSchema>> getKindSchema({ 
    required String kind,
    String? ifNoneMatch,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/kinds/{kind}/schema'.replaceAll('{' r'kind' '}', kind.toString());
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

    KindSchema? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<KindSchema, KindSchema>(rawData, 'KindSchema', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<KindSchema>(
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

  /// Get current version of a record
  /// Current state of one record (head plus live). Strong ETag &#x60;\&quot;&lt;version&gt;.&lt;observedAt epoch ms&gt;\&quot;&#x60;; &#x60;include&#x3D;observer&#x60; reads observers from the head&#39;s version when gated in.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [sourceId] - Source id, e.g. usgs-earthquakes
  /// * [externalId] - Record id within the source (URL-encoded)
  /// * [format] - Response format (alternatively Accept: application/geo+json)
  /// * [include] - Extra fields; `observer` is returned only to agent/admin tokens or when security.exposeObserver is on, otherwise omitted
  /// * [fields] - Projection: comma list of top-level properties (source, externalId, kind, title, observedAt, geometry, …) and `payload.<dot.path>` entries; unknown top-level names → 400
  /// * [precision] - Coordinate decimals in returned geometry (storage unchanged)
  /// * [ifNoneMatch] - Conditional GET: an ETag from a previous response; 304 when unchanged
  /// * [licencePolicy] - Licence policy flags (comma-separated): `no-nc` excludes records whose commercial use is not ALLOWED, `no-sa` excludes share-alike records, `redistributable` excludes records whose redistribution is not ALLOWED. Adds to the token's `licencePolicy` floor; it can never remove a token flag.
  /// * [foresignalLicencePolicy] - Header form of `licencePolicy` (comma-separated flags `no-nc`, `no-sa`, `redistributable`)
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [Record] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<Record>> getRecord({ 
    required String sourceId,
    required String externalId,
    String? format,
    List<String>? include,
    String? fields,
    int? precision = 7,
    String? ifNoneMatch,
    List<String>? licencePolicy,
    String? foresignalLicencePolicy,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/records/{sourceId}/{externalId}'.replaceAll('{' r'sourceId' '}', sourceId.toString()).replaceAll('{' r'externalId' '}', externalId.toString());
    final _options = Options(
      method: r'GET',
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
      if (format != null) r'format': format,
      if (include != null) r'include': include,
      if (fields != null) r'fields': fields,
      if (precision != null) r'precision': precision,
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

    Record? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<Record, Record>(rawData, 'Record', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<Record>(
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

  /// List record kinds
  /// Kinds currently present (head ⋈ live, current only) with counts, contributing sources and the kindDisplay block when declared.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [ifNoneMatch] - Conditional GET: an ETag from a previous response; 304 when unchanged
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [List<KindSummary>] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<List<KindSummary>>> listKinds({ 
    String? ifNoneMatch,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/kinds';
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

    List<KindSummary>? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<List<KindSummary>, KindSummary>(rawData, 'List<KindSummary>', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<List<KindSummary>>(
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

  /// Version history of a record
  /// Versions of one record ordered by seq, each with persistReason, ingestedAt, receivedAt, batchId, quality and (gated) observer.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [sourceId] - Source id, e.g. usgs-earthquakes
  /// * [externalId] - Record id within the source (URL-encoded)
  /// * [since] - observedAt ≥ since
  /// * [until] - observedAt < until
  /// * [cursor] - Opaque seq cursor from nextCursor
  /// * [limit] - Page size
  /// * [include] - Extra fields; `observer` is returned only to agent/admin tokens or when security.exposeObserver is on, otherwise omitted
  /// * [licencePolicy] - Licence policy flags (comma-separated): `no-nc` excludes records whose commercial use is not ALLOWED, `no-sa` excludes share-alike records, `redistributable` excludes records whose redistribution is not ALLOWED. Adds to the token's `licencePolicy` floor; it can never remove a token flag.
  /// * [foresignalLicencePolicy] - Header form of `licencePolicy` (comma-separated flags `no-nc`, `no-sa`, `redistributable`)
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [RecordVersionPage] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<RecordVersionPage>> listRecordVersions({ 
    required String sourceId,
    required String externalId,
    DateTime? since,
    DateTime? until,
    String? cursor,
    int? limit = 500,
    List<String>? include,
    List<String>? licencePolicy,
    String? foresignalLicencePolicy,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/records/{sourceId}/{externalId}/versions'.replaceAll('{' r'sourceId' '}', sourceId.toString()).replaceAll('{' r'externalId' '}', externalId.toString());
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
      if (since != null) r'since': since,
      if (until != null) r'until': until,
      if (cursor != null) r'cursor': cursor,
      if (limit != null) r'limit': limit,
      if (include != null) r'include': include,
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

    RecordVersionPage? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<RecordVersionPage, RecordVersionPage>(rawData, 'RecordVersionPage', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<RecordVersionPage>(
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

  /// Query current records
  /// Current state from record_head ⋈ record_live (merged payload, live geometry); expired and tombstoned records are excluded unless requested, and &#x60;watermarkSeq&#x60; is read before the query. Mirror contract: (1) open SSE with &#x60;after &#x3D; watermarkSeq&#x60;, (2) page the snapshot with &#x60;order&#x3D;id&#x60; (the cursor pins &#x60;asOfSeq ?? watermarkSeq&#x60;; later updates arrive via &#x60;/v1/changes?after&#x3D;&lt;w&gt;&#x60;), (3) apply an event only if its observedAt ≥ the stored one. &#x60;asOf&#x3D;T&#x60; replays transaction time and needs a source or kind filter (400 ASOF_FILTER_REQUIRED, ASOF_BEYOND_RETENTION with &#x60;horizon&#x60;, ASOF_INVALID).  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [source_] - Filter by source id (repeatable or comma-separated)
  /// * [kind] - Filter by kind (repeatable or comma-separated)
  /// * [bbox] - minLon,minLat,maxLon,maxLat (WGS84). minLon > maxLon crosses the antimeridian and is split into two boxes; minLat < maxLat is required.
  /// * [since] - Lower bound (inclusive) on the `timeField` axis
  /// * [until] - Upper bound (exclusive) on the `timeField` axis
  /// * [q] - Case-insensitive text match on searchable fields (name, title, callsign, place)
  /// * [includeExpired] - Include records past validUntil
  /// * [format] - Response format; wins over Accept. Streaming formats (ndjson, geojsonseq, csv) are not paged and are capped at `foresignal.api.stream.maxRows` (400 RESULT_TOO_LARGE); csv needs `fields=` for payload columns.
  /// * [cursor] - Opaque cursor from nextCursor (tampered or mismatched → 400 INVALID_CURSOR)
  /// * [limit] - Page size
  /// * [externalId] - Filter by external id (≤ 100) (repeatable or comma-separated)
  /// * [near] - Centre point `lon,lat` (GeoJSON order); requires `radiusKm`
  /// * [radiusKm] - Radius in km around `near` (0 < r ≤ 2000); required with `near`
  /// * [timeField] - Column used by since/until: effective = COALESCE(eventTime, observedAt), observed, event, or ingested (seq bounds via seq_clock)
  /// * [order] - observed (newest first; keyset observedAt, source, externalId) or id (snapshot order; pins the watermark)
  /// * [sort] - distance (requires near and radiusKm; items carry distanceKm); exclusive with order
  /// * [asOfSeq] - Pin the snapshot to this seq (entities updated after it are left out)
  /// * [asOf] - Transaction-time replay: state as known at T (batch granularity); not combinable with asOfSeq, order=observed, sort=distance, includeExpired, since or until
  /// * [include] - Extra fields; `observer` is returned only to agent/admin tokens or when security.exposeObserver is on, otherwise omitted
  /// * [fields] - Projection: comma list of top-level properties (source, externalId, kind, title, observedAt, geometry, …) and `payload.<dot.path>` entries; unknown top-level names → 400
  /// * [precision] - Coordinate decimals in returned geometry (storage unchanged)
  /// * [simplify] - Simplification tolerance in metres (topology preserved per feature; points untouched)
  /// * [ifNoneMatch] - Conditional GET: an ETag from a previous response; 304 when unchanged
  /// * [place] - Place id(s) (036), resolved to their areas and intersected with the other filters; requires a token (places are never anonymous)
  /// * [view] - Saved view id or slug (036), intersected with the other filters; a view that references a place or watchlist needs a token
  /// * [inWatchlist] - Only entities whose identity is on this watchlist (036); token required (401 for anonymous and feed keys)
  /// * [count] - Return `{count, exact, watermarkSeq}` instead of items (038); counts up to 1,000,000 exactly
  /// * [country] - ISO 3166-1 alpha-2 codes (040); unknown → 400 UNKNOWN_REGION
  /// * [admin1] - ISO 3166-2 codes, e.g. US-TX (040)
  /// * [county] - US county FIPS5 codes (040)
  /// * [licencePolicy] - Licence policy flags (comma-separated): `no-nc` excludes records whose commercial use is not ALLOWED, `no-sa` excludes share-alike records, `redistributable` excludes records whose redistribution is not ALLOWED. Adds to the token's `licencePolicy` floor; it can never remove a token flag.
  /// * [foresignalLicencePolicy] - Header form of `licencePolicy` (comma-separated flags `no-nc`, `no-sa`, `redistributable`)
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [ListRecords200Response] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<ListRecords200Response>> listRecords({ 
    List<String>? source_,
    List<String>? kind,
    String? bbox,
    DateTime? since,
    DateTime? until,
    String? q,
    bool? includeExpired = false,
    String? format,
    String? cursor,
    int? limit = 500,
    List<String>? externalId,
    String? near,
    num? radiusKm,
    String? timeField,
    String? order,
    String? sort,
    int? asOfSeq,
    DateTime? asOf,
    List<String>? include,
    String? fields,
    int? precision = 7,
    num? simplify,
    String? ifNoneMatch,
    List<String>? place,
    String? view,
    String? inWatchlist,
    bool? count = false,
    List<String>? country,
    List<String>? admin1,
    List<String>? county,
    List<String>? licencePolicy,
    String? foresignalLicencePolicy,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/records';
    final _options = Options(
      method: r'GET',
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
      if (source_ != null) r'source': source_,
      if (kind != null) r'kind': kind,
      if (bbox != null) r'bbox': bbox,
      if (since != null) r'since': since,
      if (until != null) r'until': until,
      if (q != null) r'q': q,
      if (includeExpired != null) r'includeExpired': includeExpired,
      if (format != null) r'format': format,
      if (cursor != null) r'cursor': cursor,
      if (limit != null) r'limit': limit,
      if (externalId != null) r'externalId': externalId,
      if (near != null) r'near': near,
      if (radiusKm != null) r'radiusKm': radiusKm,
      if (timeField != null) r'timeField': timeField,
      if (order != null) r'order': order,
      if (sort != null) r'sort': sort,
      if (asOfSeq != null) r'asOfSeq': asOfSeq,
      if (asOf != null) r'asOf': asOf,
      if (include != null) r'include': include,
      if (fields != null) r'fields': fields,
      if (precision != null) r'precision': precision,
      if (simplify != null) r'simplify': simplify,
      if (place != null) r'place': place,
      if (view != null) r'view': view,
      if (inWatchlist != null) r'inWatchlist': inWatchlist,
      if (count != null) r'count': count,
      if (country != null) r'country': country,
      if (admin1 != null) r'admin1': admin1,
      if (county != null) r'county': county,
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

    ListRecords200Response? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<ListRecords200Response, ListRecords200Response>(rawData, 'ListRecords200Response', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<ListRecords200Response>(
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

  /// Erase one entity
  /// Submits an &#x60;EntityPurge&#x60; writer command: writes a tombstone (qa flag PURGED; its change carries &#x60;purged: true&#x60;), deletes every older version on both tiers, then fans out: embeddings deleted, alert snapshots and messages redacted (or alerts deleted with &#x60;purgeAlerts&#x60;), notification bodies redacted, region rows and metric series dropped; Ask messages and briefing runs citing it are counted. Audited.  **Scope:** &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [sourceId] - Source id
  /// * [externalId] - External id (percent-encoded)
  /// * [purgeRequest] 
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [PurgeReport] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<PurgeReport>> purgeRecord({ 
    required String sourceId,
    required String externalId,
    PurgeRequest? purgeRequest,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/records/{sourceId}/{externalId}/purge'.replaceAll('{' r'sourceId' '}', sourceId.toString()).replaceAll('{' r'externalId' '}', externalId.toString());
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
      _bodyData = jsonEncode(purgeRequest);

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

    PurgeReport? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<PurgeReport, PurgeReport>(rawData, 'PurgeReport', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<PurgeReport>(
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

  /// All versions in a time range (playback)
  /// All versions in a range from record_version (playback), keyset on (observedAt, seq), ≤ 50,000 per page. &#x60;since&#x60; is required.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [since] - Start of range on the `timeField` axis
  /// * [source_] - Filter by source id (repeatable or comma-separated)
  /// * [kind] - Filter by kind (repeatable or comma-separated)
  /// * [bbox] - minLon,minLat,maxLon,maxLat (WGS84). minLon > maxLon crosses the antimeridian and is split into two boxes; minLat < maxLat is required.
  /// * [until] - End of range (default now)
  /// * [format] - Response format; wins over Accept. Streaming formats (ndjson, geojsonseq, csv) are not paged and are capped at `foresignal.api.stream.maxRows` (400 RESULT_TOO_LARGE); csv needs `fields=` for payload columns. `czml` (prompt 141) is not paged either: it reads up to 200,000 versions from `cursor` on and returns one CZML document (`limit` and `fields` are ignored).
  /// * [cursor] - Opaque pagination cursor from a previous response's nextCursor
  /// * [limit] - Page size
  /// * [timeField] - Column used by since/until (ingested is translated to seq bounds via seq_clock so partitions prune)
  /// * [externalId] - Filter by external id (≤ 100) (repeatable or comma-separated)
  /// * [near] - Centre point `lon,lat` (GeoJSON order); requires `radiusKm`
  /// * [radiusKm] - Radius in km around `near` (0 < r ≤ 2000); required with `near`
  /// * [fields] - Projection: comma list of top-level properties (source, externalId, kind, title, observedAt, geometry, …) and `payload.<dot.path>` entries; unknown top-level names → 400
  /// * [precision] - Coordinate decimals in returned geometry (storage unchanged)
  /// * [simplify] - Simplification tolerance in metres (topology preserved per feature; points untouched)
  /// * [place] - Place id(s) (036), resolved to their areas and intersected with the other filters; requires a token (places are never anonymous)
  /// * [view] - Saved view id or slug (036), intersected with the other filters; a view that references a place or watchlist needs a token
  /// * [inWatchlist] - Only entities whose identity is on this watchlist (036); token required (401 for anonymous and feed keys)
  /// * [count] - Return `{count, exact, watermarkSeq}` instead of items (038); counts up to 1,000,000 exactly
  /// * [licencePolicy] - Licence policy flags (comma-separated): `no-nc` excludes records whose commercial use is not ALLOWED, `no-sa` excludes share-alike records, `redistributable` excludes records whose redistribution is not ALLOWED. Adds to the token's `licencePolicy` floor; it can never remove a token flag.
  /// * [foresignalLicencePolicy] - Header form of `licencePolicy` (comma-separated flags `no-nc`, `no-sa`, `redistributable`)
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [QueryHistory200Response] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<QueryHistory200Response>> queryHistory({ 
    required DateTime since,
    List<String>? source_,
    List<String>? kind,
    String? bbox,
    DateTime? until,
    String? format,
    String? cursor,
    int? limit = 5000,
    String? timeField,
    List<String>? externalId,
    String? near,
    num? radiusKm,
    String? fields,
    int? precision = 7,
    num? simplify,
    List<String>? place,
    String? view,
    String? inWatchlist,
    bool? count = false,
    List<String>? licencePolicy,
    String? foresignalLicencePolicy,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/history';
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
      if (source_ != null) r'source': source_,
      if (kind != null) r'kind': kind,
      if (bbox != null) r'bbox': bbox,
      r'since': since,
      if (until != null) r'until': until,
      if (format != null) r'format': format,
      if (cursor != null) r'cursor': cursor,
      if (limit != null) r'limit': limit,
      if (timeField != null) r'timeField': timeField,
      if (externalId != null) r'externalId': externalId,
      if (near != null) r'near': near,
      if (radiusKm != null) r'radiusKm': radiusKm,
      if (fields != null) r'fields': fields,
      if (precision != null) r'precision': precision,
      if (simplify != null) r'simplify': simplify,
      if (place != null) r'place': place,
      if (view != null) r'view': view,
      if (inWatchlist != null) r'inWatchlist': inWatchlist,
      if (count != null) r'count': count,
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

    QueryHistory200Response? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<QueryHistory200Response, QueryHistory200Response>(rawData, 'QueryHistory200Response', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<QueryHistory200Response>(
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
