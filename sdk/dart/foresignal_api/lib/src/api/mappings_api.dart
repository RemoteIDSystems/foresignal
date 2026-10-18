//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

import 'dart:async';

// ignore: unused_import
import 'dart:convert';
import 'package:foresignal_api/src/deserialize.dart';
import 'package:dio/dio.dart';

import 'package:foresignal_api/src/model/mapping_preset.dart';
import 'package:foresignal_api/src/model/mapping_preview_request.dart';
import 'package:foresignal_api/src/model/mapping_preview_result.dart';
import 'package:foresignal_api/src/model/mapping_suggest_request.dart';
import 'package:foresignal_api/src/model/mapping_suggest_result.dart';
import 'package:foresignal_api/src/model/problem.dart';

class MappingsApi {

  final Dio _dio;

  const MappingsApi(this._dio);

  /// List device mapping presets
  /// Built-in presets (&#x60;ecowitt&#x60;, &#x60;wu-pws&#x60;, &#x60;aiscatcher&#x60;, &#x60;chirpstack-v4&#x60;, &#x60;tts-v3&#x60;, &#x60;alertmanager&#x60;, &#x60;airrohr&#x60;, &#x60;gmc&#x60;, plus later ones such as 086&#39;s &#x60;rsudp-alarm&#x60;) with their mapping, descriptor defaults and setup instructions (URL template from &#x60;publicBaseUrl&#x60;). Create a source from one with &#x60;POST /v1/custom-sources {preset, presetParams}&#x60;.  **Scope:** &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [List<MappingPreset>] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<List<MappingPreset>>> listMappingPresets({ 
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/mappings/presets';
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

    List<MappingPreset>? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<List<MappingPreset>, MappingPreset>(rawData, 'List<MappingPreset>', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<List<MappingPreset>>(
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

  /// Preview a RecordMapping over a sample
  /// Compiles the mapping with &#x60;MappingCompiler&#x60; and applies it to the sample through the standard 008 normaliser, returning exactly what ingest would write. Writes nothing. Warnings cover unmapped input keys, fields missing from the source&#39;s &#x60;fieldSpecs&#x60;, a policy-required significant field that is missing, and a time-base mismatch with the source&#39;s &#x60;observedAtMode&#x60;. Compile errors return 400 &#x60;MAPPING_INVALID&#x60; with &#x60;errors[].field&#x60; paths such as &#x60;mapping.fields[3].unit.from&#x60;.  **Scope:** &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [mappingPreviewRequest] 
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [MappingPreviewResult] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<MappingPreviewResult>> previewMapping({ 
    required MappingPreviewRequest mappingPreviewRequest,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/mappings/preview';
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
      _bodyData = jsonEncode(mappingPreviewRequest);

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

    MappingPreviewResult? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<MappingPreviewResult, MappingPreviewResult>(rawData, 'MappingPreviewResult', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<MappingPreviewResult>(
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

  /// LLM-suggested draft RecordMapping
  /// Asks the chat provider (purpose &#x60;mapping-suggest&#x60;, 026 budget) for a draft mapping. The sample is untrusted data inside delimiters; the output is validated with &#x60;MappingCompiler&#x60; with up to 2 repair rounds, then previewed over the sample. The draft is never stored or applied: saving it is a separate &#x60;PUT /v1/custom-sources/{id}&#x60;. A remote provider requires &#x60;confirmEgress: true&#x60; (409 &#x60;EGRESS_CONFIRMATION_REQUIRED&#x60;); samples from a private source are refused for remote providers (403); no provider → 409 &#x60;LLM_NOT_CONFIGURED&#x60;.  **Scope:** &#x60;admin&#x60;.
  ///
  /// Parameters:
  /// * [mappingSuggestRequest] 
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [MappingSuggestResult] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<MappingSuggestResult>> suggestMapping({ 
    required MappingSuggestRequest mappingSuggestRequest,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/mappings/suggest';
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
      _bodyData = jsonEncode(mappingSuggestRequest);

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

    MappingSuggestResult? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<MappingSuggestResult, MappingSuggestResult>(rawData, 'MappingSuggestResult', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<MappingSuggestResult>(
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
