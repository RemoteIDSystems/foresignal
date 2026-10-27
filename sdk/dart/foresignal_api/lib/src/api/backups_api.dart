//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

import 'dart:async';

// ignore: unused_import
import 'dart:convert';
import 'package:foresignal_api/src/deserialize.dart';
import 'package:dio/dio.dart';

import 'package:foresignal_api/src/model/backup.dart';
import 'package:foresignal_api/src/model/backup_page.dart';
import 'package:foresignal_api/src/model/backup_restore_request.dart';
import 'package:foresignal_api/src/model/backup_run_request.dart';
import 'package:foresignal_api/src/model/backup_settings.dart';
import 'package:foresignal_api/src/model/backup_settings_input.dart';
import 'package:foresignal_api/src/model/problem.dart';

class BackupsApi {

  final Dio _dio;

  const BackupsApi(this._dio);

  /// Get backup settings
  /// Settings key &#x60;backup&#x60;; secrets are never returned (only &#x60;secretsSet&#x60;). Requires &#x60;storage.manage&#x60;.  **Scope:** &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [BackupSettings] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<BackupSettings>> getBackupSettings({ 
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/settings/backup';
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

    BackupSettings? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<BackupSettings, BackupSettings>(rawData, 'BackupSettings', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<BackupSettings>(
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

  /// List backups
  /// Lists backup artifacts with type, size and status (180). Requires &#x60;storage.manage&#x60;.  **Scope:** &#x60;admin&#x60;.  **R2-017:** newest first; &#x60;type&#x60; filters to one type.
  ///
  /// Parameters:
  /// * [type] - Filter by type
  /// * [cursor] - Opaque pagination cursor
  /// * [limit] - Page size
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [BackupPage] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<BackupPage>> listBackups({ 
    String? type,
    String? cursor,
    int? limit = 100,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/backups';
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
      if (type != null) r'type': type,
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

    BackupPage? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<BackupPage, BackupPage>(rawData, 'BackupPage', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<BackupPage>(
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

  /// Restore from a backup
  /// Restores the instance from a backup; destructive, so &#x60;confirm&#x60; must be true (180). Requires &#x60;storage.manage&#x60;.  **Scope:** &#x60;admin&#x60;.  **R2-017:** a &#x60;config&#x60; backup is imported at once through &#x60;importSettings&#x60; (atomic: a 400 listing every error applies nothing); the answer is the &#x60;restored&#x60; backup with its &#x60;importReport&#x60;. A &#x60;database&#x60; or &#x60;full&#x60; backup is restored in the background (the answer is the &#x60;restoring&#x60; backup) with &#x60;pg_restore --clean --if-exists --single-transaction&#x60; into the live database: it replaces every table, the backup history included, then brings the schema forward with the server&#39;s migrations and marks the backup &#x60;restored&#x60;. **Operational caveat:** the restore takes locks on every table, so ingest and API writes wait on it, and anything written after the backup was taken is lost; pause collection first and restart the server afterwards so every cache is rebuilt. While it runs, no retention or archive pass starts. A failed restore puts the backup&#39;s previous status back with &#x60;error&#x60; and raises &#x60;system.backup_failed&#x60;. 409 ALREADY_RUNNING while a backup, restore, retention or archive pass runs; 409 BACKUP_NOT_RESTORABLE for a failed backup, or a config bundle whose stored copy is missing or does not match its checksum (the reason is kept in &#x60;error&#x60;); 409 BACKUP_NOT_CONFIGURED for an &#x60;s3&#x60; backup when the settings have no bucket or secrets.
  ///
  /// Parameters:
  /// * [id] - Backup id
  /// * [backupRestoreRequest] 
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [Backup] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<Backup>> restoreBackup({ 
    required String id,
    required BackupRestoreRequest backupRestoreRequest,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/backups/{id}/restore'.replaceAll('{' r'id' '}', id.toString());
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
      _bodyData = jsonEncode(backupRestoreRequest);

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

    Backup? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<Backup, Backup>(rawData, 'Backup', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<Backup>(
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

  /// Run a backup now
  /// Starts a DB (pg_dump/base) or config-export backup to the configured target (B4/180). Requires &#x60;storage.manage&#x60;.  **Scope:** &#x60;admin&#x60;.  **R2-017:** returns the &#x60;pending&#x60; backup; a worker then runs &#x60;pg_dump --format&#x3D;custom&#x60; of the application database (the password goes to it as &#x60;PGPASSWORD&#x60;, never on its command line) and/or writes the &#x60;exportSettings&#x60; bundle, stores each file on the target through the R2-016 archive tiers and verifies it there, marks the backup &#x60;complete&#x60;, and prunes complete backups of that type beyond &#x60;keepBackups&#x60;. A failure marks it &#x60;failed&#x60; with &#x60;error&#x60; and raises the operational alert &#x60;system.backup_failed&#x60;; when &#x60;pg_dump&#x60; is not on the server&#39;s PATH, a database backup fails with that reason and the server carries on. 409 BACKUP_NOT_CONFIGURED while backups are off, or an &#x60;s3&#x60; target lacks its bucket or secrets, or an &#x60;external&#x60; target its root; 409 ALREADY_RUNNING while another backup or restore runs.
  ///
  /// Parameters:
  /// * [backupRunRequest] 
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [Backup] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<Backup>> runBackup({ 
    BackupRunRequest? backupRunRequest,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/backups/run';
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
      _bodyData = jsonEncode(backupRunRequest);

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

    Backup? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<Backup, Backup>(rawData, 'Backup', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<Backup>(
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

  /// Update backup settings
  /// Replaces backup settings; an omitted secret keeps the stored one. Requires &#x60;storage.manage&#x60;.  **Scope:** &#x60;admin&#x60;.  **R2-017:** every problem is one 400: &#x60;enabled&#x60; and &#x60;target&#x60; are required, &#x60;schedule&#x60; must be a six-field cron expression, &#x60;external&#x60; needs an absolute &#x60;root&#x60;, &#x60;s3&#x60; needs &#x60;s3.endpoint&#x60; and &#x60;s3.bucket&#x60;, &#x60;keepBackups&#x60; is 1 to 10000 or null. The S3 endpoint must pass the outbound URL policy (400 URL_NOT_ALLOWED). Secrets are stored encrypted and never returned.
  ///
  /// Parameters:
  /// * [backupSettingsInput] 
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [BackupSettings] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<BackupSettings>> updateBackupSettings({ 
    required BackupSettingsInput backupSettingsInput,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/settings/backup';
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
      _bodyData = jsonEncode(backupSettingsInput);

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

    BackupSettings? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<BackupSettings, BackupSettings>(rawData, 'BackupSettings', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<BackupSettings>(
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
