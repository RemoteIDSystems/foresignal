//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

import 'dart:async';

// ignore: unused_import
import 'dart:convert';
import 'package:foresignal_api/src/deserialize.dart';
import 'package:dio/dio.dart';

import 'package:foresignal_api/src/model/event_stream_filter.dart';
import 'package:foresignal_api/src/model/problem.dart';
import 'package:foresignal_api/src/model/situation_theme.dart';
import 'package:foresignal_api/src/model/stream_events200_response.dart';

class EventsApi {

  final Dio _dio;

  const EventsApi(this._dio);

  /// Multiplexed, re-filterable event stream (SSE)
  /// One SSE connection for several topics (changes, alerts, notifications, system, and topics added later). The first event is &#x60;hello {streamId, topics, watermarkSeq, filterVersion}&#x60;; id-bearing events carry a composite &#x60;id:&#x60; holding the latest cursor of every subscribed topic, and &#x60;Last-Event-ID&#x60; resumes each topic. Swap the filter without reconnecting via PATCH &#x60;/v1/events/stream/{streamId}/filter&#x60; (debounce map pans 300–500 ms); live, touch and control events never carry an id.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [lastEventID] - Composite resume cursor (`c=<seq>;a=<rev>;n=<id>` …) from the last id-bearing event; malformed → 400 INVALID_CURSOR
  /// * [topics] - Comma-separated topics (default `changes`); registered by the EventTopic SPI: changes, alerts (017), notifications and system (018), later more. Unknown → 400 UNKNOWN_TOPIC; missing scope → 403.
  /// * [changesPeriodSource] - changes topic: source ids (repeatable or comma-separated)
  /// * [changesPeriodKind] - changes topic: kinds (repeatable or comma-separated)
  /// * [changesPeriodExternalId] - changes topic: external ids (repeatable or comma-separated)
  /// * [changesPeriodBbox] - minLon,minLat,maxLon,maxLat (WGS84). minLon > maxLon crosses the antimeridian and is split into two boxes; minLat < maxLat is required.
  /// * [changesPeriodNear] - Centre point `lon,lat` (GeoJSON order); requires `radiusKm`
  /// * [changesPeriodRadiusKm] - Radius in km around `near` (0 < r ≤ 2000); required with `near`
  /// * [changesPeriodLive] - changes topic: send `live` events
  /// * [changesPeriodTouches] - changes topic: send `touch` events
  /// * [changesPeriodPayload] - changes topic: include payloads
  /// * [alertsPeriodMinSeverity] - alerts topic: minimum severity
  /// * [alertsPeriodRuleId] - alerts topic: rule ids (repeatable or comma-separated)
  /// * [alertsPeriodSource] - alerts topic: source ids (repeatable or comma-separated)
  /// * [alertsPeriodKind] - alerts topic: kinds (repeatable or comma-separated)
  /// * [alertsPeriodIncludeSuppressed] - alerts topic: include suppressed alerts
  /// * [ticket] - Stream or tile ticket from `POST /v1/stream-tickets` (023), for clients that cannot send an `Authorization` header. Accepted only for the ticket's purpose and never in a header; stripped from logs
  /// * [changesPeriodPlace] - Place id(s) (036), resolved to their areas and intersected with the other filters; requires a token (places are never anonymous)
  /// * [changesPeriodView] - Saved view id or slug (036), intersected with the other filters; a view that references a place or watchlist needs a token
  /// * [changesPeriodInWatchlist] - Only entities whose identity is on this watchlist (036); token required (401 for anonymous and feed keys)
  /// * [alertsPeriodIncludeExercise] - alerts topic: include exercise alerts
  /// * [licencePolicy] - Licence policy flags (comma-separated): `no-nc` excludes records whose commercial use is not ALLOWED, `no-sa` excludes share-alike records, `redistributable` excludes records whose redistribution is not ALLOWED. Adds to the token's `licencePolicy` floor; it can never remove a token flag.
  /// * [foresignalLicencePolicy] - Header form of `licencePolicy` (comma-separated flags `no-nc`, `no-sa`, `redistributable`)
  /// * [workspacePeriodWorkspaceId] - `workspace` topic: the workspace to follow
  /// * [workspacePeriodClientId] - `workspace` topic: this client's id (presence, `originClientId`)
  /// * [workspacePeriodClientKind] - `workspace` topic: this client's kind (presence)
  /// * [chroniclePeriodChronicleId] - `chronicle` topic: chronicle ids (comma-separated)
  /// * [lastEventId] - Composite resume cursor, the same value as the Last-Event-ID header, for clients (browser EventSource) that cannot set the header on first connect. The header wins when both are present.
  /// * [situationsPeriodStatus] - situations topic (118): only events of situations in this status
  /// * [situationsPeriodTheme] - situations topic (118): only events of situations of this theme
  /// * [externalPeriodSourceId] - external topic (138, admin): only events of this provider
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [StreamEvents200Response] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<StreamEvents200Response>> streamEvents({ 
    String? lastEventID,
    List<String>? topics,
    List<String>? changesPeriodSource,
    List<String>? changesPeriodKind,
    List<String>? changesPeriodExternalId,
    String? changesPeriodBbox,
    String? changesPeriodNear,
    num? changesPeriodRadiusKm,
    bool? changesPeriodLive = true,
    bool? changesPeriodTouches = false,
    bool? changesPeriodPayload = false,
    String? alertsPeriodMinSeverity,
    List<String>? alertsPeriodRuleId,
    List<String>? alertsPeriodSource,
    List<String>? alertsPeriodKind,
    bool? alertsPeriodIncludeSuppressed = false,
    String? ticket,
    List<String>? changesPeriodPlace,
    String? changesPeriodView,
    String? changesPeriodInWatchlist,
    bool? alertsPeriodIncludeExercise = false,
    List<String>? licencePolicy,
    String? foresignalLicencePolicy,
    String? workspacePeriodWorkspaceId,
    String? workspacePeriodClientId,
    String? workspacePeriodClientKind,
    List<String>? chroniclePeriodChronicleId,
    String? lastEventId,
    String? situationsPeriodStatus,
    SituationTheme? situationsPeriodTheme,
    String? externalPeriodSourceId,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/events/stream';
    final _options = Options(
      method: r'GET',
      headers: <String, dynamic>{
        if (lastEventID != null) r'Last-Event-ID': lastEventID,
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
      if (topics != null) r'topics': topics,
      if (changesPeriodSource != null) r'changes.source': changesPeriodSource,
      if (changesPeriodKind != null) r'changes.kind': changesPeriodKind,
      if (changesPeriodExternalId != null) r'changes.externalId': changesPeriodExternalId,
      if (changesPeriodBbox != null) r'changes.bbox': changesPeriodBbox,
      if (changesPeriodNear != null) r'changes.near': changesPeriodNear,
      if (changesPeriodRadiusKm != null) r'changes.radiusKm': changesPeriodRadiusKm,
      if (changesPeriodLive != null) r'changes.live': changesPeriodLive,
      if (changesPeriodTouches != null) r'changes.touches': changesPeriodTouches,
      if (changesPeriodPayload != null) r'changes.payload': changesPeriodPayload,
      if (alertsPeriodMinSeverity != null) r'alerts.minSeverity': alertsPeriodMinSeverity,
      if (alertsPeriodRuleId != null) r'alerts.ruleId': alertsPeriodRuleId,
      if (alertsPeriodSource != null) r'alerts.source': alertsPeriodSource,
      if (alertsPeriodKind != null) r'alerts.kind': alertsPeriodKind,
      if (alertsPeriodIncludeSuppressed != null) r'alerts.includeSuppressed': alertsPeriodIncludeSuppressed,
      if (ticket != null) r'ticket': ticket,
      if (changesPeriodPlace != null) r'changes.place': changesPeriodPlace,
      if (changesPeriodView != null) r'changes.view': changesPeriodView,
      if (changesPeriodInWatchlist != null) r'changes.inWatchlist': changesPeriodInWatchlist,
      if (alertsPeriodIncludeExercise != null) r'alerts.includeExercise': alertsPeriodIncludeExercise,
      if (licencePolicy != null) r'licencePolicy': licencePolicy,
      if (workspacePeriodWorkspaceId != null) r'workspace.workspaceId': workspacePeriodWorkspaceId,
      if (workspacePeriodClientId != null) r'workspace.clientId': workspacePeriodClientId,
      if (workspacePeriodClientKind != null) r'workspace.clientKind': workspacePeriodClientKind,
      if (chroniclePeriodChronicleId != null) r'chronicle.chronicleId': chroniclePeriodChronicleId,
      if (lastEventId != null) r'lastEventId': lastEventId,
      if (situationsPeriodStatus != null) r'situations.status': situationsPeriodStatus,
      if (situationsPeriodTheme != null) r'situations.theme': situationsPeriodTheme,
      if (externalPeriodSourceId != null) r'external.sourceId': externalPeriodSourceId,
    };

    final _response = await _dio.request<Object>(
      _path,
      options: _options,
      queryParameters: _queryParameters,
      cancelToken: cancelToken,
      onSendProgress: onSendProgress,
      onReceiveProgress: onReceiveProgress,
    );

    StreamEvents200Response? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<StreamEvents200Response, StreamEvents200Response>(rawData, 'StreamEvents200Response', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<StreamEvents200Response>(
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

  /// Swap an open stream&#39;s filter
  /// Validates the whole body, then swaps the filter atomically on the stream&#39;s sender: the stream emits &#x60;filter-applied {atSeq, filterVersion}&#x60; and every later event uses the new filter. Omitted topic blocks are unchanged; an added topic starts live from now without replay; there is no spatial delta replay (load the newly exposed area with /v1/records or tiles). Only the owning principal may patch (anonymous callers send &#x60;Foresignal-Stream-Key&#x60;); others get 404.  **Scope:** &#x60;read&#x60; (open while &#x60;security.openReads&#x60; is on).
  ///
  /// Parameters:
  /// * [streamId] - Stream id from the `hello` event
  /// * [eventStreamFilter] 
  /// * [foresignalStreamKey] - Anonymous streams: the `streamKey` from `hello`
  /// * [cancelToken] - A [CancelToken] that can be used to cancel the operation
  /// * [headers] - Can be used to add additional headers to the request
  /// * [extras] - Can be used to add flags to the request
  /// * [validateStatus] - A [ValidateStatus] callback that can be used to determine request success based on the HTTP status of the response
  /// * [onSendProgress] - A [ProgressCallback] that can be used to get the send progress
  /// * [onReceiveProgress] - A [ProgressCallback] that can be used to get the receive progress
  ///
  /// Returns a [Future] containing a [Response] with a [EventStreamFilter] as data
  /// Throws [DioException] if API call or serialization fails
  Future<Response<EventStreamFilter>> updateEventStreamFilter({ 
    required String streamId,
    required EventStreamFilter eventStreamFilter,
    String? foresignalStreamKey,
    CancelToken? cancelToken,
    Map<String, dynamic>? headers,
    Map<String, dynamic>? extra,
    ValidateStatus? validateStatus,
    ProgressCallback? onSendProgress,
    ProgressCallback? onReceiveProgress,
  }) async {
    final _path = r'/v1/events/stream/{streamId}/filter'.replaceAll('{' r'streamId' '}', streamId.toString());
    final _options = Options(
      method: r'PATCH',
      headers: <String, dynamic>{
        if (foresignalStreamKey != null) r'Foresignal-Stream-Key': foresignalStreamKey,
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
      _bodyData = jsonEncode(eventStreamFilter);

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

    EventStreamFilter? _responseData;

    try {
final rawData = _response.data;
_responseData = rawData == null ? null : deserialize<EventStreamFilter, EventStreamFilter>(rawData, 'EventStreamFilter', growable: true);

    } catch (error, stackTrace) {
      throw DioException(
        requestOptions: _response.requestOptions,
        response: _response,
        type: DioExceptionType.unknown,
        error: error,
        stackTrace: stackTrace,
      );
    }

    return Response<EventStreamFilter>(
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
