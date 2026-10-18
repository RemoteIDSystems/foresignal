//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

import 'dart:async';

// ignore: unused_import
import 'dart:convert';
import 'package:foresignal_api/src/deserialize.dart';
import 'package:dio/dio.dart';

import 'package:foresignal_api/src/model/captured_sample.dart';
import 'package:foresignal_api/src/model/cloud_event.dart';
import 'package:foresignal_api/src/model/custom_source.dart';
import 'package:foresignal_api/src/model/custom_source_created.dart';
import 'package:foresignal_api/src/model/custom_source_input.dart';
import 'package:foresignal_api/src/model/feature_collection.dart';
import 'package:foresignal_api/src/model/gotify_message.dart';
import 'package:foresignal_api/src/model/gotify_message_input.dart';
import 'package:foresignal_api/src/model/ingest_device.dart';
import 'package:foresignal_api/src/model/ingest_device_created.dart';
import 'package:foresignal_api/src/model/ingest_device_input.dart';
import 'package:foresignal_api/src/model/ingest_format_info.dart';
import 'package:foresignal_api/src/model/ingest_key_response.dart';
import 'package:foresignal_api/src/model/ingest_outcome.dart';
import 'package:foresignal_api/src/model/ingest_records_format_request.dart';
import 'package:foresignal_api/src/model/ingest_records_path_key200_response.dart';
import 'package:foresignal_api/src/model/ingest_records_path_key_request.dart';
import 'package:foresignal_api/src/model/ingest_records_request.dart';
import 'package:foresignal_api/src/model/mapping_test_request.dart';
import 'package:foresignal_api/src/model/mapping_test_result.dart';
import 'package:foresignal_api/src/model/ntfy_message.dart';
import 'package:foresignal_api/src/model/ntfy_publish_request.dart';
import 'package:foresignal_api/src/model/problem.dart';
import 'package:foresignal_api/src/model/purge_report.dart';
import 'package:foresignal_api/src/model/purge_request.dart';
import 'package:foresignal_api/src/model/remote_media_accepted.dart';
import 'package:foresignal_api/src/model/remote_media_meta.dart';
import 'package:foresignal_api/src/model/remote_raster_bundle_accepted.dart';
import 'package:foresignal_api/src/model/remote_raster_bundle_meta.dart';

class IngestApi {

  final Dio _dio;

  const IngestApi(this._dio);

