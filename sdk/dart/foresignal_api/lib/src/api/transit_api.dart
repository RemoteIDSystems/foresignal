//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

import 'dart:async';

// ignore: unused_import
import 'dart:convert';
import 'package:foresignal_api/src/deserialize.dart';
import 'package:dio/dio.dart';

import 'package:foresignal_api/src/model/problem.dart';
import 'package:foresignal_api/src/model/transit_catalog_page.dart';

class TransitApi {

  final Dio _dio;

  const TransitApi(this._dio);

  /// Search the Mobility Database GTFS-RT snapshot
  /// Read-only search of the vendored CC0 MobilityData snapshot (keyless, active, https GTFS-RT feeds) with whether each feed is already enabled. Feeds are enabled only through the bootstrap property &#x60;foresignal.transit.catalog-feeds&#x60; and a restart; URLs are never accepted at runtime, so there is no POST. Rows are ordered by id; &#x60;entityType&#x60; matches a row that serves that entity type among others (&#x60;tu|vp&#x60; matches &#x60;vp&#x60;). An unknown cursor is &#x60;400 INVALID_CURSOR&#x60;.  **Scope:** &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [country] - ISO 3166-1 alpha-2 country code
  /// * [q] - Case-insensitive match on provider and id
  /// * [entityType] - GTFS-RT entity type: `vp` vehicle positions, `tu` trip updates, `sa` service alerts
  /// * [cursor] - Opaque pagination cursor from a previous response's nextCursor
  /// * [limit] - Page size
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [TransitCatalogPage] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<TransitCatalogPage>> searchTransitCatalog({ 
    String? country,
    String? q,
    String? entityType,
    String? cursor,
    int? limit = 100,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/transit/catalog';
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
      if (country != null) r'country': country,
      if (q != null) r'q': q,
      if (entityType != null) r'entityType': entityType,
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

    TransitCatalogPage? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<TransitCatalogPage, TransitCatalogPage>(rawData, 'TransitCatalogPage', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<TransitCatalogPage>(
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
