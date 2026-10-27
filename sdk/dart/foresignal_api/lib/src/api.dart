//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

import 'package:dio/dio.dart';
import 'package:foresignal_api/src/auth/api_key_auth.dart';
import 'package:foresignal_api/src/auth/basic_auth.dart';
import 'package:foresignal_api/src/auth/bearer_auth.dart';
import 'package:foresignal_api/src/auth/oauth.dart';
import 'package:foresignal_api/src/api/aggregators_api.dart';
import 'package:foresignal_api/src/api/alerts_api.dart';
import 'package:foresignal_api/src/api/analysis_api.dart';
import 'package:foresignal_api/src/api/analyst_api.dart';
import 'package:foresignal_api/src/api/analytics_api.dart';
import 'package:foresignal_api/src/api/anomalies_api.dart';
import 'package:foresignal_api/src/api/archive_api.dart';
import 'package:foresignal_api/src/api/ask_api.dart';
import 'package:foresignal_api/src/api/assessments_api.dart';
import 'package:foresignal_api/src/api/attention_api.dart';
import 'package:foresignal_api/src/api/audit_api.dart';
import 'package:foresignal_api/src/api/auth_api.dart';
import 'package:foresignal_api/src/api/backups_api.dart';
import 'package:foresignal_api/src/api/briefings_api.dart';
import 'package:foresignal_api/src/api/cap_api.dart';
import 'package:foresignal_api/src/api/calendars_api.dart';
import 'package:foresignal_api/src/api/changes_api.dart';
import 'package:foresignal_api/src/api/channels_api.dart';
import 'package:foresignal_api/src/api/chronicles_api.dart';
import 'package:foresignal_api/src/api/collaboration_api.dart';
import 'package:foresignal_api/src/api/egress_api.dart';
import 'package:foresignal_api/src/api/entities_api.dart';
import 'package:foresignal_api/src/api/escalation_api.dart';
import 'package:foresignal_api/src/api/events_api.dart';
import 'package:foresignal_api/src/api/evidence_api.dart';
import 'package:foresignal_api/src/api/exchange_api.dart';
import 'package:foresignal_api/src/api/exercises_api.dart';
import 'package:foresignal_api/src/api/exports_api.dart';
import 'package:foresignal_api/src/api/external_api.dart';
import 'package:foresignal_api/src/api/federation_api.dart';
import 'package:foresignal_api/src/api/feeds_api.dart';
import 'package:foresignal_api/src/api/forecasts_api.dart';
import 'package:foresignal_api/src/api/ground_stations_api.dart';
import 'package:foresignal_api/src/api/history_api.dart';
import 'package:foresignal_api/src/api/hypotheses_api.dart';
import 'package:foresignal_api/src/api/infrastructure_api.dart';
import 'package:foresignal_api/src/api/ingest_api.dart';
import 'package:foresignal_api/src/api/intelligence_api.dart';
import 'package:foresignal_api/src/api/kml_api.dart';
import 'package:foresignal_api/src/api/kinematics_api.dart';
import 'package:foresignal_api/src/api/llm_api.dart';
import 'package:foresignal_api/src/api/mcp_api.dart';
import 'package:foresignal_api/src/api/mfa_api.dart';
import 'package:foresignal_api/src/api/map_styles_api.dart';
import 'package:foresignal_api/src/api/mappings_api.dart';
import 'package:foresignal_api/src/api/maps_api.dart';
import 'package:foresignal_api/src/api/media_api.dart';
import 'package:foresignal_api/src/api/news_api.dart';
import 'package:foresignal_api/src/api/notifications_api.dart';
import 'package:foresignal_api/src/api/ogc_api.dart';
import 'package:foresignal_api/src/api/objects_api.dart';
import 'package:foresignal_api/src/api/observability_api.dart';
import 'package:foresignal_api/src/api/outlooks_api.dart';
import 'package:foresignal_api/src/api/pattern_of_life_api.dart';
import 'package:foresignal_api/src/api/peers_api.dart';
import 'package:foresignal_api/src/api/places_api.dart';
import 'package:foresignal_api/src/api/plugins_api.dart';
import 'package:foresignal_api/src/api/preferences_api.dart';
import 'package:foresignal_api/src/api/products_api.dart';
import 'package:foresignal_api/src/api/provenance_api.dart';
import 'package:foresignal_api/src/api/push_api.dart';
import 'package:foresignal_api/src/api/quotas_api.dart';
import 'package:foresignal_api/src/api/records_api.dart';
import 'package:foresignal_api/src/api/reference_api.dart';
import 'package:foresignal_api/src/api/reports_api.dart';
import 'package:foresignal_api/src/api/resources_api.dart';
import 'package:foresignal_api/src/api/roles_api.dart';
import 'package:foresignal_api/src/api/sampling_api.dart';
import 'package:foresignal_api/src/api/search_api.dart';
import 'package:foresignal_api/src/api/series_api.dart';
import 'package:foresignal_api/src/api/settings_api.dart';
import 'package:foresignal_api/src/api/share_links_api.dart';
import 'package:foresignal_api/src/api/sinks_api.dart';
import 'package:foresignal_api/src/api/situations_api.dart';
import 'package:foresignal_api/src/api/social_api.dart';
import 'package:foresignal_api/src/api/sources_api.dart';
import 'package:foresignal_api/src/api/space_api.dart';
import 'package:foresignal_api/src/api/standards_api.dart';
import 'package:foresignal_api/src/api/stats_api.dart';
import 'package:foresignal_api/src/api/system_api.dart';
import 'package:foresignal_api/src/api/tiles_api.dart';
import 'package:foresignal_api/src/api/tokens_api.dart';
import 'package:foresignal_api/src/api/transit_api.dart';
import 'package:foresignal_api/src/api/triggers_api.dart';
import 'package:foresignal_api/src/api/trips_api.dart';
import 'package:foresignal_api/src/api/users_api.dart';
import 'package:foresignal_api/src/api/views_api.dart';
import 'package:foresignal_api/src/api/watchlists_api.dart';
import 'package:foresignal_api/src/api/web_sub_api.dart';
import 'package:foresignal_api/src/api/workspaces_api.dart';