  /// Create a custom push source
  /// Registers a runtime PUSH source (STANDARD tier, &#x60;custom&#x3D;true&#x60;) after registry validation and &#x60;IngestPolicyValidator&#x60;; id unique across static and dynamic sources (409). Returns the ingest key (&#x60;eik_&#x60; + 32 random bytes) exactly once. &#x60;profile: personal-location&#x60; applies the 039 defaults. &#x60;preset&#x60; (059) fills every member the input leaves out from a MappingPreset, with &#x60;presetParams&#x60; (&#x60;lon&#x60;/&#x60;lat&#x60; rounded to 0.01° unless &#x60;exactLocation&#x60;; &#x60;tombstoneOnResolve&#x60; for alertmanager), and the response&#39;s &#x60;setup&#x60; carries the device instructions with the concrete URL (it may contain the key; shown once). &#x60;mapping&#x60; is compiled against the input&#39;s kinds and observedAtMode (400 MAPPING_INVALID). &#x60;licenceTerms&#x60; (109) sets the structured terms of the pushed data: each PUSH batch snapshots their id on its fetch row and every egress path enforces them.  **Scope:** &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [customSourceInput] 
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [CustomSourceCreated] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<CustomSourceCreated>> createCustomSource({ 
    required CustomSourceInput customSourceInput,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/custom-sources';
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
      _bodyData = jsonEncode(customSourceInput);

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

    CustomSourceCreated? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<CustomSourceCreated, CustomSourceCreated>(rawData, 'CustomSourceCreated', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<CustomSourceCreated>(
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

  /// Create a device-scoped ingest key
  /// A device key (&#x60;eid_&#x60;) authenticates only for this source and forces externalId to the bound id (a mismatching id → 403 DEVICE_ENTITY_MISMATCH). Returns the key once with OwnTracks, Overland, Traccar and GPSLogger setup data.  **Scope:** &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [sourceId] - Custom source id
  /// * [ingestDeviceInput] 
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [IngestDeviceCreated] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<IngestDeviceCreated>> createIngestDevice({ 
    required String sourceId,
    required IngestDeviceInput ingestDeviceInput,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/custom-sources/{sourceId}/devices'.replaceAll('{' r'sourceId' '}', sourceId.toString());
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
      _bodyData = jsonEncode(ingestDeviceInput);

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

    IngestDeviceCreated? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<IngestDeviceCreated, IngestDeviceCreated>(rawData, 'IngestDeviceCreated', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<IngestDeviceCreated>(
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

  /// Delete a custom source
  /// Stops ingest; existing records age out through retention.  **Scope:** &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [sourceId] - Custom source id (custom-…)
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future]
  /// Throws [DioException] if API call or serialization fails
  Future<Response<void>> deleteCustomSource({ 
    required String sourceId,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/custom-sources/{sourceId}'.replaceAll('{' r'sourceId' '}', sourceId.toString());
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

  /// Revoke a device key
  /// Ingest with the key stops immediately; the device&#39;s records stay until retention or a purge.  **Scope:** &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [sourceId] - Custom source id
  /// * [deviceId] - Ingest device id
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future]
  /// Throws [DioException] if API call or serialization fails
  Future<Response<void>> deleteIngestDevice({ 
    required String sourceId,
    required String deviceId,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/custom-sources/{sourceId}/devices/{deviceId}'.replaceAll('{' r'sourceId' '}', sourceId.toString()).replaceAll('{' r'deviceId' '}', deviceId.toString());
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

  /// Get a custom source
  /// Get a custom source  **Scope:** &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [sourceId] - Custom source id (custom-…)
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [CustomSource] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<CustomSource>> getCustomSource({ 
    required String sourceId,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/custom-sources/{sourceId}'.replaceAll('{' r'sourceId' '}', sourceId.toString());
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

    CustomSource? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<CustomSource, CustomSource>(rawData, 'CustomSource', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<CustomSource>(
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

  /// Gotify-compatible message publish
  /// Gotify-compatible publish (Proxmox and other tools). The ingest key in &#x60;X-Gotify-Key&#x60; or &#x60;?token&#x3D;&#x60; identifies the source. Kind &#x3D; the source&#39;s single kind (else &#x60;notification&#x60;); externalId from &#x60;extras[\&quot;foresignal::record\&quot;].externalId&#x60;, else a ULID. Outside &#x60;/v1&#x60;; AccessPolicy rule &#x60;/compat/gotify/_**&#x60;.  **Scope:** ingest key (or an &#x60;admin&#x60; token).
  ///
  /// Parameters:
  /// * [gotifyMessageInput] 
  /// * [xGotifyKey] - Ingest key
  /// * [token] - Ingest key (redacted in logs)
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [GotifyMessage] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<GotifyMessage>> gotifyPublish({ 
    required GotifyMessageInput gotifyMessageInput,
    String? xGotifyKey,
    String? token,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/compat/gotify/message';
    final _options = Options(
      method: r'POST',
      headers: <String, dynamic>{
        if (xGotifyKey != null) r'X-Gotify-Key': xGotifyKey,
        ...?headers,
      },
      extra: <String, dynamic>{
        'secure': <Map<String, String>>[
          {
            'type': 'http',
            'scheme': 'bearer',
            'name': 'ingestKeyBearer',
          },{
            'type': 'apiKey',
            'name': 'ingestKey',
            'keyName': 'X-Foresignal-Ingest-Key',
            'where': 'header',
          },{
            'type': 'http',
            'scheme': 'basic',
            'name': 'ingestKeyBasic',
          },{
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

    final _queryParameters = <String, dynamic>{
      if (token != null) r'token': token,
    };

    dynamic _bodyData;

    try {
      _bodyData = jsonEncode(gotifyMessageInput);

    } catch(error, stackTrace) {
      throw DioException(
         requestOptions: _options.compose(
          _dio.options,
          _path,
          queryParameters: _queryParameters,
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
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    GotifyMessage? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<GotifyMessage, GotifyMessage>(rawData, 'GotifyMessage', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<GotifyMessage>(
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

  /// Push records into a source (inbound webhook)
  /// The &#x60;native&#x60; format, unless the source has a default format (059: a source created from a preset uses the preset&#39;s format here, so a form POST from a device lands in its mapping): a GeoJSON FeatureCollection (&#x60;id&#x60; → externalId; properties &#x60;kind&#x60; (optional with a single kind), &#x60;observedAt&#x60;, &#x60;eventTime&#x60;, &#x60;validUntil&#x60;, &#x60;tombstone&#x60;, &#x60;quality&#x60;; the rest → payload) or &#x60;{records:[...]}&#x60;. Max 5 MB and 10,000 records. Credentials as for &#x60;ingestRecordsFormat&#x60; (ingest key header, Bearer, Basic, device key, admin token). Records enter the single writer as IngestRecords with the full v1.2 pipeline; the response is the v1.2 &#x60;IngestOutcome&#x60;. Per-record rejections (e.g. MISSING_OBSERVED_AT under UPSTREAM) do not fail the request.  **Scope:** ingest key (or an &#x60;admin&#x60; token).
  ///
  /// Parameters:
  /// * [sourceId] - A push-mode source id
  /// * [ingestRecordsRequest] 
  /// * [contentEncoding] - `gzip` accepted (AIS-catcher `GZIP on`); decompressed size capped at 5 MB → 413
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [IngestOutcome] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<IngestOutcome>> ingestRecords({ 
    required String sourceId,
    required IngestRecordsRequest ingestRecordsRequest,
    String? contentEncoding,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/ingest/{sourceId}'.replaceAll('{' r'sourceId' '}', sourceId.toString());
    final _options = Options(
      method: r'POST',
      headers: <String, dynamic>{
        if (contentEncoding != null) r'Content-Encoding': contentEncoding,
        ...?headers,
      },
      extra: <String, dynamic>{
        'secure': <Map<String, String>>[
          {
            'type': 'http',
            'scheme': 'bearer',
            'name': 'ingestKeyBearer',
          },{
            'type': 'apiKey',
            'name': 'ingestKey',
            'keyName': 'X-Foresignal-Ingest-Key',
            'where': 'header',
          },{
            'type': 'http',
            'scheme': 'basic',
            'name': 'ingestKeyBasic',
          },{
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
      _bodyData = jsonEncode(ingestRecordsRequest);

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

    IngestOutcome? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<IngestOutcome, IngestOutcome>(rawData, 'IngestOutcome', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<IngestOutcome>(
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

  /// Push records in a named format
  /// Parses the body with the named &#x60;IngestFormat&#x60; and submits one batch to the single writer (same versioning, policies, live/touch semantics, change stream, triggers and retention as polled data). Credentials: &#x60;X-Foresignal-Ingest-Key&#x60;, &#x60;Authorization: Bearer eik_…&#x60;, Basic with the key as password, a device key (039, bound to one entity), or an &#x60;admin&#x60; token; a configured signature verifier must also pass unless &#x60;keyOptional&#x60;. 200 when the request is valid even with per-record rejections (MISSING_OBSERVED_AT, LOW_ACCURACY, …). OwnTracks, Overland, OsmAnd and GPSLogger get their native reply shapes; the device presets (059) reply as their firmware expects (ecowitt an empty 200, wu-pws &#x60;success&#x60;, gmc &#x60;OK.&#x60;). &#x60;Content-Encoding: gzip&#x60; is accepted (decompressed size capped at 5 MB → 413). The &#x60;same&#x60; format (086) reads one SAME header per line (multimon-ng &#x60;EAS: ZCZC-…&#x60; output or bare headers), ignores &#x60;NNNN&#x60;, collapses an identical header repeated within 10 s, and stores &#x60;same-alert&#x60; records on the named counties (the state&#39;s counties for &#x60;PSS000&#x60;) with &#x60;validUntil&#x60; &#x3D; issue + purge. The &#x60;chirpstack-v4&#x60; format maps &#x60;?event&#x3D;up&#x60;, moves the device on &#x60;?event&#x3D;location&#x60;, acknowledges other events and refuses Protobuf (415). An &#x60;admin&#x60; token is the operator and is not asked for a sender signature.  **Scope:** ingest key (or an &#x60;admin&#x60; token).
  ///
  /// Parameters:
  /// * [sourceId] - A push-mode source id
  /// * [format] - Ingest format id: native, alertmanager, cloudevents, cap, mapping (024); owntracks, osmand, overland, gpslogger (039); ecowitt, wu-pws, aiscatcher, chirpstack-v4, tts-v3, airrohr, gmc (059 device presets); meshcore-json (083, the MeshCore bridge's events for the meshcore-bridge source); same (086, NOAA Weather Radio SAME headers as text/plain, for a custom source with the same-alert kind). `media`/`rasters` (054) and `mapping-preview` are reserved and served by their own operations. Unknown → 404 FORMAT_UNKNOWN
  /// * [ingestRecordsFormatRequest] - Body shape depends on the format; max 5 MB and 10,000 records
  /// * [xSignature256] - hmac-sha256 verifier: `sha256=<hex>` (header name configurable)
  /// * [webhookId] - standard-webhooks verifier
  /// * [webhookTimestamp] - standard-webhooks verifier
  /// * [webhookSignature] - standard-webhooks verifier: `v1,<base64>`
  /// * [xForesignalTimestamp] - foresignal verifier
  /// * [xForesignalSignature] - foresignal verifier: `sha256=HMAC(\"<ts>.<body>\")`
  /// * [ceId] - CloudEvents binary mode attribute `id`
  /// * [ceSource] - CloudEvents binary mode attribute `source`
  /// * [ceType] - CloudEvents binary mode attribute `type`
  /// * [ceSubject] - CloudEvents binary mode attribute `subject`
  /// * [ceTime] - CloudEvents binary mode attribute `time`
  /// * [ceSpecversion] - CloudEvents binary mode attribute `specversion`
  /// * [xLimitU] - OwnTracks user (externalId = `X-Limit-U/X-Limit-D`)
  /// * [xLimitD] - OwnTracks device
  /// * [xForesignalExternalId] - Overrides the externalId where the format allows it
  /// * [contentEncoding] - `gzip` accepted (AIS-catcher `GZIP on`); decompressed size capped at 5 MB → 413
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [IngestOutcome] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<IngestOutcome>> ingestRecordsFormat({ 
    required String sourceId,
    required String format,
    required IngestRecordsFormatRequest ingestRecordsFormatRequest,
    String? xSignature256,
    String? webhookId,
    String? webhookTimestamp,
    String? webhookSignature,
    String? xForesignalTimestamp,
    String? xForesignalSignature,
    String? ceId,
    String? ceSource,
    String? ceType,
    String? ceSubject,
    String? ceTime,
    String? ceSpecversion,
    String? xLimitU,
    String? xLimitD,
    String? xForesignalExternalId,
    String? contentEncoding,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/ingest/{sourceId}/{format}'.replaceAll('{' r'sourceId' '}', sourceId.toString()).replaceAll('{' r'format' '}', format.toString());
    final _options = Options(
      method: r'POST',
      headers: <String, dynamic>{
        if (xSignature256 != null) r'X-Signature-256': xSignature256,
        if (webhookId != null) r'webhook-id': webhookId,
        if (webhookTimestamp != null) r'webhook-timestamp': webhookTimestamp,
        if (webhookSignature != null) r'webhook-signature': webhookSignature,
        if (xForesignalTimestamp != null) r'X-Foresignal-Timestamp': xForesignalTimestamp,
        if (xForesignalSignature != null) r'X-Foresignal-Signature': xForesignalSignature,
        if (ceId != null) r'ce-id': ceId,
        if (ceSource != null) r'ce-source': ceSource,
        if (ceType != null) r'ce-type': ceType,
        if (ceSubject != null) r'ce-subject': ceSubject,
        if (ceTime != null) r'ce-time': ceTime,
        if (ceSpecversion != null) r'ce-specversion': ceSpecversion,
        if (xLimitU != null) r'X-Limit-U': xLimitU,
        if (xLimitD != null) r'X-Limit-D': xLimitD,
        if (xForesignalExternalId != null) r'X-Foresignal-External-Id': xForesignalExternalId,
        if (contentEncoding != null) r'Content-Encoding': contentEncoding,
        ...?headers,
      },
      extra: <String, dynamic>{
        'secure': <Map<String, String>>[
          {
            'type': 'http',
            'scheme': 'bearer',
            'name': 'ingestKeyBearer',
          },{
            'type': 'apiKey',
            'name': 'ingestKey',
            'keyName': 'X-Foresignal-Ingest-Key',
            'where': 'header',
          },{
            'type': 'http',
            'scheme': 'basic',
            'name': 'ingestKeyBasic',
          },{
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
      _bodyData = jsonEncode(ingestRecordsFormatRequest);

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

    IngestOutcome? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<IngestOutcome, IngestOutcome>(rawData, 'IngestOutcome', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<IngestOutcome>(
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

  /// Push a fix through a query-string format
  /// GET form of the formats that declare it: &#x60;osmand&#x60; (Traccar Client, OsmAnd) and &#x60;gpslogger&#x60; (039), &#x60;wu-pws&#x60; and &#x60;gmc&#x60; (059); other formats → 405. Fields come from the query string (see &#x60;OsmAndQuery&#x60;, &#x60;GpsLoggerFix&#x60;; the device presets map the query as a flat object of strings). Replies: an empty 200 (OsmAnd, GPSLogger), &#x60;200 text/plain success&#x60; (wu-pws), &#x60;200 text/plain OK.&#x60; (gmc, unverified). The WU protocol&#39;s &#x60;PASSWORD&#x60; parameter carries the ingest key (&#x60;ID&#x60; is ignored). GET pushes from a key holder must come from &#x60;ingestAllowCidrs&#x60; (default: private ranges; else 403 INGEST_SOURCE_DENIED). Keys in query strings (&#x60;key&#x60;, &#x60;PASSWORD&#x60;, &#x60;PASSKEY&#x60;, &#x60;auth&#x60;, &#x60;token&#x60;) are redacted from every log and fetch log.  **Scope:** ingest key (or an &#x60;admin&#x60; token).
  ///
  /// Parameters:
  /// * [sourceId] - A push-mode source id
  /// * [format] - Ingest format id: native, alertmanager, cloudevents, cap, mapping (024); owntracks, osmand, overland, gpslogger (039); ecowitt, wu-pws, aiscatcher, chirpstack-v4, tts-v3, airrohr, gmc (059 device presets); meshcore-json (083, the MeshCore bridge's events for the meshcore-bridge source); same (086, NOAA Weather Radio SAME headers as text/plain, for a custom source with the same-alert kind). `media`/`rasters` (054) and `mapping-preview` are reserved and served by their own operations. Unknown → 404 FORMAT_UNKNOWN
  /// * [id] - OsmAnd device id (must match a device key's bound entity)
  /// * [deviceid] - OsmAnd device id alias
  /// * [lat] - Latitude
  /// * [lon] - Longitude
  /// * [location] - lat,lon
  /// * [timestamp] - epoch s, epoch ms, ISO, or yyyy-MM-dd HH:mm:ss UTC
  /// * [time] - GPSLogger %TIME
  /// * [speed] - OsmAnd speed (knots)
  /// * [spd] - GPSLogger speed (m/s)
  /// * [bearing] - Bearing (deg)
  /// * [heading] - Heading (deg)
  /// * [dir] - GPSLogger direction
  /// * [altitude] - Altitude (m)
  /// * [alt] - GPSLogger altitude
  /// * [accuracy] - Accuracy (m)
  /// * [acc] - GPSLogger accuracy
  /// * [batt] - Battery (%)
  /// * [charge] - Charging
  /// * [valid] - false → rejected
  /// * [dev] - GPSLogger device (%SER)
  /// * [PASSWORD] - Weather Underground protocol key (wu-pws); redacted in logs
  /// * [ID] - Weather Underground station id (ignored)
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [String] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<String>> ingestRecordsFormatGet({ 
    required String sourceId,
    required String format,
    String? id,
    String? deviceid,
    num? lat,
    num? lon,
    String? location,
    String? timestamp,
    String? time,
    num? speed,
    num? spd,
    num? bearing,
    num? heading,
    num? dir,
    num? altitude,
    num? alt,
    num? accuracy,
    num? acc,
    num? batt,
    bool? charge,
    bool? valid,
    String? dev,
    String? PASSWORD,
    String? ID,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/ingest/{sourceId}/{format}'.replaceAll('{' r'sourceId' '}', sourceId.toString()).replaceAll('{' r'format' '}', format.toString());
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
            'name': 'ingestKeyBearer',
          },{
            'type': 'apiKey',
            'name': 'ingestKey',
            'keyName': 'X-Foresignal-Ingest-Key',
            'where': 'header',
          },{
            'type': 'http',
            'scheme': 'basic',
            'name': 'ingestKeyBasic',
          },{
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
      if (id != null) r'id': id,
      if (deviceid != null) r'deviceid': deviceid,
      if (lat != null) r'lat': lat,
      if (lon != null) r'lon': lon,
      if (location != null) r'location': location,
      if (timestamp != null) r'timestamp': timestamp,
      if (time != null) r'time': time,
      if (speed != null) r'speed': speed,
      if (spd != null) r'spd': spd,
      if (bearing != null) r'bearing': bearing,
      if (heading != null) r'heading': heading,
      if (dir != null) r'dir': dir,
      if (altitude != null) r'altitude': altitude,
      if (alt != null) r'alt': alt,
      if (accuracy != null) r'accuracy': accuracy,
      if (acc != null) r'acc': acc,
      if (batt != null) r'batt': batt,
      if (charge != null) r'charge': charge,
      if (valid != null) r'valid': valid,
      if (dev != null) r'dev': dev,
      if (PASSWORD != null) r'PASSWORD': PASSWORD,
      if (ID != null) r'ID': ID,
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

  /// Push records with the key in the path
  /// Path-key form of &#x60;ingestRecordsFormat&#x60; (query, form or JSON bodies) for tracker apps and device firmware that can set only a host and path (Ecowitt Customized upload, 059). Same limits, rejections and native replies. An &#x60;eik_&#x60; key in the path needs the source&#39;s &#x60;allowPathKey&#x60; (else 404); the sender must be in &#x60;ingestAllowCidrs&#x60; (default: private ranges; else 403 INGEST_SOURCE_DENIED).  **Scope:** open (no token; the credential is in the request).
  ///
  /// Parameters:
  /// * [sourceId] - A push-mode source id
  /// * [key] - Ingest or device key carried in the path for apps that cannot send credentials (redacted from every log)
  /// * [format] - Ingest format; ecowitt, gmc and mapping (059) need the source's allowPathKey
  /// * [contentEncoding] - `gzip` accepted (AIS-catcher `GZIP on`); decompressed size capped at 5 MB → 413
  /// * [ingestRecordsPathKeyRequest] 
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [IngestRecordsPathKey200Response] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<IngestRecordsPathKey200Response>> ingestRecordsPathKey({ 
    required String sourceId,
    required String key,
    required String format,
    String? contentEncoding,
    IngestRecordsPathKeyRequest? ingestRecordsPathKeyRequest,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/ingest/{sourceId}/k/{key}/{format}'.replaceAll('{' r'sourceId' '}', sourceId.toString()).replaceAll('{' r'key' '}', key.toString()).replaceAll('{' r'format' '}', format.toString());
    final _options = Options(
      method: r'POST',
      headers: <String, dynamic>{
        if (contentEncoding != null) r'Content-Encoding': contentEncoding,
        ...?headers,
      },
      extra: <String, dynamic>{
        'secure': <Map<String, String>>[
          {
            'type': 'apiKey',
            'name': 'ingestKeyPath',
            'keyName': 'ingestKey',
            'where': 'query',
          },
        ],
        ...?extra,
      },
      contentType: 'application/json',
      validateStatus: validateStatus,
    );

    dynamic _bodyData;

    try {
      _bodyData = jsonEncode(ingestRecordsPathKeyRequest);

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

    IngestRecordsPathKey200Response? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<IngestRecordsPathKey200Response, IngestRecordsPathKey200Response>(rawData, 'IngestRecordsPathKey200Response', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<IngestRecordsPathKey200Response>(
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

  /// Push a fix with a path key
  /// Path-key form of &#x60;ingestRecordsFormatGet&#x60; for OsmAnd/Traccar Client, GPSLogger and the GQ GMC counter (059), which cannot send credentials. &#x60;id&#x60;/&#x60;deviceid&#x60; must match a device key&#39;s bound entity (403 DEVICE_ENTITY_MISMATCH). An &#x60;eik_&#x60; key in the path needs the source&#39;s &#x60;allowPathKey&#x60; (else 404); the sender must be in &#x60;ingestAllowCidrs&#x60; (default: private ranges; else 403 INGEST_SOURCE_DENIED). The firmware sends cleartext HTTP.  **Scope:** open (no token; the credential is in the request).
  ///
  /// Parameters:
  /// * [sourceId] - A push-mode source id
  /// * [key] - Ingest or device key carried in the path for apps that cannot send credentials (redacted from every log)
  /// * [format] - Ingest format; ecowitt, gmc and mapping (059) need the source's allowPathKey
  /// * [id] - Device id
  /// * [deviceid] - Device id alias
  /// * [lat] - Latitude
  /// * [lon] - Longitude
  /// * [location] - lat,lon
  /// * [timestamp] - Timestamp
  /// * [speed] - knots
  /// * [bearing] - deg
  /// * [altitude] - m
  /// * [accuracy] - m
  /// * [batt] - %
  /// * [charge] - Charging
  /// * [valid] - false → rejected
  /// * [time] - GPSLogger time
  /// * [spd] - m/s
  /// * [dir] - deg
  /// * [alt] - m
  /// * [acc] - m
  /// * [dev] - GPSLogger device
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future]
  /// Throws [DioException] if API call or serialization fails
  Future<Response<void>> ingestRecordsPathKeyGet({ 
    required String sourceId,
    required String key,
    required String format,
    String? id,
    String? deviceid,
    num? lat,
    num? lon,
    String? location,
    String? timestamp,
    num? speed,
    num? bearing,
    num? altitude,
    num? accuracy,
    num? batt,
    bool? charge,
    bool? valid,
    String? time,
    num? spd,
    num? dir,
    num? alt,
    num? acc,
    String? dev,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/ingest/{sourceId}/k/{key}/{format}'.replaceAll('{' r'sourceId' '}', sourceId.toString()).replaceAll('{' r'key' '}', key.toString()).replaceAll('{' r'format' '}', format.toString());
    final _options = Options(
      method: r'GET',
      headers: <String, dynamic>{
        ...?headers,
      },
      extra: <String, dynamic>{
        'secure': <Map<String, String>>[
          {
            'type': 'apiKey',
            'name': 'ingestKeyPath',
            'keyName': 'ingestKey',
            'where': 'query',
          },
        ],
        ...?extra,
      },
      validateStatus: validateStatus,
    );

    final _queryParameters = <String, dynamic>{
      if (id != null) r'id': id,
      if (deviceid != null) r'deviceid': deviceid,
      if (lat != null) r'lat': lat,
      if (lon != null) r'lon': lon,
      if (location != null) r'location': location,
      if (timestamp != null) r'timestamp': timestamp,
      if (speed != null) r'speed': speed,
      if (bearing != null) r'bearing': bearing,
      if (altitude != null) r'altitude': altitude,
      if (accuracy != null) r'accuracy': accuracy,
      if (batt != null) r'batt': batt,
      if (charge != null) r'charge': charge,
      if (valid != null) r'valid': valid,
      if (time != null) r'time': time,
      if (spd != null) r'spd': spd,
      if (dir != null) r'dir': dir,
      if (alt != null) r'alt': alt,
      if (acc != null) r'acc': acc,
      if (dev != null) r'dev': dev,
    };

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    return _response;
  }

  /// Push a decoded image (remote station)
  /// A single image (&#x60;file&#x60;, PNG, JPEG or TIFF, ≤ 25 MB) plus &#x60;meta&#x60;. The running station passes it through ImageGuard before decoding, stores it with 256/1024 px thumbnails and attaches it to the matching pass: &#x60;meta.passId&#x60; when the station knows that pass, else the 049 join rule (same satellite, timestamp inside [first lock − 5 min, LOS + 5 min]), else a pass minted from the image; the pass gets a new &#x60;decoded&#x60; version. Only sources whose descriptor accepts remote media (the &#x60;satdump-{station}&#x60; family) answer; any other source, or a station whose stream is not running, is 404. &#x60;media&#x60; is a reserved segment that never routes to an IngestFormat. Each push costs one unit of the source&#39;s budget (429 &#x60;RATE_LIMITED&#x60; with &#x60;Retry-After&#x60; when spent). Syncing SatDump&#39;s output into the inbox (rsync or Syncthing) remains the recommended remote path.  **Scope:** &#x60;open&#x60; (the source&#39;s ingest key, or an admin token, is checked by the endpoint). A &#x60;satdump-{station}&#x60; source has no minted ingest key: the operator stores the key a remote station presents as the source&#39;s &#x60;ingestKey&#x60; credential (&#x60;PUT /v1/sources/{id}/credentials&#x60;).
  ///
  /// Parameters:
  /// * [sourceId] - A source whose descriptor sets `acceptsRemoteMedia=true` (the `satdump-*` family)
  /// * [file] - PNG, JPEG or TIFF, ≤ 25 MB; passes ImageGuard before decoding
  /// * [meta] 
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [RemoteMediaAccepted] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<RemoteMediaAccepted>> ingestSourceMedia({ 
    required String sourceId,
    required MultipartFile file,
    required RemoteMediaMeta meta,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/ingest/{sourceId}/media'.replaceAll('{' r'sourceId' '}', sourceId.toString());
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
            'name': 'ingestKeyBearer',
          },{
            'type': 'apiKey',
            'name': 'ingestKey',
            'keyName': 'X-Foresignal-Ingest-Key',
            'where': 'header',
          },{
            'type': 'http',
            'scheme': 'basic',
            'name': 'ingestKeyBasic',
          },{
            'type': 'http',
            'scheme': 'bearer',
            'name': 'bearerToken',
          },
        ],
        ...?extra,
      },
      contentType: 'multipart/form-data',
      validateStatus: validateStatus,
    );

    dynamic _bodyData;

    try {
      _bodyData = FormData.fromMap(<String, dynamic>{
        r'file': file,
        r'meta': meta,
      });

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

    RemoteMediaAccepted? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<RemoteMediaAccepted, RemoteMediaAccepted>(rawData, 'RemoteMediaAccepted', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<RemoteMediaAccepted>(
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

  /// Push a pass bundle (remote station)
  /// A &#x60;meta&#x60; part &#x60;{passFolder, files[], product?}&#x60; plus file parts named &#x60;files&#x60; (&#x60;dataset.json&#x60;, &#x60;product.cbor&#x60;, PNG, JPEG, TIF), each ≤ 25 MB and ≤ 200 MB in total (413 &#x60;MEDIA_TOO_LARGE&#x60;). Names are single safe path segments (no separators, no &#x60;..&#x60;, no leading dot); every listed file needs a part and every part must be listed; images pass the ImageGuard header check (400). &#x60;dataset.json&#x60; is placed in the pass folder and every other file in the product directory: &#x60;product&#x60; when given, else the single product &#x60;dataset.json&#x60; lists, else the pass folder itself. The bundle is written atomically to &#x60;${FORESIGNAL_DATA_DIR}/rasters/satdump/{station}/incoming/{uploadId}/&#x60; (the inbox mount is read-only); the station&#39;s inbox loop processes it like a ready pass folder on its next scan (composites, thumbnails, provenance, polar GeoTIFF overlays, footprint or approximate swath) and the copy expires after 2 days. Only &#x60;satdump-{station}&#x60; sources answer; others are 404. &#x60;rasters&#x60; is a reserved segment that never routes to an IngestFormat. Each push costs one unit of the source&#39;s budget.  **Scope:** &#x60;open&#x60; (the source&#39;s ingest key, or an admin token, is checked by the endpoint). The ingest key of a &#x60;satdump-{station}&#x60; source is its &#x60;ingestKey&#x60; credential.
  ///
  /// Parameters:
  /// * [sourceId] - A source whose descriptor sets `acceptsRemoteMedia=true` (the `satdump-*` family)
  /// * [meta] 
  /// * [files] - File parts (`dataset.json`, `product.cbor`, PNG, TIF), each ≤ 25 MB; bundle ≤ 200 MB
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [RemoteRasterBundleAccepted] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<RemoteRasterBundleAccepted>> ingestSourceRasters({ 
    required String sourceId,
    required RemoteRasterBundleMeta meta,
    required List<MultipartFile> files,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/ingest/{sourceId}/rasters'.replaceAll('{' r'sourceId' '}', sourceId.toString());
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
            'name': 'ingestKeyBearer',
          },{
            'type': 'apiKey',
            'name': 'ingestKey',
            'keyName': 'X-Foresignal-Ingest-Key',
            'where': 'header',
          },{
            'type': 'http',
            'scheme': 'basic',
            'name': 'ingestKeyBasic',
          },{
            'type': 'http',
            'scheme': 'bearer',
            'name': 'bearerToken',
          },
        ],
        ...?extra,
      },
      contentType: 'multipart/form-data',
      validateStatus: validateStatus,
    );

    dynamic _bodyData;

    try {
      _bodyData = FormData.fromMap(<String, dynamic>{
        r'meta': meta,
        r'files': files.toList(),
      });

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

    RemoteRasterBundleAccepted? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<RemoteRasterBundleAccepted, RemoteRasterBundleAccepted>(rawData, 'RemoteRasterBundleAccepted', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<RemoteRasterBundleAccepted>(
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

  /// CloudEvents webhook validation handshake
  /// CloudEvents HTTP webhook abuse protection: answers &#x60;OPTIONS&#x60; carrying &#x60;WebHook-Request-Origin&#x60; with 200, &#x60;WebHook-Allowed-Origin&#x60; (the requesting origin) and &#x60;WebHook-Allowed-Rate&#x60; (the source&#39;s per-minute budget). Only the &#x60;cloudevents&#x60; format answers; others → 404.  **Scope:** ingest key (or an &#x60;admin&#x60; token).
  ///
  /// Parameters:
  /// * [sourceId] - A push-mode source id
  /// * [format] - Ingest format id: native, alertmanager, cloudevents, cap, mapping (024); owntracks, osmand, overland, gpslogger (039); ecowitt, wu-pws, aiscatcher, chirpstack-v4, tts-v3, airrohr, gmc (059 device presets); meshcore-json (083, the MeshCore bridge's events for the meshcore-bridge source); same (086, NOAA Weather Radio SAME headers as text/plain, for a custom source with the same-alert kind). `media`/`rasters` (054) and `mapping-preview` are reserved and served by their own operations. Unknown → 404 FORMAT_UNKNOWN
  /// * [webHookRequestOrigin] - Origin requesting permission to deliver
  /// * [webHookRequestRate] - Requested delivery rate
  /// * [webHookRequestCallback] - Callback URL for asynchronous validation (not used)
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future]
  /// Throws [DioException] if API call or serialization fails
  Future<Response<void>> ingestWebhookHandshake({ 
    required String sourceId,
    required String format,
    required String webHookRequestOrigin,
    String? webHookRequestRate,
    String? webHookRequestCallback,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/ingest/{sourceId}/{format}'.replaceAll('{' r'sourceId' '}', sourceId.toString()).replaceAll('{' r'format' '}', format.toString());
    final _options = Options(
      method: r'OPTIONS',
      headers: <String, dynamic>{
        r'WebHook-Request-Origin': webHookRequestOrigin,
        if (webHookRequestRate != null) r'WebHook-Request-Rate': webHookRequestRate,
        if (webHookRequestCallback != null) r'WebHook-Request-Callback': webHookRequestCallback,
        ...?headers,
      },
      extra: <String, dynamic>{
        'secure': <Map<String, String>>[
          {
            'type': 'http',
            'scheme': 'bearer',
            'name': 'ingestKeyBearer',
          },{
            'type': 'apiKey',
            'name': 'ingestKey',
            'keyName': 'X-Foresignal-Ingest-Key',
            'where': 'header',
          },{
            'type': 'http',
            'scheme': 'basic',
            'name': 'ingestKeyBasic',
          },{
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

  /// List captured raw ingest samples
  /// The last 5 raw requests captured while the source&#39;s &#x60;captureSamples&#x60; setting is on (auto-off after 1 h). Held in memory only, redacted (&#x60;/k/{key}/&#x60;, &#x60;PASSWORD&#x3D;&#x60;, &#x60;PASSKEY&#x3D;&#x60;, &#x60;key&#x3D;&#x60;), headers dropped except &#x60;Content-Type&#x60;. Used to author mappings and fixtures.  **Scope:** &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [sourceId] - Custom source id (custom-…)
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [List<CapturedSample>] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<List<CapturedSample>>> listCustomSourceSamples({ 
    required String sourceId,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/custom-sources/{sourceId}/samples'.replaceAll('{' r'sourceId' '}', sourceId.toString());
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

    List<CapturedSample>? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<List<CapturedSample>, CapturedSample>(rawData, 'List<CapturedSample>', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<List<CapturedSample>>(
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

  /// List custom push sources
  /// List custom push sources  **Scope:** &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [List<CustomSource>] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<List<CustomSource>>> listCustomSources({ 
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/custom-sources';
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

    List<CustomSource>? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<List<CustomSource>, CustomSource>(rawData, 'List<CustomSource>', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<List<CustomSource>>(
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

  /// List device keys of a source
  /// Key values are never returned.  **Scope:** &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [sourceId] - Custom source id
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [List<IngestDevice>] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<List<IngestDevice>>> listIngestDevices({ 
    required String sourceId,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/custom-sources/{sourceId}/devices'.replaceAll('{' r'sourceId' '}', sourceId.toString());
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

    List<IngestDevice>? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<List<IngestDevice>, IngestDevice>(rawData, 'List<IngestDevice>', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<List<IngestDevice>>(
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

  /// List ingest formats
  /// Registered &#x60;IngestFormat&#x60; beans with their media types, options and supported signature verifiers, plus reserved names.  **Scope:** &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [List<IngestFormatInfo>] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<List<IngestFormatInfo>>> listIngestFormats({ 
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/ingest/formats';
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

    List<IngestFormatInfo>? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<List<IngestFormatInfo>, IngestFormatInfo>(rawData, 'List<IngestFormatInfo>', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<List<IngestFormatInfo>>(
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

  /// ntfy-compatible publish (POST)
  /// ntfy-compatible publish (Uptime Kuma, scripts, homelab tools). Credentials: Bearer or Basic with the ingest key, or &#x60;?auth&#x3D;&#x60;. Kind &#x3D; the source&#39;s single kind (else &#x60;notification&#x60;); payload &#x60;{title, message, priority, tags, click, topic}&#x60;. Outside &#x60;/v1&#x60;; AccessPolicy rule &#x60;/compat/ntfy/_**&#x60; uses the ingest authenticator. Body &#x3D; message text.  **Scope:** ingest key (or an &#x60;admin&#x60; token).
  ///
  /// Parameters:
  /// * [topic] - ntfy topic = the custom source id
  /// * [xTitle] - Title (aliases Title, t)
  /// * [xPriority] - 1–5 or min|low|default|high|max|urgent (aliases Priority, p)
  /// * [xTags] - Comma-separated tags (aliases Tags, ta); tag `id=<x>` sets the externalId
  /// * [xClick] - Click URL
  /// * [xForesignalExternalId] - Explicit externalId (else a ULID per message)
  /// * [auth] - ntfy compat: base64 of an Authorization header value carrying the ingest key; redacted in logs and fetch_log
  /// * [body] - Message text
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [NtfyMessage] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<NtfyMessage>> ntfyPublish({ 
    required String topic,
    String? xTitle,
    String? xPriority,
    String? xTags,
    String? xClick,
    String? xForesignalExternalId,
    String? auth,
    String? body,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/compat/ntfy/{topic}'.replaceAll('{' r'topic' '}', topic.toString());
    final _options = Options(
      method: r'POST',
      headers: <String, dynamic>{
        if (xTitle != null) r'X-Title': xTitle,
        if (xPriority != null) r'X-Priority': xPriority,
        if (xTags != null) r'X-Tags': xTags,
        if (xClick != null) r'X-Click': xClick,
        if (xForesignalExternalId != null) r'X-Foresignal-External-Id': xForesignalExternalId,
        ...?headers,
      },
      extra: <String, dynamic>{
        'secure': <Map<String, String>>[
          {
            'type': 'http',
            'scheme': 'bearer',
            'name': 'ingestKeyBearer',
          },{
            'type': 'apiKey',
            'name': 'ingestKey',
            'keyName': 'X-Foresignal-Ingest-Key',
            'where': 'header',
          },{
            'type': 'http',
            'scheme': 'basic',
            'name': 'ingestKeyBasic',
          },{
            'type': 'http',
            'scheme': 'bearer',
            'name': 'bearerToken',
          },
        ],
        ...?extra,
      },
      contentType: 'text/plain',
      validateStatus: validateStatus,
    );

    final _queryParameters = <String, dynamic>{
      if (auth != null) r'auth': auth,
    };

    dynamic _bodyData;

    try {
      _bodyData = jsonEncode(body);

    } catch(error, stackTrace) {
      throw DioException(
         requestOptions: _options.compose(
          _dio.options,
          _path,
          queryParameters: _queryParameters,
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
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    NtfyMessage? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<NtfyMessage, NtfyMessage>(rawData, 'NtfyMessage', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<NtfyMessage>(
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

  /// ntfy-compatible JSON publish
  /// ntfy-compatible publish (Uptime Kuma, scripts, homelab tools). Credentials: Bearer or Basic with the ingest key, or &#x60;?auth&#x3D;&#x60;. Kind &#x3D; the source&#39;s single kind (else &#x60;notification&#x60;); payload &#x60;{title, message, priority, tags, click, topic}&#x60;. Outside &#x60;/v1&#x60;; AccessPolicy rule &#x60;/compat/ntfy/_**&#x60; uses the ingest authenticator. JSON body &#x60;{topic, message, title, priority, tags, click}&#x60; (Uptime Kuma shape).  **Scope:** ingest key (or an &#x60;admin&#x60; token).
  ///
  /// Parameters:
  /// * [ntfyPublishRequest] 
  /// * [auth] - ntfy compat: base64 of an Authorization header value carrying the ingest key; redacted in logs and fetch_log
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [NtfyMessage] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<NtfyMessage>> ntfyPublishJson({ 
    required NtfyPublishRequest ntfyPublishRequest,
    String? auth,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/compat/ntfy';
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
            'name': 'ingestKeyBearer',
          },{
            'type': 'apiKey',
            'name': 'ingestKey',
            'keyName': 'X-Foresignal-Ingest-Key',
            'where': 'header',
          },{
            'type': 'http',
            'scheme': 'basic',
            'name': 'ingestKeyBasic',
          },{
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

    final _queryParameters = <String, dynamic>{
      if (auth != null) r'auth': auth,
    };

    dynamic _bodyData;

    try {
      _bodyData = jsonEncode(ntfyPublishRequest);

    } catch(error, stackTrace) {
      throw DioException(
         requestOptions: _options.compose(
          _dio.options,
          _path,
          queryParameters: _queryParameters,
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
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    NtfyMessage? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<NtfyMessage, NtfyMessage>(rawData, 'NtfyMessage', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<NtfyMessage>(
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

  /// ntfy-compatible publish (PUT)
  /// ntfy-compatible publish (Uptime Kuma, scripts, homelab tools). Credentials: Bearer or Basic with the ingest key, or &#x60;?auth&#x3D;&#x60;. Kind &#x3D; the source&#39;s single kind (else &#x60;notification&#x60;); payload &#x60;{title, message, priority, tags, click, topic}&#x60;. Outside &#x60;/v1&#x60;; AccessPolicy rule &#x60;/compat/ntfy/_**&#x60; uses the ingest authenticator. Body &#x3D; message text.  **Scope:** ingest key (or an &#x60;admin&#x60; token).
  ///
  /// Parameters:
  /// * [topic] - ntfy topic = the custom source id
  /// * [xTitle] - Title (aliases Title, t)
  /// * [xPriority] - 1–5 or min|low|default|high|max|urgent (aliases Priority, p)
  /// * [xTags] - Comma-separated tags (aliases Tags, ta); tag `id=<x>` sets the externalId
  /// * [xClick] - Click URL
  /// * [xForesignalExternalId] - Explicit externalId (else a ULID per message)
  /// * [auth] - ntfy compat: base64 of an Authorization header value carrying the ingest key; redacted in logs and fetch_log
  /// * [body] - Message text
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [NtfyMessage] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<NtfyMessage>> ntfyPublishPut({ 
    required String topic,
    String? xTitle,
    String? xPriority,
    String? xTags,
    String? xClick,
    String? xForesignalExternalId,
    String? auth,
    String? body,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/compat/ntfy/{topic}'.replaceAll('{' r'topic' '}', topic.toString());
    final _options = Options(
      method: r'PUT',
      headers: <String, dynamic>{
        if (xTitle != null) r'X-Title': xTitle,
        if (xPriority != null) r'X-Priority': xPriority,
        if (xTags != null) r'X-Tags': xTags,
        if (xClick != null) r'X-Click': xClick,
        if (xForesignalExternalId != null) r'X-Foresignal-External-Id': xForesignalExternalId,
        ...?headers,
      },
      extra: <String, dynamic>{
        'secure': <Map<String, String>>[
          {
            'type': 'http',
            'scheme': 'bearer',
            'name': 'ingestKeyBearer',
          },{
            'type': 'apiKey',
            'name': 'ingestKey',
            'keyName': 'X-Foresignal-Ingest-Key',
            'where': 'header',
          },{
            'type': 'http',
            'scheme': 'basic',
            'name': 'ingestKeyBasic',
          },{
            'type': 'http',
            'scheme': 'bearer',
            'name': 'bearerToken',
          },
        ],
        ...?extra,
      },
      contentType: 'text/plain',
      validateStatus: validateStatus,
    );

    final _queryParameters = <String, dynamic>{
      if (auth != null) r'auth': auth,
    };

    dynamic _bodyData;

    try {
      _bodyData = jsonEncode(body);

    } catch(error, stackTrace) {
      throw DioException(
         requestOptions: _options.compose(
          _dio.options,
          _path,
          queryParameters: _queryParameters,
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
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    NtfyMessage? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<NtfyMessage, NtfyMessage>(rawData, 'NtfyMessage', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<NtfyMessage>(
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

  /// ntfy-compatible publish via GET
  /// ntfy-compatible publish (Uptime Kuma, scripts, homelab tools). Credentials: Bearer or Basic with the ingest key, or &#x60;?auth&#x3D;&#x60;. Kind &#x3D; the source&#39;s single kind (else &#x60;notification&#x60;); payload &#x60;{title, message, priority, tags, click, topic}&#x60;. Outside &#x60;/v1&#x60;; AccessPolicy rule &#x60;/compat/ntfy/_**&#x60; uses the ingest authenticator. Fields from the query string.  **Scope:** ingest key (or an &#x60;admin&#x60; token).
  ///
  /// Parameters:
  /// * [topic] - ntfy topic = the custom source id
  /// * [action] - ntfy action path segment
  /// * [message] - Message text
  /// * [title] - Title
  /// * [priority] - 1–5 or min…max
  /// * [tags] - Comma-separated tags
  /// * [auth] - ntfy compat: base64 of an Authorization header value carrying the ingest key; redacted in logs and fetch_log
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [NtfyMessage] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<NtfyMessage>> ntfyPublishViaGet({ 
    required String topic,
    required String action,
    String? message,
    String? title,
    String? priority,
    String? tags,
    String? auth,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/compat/ntfy/{topic}/{action}'.replaceAll('{' r'topic' '}', topic.toString()).replaceAll('{' r'action' '}', action.toString());
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
            'name': 'ingestKeyBearer',
          },{
            'type': 'apiKey',
            'name': 'ingestKey',
            'keyName': 'X-Foresignal-Ingest-Key',
            'where': 'header',
          },{
            'type': 'http',
            'scheme': 'basic',
            'name': 'ingestKeyBasic',
          },{
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
      if (message != null) r'message': message,
      if (title != null) r'title': title,
      if (priority != null) r'priority': priority,
      if (tags != null) r'tags': tags,
      if (auth != null) r'auth': auth,
    };

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    NtfyMessage? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<NtfyMessage, NtfyMessage>(rawData, 'NtfyMessage', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<NtfyMessage>(
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

  /// ntfy-compatible publish via action path
  /// ntfy-compatible publish (Uptime Kuma, scripts, homelab tools). Credentials: Bearer or Basic with the ingest key, or &#x60;?auth&#x3D;&#x60;. Kind &#x3D; the source&#39;s single kind (else &#x60;notification&#x60;); payload &#x60;{title, message, priority, tags, click, topic}&#x60;. Outside &#x60;/v1&#x60;; AccessPolicy rule &#x60;/compat/ntfy/_**&#x60; uses the ingest authenticator. Fields from the query string (body ignored).  **Scope:** ingest key (or an &#x60;admin&#x60; token).
  ///
  /// Parameters:
  /// * [topic] - ntfy topic = the custom source id
  /// * [action] - ntfy action path segment
  /// * [message] - Message text
  /// * [title] - Title
  /// * [priority] - 1–5 or min…max
  /// * [tags] - Comma-separated tags
  /// * [auth] - ntfy compat: base64 of an Authorization header value carrying the ingest key; redacted in logs and fetch_log
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [NtfyMessage] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<NtfyMessage>> ntfyPublishViaPost({ 
    required String topic,
    required String action,
    String? message,
    String? title,
    String? priority,
    String? tags,
    String? auth,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/compat/ntfy/{topic}/{action}'.replaceAll('{' r'topic' '}', topic.toString()).replaceAll('{' r'action' '}', action.toString());
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
            'name': 'ingestKeyBearer',
          },{
            'type': 'apiKey',
            'name': 'ingestKey',
            'keyName': 'X-Foresignal-Ingest-Key',
            'where': 'header',
          },{
            'type': 'http',
            'scheme': 'basic',
            'name': 'ingestKeyBasic',
          },{
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
      if (message != null) r'message': message,
      if (title != null) r'title': title,
      if (priority != null) r'priority': priority,
      if (tags != null) r'tags': tags,
      if (auth != null) r'auth': auth,
    };

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    NtfyMessage? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<NtfyMessage, NtfyMessage>(rawData, 'NtfyMessage', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<NtfyMessage>(
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

  /// Purge a device&#39;s entity and revoke its key
  /// Runs an entity purge for the device&#39;s bound entity (see &#x60;purgeRecord&#x60;) and revokes the key. Audited.  **Scope:** &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [sourceId] - Custom source id
  /// * [deviceId] - Ingest device id
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
  Future<Response<PurgeReport>> purgeDevice({ 
    required String sourceId,
    required String deviceId,
    PurgeRequest? purgeRequest,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/custom-sources/{sourceId}/devices/{deviceId}/purge'.replaceAll('{' r'sourceId' '}', sourceId.toString()).replaceAll('{' r'deviceId' '}', deviceId.toString());
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

  /// Rotate a device key
  /// Returns the new key and setup data once; the old key stops working.  **Scope:** &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [sourceId] - Custom source id
  /// * [deviceId] - Ingest device id
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [IngestDeviceCreated] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<IngestDeviceCreated>> rotateIngestDeviceKey({ 
    required String sourceId,
    required String deviceId,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/custom-sources/{sourceId}/devices/{deviceId}/key/rotate'.replaceAll('{' r'sourceId' '}', sourceId.toString()).replaceAll('{' r'deviceId' '}', deviceId.toString());
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
      validateStatus: validateStatus,
    );

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    IngestDeviceCreated? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<IngestDeviceCreated, IngestDeviceCreated>(rawData, 'IngestDeviceCreated', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<IngestDeviceCreated>(
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

  /// Rotate a custom source&#39;s ingest key
  /// Returns the new key exactly once; the old key stops working immediately.  **Scope:** &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [sourceId] - Custom source id (custom-…)
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [IngestKeyResponse] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<IngestKeyResponse>> rotateIngestKey({ 
    required String sourceId,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/custom-sources/{sourceId}/ingest-key/rotate'.replaceAll('{' r'sourceId' '}', sourceId.toString());
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
      validateStatus: validateStatus,
    );

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    IngestKeyResponse? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<IngestKeyResponse, IngestKeyResponse>(rawData, 'IngestKeyResponse', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<IngestKeyResponse>(
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

  /// Run a mapping over captured samples
  /// Runs the proposed mapping (body) or, when the body is empty or omits &#x60;mapping&#x60;, the stored one over the source&#39;s captured samples. Writes nothing. 400 &#x60;MAPPING_INVALID&#x60; for a mapping that does not compile.  **Scope:** &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [sourceId] - Custom source id (custom-…)
  /// * [mappingTestRequest] 
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [MappingTestResult] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<MappingTestResult>> testCustomSourceMapping({ 
    required String sourceId,
    MappingTestRequest? mappingTestRequest,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/custom-sources/{sourceId}/mapping/test'.replaceAll('{' r'sourceId' '}', sourceId.toString());
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
      _bodyData = jsonEncode(mappingTestRequest);

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

    MappingTestResult? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<MappingTestResult, MappingTestResult>(rawData, 'MappingTestResult', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<MappingTestResult>(
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

  /// Update a custom source
  /// The id cannot change. Removing a kind that has current heads → 409 KIND_IN_USE; changing &#x60;observedAtMode&#x60; while heads exist → 409 OBSERVED_AT_MODE_LOCKED; a SAMPLED/LATEST policy with emptied &#x60;significantFields&#x60; → 400 INGEST_POLICY_INVALID. Changed &#x60;volatileFields&#x60;/&#x60;significantFields&#x60; trigger the writer&#39;s lazy recipe migration. Verifier secrets are write-only (omit to keep).  **Scope:** &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [sourceId] - Custom source id (custom-…)
  /// * [customSourceInput] 
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [CustomSource] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<CustomSource>> updateCustomSource({ 
    required String sourceId,
    required CustomSourceInput customSourceInput,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/custom-sources/{sourceId}'.replaceAll('{' r'sourceId' '}', sourceId.toString());
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
      _bodyData = jsonEncode(customSourceInput);

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

    CustomSource? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<CustomSource, CustomSource>(rawData, 'CustomSource', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<CustomSource>(
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
