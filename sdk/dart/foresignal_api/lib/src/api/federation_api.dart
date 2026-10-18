//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

import 'dart:async';

// ignore: unused_import
import 'dart:convert';
import 'package:foresignal_api/src/deserialize.dart';
import 'package:dio/dio.dart';

import 'package:foresignal_api/src/model/compact_message.dart';
import 'package:foresignal_api/src/model/federation_change_page.dart';
import 'package:foresignal_api/src/model/federation_info.dart';
import 'package:foresignal_api/src/model/federation_ingest_batch.dart';
import 'package:foresignal_api/src/model/federation_ingest_result.dart';
import 'package:foresignal_api/src/model/federation_snapshot_page.dart';
import 'package:foresignal_api/src/model/problem.dart';
import 'package:foresignal_api/src/model/stream_federation200_response.dart';

class FederationApi {

  final Dio _dio;

  const FederationApi(this._dio);

  /// Receive a batch from an edge
  /// Hub endpoint for edge relays (peer row in mode RECEIVE): up to 1,000 version items with provenance, optionally gzip-encoded. The batch body must carry the edge&#39;s detached JWS in &#x60;Foresignal-Signature&#x60;, verified against the pinned key (&#x60;403 PEER_KEY_MISMATCH&#x60;). Idempotent on (origin_ref, origin_seq); the same loop, hop, dedup, licence and private checks as PULL apply. LOCAL_ONLY records are accepted only from &#x60;sameOperator&#x60; peers and stay LOCAL_ONLY.  **Scope:** &#x60;peer&#x60; (peer ingest key &#x60;fsp_…&#x60; sent as a Bearer token).
  ///
  /// Parameters:
  /// * [peerId] - Peer id
  /// * [foresignalSignature] - Detached JWS of the edge over the request body
  /// * [federationIngestBatch] 
  /// * [contentEncoding] - Optional gzip encoding
  /// * [foresignalKey] - The edge's Ed25519 public key (raw 32-byte point, base64url). Required on the first batch, when no key is pinned yet (TOFU): its RFC 7638 thumbprint must equal the JWS kid. Later batches must be signed by the pinned key.
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [FederationIngestResult] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<FederationIngestResult>> federationIngest({ 
    required String peerId,
    required String foresignalSignature,
    required FederationIngestBatch federationIngestBatch,
    String? contentEncoding,
    String? foresignalKey,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/federation/ingest/{peerId}'.replaceAll('{' r'peerId' '}', peerId.toString());
    final _options = Options(
      method: r'POST',
      headers: <String, dynamic>{
        r'Foresignal-Signature': foresignalSignature,
        if (contentEncoding != null) r'Content-Encoding': contentEncoding,
        if (foresignalKey != null) r'Foresignal-Key': foresignalKey,
        ...?headers,
      },
      extra: <String, dynamic>{
        'secure': <Map<String, String>>[
          {
            'type': 'http',
            'scheme': 'bearer',
            'name': 'peerCredential',
          },
        ],
        ...?extra,
      },
      contentType: 'application/json',
      validateStatus: validateStatus,
    );

    dynamic _bodyData;

    try {
      _bodyData = jsonEncode(federationIngestBatch);

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

    FederationIngestResult? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<FederationIngestResult, FederationIngestResult>(rawData, 'FederationIngestResult', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<FederationIngestResult>(
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

  /// Pending compact messages as a bundle
  /// A CBOR array (definite length; each element a CompactMessage exactly as the 084 profile encodes it) of pending CompactMessages (fired and resolved alerts, intelligence events and their tombstones; never tracks) for store-and-forward gateways (sneakernet, DTN bundle agents).  **Scope:** &#x60;read&#x60; with the &#x60;federation&#x60; token capability (restricted tokens, 023/035).
  ///
  /// Parameters:
  /// * [since] - Only messages queued at or after
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [List<CompactMessage>] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<List<CompactMessage>>> getFederationBundle({ 
    DateTime? since,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/federation/bundle';
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
      if (since != null) r'since': since,
    };

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    List<CompactMessage>? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<List<CompactMessage>, CompactMessage>(rawData, 'List<CompactMessage>', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<List<CompactMessage>>(
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

  /// Instance identity for federation
  /// Instance id, name, versions, JWKS URI and capabilities. The &#x60;Foresignal-Signature&#x60; header carries a detached JWS over the body (109), which peers verify against the pinned key.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [FederationInfo] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<FederationInfo>> getFederationInfo({ 
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/federation/info';
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

    FederationInfo? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<FederationInfo, FederationInfo>(rawData, 'FederationInfo', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<FederationInfo>(
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

  /// Federation snapshot for resync
  /// Paged current heads (&#x60;record_head ⋈ record_live&#x60;) pinned to the watermark of the first page; pass &#x60;atSeq&#x60; and &#x60;cursor&#x60; for later pages, then continue the change feed from &#x60;atSeq&#x60;. Per record: FORBIDDEN and LOCAL_ONLY licences (opensky-states, acled, aisstream, local receivers, MLAT positions), private sources and exercise records (unless the token sets includeExercise) are excluded. Restriction columns of the token still apply.  **Scope:** &#x60;read&#x60; with the &#x60;federation&#x60; token capability (restricted tokens, 023/035).
  ///
  /// Parameters:
  /// * [atSeq] - Watermark from the first page
  /// * [cursor] - Opaque pagination cursor from a previous response's nextCursor
  /// * [limit] - Page size
  /// * [source_] - Filter by source id (repeatable or comma-separated)
  /// * [kind] - Filter by kind (repeatable or comma-separated)
  /// * [bbox] - minLon,minLat,maxLon,maxLat (WGS84)
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [FederationSnapshotPage] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<FederationSnapshotPage>> getFederationSnapshot({ 
    int? atSeq,
    String? cursor,
    int? limit = 1000,
    List<String>? source_,
    List<String>? kind,
    String? bbox,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/federation/snapshot';
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
      if (atSeq != null) r'atSeq': atSeq,
      if (cursor != null) r'cursor': cursor,
      if (limit != null) r'limit': limit,
      if (source_ != null) r'source': source_,
      if (kind != null) r'kind': kind,
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

    FederationSnapshotPage? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<FederationSnapshotPage, FederationSnapshotPage>(rawData, 'FederationSnapshotPage', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<FederationSnapshotPage>(
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

  /// Federation change feed
  /// Full version items with licence and provenance after &#x60;after&#x60;, in seq order. Per record: FORBIDDEN and LOCAL_ONLY licences (opensky-states, acled, aisstream, local receivers, MLAT positions), private sources and exercise records (unless the token sets includeExercise) are excluded. Restriction columns of the token still apply. Below the purge horizon → &#x60;410 RESYNC_REQUIRED&#x60;.  **Scope:** &#x60;read&#x60; with the &#x60;federation&#x60; token capability (restricted tokens, 023/035).
  ///
  /// Parameters:
  /// * [after] - Last seq received
  /// * [limit] - Page size
  /// * [source_] - Filter by source id (repeatable or comma-separated)
  /// * [kind] - Filter by kind (repeatable or comma-separated)
  /// * [bbox] - minLon,minLat,maxLon,maxLat (WGS84)
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [FederationChangePage] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<FederationChangePage>> listFederationChanges({ 
    int? after,
    int? limit = 1000,
    List<String>? source_,
    List<String>? kind,
    String? bbox,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/federation/changes';
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
      if (after != null) r'after': after,
      if (limit != null) r'limit': limit,
      if (source_ != null) r'source': source_,
      if (kind != null) r'kind': kind,
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

    FederationChangePage? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<FederationChangePage, FederationChangePage>(rawData, 'FederationChangePage', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<FederationChangePage>(
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

  /// Receive a compact message bundle
  /// Accepts a CBOR array of CompactMessages from a peer and decodes them into &#x60;remote-event&#x60; records on &#x60;peer-{id}-events&#x60; (reliability F, quality flag compact). Messages whose origin (keys 10, 11) is this instance or already seen are dropped; received messages are never re-federated.  **Scope:** &#x60;peer&#x60; (peer ingest key &#x60;fsp_…&#x60; sent as a Bearer token).
  ///
  /// Parameters:
  /// * [compactMessage] 
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [FederationIngestResult] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<FederationIngestResult>> receiveFederationBundle({ 
    required List<CompactMessage> compactMessage,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/federation/bundle';
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
            'name': 'peerCredential',
          },
        ],
        ...?extra,
      },
      contentType: 'application/cbor',
      validateStatus: validateStatus,
    );

    dynamic _bodyData;

    try {
      _bodyData = jsonEncode(compactMessage);

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

    FederationIngestResult? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<FederationIngestResult, FederationIngestResult>(rawData, 'FederationIngestResult', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<FederationIngestResult>(
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

  /// Federation change stream (SSE)
  /// SSE with the 015 lanes: &#x60;change&#x60; (full FederationItem, id &#x3D; seq), optional &#x60;live&#x60; with &#x60;live&#x3D;true&#x60;, &#x60;heartbeat&#x60;, &#x60;resync&#x60;, and &#x60;reset&#x60; below the purge horizon. Resumes from &#x60;Last-Event-ID&#x60; or &#x60;after&#x60;. Per record: FORBIDDEN and LOCAL_ONLY licences (opensky-states, acled, aisstream, local receivers, MLAT positions), private sources and exercise records (unless the token sets includeExercise) are excluded. Restriction columns of the token still apply.  **Scope:** &#x60;read&#x60; with the &#x60;federation&#x60; token capability (restricted tokens, 023/035).
  ///
  /// Parameters:
  /// * [lastEventID] - SSE resume: last seq received
  /// * [after] - Alternative to Last-Event-ID
  /// * [live] - Include the live lane
  /// * [source_] - Filter by source id (repeatable or comma-separated)
  /// * [kind] - Filter by kind (repeatable or comma-separated)
  /// * [bbox] - minLon,minLat,maxLon,maxLat (WGS84)
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [StreamFederation200Response] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<StreamFederation200Response>> streamFederation({ 
    String? lastEventID,
    int? after,
    bool? live = false,
    List<String>? source_,
    List<String>? kind,
    String? bbox,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/federation/stream';
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
      if (live != null) r'live': live,
      if (source_ != null) r'source': source_,
      if (kind != null) r'kind': kind,
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

    StreamFederation200Response? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<StreamFederation200Response, StreamFederation200Response>(rawData, 'StreamFederation200Response', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<StreamFederation200Response>(
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