class ForesignalApi {
  static const String basePath = r'http://localhost:8080';

  final Dio dio;
  ForesignalApi({
    Dio? dio,
    String? basePathOverride,
    List<Interceptor>? interceptors,
  })  : 
        this.dio = dio ??
            Dio(BaseOptions(
              baseUrl: basePathOverride ?? basePath,
              connectTimeout: const Duration(milliseconds: 5000),
              receiveTimeout: const Duration(milliseconds: 3000),
            )) {
    if (interceptors == null) {
      this.dio.interceptors.addAll([
        OAuthInterceptor(),
        BasicAuthInterceptor(),
        BearerAuthInterceptor(),
        ApiKeyAuthInterceptor(),
      ]);
    } else {
      this.dio.interceptors.addAll(interceptors);
    }
  }

  void setOAuthToken(String name, String token) {
    if (this.dio.interceptors.any((i) => i is OAuthInterceptor)) {
      (this.dio.interceptors.firstWhere((i) => i is OAuthInterceptor) as OAuthInterceptor).tokens[name] = token;
    }
  }

  /// Removes the OAuth token associated with the given [name].
  ///
  /// If no [OAuthInterceptor] is registered or no token exists for the given
  /// [name], this method has no effect.
  void removeOAuthToken(String name) {
    if (this.dio.interceptors.any((i) => i is OAuthInterceptor)) {
      (this.dio.interceptors.firstWhere((i) => i is OAuthInterceptor) as OAuthInterceptor).tokens.remove(name);
    }
  }

  void setBearerAuth(String name, String token) {
    if (this.dio.interceptors.any((i) => i is BearerAuthInterceptor)) {
      (this.dio.interceptors.firstWhere((i) => i is BearerAuthInterceptor) as BearerAuthInterceptor).tokens[name] = token;
    }
  }

