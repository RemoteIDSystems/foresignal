//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

import 'dart:async';

// ignore: unused_import
import 'dart:convert';
import 'package:foresignal_api/src/deserialize.dart';
import 'package:dio/dio.dart';

import 'package:foresignal_api/src/model/backfill_job.dart';
import 'package:foresignal_api/src/model/collection_set.dart';
import 'package:foresignal_api/src/model/collection_set_update.dart';
import 'package:foresignal_api/src/model/cot_backfill_request.dart';
import 'package:foresignal_api/src/model/credentials_update.dart';
import 'package:foresignal_api/src/model/doctor_options.dart';
import 'package:foresignal_api/src/model/doctor_report.dart';
import 'package:foresignal_api/src/model/doctor_report_page.dart';
import 'package:foresignal_api/src/model/doctor_run_accepted.dart';
import 'package:foresignal_api/src/model/enable_core_sources_result.dart';
import 'package:foresignal_api/src/model/enable_free_sources_result.dart';
import 'package:foresignal_api/src/model/fetch_outcome.dart';
import 'package:foresignal_api/src/model/fetch_page.dart';
import 'package:foresignal_api/src/model/probe_result.dart';
import 'package:foresignal_api/src/model/problem.dart';
import 'package:foresignal_api/src/model/source_detail.dart';
import 'package:foresignal_api/src/model/source_health.dart';
import 'package:foresignal_api/src/model/source_settings_update.dart';
import 'package:foresignal_api/src/model/source_summary.dart';
import 'package:foresignal_api/src/model/usage_series.dart';
import 'package:foresignal_api/src/model/user_source_view.dart';
import 'package:foresignal_api/src/model/user_source_view_update.dart';

class SourcesApi {

  final Dio _dio;

  const SourcesApi(this._dio);

