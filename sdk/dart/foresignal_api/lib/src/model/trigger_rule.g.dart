// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'trigger_rule.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TriggerRuleCWProxy {
  TriggerRule name(String name);

  TriggerRule enabled(bool? enabled);

  TriggerRule source_(
    @Deprecated('source_ has been deprecated') String? source_,
  );

  TriggerRule kind(@Deprecated('kind has been deprecated') String? kind);

  TriggerRule conditions(List<TriggerRuleInputConditionsInner>? conditions);

  TriggerRule geofence(Geometry? geofence);

  TriggerRule mode(TriggerRuleModeEnum? mode);

  TriggerRule onlyNew(bool? onlyNew);

  TriggerRule cooldown(String? cooldown);

  TriggerRule messageTemplate(String? messageTemplate);

  TriggerRule severity(TriggerRuleSeverityEnum? severity);

  TriggerRule enrich(bool? enrich);

  TriggerRule enrichRadiusKm(num? enrichRadiusKm);

  TriggerRule enrichWindow(String? enrichWindow);

  TriggerRule tags(List<String>? tags);

  TriggerRule sources(List<String>? sources);

  TriggerRule kinds(List<String>? kinds);

  TriggerRule bufferMeters(num? bufferMeters);

  TriggerRule hysteresisMeters(num? hysteresisMeters);

  TriggerRule near(Near? near);

  TriggerRule reappearAsEnter(bool? reappearAsEnter);

  TriggerRule cooldownScope(TriggerRuleCooldownScopeEnum? cooldownScope);

  TriggerRule autoResolve(TriggerRuleInputAutoResolve? autoResolve);

  TriggerRule maxAlertsPerHour(int? maxAlertsPerHour);

  TriggerRule autoDisableOnStorm(bool? autoDisableOnStorm);

  TriggerRule activeFrom(DateTime? activeFrom);

  TriggerRule activeUntil(DateTime? activeUntil);

  TriggerRule activeWindow(ActiveWindow? activeWindow);

  TriggerRule forecastHorizon(String? forecastHorizon);

  TriggerRule escalationPolicyId(String? escalationPolicyId);

  TriggerRule geofenceRef(GeofenceRef? geofenceRef);

  TriggerRule weatherContext(TriggerRuleWeatherContextEnum? weatherContext);

  TriggerRule type(TriggerRuleTypeEnum? type);

  TriggerRule window(TriggerRuleInputWindow? window);

  TriggerRule id(String id);

  TriggerRule createdAt(DateTime createdAt);

  TriggerRule updatedAt(DateTime updatedAt);

  TriggerRule lastFiredAt(DateTime? lastFiredAt);

  TriggerRule alertCount(int? alertCount);

  TriggerRule lastError(String? lastError);

  TriggerRule lastErrorAt(DateTime? lastErrorAt);

  TriggerRule openAlertCount(int? openAlertCount);

  TriggerRule suppressedCount(int? suppressedCount);

  TriggerRule lastStormAt(DateTime? lastStormAt);

  TriggerRule feedbackCounts(TriggerRuleAllOfFeedbackCounts? feedbackCounts);

  TriggerRule snoozedUntil(DateTime? snoozedUntil);

  TriggerRule geofenceRoute(Geometry? geofenceRoute);

  TriggerRule mutedUntil(DateTime? mutedUntil);

  TriggerRule geofenceOrigin(TriggerRuleGeofenceOriginEnum? geofenceOrigin);

  TriggerRule anchorStatus(TriggerRuleAnchorStatusEnum? anchorStatus);

  TriggerRule windowStatus(WindowStatus? windowStatus);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TriggerRule(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TriggerRule(...).copyWith(id: 12, name: "My name")
  /// ```
  TriggerRule call({
    String name,
    bool? enabled,
    @Deprecated('source_ has been deprecated') String? source_,
    @Deprecated('kind has been deprecated') String? kind,
    List<TriggerRuleInputConditionsInner>? conditions,
    Geometry? geofence,
    TriggerRuleModeEnum? mode,
    bool? onlyNew,
    String? cooldown,
    String? messageTemplate,
    TriggerRuleSeverityEnum? severity,
    bool? enrich,
    num? enrichRadiusKm,
    String? enrichWindow,
    List<String>? tags,
    List<String>? sources,
    List<String>? kinds,
    num? bufferMeters,
    num? hysteresisMeters,
    Near? near,
    bool? reappearAsEnter,
    TriggerRuleCooldownScopeEnum? cooldownScope,
    TriggerRuleInputAutoResolve? autoResolve,
    int? maxAlertsPerHour,
    bool? autoDisableOnStorm,
    DateTime? activeFrom,
    DateTime? activeUntil,
    ActiveWindow? activeWindow,
    String? forecastHorizon,
    String? escalationPolicyId,
    GeofenceRef? geofenceRef,
    TriggerRuleWeatherContextEnum? weatherContext,
    TriggerRuleTypeEnum? type,
    TriggerRuleInputWindow? window,
    String id,
    DateTime createdAt,
    DateTime updatedAt,
    DateTime? lastFiredAt,
    int? alertCount,
    String? lastError,
    DateTime? lastErrorAt,
    int? openAlertCount,
    int? suppressedCount,
    DateTime? lastStormAt,
    TriggerRuleAllOfFeedbackCounts? feedbackCounts,
    DateTime? snoozedUntil,
    Geometry? geofenceRoute,
    DateTime? mutedUntil,
    TriggerRuleGeofenceOriginEnum? geofenceOrigin,
    TriggerRuleAnchorStatusEnum? anchorStatus,
    WindowStatus? windowStatus,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTriggerRule.copyWith(...)` or call `instanceOfTriggerRule.copyWith.fieldName(value)` for a single field.
class _$TriggerRuleCWProxyImpl implements _$TriggerRuleCWProxy {
  const _$TriggerRuleCWProxyImpl(this._value);

  final TriggerRule _value;

  @override
  TriggerRule name(String name) => call(name: name);

  @override
  TriggerRule enabled(bool? enabled) => call(enabled: enabled);

  @override
  TriggerRule source_(
    @Deprecated('source_ has been deprecated') String? source_,
  ) => call(source_: source_);

  @override
  TriggerRule kind(@Deprecated('kind has been deprecated') String? kind) =>
      call(kind: kind);

  @override
  TriggerRule conditions(List<TriggerRuleInputConditionsInner>? conditions) =>
      call(conditions: conditions);

  @override
  TriggerRule geofence(Geometry? geofence) => call(geofence: geofence);

  @override
  TriggerRule mode(TriggerRuleModeEnum? mode) => call(mode: mode);

  @override
  TriggerRule onlyNew(bool? onlyNew) => call(onlyNew: onlyNew);

  @override
  TriggerRule cooldown(String? cooldown) => call(cooldown: cooldown);

  @override
  TriggerRule messageTemplate(String? messageTemplate) =>
      call(messageTemplate: messageTemplate);

  @override
  TriggerRule severity(TriggerRuleSeverityEnum? severity) =>
      call(severity: severity);

  @override
  TriggerRule enrich(bool? enrich) => call(enrich: enrich);

  @override
  TriggerRule enrichRadiusKm(num? enrichRadiusKm) =>
      call(enrichRadiusKm: enrichRadiusKm);

  @override
  TriggerRule enrichWindow(String? enrichWindow) =>
      call(enrichWindow: enrichWindow);

  @override
  TriggerRule tags(List<String>? tags) => call(tags: tags);

  @override
  TriggerRule sources(List<String>? sources) => call(sources: sources);

  @override
  TriggerRule kinds(List<String>? kinds) => call(kinds: kinds);

  @override
  TriggerRule bufferMeters(num? bufferMeters) =>
      call(bufferMeters: bufferMeters);

  @override
  TriggerRule hysteresisMeters(num? hysteresisMeters) =>
      call(hysteresisMeters: hysteresisMeters);

  @override
  TriggerRule near(Near? near) => call(near: near);

  @override
  TriggerRule reappearAsEnter(bool? reappearAsEnter) =>
      call(reappearAsEnter: reappearAsEnter);

  @override
  TriggerRule cooldownScope(TriggerRuleCooldownScopeEnum? cooldownScope) =>
      call(cooldownScope: cooldownScope);

  @override
  TriggerRule autoResolve(TriggerRuleInputAutoResolve? autoResolve) =>
      call(autoResolve: autoResolve);

  @override
  TriggerRule maxAlertsPerHour(int? maxAlertsPerHour) =>
      call(maxAlertsPerHour: maxAlertsPerHour);

  @override
  TriggerRule autoDisableOnStorm(bool? autoDisableOnStorm) =>
      call(autoDisableOnStorm: autoDisableOnStorm);

  @override
  TriggerRule activeFrom(DateTime? activeFrom) => call(activeFrom: activeFrom);

  @override
  TriggerRule activeUntil(DateTime? activeUntil) =>
      call(activeUntil: activeUntil);

  @override
  TriggerRule activeWindow(ActiveWindow? activeWindow) =>
      call(activeWindow: activeWindow);

  @override
  TriggerRule forecastHorizon(String? forecastHorizon) =>
      call(forecastHorizon: forecastHorizon);

  @override
  TriggerRule escalationPolicyId(String? escalationPolicyId) =>
      call(escalationPolicyId: escalationPolicyId);

  @override
  TriggerRule geofenceRef(GeofenceRef? geofenceRef) =>
      call(geofenceRef: geofenceRef);

  @override
  TriggerRule weatherContext(TriggerRuleWeatherContextEnum? weatherContext) =>
      call(weatherContext: weatherContext);

  @override
  TriggerRule type(TriggerRuleTypeEnum? type) => call(type: type);

  @override
  TriggerRule window(TriggerRuleInputWindow? window) => call(window: window);

  @override
  TriggerRule id(String id) => call(id: id);

  @override
  TriggerRule createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  TriggerRule updatedAt(DateTime updatedAt) => call(updatedAt: updatedAt);

  @override
  TriggerRule lastFiredAt(DateTime? lastFiredAt) =>
      call(lastFiredAt: lastFiredAt);

  @override
  TriggerRule alertCount(int? alertCount) => call(alertCount: alertCount);

  @override
  TriggerRule lastError(String? lastError) => call(lastError: lastError);

  @override
  TriggerRule lastErrorAt(DateTime? lastErrorAt) =>
      call(lastErrorAt: lastErrorAt);

  @override
  TriggerRule openAlertCount(int? openAlertCount) =>
      call(openAlertCount: openAlertCount);

  @override
  TriggerRule suppressedCount(int? suppressedCount) =>
      call(suppressedCount: suppressedCount);

  @override
  TriggerRule lastStormAt(DateTime? lastStormAt) =>
      call(lastStormAt: lastStormAt);

  @override
  TriggerRule feedbackCounts(TriggerRuleAllOfFeedbackCounts? feedbackCounts) =>
      call(feedbackCounts: feedbackCounts);

  @override
  TriggerRule snoozedUntil(DateTime? snoozedUntil) =>
      call(snoozedUntil: snoozedUntil);

  @override
  TriggerRule geofenceRoute(Geometry? geofenceRoute) =>
      call(geofenceRoute: geofenceRoute);

  @override
  TriggerRule mutedUntil(DateTime? mutedUntil) => call(mutedUntil: mutedUntil);

  @override
  TriggerRule geofenceOrigin(TriggerRuleGeofenceOriginEnum? geofenceOrigin) =>
      call(geofenceOrigin: geofenceOrigin);

  @override
  TriggerRule anchorStatus(TriggerRuleAnchorStatusEnum? anchorStatus) =>
      call(anchorStatus: anchorStatus);

  @override
  TriggerRule windowStatus(WindowStatus? windowStatus) =>
      call(windowStatus: windowStatus);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TriggerRule(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TriggerRule(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TriggerRule call({
    Object? name = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    @Deprecated('source_ has been deprecated')
    Object? source_ = const $CopyWithPlaceholder(),
    @Deprecated('kind has been deprecated')
    Object? kind = const $CopyWithPlaceholder(),
    Object? conditions = const $CopyWithPlaceholder(),
    Object? geofence = const $CopyWithPlaceholder(),
    Object? mode = const $CopyWithPlaceholder(),
    Object? onlyNew = const $CopyWithPlaceholder(),
    Object? cooldown = const $CopyWithPlaceholder(),
    Object? messageTemplate = const $CopyWithPlaceholder(),
    Object? severity = const $CopyWithPlaceholder(),
    Object? enrich = const $CopyWithPlaceholder(),
    Object? enrichRadiusKm = const $CopyWithPlaceholder(),
    Object? enrichWindow = const $CopyWithPlaceholder(),
    Object? tags = const $CopyWithPlaceholder(),
    Object? sources = const $CopyWithPlaceholder(),
    Object? kinds = const $CopyWithPlaceholder(),
    Object? bufferMeters = const $CopyWithPlaceholder(),
    Object? hysteresisMeters = const $CopyWithPlaceholder(),
    Object? near = const $CopyWithPlaceholder(),
    Object? reappearAsEnter = const $CopyWithPlaceholder(),
    Object? cooldownScope = const $CopyWithPlaceholder(),
    Object? autoResolve = const $CopyWithPlaceholder(),
    Object? maxAlertsPerHour = const $CopyWithPlaceholder(),
    Object? autoDisableOnStorm = const $CopyWithPlaceholder(),
    Object? activeFrom = const $CopyWithPlaceholder(),
    Object? activeUntil = const $CopyWithPlaceholder(),
    Object? activeWindow = const $CopyWithPlaceholder(),
    Object? forecastHorizon = const $CopyWithPlaceholder(),
    Object? escalationPolicyId = const $CopyWithPlaceholder(),
    Object? geofenceRef = const $CopyWithPlaceholder(),
    Object? weatherContext = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? window = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
    Object? lastFiredAt = const $CopyWithPlaceholder(),
    Object? alertCount = const $CopyWithPlaceholder(),
    Object? lastError = const $CopyWithPlaceholder(),
    Object? lastErrorAt = const $CopyWithPlaceholder(),
    Object? openAlertCount = const $CopyWithPlaceholder(),
    Object? suppressedCount = const $CopyWithPlaceholder(),
    Object? lastStormAt = const $CopyWithPlaceholder(),
    Object? feedbackCounts = const $CopyWithPlaceholder(),
    Object? snoozedUntil = const $CopyWithPlaceholder(),
    Object? geofenceRoute = const $CopyWithPlaceholder(),
    Object? mutedUntil = const $CopyWithPlaceholder(),
    Object? geofenceOrigin = const $CopyWithPlaceholder(),
    Object? anchorStatus = const $CopyWithPlaceholder(),
    Object? windowStatus = const $CopyWithPlaceholder(),
  }) {
    return TriggerRule(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      enabled: enabled == const $CopyWithPlaceholder()
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool?,
      source_: source_ == const $CopyWithPlaceholder()
          ? _value.source_
          // ignore: cast_nullable_to_non_nullable
          : source_ as String?,
      kind: kind == const $CopyWithPlaceholder()
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as String?,
      conditions: conditions == const $CopyWithPlaceholder()
          ? _value.conditions
          // ignore: cast_nullable_to_non_nullable
          : conditions as List<TriggerRuleInputConditionsInner>?,
      geofence: geofence == const $CopyWithPlaceholder()
          ? _value.geofence
          // ignore: cast_nullable_to_non_nullable
          : geofence as Geometry?,
      mode: mode == const $CopyWithPlaceholder()
          ? _value.mode
          // ignore: cast_nullable_to_non_nullable
          : mode as TriggerRuleModeEnum?,
      onlyNew: onlyNew == const $CopyWithPlaceholder()
          ? _value.onlyNew
          // ignore: cast_nullable_to_non_nullable
          : onlyNew as bool?,
      cooldown: cooldown == const $CopyWithPlaceholder()
          ? _value.cooldown
          // ignore: cast_nullable_to_non_nullable
          : cooldown as String?,
      messageTemplate: messageTemplate == const $CopyWithPlaceholder()
          ? _value.messageTemplate
          // ignore: cast_nullable_to_non_nullable
          : messageTemplate as String?,
      severity: severity == const $CopyWithPlaceholder()
          ? _value.severity
          // ignore: cast_nullable_to_non_nullable
          : severity as TriggerRuleSeverityEnum?,
      enrich: enrich == const $CopyWithPlaceholder()
          ? _value.enrich
          // ignore: cast_nullable_to_non_nullable
          : enrich as bool?,
      enrichRadiusKm: enrichRadiusKm == const $CopyWithPlaceholder()
          ? _value.enrichRadiusKm
          // ignore: cast_nullable_to_non_nullable
          : enrichRadiusKm as num?,
      enrichWindow: enrichWindow == const $CopyWithPlaceholder()
          ? _value.enrichWindow
          // ignore: cast_nullable_to_non_nullable
          : enrichWindow as String?,
      tags: tags == const $CopyWithPlaceholder()
          ? _value.tags
          // ignore: cast_nullable_to_non_nullable
          : tags as List<String>?,
      sources: sources == const $CopyWithPlaceholder()
          ? _value.sources
          // ignore: cast_nullable_to_non_nullable
          : sources as List<String>?,
      kinds: kinds == const $CopyWithPlaceholder()
          ? _value.kinds
          // ignore: cast_nullable_to_non_nullable
          : kinds as List<String>?,
      bufferMeters: bufferMeters == const $CopyWithPlaceholder()
          ? _value.bufferMeters
          // ignore: cast_nullable_to_non_nullable
          : bufferMeters as num?,
      hysteresisMeters: hysteresisMeters == const $CopyWithPlaceholder()
          ? _value.hysteresisMeters
          // ignore: cast_nullable_to_non_nullable
          : hysteresisMeters as num?,
      near: near == const $CopyWithPlaceholder()
          ? _value.near
          // ignore: cast_nullable_to_non_nullable
          : near as Near?,
      reappearAsEnter: reappearAsEnter == const $CopyWithPlaceholder()
          ? _value.reappearAsEnter
          // ignore: cast_nullable_to_non_nullable
          : reappearAsEnter as bool?,
      cooldownScope: cooldownScope == const $CopyWithPlaceholder()
          ? _value.cooldownScope
          // ignore: cast_nullable_to_non_nullable
          : cooldownScope as TriggerRuleCooldownScopeEnum?,
      autoResolve: autoResolve == const $CopyWithPlaceholder()
          ? _value.autoResolve
          // ignore: cast_nullable_to_non_nullable
          : autoResolve as TriggerRuleInputAutoResolve?,
      maxAlertsPerHour: maxAlertsPerHour == const $CopyWithPlaceholder()
          ? _value.maxAlertsPerHour
          // ignore: cast_nullable_to_non_nullable
          : maxAlertsPerHour as int?,
      autoDisableOnStorm: autoDisableOnStorm == const $CopyWithPlaceholder()
          ? _value.autoDisableOnStorm
          // ignore: cast_nullable_to_non_nullable
          : autoDisableOnStorm as bool?,
      activeFrom: activeFrom == const $CopyWithPlaceholder()
          ? _value.activeFrom
          // ignore: cast_nullable_to_non_nullable
          : activeFrom as DateTime?,
      activeUntil: activeUntil == const $CopyWithPlaceholder()
          ? _value.activeUntil
          // ignore: cast_nullable_to_non_nullable
          : activeUntil as DateTime?,
      activeWindow: activeWindow == const $CopyWithPlaceholder()
          ? _value.activeWindow
          // ignore: cast_nullable_to_non_nullable
          : activeWindow as ActiveWindow?,
      forecastHorizon: forecastHorizon == const $CopyWithPlaceholder()
          ? _value.forecastHorizon
          // ignore: cast_nullable_to_non_nullable
          : forecastHorizon as String?,
      escalationPolicyId: escalationPolicyId == const $CopyWithPlaceholder()
          ? _value.escalationPolicyId
          // ignore: cast_nullable_to_non_nullable
          : escalationPolicyId as String?,
      geofenceRef: geofenceRef == const $CopyWithPlaceholder()
          ? _value.geofenceRef
          // ignore: cast_nullable_to_non_nullable
          : geofenceRef as GeofenceRef?,
      weatherContext: weatherContext == const $CopyWithPlaceholder()
          ? _value.weatherContext
          // ignore: cast_nullable_to_non_nullable
          : weatherContext as TriggerRuleWeatherContextEnum?,
      type: type == const $CopyWithPlaceholder()
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as TriggerRuleTypeEnum?,
      window: window == const $CopyWithPlaceholder()
          ? _value.window
          // ignore: cast_nullable_to_non_nullable
          : window as TriggerRuleInputWindow?,
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      updatedAt: updatedAt == const $CopyWithPlaceholder() || updatedAt == null
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime,
      lastFiredAt: lastFiredAt == const $CopyWithPlaceholder()
          ? _value.lastFiredAt
          // ignore: cast_nullable_to_non_nullable
          : lastFiredAt as DateTime?,
      alertCount: alertCount == const $CopyWithPlaceholder()
          ? _value.alertCount
          // ignore: cast_nullable_to_non_nullable
          : alertCount as int?,
      lastError: lastError == const $CopyWithPlaceholder()
          ? _value.lastError
          // ignore: cast_nullable_to_non_nullable
          : lastError as String?,
      lastErrorAt: lastErrorAt == const $CopyWithPlaceholder()
          ? _value.lastErrorAt
          // ignore: cast_nullable_to_non_nullable
          : lastErrorAt as DateTime?,
      openAlertCount: openAlertCount == const $CopyWithPlaceholder()
          ? _value.openAlertCount
          // ignore: cast_nullable_to_non_nullable
          : openAlertCount as int?,
      suppressedCount: suppressedCount == const $CopyWithPlaceholder()
          ? _value.suppressedCount
          // ignore: cast_nullable_to_non_nullable
          : suppressedCount as int?,
      lastStormAt: lastStormAt == const $CopyWithPlaceholder()
          ? _value.lastStormAt
          // ignore: cast_nullable_to_non_nullable
          : lastStormAt as DateTime?,
      feedbackCounts: feedbackCounts == const $CopyWithPlaceholder()
          ? _value.feedbackCounts
          // ignore: cast_nullable_to_non_nullable
          : feedbackCounts as TriggerRuleAllOfFeedbackCounts?,
      snoozedUntil: snoozedUntil == const $CopyWithPlaceholder()
          ? _value.snoozedUntil
          // ignore: cast_nullable_to_non_nullable
          : snoozedUntil as DateTime?,
      geofenceRoute: geofenceRoute == const $CopyWithPlaceholder()
          ? _value.geofenceRoute
          // ignore: cast_nullable_to_non_nullable
          : geofenceRoute as Geometry?,
      mutedUntil: mutedUntil == const $CopyWithPlaceholder()
          ? _value.mutedUntil
          // ignore: cast_nullable_to_non_nullable
          : mutedUntil as DateTime?,
      geofenceOrigin: geofenceOrigin == const $CopyWithPlaceholder()
          ? _value.geofenceOrigin
          // ignore: cast_nullable_to_non_nullable
          : geofenceOrigin as TriggerRuleGeofenceOriginEnum?,
      anchorStatus: anchorStatus == const $CopyWithPlaceholder()
          ? _value.anchorStatus
          // ignore: cast_nullable_to_non_nullable
          : anchorStatus as TriggerRuleAnchorStatusEnum?,
      windowStatus: windowStatus == const $CopyWithPlaceholder()
          ? _value.windowStatus
          // ignore: cast_nullable_to_non_nullable
          : windowStatus as WindowStatus?,
    );
  }
}

extension $TriggerRuleCopyWith on TriggerRule {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTriggerRule.copyWith(...)` or `instanceOfTriggerRule.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TriggerRuleCWProxy get copyWith => _$TriggerRuleCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TriggerRule _$TriggerRuleFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('TriggerRule', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['name', 'id', 'createdAt', 'updatedAt'],
  );
  final val = TriggerRule(
    name: $checkedConvert('name', (v) => v as String),
    enabled: $checkedConvert('enabled', (v) => v as bool? ?? true),
    source_: $checkedConvert('source', (v) => v as String?),
    kind: $checkedConvert('kind', (v) => v as String?),
    conditions: $checkedConvert(
      'conditions',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => TriggerRuleInputConditionsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    geofence: $checkedConvert(
      'geofence',
      (v) => v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
    ),
    mode: $checkedConvert(
      'mode',
      (v) => $enumDecodeNullable(_$TriggerRuleModeEnumEnumMap, v),
    ),
    onlyNew: $checkedConvert('onlyNew', (v) => v as bool? ?? false),
    cooldown: $checkedConvert('cooldown', (v) => v as String?),
    messageTemplate: $checkedConvert(
      'messageTemplate',
      (v) => v as String? ?? '{{name}}: {{source}}/{{externalId}}',
    ),
    severity: $checkedConvert(
      'severity',
      (v) => $enumDecodeNullable(_$TriggerRuleSeverityEnumEnumMap, v),
    ),
    enrich: $checkedConvert('enrich', (v) => v as bool? ?? false),
    enrichRadiusKm: $checkedConvert('enrichRadiusKm', (v) => v as num? ?? 100),
    enrichWindow: $checkedConvert('enrichWindow', (v) => v as String? ?? '24h'),
    tags: $checkedConvert(
      'tags',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    sources: $checkedConvert(
      'sources',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    kinds: $checkedConvert(
      'kinds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    bufferMeters: $checkedConvert('bufferMeters', (v) => v as num?),
    hysteresisMeters: $checkedConvert('hysteresisMeters', (v) => v as num?),
    near: $checkedConvert(
      'near',
      (v) => v == null ? null : Near.fromJson(v as Map<String, dynamic>),
    ),
    reappearAsEnter: $checkedConvert('reappearAsEnter', (v) => v as bool?),
    cooldownScope: $checkedConvert(
      'cooldownScope',
      (v) => $enumDecodeNullable(_$TriggerRuleCooldownScopeEnumEnumMap, v),
    ),
    autoResolve: $checkedConvert(
      'autoResolve',
      (v) => v == null
          ? null
          : TriggerRuleInputAutoResolve.fromJson(v as Map<String, dynamic>),
    ),
    maxAlertsPerHour: $checkedConvert(
      'maxAlertsPerHour',
      (v) => (v as num?)?.toInt(),
    ),
    autoDisableOnStorm: $checkedConvert(
      'autoDisableOnStorm',
      (v) => v as bool? ?? false,
    ),
    activeFrom: $checkedConvert(
      'activeFrom',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    activeUntil: $checkedConvert(
      'activeUntil',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    activeWindow: $checkedConvert(
      'activeWindow',
      (v) =>
          v == null ? null : ActiveWindow.fromJson(v as Map<String, dynamic>),
    ),
    forecastHorizon: $checkedConvert('forecastHorizon', (v) => v as String?),
    escalationPolicyId: $checkedConvert(
      'escalationPolicyId',
      (v) => v as String?,
    ),
    geofenceRef: $checkedConvert(
      'geofenceRef',
      (v) => v == null ? null : GeofenceRef.fromJson(v as Map<String, dynamic>),
    ),
    weatherContext: $checkedConvert(
      'weatherContext',
      (v) => $enumDecodeNullable(_$TriggerRuleWeatherContextEnumEnumMap, v),
    ),
    type: $checkedConvert(
      'type',
      (v) => $enumDecodeNullable(_$TriggerRuleTypeEnumEnumMap, v),
    ),
    window: $checkedConvert(
      'window',
      (v) => v == null
          ? null
          : TriggerRuleInputWindow.fromJson(v as Map<String, dynamic>),
    ),
    id: $checkedConvert('id', (v) => v as String),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    updatedAt: $checkedConvert('updatedAt', (v) => DateTime.parse(v as String)),
    lastFiredAt: $checkedConvert(
      'lastFiredAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    alertCount: $checkedConvert('alertCount', (v) => (v as num?)?.toInt()),
    lastError: $checkedConvert('lastError', (v) => v as String?),
    lastErrorAt: $checkedConvert(
      'lastErrorAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    openAlertCount: $checkedConvert(
      'openAlertCount',
      (v) => (v as num?)?.toInt(),
    ),
    suppressedCount: $checkedConvert(
      'suppressedCount',
      (v) => (v as num?)?.toInt(),
    ),
    lastStormAt: $checkedConvert(
      'lastStormAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    feedbackCounts: $checkedConvert(
      'feedbackCounts',
      (v) => v == null
          ? null
          : TriggerRuleAllOfFeedbackCounts.fromJson(v as Map<String, dynamic>),
    ),
    snoozedUntil: $checkedConvert(
      'snoozedUntil',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    geofenceRoute: $checkedConvert(
      'geofenceRoute',
      (v) => v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
    ),
    mutedUntil: $checkedConvert(
      'mutedUntil',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    geofenceOrigin: $checkedConvert(
      'geofenceOrigin',
      (v) => $enumDecodeNullable(_$TriggerRuleGeofenceOriginEnumEnumMap, v),
    ),
    anchorStatus: $checkedConvert(
      'anchorStatus',
      (v) => $enumDecodeNullable(_$TriggerRuleAnchorStatusEnumEnumMap, v),
    ),
    windowStatus: $checkedConvert(
      'windowStatus',
      (v) =>
          v == null ? null : WindowStatus.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
}, fieldKeyMap: const {'source_': 'source'});

Map<String, dynamic> _$TriggerRuleToJson(
  TriggerRule instance,
) => <String, dynamic>{
  'name': instance.name,
  'enabled': ?instance.enabled,
  'source': ?instance.source_,
  'kind': ?instance.kind,
  'conditions': ?instance.conditions?.map((e) => e.toJson()).toList(),
  'geofence': ?instance.geofence?.toJson(),
  'mode': ?_$TriggerRuleModeEnumEnumMap[instance.mode],
  'onlyNew': ?instance.onlyNew,
  'cooldown': ?instance.cooldown,
  'messageTemplate': ?instance.messageTemplate,
  'severity': ?_$TriggerRuleSeverityEnumEnumMap[instance.severity],
  'enrich': ?instance.enrich,
  'enrichRadiusKm': ?instance.enrichRadiusKm,
  'enrichWindow': ?instance.enrichWindow,
  'tags': ?instance.tags,
  'sources': ?instance.sources,
  'kinds': ?instance.kinds,
  'bufferMeters': ?instance.bufferMeters,
  'hysteresisMeters': ?instance.hysteresisMeters,
  'near': ?instance.near?.toJson(),
  'reappearAsEnter': ?instance.reappearAsEnter,
  'cooldownScope':
      ?_$TriggerRuleCooldownScopeEnumEnumMap[instance.cooldownScope],
  'autoResolve': ?instance.autoResolve?.toJson(),
  'maxAlertsPerHour': ?instance.maxAlertsPerHour,
  'autoDisableOnStorm': ?instance.autoDisableOnStorm,
  'activeFrom': ?instance.activeFrom?.toIso8601String(),
  'activeUntil': ?instance.activeUntil?.toIso8601String(),
  'activeWindow': ?instance.activeWindow?.toJson(),
  'forecastHorizon': ?instance.forecastHorizon,
  'escalationPolicyId': ?instance.escalationPolicyId,
  'geofenceRef': ?instance.geofenceRef?.toJson(),
  'weatherContext':
      ?_$TriggerRuleWeatherContextEnumEnumMap[instance.weatherContext],
  'type': ?_$TriggerRuleTypeEnumEnumMap[instance.type],
  'window': ?instance.window?.toJson(),
  'id': instance.id,
  'createdAt': instance.createdAt.toIso8601String(),
  'updatedAt': instance.updatedAt.toIso8601String(),
  'lastFiredAt': ?instance.lastFiredAt?.toIso8601String(),
  'alertCount': ?instance.alertCount,
  'lastError': ?instance.lastError,
  'lastErrorAt': ?instance.lastErrorAt?.toIso8601String(),
  'openAlertCount': ?instance.openAlertCount,
  'suppressedCount': ?instance.suppressedCount,
  'lastStormAt': ?instance.lastStormAt?.toIso8601String(),
  'feedbackCounts': ?instance.feedbackCounts?.toJson(),
  'snoozedUntil': ?instance.snoozedUntil?.toIso8601String(),
  'geofenceRoute': ?instance.geofenceRoute?.toJson(),
  'mutedUntil': ?instance.mutedUntil?.toIso8601String(),
  'geofenceOrigin':
      ?_$TriggerRuleGeofenceOriginEnumEnumMap[instance.geofenceOrigin],
  'anchorStatus': ?_$TriggerRuleAnchorStatusEnumEnumMap[instance.anchorStatus],
  'windowStatus': ?instance.windowStatus?.toJson(),
};

const _$TriggerRuleModeEnumEnumMap = {
  TriggerRuleModeEnum.match: 'match',
  TriggerRuleModeEnum.enter: 'enter',
  TriggerRuleModeEnum.exit: 'exit',
};

const _$TriggerRuleSeverityEnumEnumMap = {
  TriggerRuleSeverityEnum.info: 'info',
  TriggerRuleSeverityEnum.warning: 'warning',
  TriggerRuleSeverityEnum.critical: 'critical',
};

const _$TriggerRuleCooldownScopeEnumEnumMap = {
  TriggerRuleCooldownScopeEnum.record: 'record',
  TriggerRuleCooldownScopeEnum.rule: 'rule',
};

const _$TriggerRuleWeatherContextEnumEnumMap = {
  TriggerRuleWeatherContextEnum.auto: 'auto',
  TriggerRuleWeatherContextEnum.off: 'off',
};

const _$TriggerRuleTypeEnumEnumMap = {
  TriggerRuleTypeEnum.record: 'record',
  TriggerRuleTypeEnum.count: 'count',
  TriggerRuleTypeEnum.absence: 'absence',
  TriggerRuleTypeEnum.anomaly: 'anomaly',
};

const _$TriggerRuleGeofenceOriginEnumEnumMap = {
  TriggerRuleGeofenceOriginEnum.inline: 'inline',
  TriggerRuleGeofenceOriginEnum.near: 'near',
};

const _$TriggerRuleAnchorStatusEnumEnumMap = {
  TriggerRuleAnchorStatusEnum.active: 'active',
  TriggerRuleAnchorStatusEnum.stale: 'stale',
};