  /// Removes the bearer authentication token associated with the given [name].
  ///
  /// If no [BearerAuthInterceptor] is registered or no token exists for the
  /// given [name], this method has no effect.
  void removeBearerAuth(String name) {
    if (this.dio.interceptors.any((i) => i is BearerAuthInterceptor)) {
      (this.dio.interceptors.firstWhere((i) => i is BearerAuthInterceptor) as BearerAuthInterceptor).tokens.remove(name);
    }
  }

  void setBasicAuth(String name, String username, String password) {
    if (this.dio.interceptors.any((i) => i is BasicAuthInterceptor)) {
      (this.dio.interceptors.firstWhere((i) => i is BasicAuthInterceptor) as BasicAuthInterceptor).authInfo[name] = BasicAuthInfo(username, password);
    }
  }

  /// Removes the basic authentication credentials associated with the given [name].
  ///
  /// If no [BasicAuthInterceptor] is registered or no credentials exist for the
  /// given [name], this method has no effect.
  void removeBasicAuth(String name) {
    if (this.dio.interceptors.any((i) => i is BasicAuthInterceptor)) {
      (this.dio.interceptors.firstWhere((i) => i is BasicAuthInterceptor) as BasicAuthInterceptor).authInfo.remove(name);
    }
  }

  void setApiKey(String name, String apiKey) {
    if (this.dio.interceptors.any((i) => i is ApiKeyAuthInterceptor)) {
      (this.dio.interceptors.firstWhere((element) => element is ApiKeyAuthInterceptor) as ApiKeyAuthInterceptor).apiKeys[name] = apiKey;
    }
  }

  /// Removes the API key associated with the given [name].
  ///
  /// If no [ApiKeyAuthInterceptor] is registered or no API key exists for the
  /// given [name], this method has no effect.
  void removeApiKey(String name) {
    if (this.dio.interceptors.any((i) => i is ApiKeyAuthInterceptor)) {
      (this.dio.interceptors.firstWhere((element) => element is ApiKeyAuthInterceptor) as ApiKeyAuthInterceptor).apiKeys.remove(name);
    }
  }

