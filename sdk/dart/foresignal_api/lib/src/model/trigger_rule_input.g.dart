// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'trigger_rule_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TriggerRuleInputCWProxy {
  TriggerRuleInput name(String name);

  TriggerRuleInput enabled(bool? enabled);

  TriggerRuleInput source_(
    @Deprecated('source_ has been deprecated') String? source_,
  );

  TriggerRuleInput kind(@Deprecated('kind has been deprecated') String? kind);

  TriggerRuleInput conditions(
    List<TriggerRuleInputConditionsInner>? conditions,
  );

  TriggerRuleInput geofence(Geometry? geofence);

  TriggerRuleInput mode(TriggerRuleInputModeEnum? mode);

  TriggerRuleInput onlyNew(bool? onlyNew);

  TriggerRuleInput cooldown(String? cooldown);

  TriggerRuleInput messageTemplate(String? messageTemplate);

  TriggerRuleInput severity(TriggerRuleInputSeverityEnum? severity);

  TriggerRuleInput enrich(bool? enrich);

  TriggerRuleInput enrichRadiusKm(num? enrichRadiusKm);

  TriggerRuleInput enrichWindow(String? enrichWindow);

  TriggerRuleInput tags(List<String>? tags);

  TriggerRuleInput sources(List<String>? sources);

  TriggerRuleInput kinds(List<String>? kinds);

  TriggerRuleInput bufferMeters(num? bufferMeters);

  TriggerRuleInput hysteresisMeters(num? hysteresisMeters);

  TriggerRuleInput near(Near? near);

  TriggerRuleInput reappearAsEnter(bool? reappearAsEnter);

  TriggerRuleInput cooldownScope(
    TriggerRuleInputCooldownScopeEnum? cooldownScope,
  );

  TriggerRuleInput autoResolve(TriggerRuleInputAutoResolve? autoResolve);

  TriggerRuleInput maxAlertsPerHour(int? maxAlertsPerHour);

  TriggerRuleInput autoDisableOnStorm(bool? autoDisableOnStorm);

  TriggerRuleInput activeFrom(DateTime? activeFrom);

  TriggerRuleInput activeUntil(DateTime? activeUntil);

  TriggerRuleInput activeWindow(ActiveWindow? activeWindow);

  TriggerRuleInput forecastHorizon(String? forecastHorizon);

  TriggerRuleInput escalationPolicyId(String? escalationPolicyId);

  TriggerRuleInput geofenceRef(GeofenceRef? geofenceRef);

  TriggerRuleInput weatherContext(
    TriggerRuleInputWeatherContextEnum? weatherContext,
  );

  TriggerRuleInput type(TriggerRuleInputTypeEnum? type);

  TriggerRuleInput window(TriggerRuleInputWindow? window);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TriggerRuleInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TriggerRuleInput(...).copyWith(id: 12, name: "My name")
  /// ```
  TriggerRuleInput call({
    String name,
    bool? enabled,
    @Deprecated('source_ has been deprecated') String? source_,
    @Deprecated('kind has been deprecated') String? kind,
    List<TriggerRuleInputConditionsInner>? conditions,
    Geometry? geofence,
    TriggerRuleInputModeEnum? mode,
    bool? onlyNew,
    String? cooldown,
    String? messageTemplate,
    TriggerRuleInputSeverityEnum? severity,
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
    TriggerRuleInputCooldownScopeEnum? cooldownScope,
    TriggerRuleInputAutoResolve? autoResolve,
    int? maxAlertsPerHour,
    bool? autoDisableOnStorm,
    DateTime? activeFrom,
    DateTime? activeUntil,
    ActiveWindow? activeWindow,
    String? forecastHorizon,
    String? escalationPolicyId,
    GeofenceRef? geofenceRef,
    TriggerRuleInputWeatherContextEnum? weatherContext,
    TriggerRuleInputTypeEnum? type,
    TriggerRuleInputWindow? window,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTriggerRuleInput.copyWith(...)` or call `instanceOfTriggerRuleInput.copyWith.fieldName(value)` for a single field.
class _$TriggerRuleInputCWProxyImpl implements _$TriggerRuleInputCWProxy {
  const _$TriggerRuleInputCWProxyImpl(this._value);

  final TriggerRuleInput _value;

  @override
  TriggerRuleInput name(String name) => call(name: name);

  @override
  TriggerRuleInput enabled(bool? enabled) => call(enabled: enabled);

  @override
  TriggerRuleInput source_(
    @Deprecated('source_ has been deprecated') String? source_,
  ) => call(source_: source_);

  @override
  TriggerRuleInput kind(@Deprecated('kind has been deprecated') String? kind) =>
      call(kind: kind);

  @override
  TriggerRuleInput conditions(
    List<TriggerRuleInputConditionsInner>? conditions,
  ) => call(conditions: conditions);

  @override
  TriggerRuleInput geofence(Geometry? geofence) => call(geofence: geofence);

  @override
  TriggerRuleInput mode(TriggerRuleInputModeEnum? mode) => call(mode: mode);

  @override
  TriggerRuleInput onlyNew(bool? onlyNew) => call(onlyNew: onlyNew);

  @override
  TriggerRuleInput cooldown(String? cooldown) => call(cooldown: cooldown);

  @override
  TriggerRuleInput messageTemplate(String? messageTemplate) =>
      call(messageTemplate: messageTemplate);

  @override
  TriggerRuleInput severity(TriggerRuleInputSeverityEnum? severity) =>
      call(severity: severity);

  @override
  TriggerRuleInput enrich(bool? enrich) => call(enrich: enrich);

  @override
  TriggerRuleInput enrichRadiusKm(num? enrichRadiusKm) =>
      call(enrichRadiusKm: enrichRadiusKm);

  @override
  TriggerRuleInput enrichWindow(String? enrichWindow) =>
      call(enrichWindow: enrichWindow);

  @override
  TriggerRuleInput tags(List<String>? tags) => call(tags: tags);

  @override
  TriggerRuleInput sources(List<String>? sources) => call(sources: sources);

  @override
  TriggerRuleInput kinds(List<String>? kinds) => call(kinds: kinds);

  @override
  TriggerRuleInput bufferMeters(num? bufferMeters) =>
      call(bufferMeters: bufferMeters);

  @override
  TriggerRuleInput hysteresisMeters(num? hysteresisMeters) =>
      call(hysteresisMeters: hysteresisMeters);

  @override
  TriggerRuleInput near(Near? near) => call(near: near);

  @override
  TriggerRuleInput reappearAsEnter(bool? reappearAsEnter) =>
      call(reappearAsEnter: reappearAsEnter);

  @override
  TriggerRuleInput cooldownScope(
    TriggerRuleInputCooldownScopeEnum? cooldownScope,
  ) => call(cooldownScope: cooldownScope);

  @override
  TriggerRuleInput autoResolve(TriggerRuleInputAutoResolve? autoResolve) =>
      call(autoResolve: autoResolve);

  @override
  TriggerRuleInput maxAlertsPerHour(int? maxAlertsPerHour) =>
      call(maxAlertsPerHour: maxAlertsPerHour);

  @override
  TriggerRuleInput autoDisableOnStorm(bool? autoDisableOnStorm) =>
      call(autoDisableOnStorm: autoDisableOnStorm);

  @override
  TriggerRuleInput activeFrom(DateTime? activeFrom) =>
      call(activeFrom: activeFrom);

  @override
  TriggerRuleInput activeUntil(DateTime? activeUntil) =>
      call(activeUntil: activeUntil);

  @override
  TriggerRuleInput activeWindow(ActiveWindow? activeWindow) =>
      call(activeWindow: activeWindow);

  @override
  TriggerRuleInput forecastHorizon(String? forecastHorizon) =>
      call(forecastHorizon: forecastHorizon);

  @override
  TriggerRuleInput escalationPolicyId(String? escalationPolicyId) =>
      call(escalationPolicyId: escalationPolicyId);

  @override
  TriggerRuleInput geofenceRef(GeofenceRef? geofenceRef) =>
      call(geofenceRef: geofenceRef);

  @override
  TriggerRuleInput weatherContext(
    TriggerRuleInputWeatherContextEnum? weatherContext,
  ) => call(weatherContext: weatherContext);

  @override
  TriggerRuleInput type(TriggerRuleInputTypeEnum? type) => call(type: type);

  @override
  TriggerRuleInput window(TriggerRuleInputWindow? window) =>
      call(window: window);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TriggerRuleInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TriggerRuleInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TriggerRuleInput call({
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
  }) {
    return TriggerRuleInput(
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
          : mode as TriggerRuleInputModeEnum?,
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
          : severity as TriggerRuleInputSeverityEnum?,
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
          : cooldownScope as TriggerRuleInputCooldownScopeEnum?,
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
          : weatherContext as TriggerRuleInputWeatherContextEnum?,
      type: type == const $CopyWithPlaceholder()
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as TriggerRuleInputTypeEnum?,
      window: window == const $CopyWithPlaceholder()
          ? _value.window
          // ignore: cast_nullable_to_non_nullable
          : window as TriggerRuleInputWindow?,
    );
  }
}

extension $TriggerRuleInputCopyWith on TriggerRuleInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTriggerRuleInput.copyWith(...)` or `instanceOfTriggerRuleInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TriggerRuleInputCWProxy get copyWith => _$TriggerRuleInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TriggerRuleInput _$TriggerRuleInputFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('TriggerRuleInput', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['name']);
  final val = TriggerRuleInput(
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
      (v) => $enumDecodeNullable(_$TriggerRuleInputModeEnumEnumMap, v),
    ),
    onlyNew: $checkedConvert('onlyNew', (v) => v as bool? ?? false),
    cooldown: $checkedConvert('cooldown', (v) => v as String?),
    messageTemplate: $checkedConvert(
      'messageTemplate',
      (v) => v as String? ?? '{{name}}: {{source}}/{{externalId}}',
    ),
    severity: $checkedConvert(
      'severity',
      (v) => $enumDecodeNullable(_$TriggerRuleInputSeverityEnumEnumMap, v),
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
      (v) => $enumDecodeNullable(_$TriggerRuleInputCooldownScopeEnumEnumMap, v),
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
      (v) =>
          $enumDecodeNullable(_$TriggerRuleInputWeatherContextEnumEnumMap, v),
    ),
    type: $checkedConvert(
      'type',
      (v) => $enumDecodeNullable(_$TriggerRuleInputTypeEnumEnumMap, v),
    ),
    window: $checkedConvert(
      'window',
      (v) => v == null
          ? null
          : TriggerRuleInputWindow.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
}, fieldKeyMap: const {'source_': 'source'});

Map<String, dynamic> _$TriggerRuleInputToJson(TriggerRuleInput instance) =>
    <String, dynamic>{
      'name': instance.name,
      'enabled': ?instance.enabled,
      'source': ?instance.source_,
      'kind': ?instance.kind,
      'conditions': ?instance.conditions?.map((e) => e.toJson()).toList(),
      'geofence': ?instance.geofence?.toJson(),
      'mode': ?_$TriggerRuleInputModeEnumEnumMap[instance.mode],
      'onlyNew': ?instance.onlyNew,
      'cooldown': ?instance.cooldown,
      'messageTemplate': ?instance.messageTemplate,
      'severity': ?_$TriggerRuleInputSeverityEnumEnumMap[instance.severity],
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
          ?_$TriggerRuleInputCooldownScopeEnumEnumMap[instance.cooldownScope],
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
          ?_$TriggerRuleInputWeatherContextEnumEnumMap[instance.weatherContext],
      'type': ?_$TriggerRuleInputTypeEnumEnumMap[instance.type],
      'window': ?instance.window?.toJson(),
    };

const _$TriggerRuleInputModeEnumEnumMap = {
  TriggerRuleInputModeEnum.match: 'match',
  TriggerRuleInputModeEnum.enter: 'enter',
  TriggerRuleInputModeEnum.exit: 'exit',
};

const _$TriggerRuleInputSeverityEnumEnumMap = {
  TriggerRuleInputSeverityEnum.info: 'info',
  TriggerRuleInputSeverityEnum.warning: 'warning',
  TriggerRuleInputSeverityEnum.critical: 'critical',
};

const _$TriggerRuleInputCooldownScopeEnumEnumMap = {
  TriggerRuleInputCooldownScopeEnum.record: 'record',
  TriggerRuleInputCooldownScopeEnum.rule: 'rule',
};

const _$TriggerRuleInputWeatherContextEnumEnumMap = {
  TriggerRuleInputWeatherContextEnum.auto: 'auto',
  TriggerRuleInputWeatherContextEnum.off: 'off',
};

const _$TriggerRuleInputTypeEnumEnumMap = {
  TriggerRuleInputTypeEnum.record: 'record',
  TriggerRuleInputTypeEnum.count: 'count',
  TriggerRuleInputTypeEnum.absence: 'absence',
  TriggerRuleInputTypeEnum.anomaly: 'anomaly',
};
