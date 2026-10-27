//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

import 'dart:async';

// ignore: unused_import
import 'dart:convert';
import 'package:foresignal_api/src/deserialize.dart';
import 'package:dio/dio.dart';

import 'package:foresignal_api/src/model/problem.dart';
import 'package:foresignal_api/src/model/stix_bundle.dart';
import 'package:foresignal_api/src/model/taxii_api_root.dart';
import 'package:foresignal_api/src/model/taxii_collection.dart';
import 'package:foresignal_api/src/model/taxii_collections.dart';
import 'package:foresignal_api/src/model/taxii_discovery.dart';
import 'package:foresignal_api/src/model/taxii_envelope.dart';

class StandardsApi {

  final Dio _dio;

  const StandardsApi(this._dio);

  /// Export a STIX 2.1 bundle
  /// &#x60;cisa-kev&#x60;/&#x60;nvd-cves&#x60; → &#x60;vulnerability&#x60;, abuse.ch IOCs → &#x60;indicator&#x60; (pattern from the IOC type), plus a Foresignal &#x60;identity&#x60;; deterministic UUIDv5 ids from record refs. Every record passes &#x60;EgressPolicy&#x60; on the EXPORT path (private sources, &#x60;LOCAL_ONLY&#x60; egress and license terms); refused records are counted in &#x60;Foresignal-Licence-Filtered&#x60;. Kinds: &#x60;vulnerability&#x60;, &#x60;malware-url&#x60;, &#x60;malware-c2&#x60; (default all three).  **Scope:** &#x60;agent&#x60;.
  ///
  /// Parameters:
  /// * [kind] - Record kinds (comma-separated)
  /// * [since] - observedAt ≥ since
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [StixBundle] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<StixBundle>> exportStix({ 
    List<String>? kind,
    DateTime? since,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/export/stix';
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
      if (kind != null) r'kind': kind,
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

    StixBundle? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<StixBundle, StixBundle>(rawData, 'StixBundle', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<StixBundle>(
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

  /// FDSN event service version
  /// Service version string.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [String] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<String>> getFdsnEventVersion({ 
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/fdsnws/event/1/version';
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

  /// FDSN event WADL
  /// Minimal WADL describing the query parameters.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [String] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<String>> getFdsnEventWadl({ 
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/fdsnws/event/1/application.wadl';
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

  /// TAXII API root
  /// TAXII 2.1 API root.  **Scope:** &#x60;agent&#x60;.
  ///
  /// Parameters:
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [TaxiiApiRoot] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<TaxiiApiRoot>> getTaxiiApiRoot({ 
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/taxii2/foresignal/';
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

    TaxiiApiRoot? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<TaxiiApiRoot, TaxiiApiRoot>(rawData, 'TaxiiApiRoot', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<TaxiiApiRoot>(
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

  /// TAXII collection
  /// One TAXII collection.  **Scope:** &#x60;agent&#x60;.
  ///
  /// Parameters:
  /// * [collectionId] - Collection id
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [TaxiiCollection] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<TaxiiCollection>> getTaxiiCollection({ 
    required String collectionId,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/taxii2/foresignal/collections/{collectionId}/'.replaceAll('{' r'collectionId' '}', collectionId.toString());
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

    TaxiiCollection? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<TaxiiCollection, TaxiiCollection>(rawData, 'TaxiiCollection', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<TaxiiCollection>(
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

  /// TAXII collection objects
  /// STIX object versions in ingest order with &#x60;added_after&#x60; (strictly after the version&#39;s ingest time) and &#x60;next&#x60; pagination (repeat the same &#x60;added_after&#x60; with &#x60;next&#x60;); the default page is 100; &#x60;X-TAXII-Date-Added-First&#x60;/&#x60;-Last&#x60; headers.  **Scope:** &#x60;agent&#x60;.
  ///
  /// Parameters:
  /// * [collectionId] - Collection id
  /// * [addedAfter] - Objects added after
  /// * [limit] - Page size
  /// * [next] - Pagination token
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [TaxiiEnvelope] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<TaxiiEnvelope>> getTaxiiCollectionObjects({ 
    required String collectionId,
    DateTime? addedAfter,
    int? limit,
    String? next,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/taxii2/foresignal/collections/{collectionId}/objects/'.replaceAll('{' r'collectionId' '}', collectionId.toString());
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
      if (addedAfter != null) r'added_after': addedAfter,
      if (limit != null) r'limit': limit,
      if (next != null) r'next': next,
    };

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    TaxiiEnvelope? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<TaxiiEnvelope, TaxiiEnvelope>(rawData, 'TaxiiEnvelope', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<TaxiiEnvelope>(
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

  /// TAXII discovery
  /// TAXII 2.1 discovery.  **Scope:** &#x60;agent&#x60;.
  ///
  /// Parameters:
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [TaxiiDiscovery] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<TaxiiDiscovery>> getTaxiiDiscovery({ 
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/taxii2/';
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

    TaxiiDiscovery? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<TaxiiDiscovery, TaxiiDiscovery>(rawData, 'TaxiiDiscovery', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<TaxiiDiscovery>(
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

  /// TAXII collections
  /// Read-only collections (e.g. vulnerabilities, indicators).  **Scope:** &#x60;agent&#x60;.
  ///
  /// Parameters:
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [TaxiiCollections] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<TaxiiCollections>> listTaxiiCollections({ 
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/taxii2/foresignal/collections/';
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

    TaxiiCollections? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<TaxiiCollections, TaxiiCollections>(rawData, 'TaxiiCollections', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<TaxiiCollections>(
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

  /// FDSN event query
  /// fdsnws-event 1.2 over &#x60;earthquake&#x60; records: QuakeML 1.2 BED (&#x60;smi:foresignal/{source}/{externalId}&#x60; publicIDs) or the pipe-separated text format. &#x60;204&#x60; when no events match (FDSN convention; &#x60;nodata&#x3D;404&#x60; selects 404); errors use FDSN plain-text bodies. Long and short parameter names are accepted (&#x60;start&#x60;, &#x60;minlat&#x60;, &#x60;minmag&#x60;, …); unsupported parameters are a 400. Depth is in meters in QuakeML and in km in the text format.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).  **v2.0 (R2-013):** a signed-in user needs &#x60;data.read&#x60; (403 PERMISSION_DENIED otherwise, audited); what they read is clamped to their data restriction (&#x60;User.dataRestriction&#x60;) and source view. API tokens keep their scope rules.
  ///
  /// Parameters:
  /// * [starttime] - Origin time ≥
  /// * [endtime] - Origin time ≤
  /// * [minlatitude] - Box
  /// * [maxlatitude] - Box
  /// * [minlongitude] - Box
  /// * [maxlongitude] - Box
  /// * [latitude] - Radius centre
  /// * [longitude] - Radius centre
  /// * [minradius] - Degrees
  /// * [maxradius] - Degrees
  /// * [minmagnitude] - Magnitude ≥
  /// * [maxmagnitude] - Magnitude ≤
  /// * [orderby] - Ordering
  /// * [limit] - Maximum events
  /// * [format] - Output
  /// * [nodata] - Status of an empty result: 204 (default, FDSN convention) or 404 with an FDSN plain-text body
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [String] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<String>> queryFdsnEvents({ 
    DateTime? starttime,
    DateTime? endtime,
    num? minlatitude,
    num? maxlatitude,
    num? minlongitude,
    num? maxlongitude,
    num? latitude,
    num? longitude,
    num? minradius,
    num? maxradius,
    num? minmagnitude,
    num? maxmagnitude,
    String? orderby,
    int? limit,
    String? format,
    int? nodata,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/fdsnws/event/1/query';
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
      if (starttime != null) r'starttime': starttime,
      if (endtime != null) r'endtime': endtime,
      if (minlatitude != null) r'minlatitude': minlatitude,
      if (maxlatitude != null) r'maxlatitude': maxlatitude,
      if (minlongitude != null) r'minlongitude': minlongitude,
      if (maxlongitude != null) r'maxlongitude': maxlongitude,
      if (latitude != null) r'latitude': latitude,
      if (longitude != null) r'longitude': longitude,
      if (minradius != null) r'minradius': minradius,
      if (maxradius != null) r'maxradius': maxradius,
      if (minmagnitude != null) r'minmagnitude': minmagnitude,
      if (maxmagnitude != null) r'maxmagnitude': maxmagnitude,
      if (orderby != null) r'orderby': orderby,
      if (limit != null) r'limit': limit,
      if (format != null) r'format': format,
      if (nodata != null) r'nodata': nodata,
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

}