  /// Get AggregatorsApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  AggregatorsApi getAggregatorsApi() {
    return AggregatorsApi(dio);
  }

  /// Get AlertsApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  AlertsApi getAlertsApi() {
    return AlertsApi(dio);
  }

  /// Get AnalysisApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  AnalysisApi getAnalysisApi() {
    return AnalysisApi(dio);
  }

  /// Get AnalystApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  AnalystApi getAnalystApi() {
    return AnalystApi(dio);
  }

  /// Get AnalyticsApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  AnalyticsApi getAnalyticsApi() {
    return AnalyticsApi(dio);
  }

  /// Get AnomaliesApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  AnomaliesApi getAnomaliesApi() {
    return AnomaliesApi(dio);
  }

  /// Get ArchiveApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  ArchiveApi getArchiveApi() {
    return ArchiveApi(dio);
  }

  /// Get AskApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  AskApi getAskApi() {
    return AskApi(dio);
  }

  /// Get AssessmentsApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  AssessmentsApi getAssessmentsApi() {
    return AssessmentsApi(dio);
  }

  /// Get AttentionApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  AttentionApi getAttentionApi() {
    return AttentionApi(dio);
  }

  /// Get AuditApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  AuditApi getAuditApi() {
    return AuditApi(dio);
  }

  /// Get AuthApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  AuthApi getAuthApi() {
    return AuthApi(dio);
  }

  /// Get BackupsApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  BackupsApi getBackupsApi() {
    return BackupsApi(dio);
  }

  /// Get BriefingsApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  BriefingsApi getBriefingsApi() {
    return BriefingsApi(dio);
  }

  /// Get CAPApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  CAPApi getCAPApi() {
    return CAPApi(dio);
  }

  /// Get CalendarsApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  CalendarsApi getCalendarsApi() {
    return CalendarsApi(dio);
  }

  /// Get ChangesApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  ChangesApi getChangesApi() {
    return ChangesApi(dio);
  }

  /// Get ChannelsApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  ChannelsApi getChannelsApi() {
    return ChannelsApi(dio);
  }

  /// Get ChroniclesApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  ChroniclesApi getChroniclesApi() {
    return ChroniclesApi(dio);
  }

  /// Get CollaborationApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  CollaborationApi getCollaborationApi() {
    return CollaborationApi(dio);
  }

  /// Get EgressApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  EgressApi getEgressApi() {
    return EgressApi(dio);
  }

  /// Get EntitiesApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  EntitiesApi getEntitiesApi() {
    return EntitiesApi(dio);
  }

  /// Get EscalationApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  EscalationApi getEscalationApi() {
    return EscalationApi(dio);
  }

  /// Get EventsApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  EventsApi getEventsApi() {
    return EventsApi(dio);
  }

  /// Get EvidenceApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  EvidenceApi getEvidenceApi() {
    return EvidenceApi(dio);
  }

  /// Get ExchangeApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  ExchangeApi getExchangeApi() {
    return ExchangeApi(dio);
  }

  /// Get ExercisesApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  ExercisesApi getExercisesApi() {
    return ExercisesApi(dio);
  }

  /// Get ExportsApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  ExportsApi getExportsApi() {
    return ExportsApi(dio);
  }

  /// Get ExternalApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  ExternalApi getExternalApi() {
    return ExternalApi(dio);
  }

  /// Get FederationApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  FederationApi getFederationApi() {
    return FederationApi(dio);
  }

  /// Get FeedsApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  FeedsApi getFeedsApi() {
    return FeedsApi(dio);
  }

  /// Get ForecastsApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  ForecastsApi getForecastsApi() {
    return ForecastsApi(dio);
  }

  /// Get GroundStationsApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  GroundStationsApi getGroundStationsApi() {
    return GroundStationsApi(dio);
  }

  /// Get HistoryApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  HistoryApi getHistoryApi() {
    return HistoryApi(dio);
  }

  /// Get HypothesesApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  HypothesesApi getHypothesesApi() {
    return HypothesesApi(dio);
  }

  /// Get InfrastructureApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  InfrastructureApi getInfrastructureApi() {
    return InfrastructureApi(dio);
  }

  /// Get IngestApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  IngestApi getIngestApi() {
    return IngestApi(dio);
  }

  /// Get IntelligenceApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  IntelligenceApi getIntelligenceApi() {
    return IntelligenceApi(dio);
  }

  /// Get KMLApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  KMLApi getKMLApi() {
    return KMLApi(dio);
  }

  /// Get KinematicsApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  KinematicsApi getKinematicsApi() {
    return KinematicsApi(dio);
  }

  /// Get LLMApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  LLMApi getLLMApi() {
    return LLMApi(dio);
  }

  /// Get MCPApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  MCPApi getMCPApi() {
    return MCPApi(dio);
  }

  /// Get MFAApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  MFAApi getMFAApi() {
    return MFAApi(dio);
  }

  /// Get MapStylesApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  MapStylesApi getMapStylesApi() {
    return MapStylesApi(dio);
  }

  /// Get MappingsApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  MappingsApi getMappingsApi() {
    return MappingsApi(dio);
  }

  /// Get MapsApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  MapsApi getMapsApi() {
    return MapsApi(dio);
  }

  /// Get MediaApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  MediaApi getMediaApi() {
    return MediaApi(dio);
  }

  /// Get NewsApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  NewsApi getNewsApi() {
    return NewsApi(dio);
  }

  /// Get NotificationsApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  NotificationsApi getNotificationsApi() {
    return NotificationsApi(dio);
  }

  /// Get OGCApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  OGCApi getOGCApi() {
    return OGCApi(dio);
  }

  /// Get ObjectsApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  ObjectsApi getObjectsApi() {
    return ObjectsApi(dio);
  }

  /// Get ObservabilityApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  ObservabilityApi getObservabilityApi() {
    return ObservabilityApi(dio);
  }

  /// Get OutlooksApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  OutlooksApi getOutlooksApi() {
    return OutlooksApi(dio);
  }

  /// Get PatternOfLifeApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  PatternOfLifeApi getPatternOfLifeApi() {
    return PatternOfLifeApi(dio);
  }

  /// Get PeersApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  PeersApi getPeersApi() {
    return PeersApi(dio);
  }

  /// Get PlacesApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  PlacesApi getPlacesApi() {
    return PlacesApi(dio);
  }

  /// Get PluginsApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  PluginsApi getPluginsApi() {
    return PluginsApi(dio);
  }

  /// Get PreferencesApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  PreferencesApi getPreferencesApi() {
    return PreferencesApi(dio);
  }

  /// Get ProductsApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  ProductsApi getProductsApi() {
    return ProductsApi(dio);
  }

  /// Get ProvenanceApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  ProvenanceApi getProvenanceApi() {
    return ProvenanceApi(dio);
  }

  /// Get PushApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  PushApi getPushApi() {
    return PushApi(dio);
  }

  /// Get QuotasApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  QuotasApi getQuotasApi() {
    return QuotasApi(dio);
  }

  /// Get RecordsApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  RecordsApi getRecordsApi() {
    return RecordsApi(dio);
  }

  /// Get ReferenceApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  ReferenceApi getReferenceApi() {
    return ReferenceApi(dio);
  }

  /// Get ReportsApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  ReportsApi getReportsApi() {
    return ReportsApi(dio);
  }

  /// Get ResourcesApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  ResourcesApi getResourcesApi() {
    return ResourcesApi(dio);
  }

  /// Get RolesApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  RolesApi getRolesApi() {
    return RolesApi(dio);
  }

  /// Get SamplingApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  SamplingApi getSamplingApi() {
    return SamplingApi(dio);
  }

  /// Get SearchApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  SearchApi getSearchApi() {
    return SearchApi(dio);
  }

  /// Get SeriesApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  SeriesApi getSeriesApi() {
    return SeriesApi(dio);
  }

  /// Get SettingsApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  SettingsApi getSettingsApi() {
    return SettingsApi(dio);
  }

  /// Get ShareLinksApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  ShareLinksApi getShareLinksApi() {
    return ShareLinksApi(dio);
  }

  /// Get SinksApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  SinksApi getSinksApi() {
    return SinksApi(dio);
  }

  /// Get SituationsApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  SituationsApi getSituationsApi() {
    return SituationsApi(dio);
  }

  /// Get SocialApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  SocialApi getSocialApi() {
    return SocialApi(dio);
  }

  /// Get SourcesApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  SourcesApi getSourcesApi() {
    return SourcesApi(dio);
  }

  /// Get SpaceApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  SpaceApi getSpaceApi() {
    return SpaceApi(dio);
  }

  /// Get StandardsApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  StandardsApi getStandardsApi() {
    return StandardsApi(dio);
  }

  /// Get StatsApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  StatsApi getStatsApi() {
    return StatsApi(dio);
  }

  /// Get SystemApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  SystemApi getSystemApi() {
    return SystemApi(dio);
  }

  /// Get TilesApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  TilesApi getTilesApi() {
    return TilesApi(dio);
  }

  /// Get TokensApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  TokensApi getTokensApi() {
    return TokensApi(dio);
  }

  /// Get TransitApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  TransitApi getTransitApi() {
    return TransitApi(dio);
  }

  /// Get TriggersApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  TriggersApi getTriggersApi() {
    return TriggersApi(dio);
  }

  /// Get TripsApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  TripsApi getTripsApi() {
    return TripsApi(dio);
  }

  /// Get UsersApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  UsersApi getUsersApi() {
    return UsersApi(dio);
  }

  /// Get ViewsApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  ViewsApi getViewsApi() {
    return ViewsApi(dio);
  }

  /// Get WatchlistsApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  WatchlistsApi getWatchlistsApi() {
    return WatchlistsApi(dio);
  }

  /// Get WebSubApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  WebSubApi getWebSubApi() {
    return WebSubApi(dio);
  }

  /// Get WorkspacesApi instance, base route and serializer can be overridden by a given but be careful,
  /// by doing that all interceptors will not be executed
  WorkspacesApi getWorkspacesApi() {
    return WorkspacesApi(dio);
  }
}