  /// Backfill CFTC COT history
  /// Starts a resumable, budget-limited job that pages backwards from the oldest stored report to &#x60;from&#x60; (default 2010-01-01). Idempotent: existing versions are not duplicated.  **Scope:** &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [cotBackfillRequest] 
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [BackfillJob] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<BackfillJob>> backfillCftcCot({ 
    CotBackfillRequest? cotBackfillRequest,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/sources/cftc-cot/backfill';
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
      _bodyData = jsonEncode(cotBackfillRequest);

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

    BackfillJob? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<BackfillJob, BackfillJob>(rawData, 'BackfillJob', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<BackfillJob>(
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

  /// Remove upstream credentials
  /// Also disables the source if it requires credentials.  **Scope:** &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [sourceId] - Source id, e.g. usgs-earthquakes
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future]
  /// Throws [DioException] if API call or serialization fails
  Future<Response<void>> deleteSourceCredentials({ 
    required String sourceId,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/sources/{sourceId}/credentials'.replaceAll('{' r'sourceId' '}', sourceId.toString());
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

  /// Enable the core sources
  /// Enables every currently disabled source of the core set that runs without operator input: the small live-tracking set a fresh install seeds enabled (prompt 150): &#x60;adsblol-world&#x60;, &#x60;adsblol-mil&#x60;, &#x60;airplaneslive-mil&#x60;, &#x60;adsbfi-blindspots&#x60;, &#x60;airplaneslive-blindspots&#x60;, &#x60;opensky-states&#x60;, &#x60;derived-aircraft&#x60;, &#x60;celestrak-gp&#x60;, &#x60;usgs-earthquakes&#x60;, &#x60;nws-alerts&#x60;, &#x60;nasa-firms&#x60;, &#x60;meshtastic-map&#x60; and &#x60;meshtastic-mqtt&#x60;. A core source that needs operator input (&#x60;nasa-firms&#x60; without its MAP_KEY) is skipped, as is every source outside the core set. Each source is enabled through the settings-update path, so the collector picks it up within seconds. Disables nothing; enabled sources and custom or external sources are left untouched. Idempotent: a second call enables nothing. This is how an install seeded before prompt 150 adopts the core default without re-seeding.  **Scope:** &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [EnableCoreSourcesResult] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<EnableCoreSourcesResult>> enableCoreSources({ 
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/sources/enable-core';
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

    EnableCoreSourcesResult? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<EnableCoreSourcesResult, EnableCoreSourcesResult>(rawData, 'EnableCoreSourcesResult', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<EnableCoreSourcesResult>(
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

  /// Enable every free source
  /// Enables every currently disabled built-in source that runs without operator input: no required credential, no required parameter, and parameters that are valid with nothing supplied (the same checks &#x60;PUT /v1/sources/{sourceId}&#x60; applies when enabling), unless the source is held off by default for its bandwidth or terms (prompt 146: &#x60;wiki-edit-surge&#x60;, which the operator enables with &#x60;PUT /v1/sources/wiki-edit-surge&#x60;). Each source is enabled through the settings-update path, so the collector picks it up within seconds and fetches it at its default interval and budget. Enabled sources, credential- or parameter-gated sources, held-off sources and custom or external sources are left untouched. Idempotent: a second call enables nothing. A fresh install seeds only the core set enabled (prompt 150, &#x60;enableCoreSources&#x60;); this call turns on every free source on top of it.  **Scope:** &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [EnableFreeSourcesResult] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<EnableFreeSourcesResult>> enableFreeSources({ 
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/sources/enable-free';
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

    EnableFreeSourcesResult? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<EnableFreeSourcesResult, EnableFreeSourcesResult>(rawData, 'EnableFreeSourcesResult', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<EnableFreeSourcesResult>(
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

  /// Fetch once now
  /// Runs one fetch immediately (trigger MANUAL) and ingests normally, even while the source is disabled. Consumes budget; the outcome carries per-reason counts and the fetch_log id. Streaming, push and derived sources return 409 NOT_SUPPORTED_FOR_MODE (use probe for streams).  **Scope:** &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [sourceId] - Source id, e.g. usgs-earthquakes
  /// * [allowShrink] - Allow complete-set reconciliation to tombstone more than `reconcile.maxAbsentRatio` of the dataset, once (094). The run sends no stored conditional-GET validators, so it re-reads every file in full.
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [FetchOutcome] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<FetchOutcome>> fetchSourceNow({ 
    required String sourceId,
    bool? allowShrink = false,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/sources/{sourceId}/fetch'.replaceAll('{' r'sourceId' '}', sourceId.toString());
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

    final _queryParameters = <String, dynamic>{
      if (allowShrink != null) r'allowShrink': allowShrink,
    };

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    FetchOutcome? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<FetchOutcome, FetchOutcome>(rawData, 'FetchOutcome', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<FetchOutcome>(
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

  /// Get the instance collection set
  /// Returns the instance collection set: every registered source with &#x60;inCollection&#x60; (whether the instance may collect it) and &#x60;enabledByUsers&#x60; (how many active users&#39; views keep it). The collector pulls a source once when it is in this set and some active user&#39;s view keeps it, or, while no user is active, whenever it is in this set. Requires &#x60;collection.manage&#x60;.  **Scope:** &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [CollectionSet] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<CollectionSet>> getCollectionSources({ 
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/sources/collection';
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

    CollectionSet? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<CollectionSet, CollectionSet>(rawData, 'CollectionSet', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<CollectionSet>(
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

  /// Get a doctor report
  /// One doctor report; patches are always labeled \&quot;proposed, not applied\&quot;. 202 with &#x60;DoctorRunAccepted&#x60; while the run is still in progress.  **Scope:** &#x60;agent&#x60;.
  ///
  /// Parameters:
  /// * [reportId] - Report id
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [DoctorReport] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<DoctorReport>> getDoctorReport({ 
    required String reportId,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/doctor/reports/{reportId}'.replaceAll('{' r'reportId' '}', reportId.toString());
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

    DoctorReport? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<DoctorReport, DoctorReport>(rawData, 'DoctorReport', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<DoctorReport>(
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

  /// Get my enabled-source view
  /// Returns the caller&#39;s own source view over the shared collection: one entry per source the caller may see, with &#x60;enabled&#x60; and the caller&#39;s display overrides. A view is opt-out: a source the caller never turned off is &#x60;enabled&#x60; with no overrides. The instance-wide collection set is getCollectionSources. A view belongs to a signed-in user, so an API token gets &#x60;401&#x60;. Requires &#x60;self.sources&#x60;.  **Scope:** &#x60;read&#x60;.
  ///
  /// Parameters:
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [UserSourceView] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<UserSourceView>> getMySources({ 
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/me/sources';
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

    UserSourceView? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<UserSourceView, UserSourceView>(rawData, 'UserSourceView', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<UserSourceView>(
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

  /// Get a source
  /// Get a source  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).  **v2.0 (per-user):** a source&#39;s *enablement and display* are now the caller&#39;s own per-user view (getMySources/updateMySources) over the shared collection; the instance collection set is managed with &#x60;collection.manage&#x60; (getCollectionSources). The source descriptor and health stay global. Request and response shapes are unchanged.
  ///
  /// Parameters:
  /// * [sourceId] - Source id, e.g. usgs-earthquakes
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [SourceDetail] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<SourceDetail>> getSource({ 
    required String sourceId,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/sources/{sourceId}'.replaceAll('{' r'sourceId' '}', sourceId.toString());
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

    SourceDetail? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<SourceDetail, SourceDetail>(rawData, 'SourceDetail', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<SourceDetail>(
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

  /// Health of one source
  /// Health of one source  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [sourceId] - Source id, e.g. usgs-earthquakes
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [SourceHealth] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<SourceHealth>> getSourceHealth({ 
    required String sourceId,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/sources/{sourceId}/health'.replaceAll('{' r'sourceId' '}', sourceId.toString());
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

    SourceHealth? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<SourceHealth, SourceHealth>(rawData, 'SourceHealth', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<SourceHealth>(
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

  /// Request/bytes usage over time
  /// Request, byte and ingest counters (records seen, versions, live updates, touches, late, skipped, rejected, version bytes) per bucket, with totals. Ranges over 31 days at minute resolution are rejected.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [sourceId] - Source id, e.g. usgs-earthquakes
  /// * [from] - Start (default now-24h)
  /// * [to] - End (default now)
  /// * [resolution] - Bucket size
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [UsageSeries] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<UsageSeries>> getSourceUsage({ 
    required String sourceId,
    DateTime? from,
    DateTime? to,
    String? resolution,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/sources/{sourceId}/usage'.replaceAll('{' r'sourceId' '}', sourceId.toString());
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
      if (resolution != null) r'resolution': resolution,
    };

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    UsageSeries? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<UsageSeries, UsageSeries>(rawData, 'UsageSeries', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<UsageSeries>(
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

  /// List a source&#39;s doctor reports
  /// Doctor reports for one source, newest first.  **Scope:** &#x60;agent&#x60;.
  ///
  /// Parameters:
  /// * [sourceId] - Source id
  /// * [cursor] - Opaque pagination cursor from a previous response's nextCursor
  /// * [limit] - Page size
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [DoctorReportPage] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<DoctorReportPage>> listSourceDoctorReports({ 
    required String sourceId,
    String? cursor,
    int? limit = 100,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/sources/{sourceId}/doctor/reports'.replaceAll('{' r'sourceId' '}', sourceId.toString());
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

    DoctorReportPage? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<DoctorReportPage, DoctorReportPage>(rawData, 'DoctorReportPage', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<DoctorReportPage>(
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

  /// Per-fetch provenance log
  /// Fetch-log rows of one source (scheduled, manual, stream flushes and pushes), newest first, keyset-paged on id. Rows carry counts, seq range, redacted URL, ETag, body hash and licence; never request or response bodies.  **Scope:** &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [sourceId] - Source id, e.g. usgs-earthquakes
  /// * [after] - Exclusive id cursor (`nextAfter` of the previous page)
  /// * [limit] - Page size
  /// * [status] - Filter by status (repeatable or comma-separated)
  /// * [trigger] - Filter by trigger (repeatable or comma-separated)
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [FetchPage] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<FetchPage>> listSourceFetches({ 
    required String sourceId,
    int? after,
    int? limit = 100,
    List<String>? status,
    List<String>? trigger,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/sources/{sourceId}/fetches'.replaceAll('{' r'sourceId' '}', sourceId.toString());
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
      if (status != null) r'status': status,
      if (trigger != null) r'trigger': trigger,
    };

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    FetchPage? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<FetchPage, FetchPage>(rawData, 'FetchPage', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<FetchPage>(
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

  /// Health of all sources
  /// Machine-readable health for every source; intended for monitoring and the adapter-maintenance agent.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [status] - Filter by status
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [List<SourceHealth>] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<List<SourceHealth>>> listSourceHealth({ 
    List<String>? status,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/sources/health';
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
      if (status != null) r'status': status,
    };

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    List<SourceHealth>? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<List<SourceHealth>, SourceHealth>(rawData, 'List<SourceHealth>', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<List<SourceHealth>>(
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

  /// List all sources
  /// Catalogue of every available adapter with its settings, live health status and today&#39;s budget use.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).  **v2.0 (per-user):** a source&#39;s *enablement and display* are now the caller&#39;s own per-user view (getMySources/updateMySources) over the shared collection; the instance collection set is managed with &#x60;collection.manage&#x60; (getCollectionSources). The source descriptor and health stay global. Request and response shapes are unchanged.
  ///
  /// Parameters:
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [List<SourceSummary>] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<List<SourceSummary>>> listSources({ 
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/sources';
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

    List<SourceSummary>? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<List<SourceSummary>, SourceSummary>(rawData, 'List<SourceSummary>', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<List<SourceSummary>>(
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

  /// Diagnostic probe
  /// Fetches once (or captures a stream for &#x60;seconds&#x60;) and returns the recorded exchanges, credentials redacted, plus up to 100 parsed previews with eventTime, validFrom, quality and observers. Never ingests, never writes fetch_log, never changes health; consumes budget. A parse failure fills &#x60;parseError&#x60; and still returns the exchanges. Push and derived sources return 409 NOT_SUPPORTED_FOR_MODE.  **Scope:** &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [sourceId] - Source id, e.g. usgs-earthquakes
  /// * [maxBytes] - Truncate raw body to this many bytes
  /// * [seconds] - Capture window for streaming sources
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [ProbeResult] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<ProbeResult>> probeSource({ 
    required String sourceId,
    int? maxBytes = 262144,
    int? seconds = 10,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/sources/{sourceId}/probe'.replaceAll('{' r'sourceId' '}', sourceId.toString());
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

    final _queryParameters = <String, dynamic>{
      if (maxBytes != null) r'maxBytes': maxBytes,
      if (seconds != null) r'seconds': seconds,
    };

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    ProbeResult? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<ProbeResult, ProbeResult>(rawData, 'ProbeResult', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<ProbeResult>(
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

  /// Diagnose a source
  /// Probes the upstream (budgeted; skipped with &#x60;PROBE_SKIPPED_BUDGET&#x60; when out of budget or in backoff), diffs its structure against the recorded fixture, runs deterministic rules and, if enabled, asks the LLM for an explanation and a parser patch proposed as text (never applied). Synchronous up to 90 s, else 202 with &#x60;Location&#x60;. One run per source per 10 min (&#x60;DOCTOR_BUSY&#x60;).  **Scope:** &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [sourceId] - Source id
  /// * [doctorOptions] 
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [DoctorReport] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<DoctorReport>> runSourceDoctor({ 
    required String sourceId,
    DoctorOptions? doctorOptions,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/sources/{sourceId}/doctor'.replaceAll('{' r'sourceId' '}', sourceId.toString());
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
      _bodyData = jsonEncode(doctorOptions);

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

    DoctorReport? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<DoctorReport, DoctorReport>(rawData, 'DoctorReport', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<DoctorReport>(
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

  /// Set upstream credentials
  /// Write-only. Values are encrypted at rest and never returned by any endpoint.  **Scope:** &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [sourceId] - Source id, e.g. usgs-earthquakes
  /// * [credentialsUpdate] 
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future]
  /// Throws [DioException] if API call or serialization fails
  Future<Response<void>> setSourceCredentials({ 
    required String sourceId,
    required CredentialsUpdate credentialsUpdate,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/sources/{sourceId}/credentials'.replaceAll('{' r'sourceId' '}', sourceId.toString());
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
      _bodyData = jsonEncode(credentialsUpdate);

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

    return _response;
  }

  /// Update the instance collection set
  /// Sets &#x60;inCollection&#x60; for every listed source, all or nothing; a source left out keeps its state and &#x60;enabledByUsers&#x60; is ignored. Each change applies the checks of updateSource, so collecting a source without its required credentials is &#x60;409 CREDENTIALS_REQUIRED&#x60;. A source taken out of the set is pulled for no one, regardless of per-user views. Requires &#x60;collection.manage&#x60;.  **Scope:** &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [collectionSetUpdate] 
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [CollectionSet] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<CollectionSet>> updateCollectionSources({ 
    required CollectionSetUpdate collectionSetUpdate,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/sources/collection';
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
      _bodyData = jsonEncode(collectionSetUpdate);

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

    CollectionSet? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<CollectionSet, CollectionSet>(rawData, 'CollectionSet', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<CollectionSet>(
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

  /// Update my enabled-source view
  /// Replaces the caller&#39;s source view; a source left out returns to the default (enabled, no overrides). A source may be listed whether or not the instance collects it; the preference takes effect while it is in the collection set. The collector pulls a source once while any active user&#39;s view keeps it, so a view never adds a second pull. Afterwards the caller&#39;s reads (records, changes, tiles, search, statistics) show only the sources in their view. An API token gets &#x60;401&#x60;. Requires &#x60;self.sources&#x60;.  **Scope:** &#x60;read&#x60;.
  ///
  /// Parameters:
  /// * [userSourceViewUpdate] 
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [UserSourceView] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<UserSourceView>> updateMySources({ 
    required UserSourceViewUpdate userSourceViewUpdate,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/me/sources';
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
      _bodyData = jsonEncode(userSourceViewUpdate);

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

    UserSourceView? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<UserSourceView, UserSourceView>(rawData, 'UserSourceView', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<UserSourceView>(
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

  /// Update source settings
  /// Enable/disable (subscribe/unsubscribe), set interval, params, budget, retention and the ingest-policy override. Changes apply immediately; omitted fields are unchanged, an explicit &#x60;ingestPolicy: null&#x60; clears the override. Policy violations return 400 INGEST_POLICY_INVALID with every violation in &#x60;errors[]&#x60; (e.g. &#x60;ingestPolicy.keyframe&#x60;); retention on a TRACK source returns 400 RETENTION_TIER_FIXED (use &#x60;retention.trackWindow&#x60;). A parameter value allowed only on a private source (086: &#x60;local-remoteid&#x60;&#39;s &#x60;keepOperatorPosition&#x3D;true&#x60;) returns 400 PRIVATE_REQUIRED unless the source is effectively private (descriptor or &#x60;private: true&#x60;, in the same request or already set), and so does making such a source public.  **Scope:** &#x60;admin&#x60;.  **v2.0 (per-user):** a source&#39;s *enablement and display* are now the caller&#39;s own per-user view (getMySources/updateMySources) over the shared collection; the instance collection set is managed with &#x60;collection.manage&#x60; (getCollectionSources). The source descriptor and health stay global. Request and response shapes are unchanged.
  ///
  /// Parameters:
  /// * [sourceId] - Source id, e.g. usgs-earthquakes
  /// * [sourceSettingsUpdate] 
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [SourceDetail] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<SourceDetail>> updateSource({ 
    required String sourceId,
    required SourceSettingsUpdate sourceSettingsUpdate,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/sources/{sourceId}'.replaceAll('{' r'sourceId' '}', sourceId.toString());
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
      _bodyData = jsonEncode(sourceSettingsUpdate);

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

    SourceDetail? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<SourceDetail, SourceDetail>(rawData, 'SourceDetail', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<SourceDetail>(
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
