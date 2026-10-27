//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

import 'dart:async';

// ignore: unused_import
import 'dart:convert';
import 'package:foresignal_api/src/deserialize.dart';
import 'package:dio/dio.dart';

import 'package:foresignal_api/src/model/llm_model_info.dart';
import 'package:foresignal_api/src/model/llm_probe_result.dart';
import 'package:foresignal_api/src/model/llm_provider.dart';
import 'package:foresignal_api/src/model/llm_provider_budget.dart';
import 'package:foresignal_api/src/model/llm_provider_input.dart';
import 'package:foresignal_api/src/model/llm_settings.dart';
import 'package:foresignal_api/src/model/llm_test_result.dart';
import 'package:foresignal_api/src/model/llm_usage_series.dart';
import 'package:foresignal_api/src/model/me_llm_budget.dart';
import 'package:foresignal_api/src/model/problem.dart';
import 'package:foresignal_api/src/model/prompt_template.dart';
import 'package:foresignal_api/src/model/prompt_template_list.dart';

class LLMApi {

  final Dio _dio;

  const LLMApi(this._dio);

  /// Add an LLM provider
  /// Add an LLM provider. Defaults: &#x60;anthropic&#x60; → &#x60;baseUrl&#x60; https://api.anthropic.com, &#x60;chatModel&#x60; claude-opus-5-5, &#x60;options.effort&#x60; medium, &#x60;options.maxTokens&#x60; 16000, and &#x60;apiKey&#x60; is required; &#x60;ollama&#x60; → &#x60;baseUrl&#x60; http://ollama:11434; &#x60;openai-compatible&#x60; must name &#x60;baseUrl&#x60;. &#x60;baseUrl&#x60; must be http or https with a host and no user info or fragment; private-network URLs are allowed (035 adds OutboundUrlPolicy LAN_ALLOWED). The key is encrypted at rest and never returned (&#x60;apiKeySet&#x60;).  **Scope:** &#x60;admin&#x60;.  **v2.0 (shared and personal, R2-011):** a provider is **shared** (&#x60;shared: true&#x60;, no owner; every provider from before v2.0, managed with &#x60;llm.shared.manage&#x60;) or **personal** (&#x60;ownerPrincipalId&#x60;, the owning user&#39;s principal, managed by that user with &#x60;self.llm&#x60;). A signed-in user reaches the provider operations with either permission: they see their own providers and the shared ones; they change, delete and test their own, list its models and read its budget; on a shared provider those need &#x60;llm.shared.manage&#x60; (403 PERMISSION_DENIED otherwise), and another user&#39;s provider is 404, like an unknown one. A session holding &#x60;users.manage&#x60; and every API token see and manage all providers. On create, &#x60;shared&#x60; picks the kind: absent, a user holding &#x60;self.llm&#x60; gets a personal provider and anyone else a shared one; an API token always creates shared providers; a replace keeps the kind. Only a shared provider can hold an instance role (getLlmSettings) or carry &#x60;perUser*CapUsd&#x60;, the caps on each user&#39;s own spend on it. A personal provider is never &#x60;local&#x60; for LLM egress. Calls made for a user are metered to that user as well (getMyLlmBudget). The shapes change only by the additive &#x60;shared&#x60;, &#x60;ownerPrincipalId&#x60; and &#x60;perUser*CapUsd&#x60;.
  ///
  /// Parameters:
  /// * [llmProviderInput] 
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [LlmProvider] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<LlmProvider>> createLlmProvider({ 
    required LlmProviderInput llmProviderInput,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/llm/providers';
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
      _bodyData = jsonEncode(llmProviderInput);

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

    LlmProvider? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<LlmProvider, LlmProvider>(rawData, 'LlmProvider', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<LlmProvider>(
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

  /// Delete an LLM provider
  /// Returns 409 while it is assigned to a role.  **Scope:** &#x60;admin&#x60;.  **v2.0 (shared and personal, R2-011):** a provider is **shared** (&#x60;shared: true&#x60;, no owner; every provider from before v2.0, managed with &#x60;llm.shared.manage&#x60;) or **personal** (&#x60;ownerPrincipalId&#x60;, the owning user&#39;s principal, managed by that user with &#x60;self.llm&#x60;). A signed-in user reaches the provider operations with either permission: they see their own providers and the shared ones; they change, delete and test their own, list its models and read its budget; on a shared provider those need &#x60;llm.shared.manage&#x60; (403 PERMISSION_DENIED otherwise), and another user&#39;s provider is 404, like an unknown one. A session holding &#x60;users.manage&#x60; and every API token see and manage all providers. On create, &#x60;shared&#x60; picks the kind: absent, a user holding &#x60;self.llm&#x60; gets a personal provider and anyone else a shared one; an API token always creates shared providers; a replace keeps the kind. Only a shared provider can hold an instance role (getLlmSettings) or carry &#x60;perUser*CapUsd&#x60;, the caps on each user&#39;s own spend on it. A personal provider is never &#x60;local&#x60; for LLM egress. Calls made for a user are metered to that user as well (getMyLlmBudget). The shapes change only by the additive &#x60;shared&#x60;, &#x60;ownerPrincipalId&#x60; and &#x60;perUser*CapUsd&#x60;.
  ///
  /// Parameters:
  /// * [providerId] - LLM provider id
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future]
  /// Throws [DioException] if API call or serialization fails
  Future<Response<void>> deleteLlmProvider({ 
    required String providerId,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/llm/providers/{providerId}'.replaceAll('{' r'providerId' '}', providerId.toString());
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

  /// Get an LLM provider
  /// Get an LLM provider  **Scope:** &#x60;admin&#x60;.  **v2.0 (shared and personal, R2-011):** a provider is **shared** (&#x60;shared: true&#x60;, no owner; every provider from before v2.0, managed with &#x60;llm.shared.manage&#x60;) or **personal** (&#x60;ownerPrincipalId&#x60;, the owning user&#39;s principal, managed by that user with &#x60;self.llm&#x60;). A signed-in user reaches the provider operations with either permission: they see their own providers and the shared ones; they change, delete and test their own, list its models and read its budget; on a shared provider those need &#x60;llm.shared.manage&#x60; (403 PERMISSION_DENIED otherwise), and another user&#39;s provider is 404, like an unknown one. A session holding &#x60;users.manage&#x60; and every API token see and manage all providers. On create, &#x60;shared&#x60; picks the kind: absent, a user holding &#x60;self.llm&#x60; gets a personal provider and anyone else a shared one; an API token always creates shared providers; a replace keeps the kind. Only a shared provider can hold an instance role (getLlmSettings) or carry &#x60;perUser*CapUsd&#x60;, the caps on each user&#39;s own spend on it. A personal provider is never &#x60;local&#x60; for LLM egress. Calls made for a user are metered to that user as well (getMyLlmBudget). The shapes change only by the additive &#x60;shared&#x60;, &#x60;ownerPrincipalId&#x60; and &#x60;perUser*CapUsd&#x60;.
  ///
  /// Parameters:
  /// * [providerId] - LLM provider id
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [LlmProvider] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<LlmProvider>> getLlmProvider({ 
    required String providerId,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/llm/providers/{providerId}'.replaceAll('{' r'providerId' '}', providerId.toString());
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

    LlmProvider? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<LlmProvider, LlmProvider>(rawData, 'LlmProvider', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<LlmProvider>(
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

  /// A provider&#39;s spend against its USD caps
  /// Per window (the UTC hour, day and month) the USD spent, the estimated USD of calls in flight, the cap, what remains and when the window resets; the token and request limits; and, for providers whose API reports one to the same key, the provider-reported balance (DeepSeek, OpenRouter), shown for information and never enforced. Spend is Foresignal&#39;s own metered tokens at the provider&#39;s &#x60;pricing&#x60;.  **Scope:** &#x60;admin&#x60;.  **v2.0 (shared and personal, R2-011):** a provider is **shared** (&#x60;shared: true&#x60;, no owner; every provider from before v2.0, managed with &#x60;llm.shared.manage&#x60;) or **personal** (&#x60;ownerPrincipalId&#x60;, the owning user&#39;s principal, managed by that user with &#x60;self.llm&#x60;). A signed-in user reaches the provider operations with either permission: they see their own providers and the shared ones; they change, delete and test their own, list its models and read its budget; on a shared provider those need &#x60;llm.shared.manage&#x60; (403 PERMISSION_DENIED otherwise), and another user&#39;s provider is 404, like an unknown one. A session holding &#x60;users.manage&#x60; and every API token see and manage all providers. On create, &#x60;shared&#x60; picks the kind: absent, a user holding &#x60;self.llm&#x60; gets a personal provider and anyone else a shared one; an API token always creates shared providers; a replace keeps the kind. Only a shared provider can hold an instance role (getLlmSettings) or carry &#x60;perUser*CapUsd&#x60;, the caps on each user&#39;s own spend on it. A personal provider is never &#x60;local&#x60; for LLM egress. Calls made for a user are metered to that user as well (getMyLlmBudget). The shapes change only by the additive &#x60;shared&#x60;, &#x60;ownerPrincipalId&#x60; and &#x60;perUser*CapUsd&#x60;.
  ///
  /// Parameters:
  /// * [providerId] - LLM provider id
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [LlmProviderBudget] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<LlmProviderBudget>> getLlmProviderBudget({ 
    required String providerId,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/llm/providers/{providerId}/budget'.replaceAll('{' r'providerId' '}', providerId.toString());
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

    LlmProviderBudget? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<LlmProviderBudget, LlmProviderBudget>(rawData, 'LlmProviderBudget', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<LlmProviderBudget>(
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

  /// Get LLM roles and options
  /// Get LLM roles and options  **Scope:** &#x60;admin&#x60;.  **v2.0 (shared and personal, R2-011):** this operation concerns the shared LLM configuration only and needs &#x60;llm.shared.manage&#x60;; personal providers are managed through the provider operations with &#x60;self.llm&#x60;, and the instance roles can be held only by shared providers.
  ///
  /// Parameters:
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [LlmSettings] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<LlmSettings>> getLlmSettings({ 
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/llm/settings';
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

    LlmSettings? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<LlmSettings, LlmSettings>(rawData, 'LlmSettings', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<LlmSettings>(
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

  /// LLM usage over time
  /// LLM usage over time  **Scope:** &#x60;admin&#x60;.  **v2.0 (shared and personal, R2-011):** this operation concerns the shared LLM configuration only and needs &#x60;llm.shared.manage&#x60;; personal providers are managed through the provider operations with &#x60;self.llm&#x60;, and the instance roles can be held only by shared providers.
  ///
  /// Parameters:
  /// * [provider] - Provider id (default all)
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
  /// Returns a [Future] containing a [Response] with a [LlmUsageSeries] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<LlmUsageSeries>> getLlmUsage({ 
    String? provider,
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
    final _path = r'/v1/llm/usage';
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
      if (provider != null) r'provider': provider,
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

    LlmUsageSeries? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<LlmUsageSeries, LlmUsageSeries>(rawData, 'LlmUsageSeries', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<LlmUsageSeries>(
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

  /// Get my LLM budget and spend
  /// Returns the calling user&#39;s LLM spend this UTC month on every provider they can use: the enabled shared providers and their own personal ones, by id. &#x60;spentUsd&#x60; counts only calls made for this user (their own requests, and work done on their behalf such as enriching their alerts); calls made with an API token are the machine&#39;s and are not counted. On a shared provider &#x60;capUsd&#x60; is its &#x60;perUserMonthlyCapUsd&#x60;, the user&#39;s share; on a personal provider it is the provider&#39;s own &#x60;monthlyCapUsd&#x60;. &#x60;remainingUsd&#x60; is the cap minus the spend, never below zero, or null when uncapped. A budget belongs to a signed-in user, so an API token gets &#x60;401&#x60;. Requires &#x60;self.llm&#x60;.  **Scope:** &#x60;read&#x60;.
  ///
  /// Parameters:
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [MeLlmBudget] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<MeLlmBudget>> getMyLlmBudget({ 
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/me/llm/budget';
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

    MeLlmBudget? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<MeLlmBudget, MeLlmBudget>(rawData, 'MeLlmBudget', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<MeLlmBudget>(
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

  /// Get an LLM prompt template
  /// The versioned system prompt and instructions of an LLM role, as recorded by manifests ({templateId, version}). Operator &#x60;instructions&#x60; are returned to agent scope only; filled-in prompts are not exposed here.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [templateId] - Template id
  /// * [version] - Template version (default latest)
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [PromptTemplate] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<PromptTemplate>> getPromptTemplate({ 
    required String templateId,
    String? version,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/prompt-templates/{templateId}'.replaceAll('{' r'templateId' '}', templateId.toString());
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
      if (version != null) r'version': version,
    };

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    PromptTemplate? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<PromptTemplate, PromptTemplate>(rawData, 'PromptTemplate', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<PromptTemplate>(
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

  /// List models available from a provider
  /// Queries the provider&#39;s model list (Anthropic Models API, OpenAI-compatible /models, Ollama /api/tags).  **Scope:** &#x60;admin&#x60;.  **v2.0 (shared and personal, R2-011):** a provider is **shared** (&#x60;shared: true&#x60;, no owner; every provider from before v2.0, managed with &#x60;llm.shared.manage&#x60;) or **personal** (&#x60;ownerPrincipalId&#x60;, the owning user&#39;s principal, managed by that user with &#x60;self.llm&#x60;). A signed-in user reaches the provider operations with either permission: they see their own providers and the shared ones; they change, delete and test their own, list its models and read its budget; on a shared provider those need &#x60;llm.shared.manage&#x60; (403 PERMISSION_DENIED otherwise), and another user&#39;s provider is 404, like an unknown one. A session holding &#x60;users.manage&#x60; and every API token see and manage all providers. On create, &#x60;shared&#x60; picks the kind: absent, a user holding &#x60;self.llm&#x60; gets a personal provider and anyone else a shared one; an API token always creates shared providers; a replace keeps the kind. Only a shared provider can hold an instance role (getLlmSettings) or carry &#x60;perUser*CapUsd&#x60;, the caps on each user&#39;s own spend on it. A personal provider is never &#x60;local&#x60; for LLM egress. Calls made for a user are metered to that user as well (getMyLlmBudget). The shapes change only by the additive &#x60;shared&#x60;, &#x60;ownerPrincipalId&#x60; and &#x60;perUser*CapUsd&#x60;.
  ///
  /// Parameters:
  /// * [providerId] - LLM provider id
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [List<LlmModelInfo>] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<List<LlmModelInfo>>> listLlmProviderModels({ 
    required String providerId,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/llm/providers/{providerId}/models'.replaceAll('{' r'providerId' '}', providerId.toString());
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

    List<LlmModelInfo>? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<List<LlmModelInfo>, LlmModelInfo>(rawData, 'List<LlmModelInfo>', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<List<LlmModelInfo>>(
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

  /// List LLM providers
  /// API keys are never returned.  **Scope:** &#x60;admin&#x60;.  **v2.0 (shared and personal, R2-011):** a provider is **shared** (&#x60;shared: true&#x60;, no owner; every provider from before v2.0, managed with &#x60;llm.shared.manage&#x60;) or **personal** (&#x60;ownerPrincipalId&#x60;, the owning user&#39;s principal, managed by that user with &#x60;self.llm&#x60;). A signed-in user reaches the provider operations with either permission: they see their own providers and the shared ones; they change, delete and test their own, list its models and read its budget; on a shared provider those need &#x60;llm.shared.manage&#x60; (403 PERMISSION_DENIED otherwise), and another user&#39;s provider is 404, like an unknown one. A session holding &#x60;users.manage&#x60; and every API token see and manage all providers. On create, &#x60;shared&#x60; picks the kind: absent, a user holding &#x60;self.llm&#x60; gets a personal provider and anyone else a shared one; an API token always creates shared providers; a replace keeps the kind. Only a shared provider can hold an instance role (getLlmSettings) or carry &#x60;perUser*CapUsd&#x60;, the caps on each user&#39;s own spend on it. A personal provider is never &#x60;local&#x60; for LLM egress. Calls made for a user are metered to that user as well (getMyLlmBudget). The shapes change only by the additive &#x60;shared&#x60;, &#x60;ownerPrincipalId&#x60; and &#x60;perUser*CapUsd&#x60;.
  ///
  /// Parameters:
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [List<LlmProvider>] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<List<LlmProvider>>> listLlmProviders({ 
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/llm/providers';
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

    List<LlmProvider>? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<List<LlmProvider>, LlmProvider>(rawData, 'List<LlmProvider>', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<List<LlmProvider>>(
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

  /// List the LLM prompt templates
  /// Every bundled prompt template: its id, LLM role, newest version and all versions, so a client can offer them to getPromptTemplate. Texts are not included.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [PromptTemplateList] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<PromptTemplateList>> listPromptTemplates({ 
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/prompt-templates';
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

    PromptTemplateList? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<PromptTemplateList, PromptTemplateList>(rawData, 'PromptTemplateList', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<PromptTemplateList>(
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

  /// Test an unsaved LLM provider and list its models
  /// Dry run for the provider editor: takes an LlmProviderInput that is not saved and, in one round trip, lists the provider&#39;s models and runs the testLlmProvider probes (a tiny chat when &#x60;chatModel&#x60; is set, an embedding when an embedding model and dimensions are). Without a model the model listing is the connection test. Nothing is persisted: no provider, no usage, no health, no budget; the &#x60;apiKey&#x60; is used for these requests and discarded. Without &#x60;apiKey&#x60;, the stored key of the saved provider named by &#x60;id&#x60; is used when there is one, so a saved provider can be tested with edits that are not saved yet. The body is validated as createLlmProvider validates it (400, and 400 URL_NOT_ALLOWED for a base URL the outbound URL policy refuses); the requests go through the same outbound URL policy and pinning as a saved provider&#39;s. A provider failure is reported in the result, never as an error status.  **Scope:** &#x60;admin&#x60;.  **v2.0 (shared and personal, R2-011):** this operation concerns the shared LLM configuration only and needs &#x60;llm.shared.manage&#x60;; personal providers are managed through the provider operations with &#x60;self.llm&#x60;, and the instance roles can be held only by shared providers.
  ///
  /// Parameters:
  /// * [llmProviderInput] 
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [LlmProbeResult] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<LlmProbeResult>> probeLlmProvider({ 
    required LlmProviderInput llmProviderInput,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/llm/probe';
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
      _bodyData = jsonEncode(llmProviderInput);

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

    LlmProbeResult? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<LlmProbeResult, LlmProbeResult>(rawData, 'LlmProbeResult', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<LlmProbeResult>(
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

  /// Test an LLM provider
  /// Sends a tiny chat request (\&quot;Reply with the single word: ok\&quot;, 16 output tokens, purpose &#x60;test&#x60;) and an embedding of \&quot;test\&quot; when the provider has an embedding model, and reports latency, model, token usage (cached tokens included) and the normalized stop reason. Consumes budget. A provider failure or refusal is reported in the result (&#x60;success: false&#x60;, &#x60;error&#x60;, &#x60;refusalCategory&#x60;); only a budget refusal is an error status. A destination refused by the outbound URL policy at send time answers &#x60;success: false&#x60; with an &#x60;error&#x60; containing URL_NOT_ALLOWED (035).  **Scope:** &#x60;admin&#x60;.  **v2.0 (shared and personal, R2-011):** a provider is **shared** (&#x60;shared: true&#x60;, no owner; every provider from before v2.0, managed with &#x60;llm.shared.manage&#x60;) or **personal** (&#x60;ownerPrincipalId&#x60;, the owning user&#39;s principal, managed by that user with &#x60;self.llm&#x60;). A signed-in user reaches the provider operations with either permission: they see their own providers and the shared ones; they change, delete and test their own, list its models and read its budget; on a shared provider those need &#x60;llm.shared.manage&#x60; (403 PERMISSION_DENIED otherwise), and another user&#39;s provider is 404, like an unknown one. A session holding &#x60;users.manage&#x60; and every API token see and manage all providers. On create, &#x60;shared&#x60; picks the kind: absent, a user holding &#x60;self.llm&#x60; gets a personal provider and anyone else a shared one; an API token always creates shared providers; a replace keeps the kind. Only a shared provider can hold an instance role (getLlmSettings) or carry &#x60;perUser*CapUsd&#x60;, the caps on each user&#39;s own spend on it. A personal provider is never &#x60;local&#x60; for LLM egress. Calls made for a user are metered to that user as well (getMyLlmBudget). The shapes change only by the additive &#x60;shared&#x60;, &#x60;ownerPrincipalId&#x60; and &#x60;perUser*CapUsd&#x60;.
  ///
  /// Parameters:
  /// * [providerId] - LLM provider id
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [LlmTestResult] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<LlmTestResult>> testLlmProvider({ 
    required String providerId,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/llm/providers/{providerId}/test'.replaceAll('{' r'providerId' '}', providerId.toString());
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

    LlmTestResult? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<LlmTestResult, LlmTestResult>(rawData, 'LlmTestResult', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<LlmTestResult>(
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

  /// Replace an LLM provider
  /// Omit &#x60;apiKey&#x60; to keep the stored key. Changing model or dimensions of the active embedding provider (or reassigning &#x60;embeddingProvider&#x60;) publishes an embedding-model change and starts a zero-downtime reindex (027). &#x60;baseUrl&#x60; passes OutboundUrlPolicy LAN_ALLOWED (035). A body &#x60;id&#x60;, when present, must equal the path id. The change is refused (400) when it would leave a role the provider holds unserved (disabled, no chatModel, or no embedding model).  **Scope:** &#x60;admin&#x60;.  **v2.0 (shared and personal, R2-011):** a provider is **shared** (&#x60;shared: true&#x60;, no owner; every provider from before v2.0, managed with &#x60;llm.shared.manage&#x60;) or **personal** (&#x60;ownerPrincipalId&#x60;, the owning user&#39;s principal, managed by that user with &#x60;self.llm&#x60;). A signed-in user reaches the provider operations with either permission: they see their own providers and the shared ones; they change, delete and test their own, list its models and read its budget; on a shared provider those need &#x60;llm.shared.manage&#x60; (403 PERMISSION_DENIED otherwise), and another user&#39;s provider is 404, like an unknown one. A session holding &#x60;users.manage&#x60; and every API token see and manage all providers. On create, &#x60;shared&#x60; picks the kind: absent, a user holding &#x60;self.llm&#x60; gets a personal provider and anyone else a shared one; an API token always creates shared providers; a replace keeps the kind. Only a shared provider can hold an instance role (getLlmSettings) or carry &#x60;perUser*CapUsd&#x60;, the caps on each user&#39;s own spend on it. A personal provider is never &#x60;local&#x60; for LLM egress. Calls made for a user are metered to that user as well (getMyLlmBudget). The shapes change only by the additive &#x60;shared&#x60;, &#x60;ownerPrincipalId&#x60; and &#x60;perUser*CapUsd&#x60;.
  ///
  /// Parameters:
  /// * [providerId] - LLM provider id
  /// * [llmProviderInput] 
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [LlmProvider] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<LlmProvider>> updateLlmProvider({ 
    required String providerId,
    required LlmProviderInput llmProviderInput,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/llm/providers/{providerId}'.replaceAll('{' r'providerId' '}', providerId.toString());
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
      _bodyData = jsonEncode(llmProviderInput);

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

    LlmProvider? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<LlmProvider, LlmProvider>(rawData, 'LlmProvider', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<LlmProvider>(
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

  /// Set LLM roles and options
  /// Replaces the LLM roles and options; omitted members take their defaults. Reassigning &#x60;embeddingProvider&#x60; publishes an embedding-model change (027 reindexes). &#x60;allowPrivateLocal&#x60; is accepted only as false until prompt 039.  **Scope:** &#x60;admin&#x60;.  **v2.0 (shared and personal, R2-011):** this operation concerns the shared LLM configuration only and needs &#x60;llm.shared.manage&#x60;; personal providers are managed through the provider operations with &#x60;self.llm&#x60;, and the instance roles can be held only by shared providers.
  ///
  /// Parameters:
  /// * [llmSettings] 
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [LlmSettings] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<LlmSettings>> updateLlmSettings({ 
    required LlmSettings llmSettings,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/llm/settings';
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
      _bodyData = jsonEncode(llmSettings);

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

    LlmSettings? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<LlmSettings, LlmSettings>(rawData, 'LlmSettings', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<LlmSettings>(
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
