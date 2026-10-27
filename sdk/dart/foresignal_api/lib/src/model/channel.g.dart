// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'channel.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChannelCWProxy {
  Channel id(String id);

  Channel name(String name);

  Channel type(ChannelTypeEnum type);

  Channel config(ChannelInputConfig? config);

  Channel secretFields(List<String>? secretFields);

  Channel events(List<String> events);

  Channel ruleIds(List<String>? ruleIds);

  Channel minSeverity(ChannelMinSeverityEnum? minSeverity);

  Channel titleTemplate(String? titleTemplate);

  Channel bodyTemplate(String? bodyTemplate);

  Channel waitForEnrichmentSeconds(int? waitForEnrichmentSeconds);

  Channel enabled(bool enabled);

  Channel createdAt(DateTime? createdAt);

  Channel updatedAt(DateTime? updatedAt);

  Channel lastDeliveryAt(DateTime? lastDeliveryAt);

  Channel failedLast24h(int? failedLast24h);

  Channel timezone(String? timezone);

  Channel quietHours(QuietHours? quietHours);

  Channel groupWindowSeconds(int? groupWindowSeconds);

  Channel backlogCap(int? backlogCap);

  Channel editInPlace(bool? editInPlace);

  Channel deferWhileActiveSeconds(int? deferWhileActiveSeconds);

  Channel warnings(List<ChannelWarningsInner>? warnings);

  Channel interactive(ChannelInteractiveStatus? interactive);

  Channel acceptExercise(bool? acceptExercise);

  Channel language(String? language);

  Channel ownerPrincipalId(String? ownerPrincipalId);

  Channel global(bool? global);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Channel(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Channel(...).copyWith(id: 12, name: "My name")
  /// ```
  Channel call({
    String id,
    String name,
    ChannelTypeEnum type,
    ChannelInputConfig? config,
    List<String>? secretFields,
    List<String> events,
    List<String>? ruleIds,
    ChannelMinSeverityEnum? minSeverity,
    String? titleTemplate,
    String? bodyTemplate,
    int? waitForEnrichmentSeconds,
    bool enabled,
    DateTime? createdAt,
    DateTime? updatedAt,
    DateTime? lastDeliveryAt,
    int? failedLast24h,
    String? timezone,
    QuietHours? quietHours,
    int? groupWindowSeconds,
    int? backlogCap,
    bool? editInPlace,
    int? deferWhileActiveSeconds,
    List<ChannelWarningsInner>? warnings,
    ChannelInteractiveStatus? interactive,
    bool? acceptExercise,
    String? language,
    String? ownerPrincipalId,
    bool? global,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChannel.copyWith(...)` or call `instanceOfChannel.copyWith.fieldName(value)` for a single field.
class _$ChannelCWProxyImpl implements _$ChannelCWProxy {
  const _$ChannelCWProxyImpl(this._value);

  final Channel _value;

  @override
  Channel id(String id) => call(id: id);

  @override
  Channel name(String name) => call(name: name);

  @override
  Channel type(ChannelTypeEnum type) => call(type: type);

  @override
  Channel config(ChannelInputConfig? config) => call(config: config);

  @override
  Channel secretFields(List<String>? secretFields) =>
      call(secretFields: secretFields);

  @override
  Channel events(List<String> events) => call(events: events);

  @override
  Channel ruleIds(List<String>? ruleIds) => call(ruleIds: ruleIds);

  @override
  Channel minSeverity(ChannelMinSeverityEnum? minSeverity) =>
      call(minSeverity: minSeverity);

  @override
  Channel titleTemplate(String? titleTemplate) =>
      call(titleTemplate: titleTemplate);

  @override
  Channel bodyTemplate(String? bodyTemplate) =>
      call(bodyTemplate: bodyTemplate);

  @override
  Channel waitForEnrichmentSeconds(int? waitForEnrichmentSeconds) =>
      call(waitForEnrichmentSeconds: waitForEnrichmentSeconds);

  @override
  Channel enabled(bool enabled) => call(enabled: enabled);

  @override
  Channel createdAt(DateTime? createdAt) => call(createdAt: createdAt);

  @override
  Channel updatedAt(DateTime? updatedAt) => call(updatedAt: updatedAt);

  @override
  Channel lastDeliveryAt(DateTime? lastDeliveryAt) =>
      call(lastDeliveryAt: lastDeliveryAt);

  @override
  Channel failedLast24h(int? failedLast24h) =>
      call(failedLast24h: failedLast24h);

  @override
  Channel timezone(String? timezone) => call(timezone: timezone);

  @override
  Channel quietHours(QuietHours? quietHours) => call(quietHours: quietHours);

  @override
  Channel groupWindowSeconds(int? groupWindowSeconds) =>
      call(groupWindowSeconds: groupWindowSeconds);

  @override
  Channel backlogCap(int? backlogCap) => call(backlogCap: backlogCap);

  @override
  Channel editInPlace(bool? editInPlace) => call(editInPlace: editInPlace);

  @override
  Channel deferWhileActiveSeconds(int? deferWhileActiveSeconds) =>
      call(deferWhileActiveSeconds: deferWhileActiveSeconds);

  @override
  Channel warnings(List<ChannelWarningsInner>? warnings) =>
      call(warnings: warnings);

  @override
  Channel interactive(ChannelInteractiveStatus? interactive) =>
      call(interactive: interactive);

  @override
  Channel acceptExercise(bool? acceptExercise) =>
      call(acceptExercise: acceptExercise);

  @override
  Channel language(String? language) => call(language: language);

  @override
  Channel ownerPrincipalId(String? ownerPrincipalId) =>
      call(ownerPrincipalId: ownerPrincipalId);

  @override
  Channel global(bool? global) => call(global: global);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Channel(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Channel(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Channel call({
    Object? id = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? config = const $CopyWithPlaceholder(),
    Object? secretFields = const $CopyWithPlaceholder(),
    Object? events = const $CopyWithPlaceholder(),
    Object? ruleIds = const $CopyWithPlaceholder(),
    Object? minSeverity = const $CopyWithPlaceholder(),
    Object? titleTemplate = const $CopyWithPlaceholder(),
    Object? bodyTemplate = const $CopyWithPlaceholder(),
    Object? waitForEnrichmentSeconds = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
    Object? lastDeliveryAt = const $CopyWithPlaceholder(),
    Object? failedLast24h = const $CopyWithPlaceholder(),
    Object? timezone = const $CopyWithPlaceholder(),
    Object? quietHours = const $CopyWithPlaceholder(),
    Object? groupWindowSeconds = const $CopyWithPlaceholder(),
    Object? backlogCap = const $CopyWithPlaceholder(),
    Object? editInPlace = const $CopyWithPlaceholder(),
    Object? deferWhileActiveSeconds = const $CopyWithPlaceholder(),
    Object? warnings = const $CopyWithPlaceholder(),
    Object? interactive = const $CopyWithPlaceholder(),
    Object? acceptExercise = const $CopyWithPlaceholder(),
    Object? language = const $CopyWithPlaceholder(),
    Object? ownerPrincipalId = const $CopyWithPlaceholder(),
    Object? global = const $CopyWithPlaceholder(),
  }) {
    return Channel(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ChannelTypeEnum,
      config: config == const $CopyWithPlaceholder()
          ? _value.config
          // ignore: cast_nullable_to_non_nullable
          : config as ChannelInputConfig?,
      secretFields: secretFields == const $CopyWithPlaceholder()
          ? _value.secretFields
          // ignore: cast_nullable_to_non_nullable
          : secretFields as List<String>?,
      events: events == const $CopyWithPlaceholder() || events == null
          ? _value.events
          // ignore: cast_nullable_to_non_nullable
          : events as List<String>,
      ruleIds: ruleIds == const $CopyWithPlaceholder()
          ? _value.ruleIds
          // ignore: cast_nullable_to_non_nullable
          : ruleIds as List<String>?,
      minSeverity: minSeverity == const $CopyWithPlaceholder()
          ? _value.minSeverity
          // ignore: cast_nullable_to_non_nullable
          : minSeverity as ChannelMinSeverityEnum?,
      titleTemplate: titleTemplate == const $CopyWithPlaceholder()
          ? _value.titleTemplate
          // ignore: cast_nullable_to_non_nullable
          : titleTemplate as String?,
      bodyTemplate: bodyTemplate == const $CopyWithPlaceholder()
          ? _value.bodyTemplate
          // ignore: cast_nullable_to_non_nullable
          : bodyTemplate as String?,
      waitForEnrichmentSeconds:
          waitForEnrichmentSeconds == const $CopyWithPlaceholder()
          ? _value.waitForEnrichmentSeconds
          // ignore: cast_nullable_to_non_nullable
          : waitForEnrichmentSeconds as int?,
      enabled: enabled == const $CopyWithPlaceholder() || enabled == null
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool,
      createdAt: createdAt == const $CopyWithPlaceholder()
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime?,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
      lastDeliveryAt: lastDeliveryAt == const $CopyWithPlaceholder()
          ? _value.lastDeliveryAt
          // ignore: cast_nullable_to_non_nullable
          : lastDeliveryAt as DateTime?,
      failedLast24h: failedLast24h == const $CopyWithPlaceholder()
          ? _value.failedLast24h
          // ignore: cast_nullable_to_non_nullable
          : failedLast24h as int?,
      timezone: timezone == const $CopyWithPlaceholder()
          ? _value.timezone
          // ignore: cast_nullable_to_non_nullable
          : timezone as String?,
      quietHours: quietHours == const $CopyWithPlaceholder()
          ? _value.quietHours
          // ignore: cast_nullable_to_non_nullable
          : quietHours as QuietHours?,
      groupWindowSeconds: groupWindowSeconds == const $CopyWithPlaceholder()
          ? _value.groupWindowSeconds
          // ignore: cast_nullable_to_non_nullable
          : groupWindowSeconds as int?,
      backlogCap: backlogCap == const $CopyWithPlaceholder()
          ? _value.backlogCap
          // ignore: cast_nullable_to_non_nullable
          : backlogCap as int?,
      editInPlace: editInPlace == const $CopyWithPlaceholder()
          ? _value.editInPlace
          // ignore: cast_nullable_to_non_nullable
          : editInPlace as bool?,
      deferWhileActiveSeconds:
          deferWhileActiveSeconds == const $CopyWithPlaceholder()
          ? _value.deferWhileActiveSeconds
          // ignore: cast_nullable_to_non_nullable
          : deferWhileActiveSeconds as int?,
      warnings: warnings == const $CopyWithPlaceholder()
          ? _value.warnings
          // ignore: cast_nullable_to_non_nullable
          : warnings as List<ChannelWarningsInner>?,
      interactive: interactive == const $CopyWithPlaceholder()
          ? _value.interactive
          // ignore: cast_nullable_to_non_nullable
          : interactive as ChannelInteractiveStatus?,
      acceptExercise: acceptExercise == const $CopyWithPlaceholder()
          ? _value.acceptExercise
          // ignore: cast_nullable_to_non_nullable
          : acceptExercise as bool?,
      language: language == const $CopyWithPlaceholder()
          ? _value.language
          // ignore: cast_nullable_to_non_nullable
          : language as String?,
      ownerPrincipalId: ownerPrincipalId == const $CopyWithPlaceholder()
          ? _value.ownerPrincipalId
          // ignore: cast_nullable_to_non_nullable
          : ownerPrincipalId as String?,
      global: global == const $CopyWithPlaceholder()
          ? _value.global
          // ignore: cast_nullable_to_non_nullable
          : global as bool?,
    );
  }
}

extension $ChannelCopyWith on Channel {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChannel.copyWith(...)` or `instanceOfChannel.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChannelCWProxy get copyWith => _$ChannelCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Channel _$ChannelFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('Channel', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['id', 'name', 'type', 'events', 'enabled'],
  );
  final val = Channel(
    id: $checkedConvert('id', (v) => v as String),
    name: $checkedConvert('name', (v) => v as String),
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$ChannelTypeEnumEnumMap, v),
    ),
    config: $checkedConvert(
      'config',
      (v) => v == null
          ? null
          : ChannelInputConfig.fromJson(v as Map<String, dynamic>),
    ),
    secretFields: $checkedConvert(
      'secretFields',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    events: $checkedConvert(
      'events',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
    ruleIds: $checkedConvert(
      'ruleIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    minSeverity: $checkedConvert(
      'minSeverity',
      (v) => $enumDecodeNullable(_$ChannelMinSeverityEnumEnumMap, v),
    ),
    titleTemplate: $checkedConvert('titleTemplate', (v) => v as String?),
    bodyTemplate: $checkedConvert('bodyTemplate', (v) => v as String?),
    waitForEnrichmentSeconds: $checkedConvert(
      'waitForEnrichmentSeconds',
      (v) => (v as num?)?.toInt(),
    ),
    enabled: $checkedConvert('enabled', (v) => v as bool),
    createdAt: $checkedConvert(
      'createdAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    updatedAt: $checkedConvert(
      'updatedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    lastDeliveryAt: $checkedConvert(
      'lastDeliveryAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    failedLast24h: $checkedConvert(
      'failedLast24h',
      (v) => (v as num?)?.toInt(),
    ),
    timezone: $checkedConvert('timezone', (v) => v as String?),
    quietHours: $checkedConvert(
      'quietHours',
      (v) => v == null ? null : QuietHours.fromJson(v as Map<String, dynamic>),
    ),
    groupWindowSeconds: $checkedConvert(
      'groupWindowSeconds',
      (v) => (v as num?)?.toInt() ?? 0,
    ),
    backlogCap: $checkedConvert(
      'backlogCap',
      (v) => (v as num?)?.toInt() ?? 20,
    ),
    editInPlace: $checkedConvert('editInPlace', (v) => v as bool? ?? true),
    deferWhileActiveSeconds: $checkedConvert(
      'deferWhileActiveSeconds',
      (v) => (v as num?)?.toInt() ?? 0,
    ),
    warnings: $checkedConvert(
      'warnings',
      (v) => (v as List<dynamic>?)
          ?.map((e) => ChannelWarningsInner.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    interactive: $checkedConvert(
      'interactive',
      (v) => v == null
          ? null
          : ChannelInteractiveStatus.fromJson(v as Map<String, dynamic>),
    ),
    acceptExercise: $checkedConvert(
      'acceptExercise',
      (v) => v as bool? ?? false,
    ),
    language: $checkedConvert('language', (v) => v as String?),
    ownerPrincipalId: $checkedConvert('ownerPrincipalId', (v) => v as String?),
    global: $checkedConvert('global', (v) => v as bool?),
  );
  return val;
});

Map<String, dynamic> _$ChannelToJson(Channel instance) => <String, dynamic>{
  'id': instance.id,
  'name': instance.name,
  'type': _$ChannelTypeEnumEnumMap[instance.type]!,
  'config': ?instance.config?.toJson(),
  'secretFields': ?instance.secretFields,
  'events': instance.events,
  'ruleIds': ?instance.ruleIds,
  'minSeverity': ?_$ChannelMinSeverityEnumEnumMap[instance.minSeverity],
  'titleTemplate': ?instance.titleTemplate,
  'bodyTemplate': ?instance.bodyTemplate,
  'waitForEnrichmentSeconds': ?instance.waitForEnrichmentSeconds,
  'enabled': instance.enabled,
  'createdAt': ?instance.createdAt?.toIso8601String(),
  'updatedAt': ?instance.updatedAt?.toIso8601String(),
  'lastDeliveryAt': ?instance.lastDeliveryAt?.toIso8601String(),
  'failedLast24h': ?instance.failedLast24h,
  'timezone': ?instance.timezone,
  'quietHours': ?instance.quietHours?.toJson(),
  'groupWindowSeconds': ?instance.groupWindowSeconds,
  'backlogCap': ?instance.backlogCap,
  'editInPlace': ?instance.editInPlace,
  'deferWhileActiveSeconds': ?instance.deferWhileActiveSeconds,
  'warnings': ?instance.warnings?.map((e) => e.toJson()).toList(),
  'interactive': ?instance.interactive?.toJson(),
  'acceptExercise': ?instance.acceptExercise,
  'language': ?instance.language,
  'ownerPrincipalId': ?instance.ownerPrincipalId,
  'global': ?instance.global,
};

const _$ChannelTypeEnumEnumMap = {
  ChannelTypeEnum.webhook: 'webhook',
  ChannelTypeEnum.slack: 'slack',
  ChannelTypeEnum.mattermost: 'mattermost',
  ChannelTypeEnum.discord: 'discord',
  ChannelTypeEnum.teams: 'teams',
  ChannelTypeEnum.googleChat: 'google-chat',
  ChannelTypeEnum.telegram: 'telegram',
  ChannelTypeEnum.matrix: 'matrix',
  ChannelTypeEnum.ntfy: 'ntfy',
  ChannelTypeEnum.gotify: 'gotify',
  ChannelTypeEnum.pushover: 'pushover',
  ChannelTypeEnum.email: 'email',
  ChannelTypeEnum.webpush: 'webpush',
  ChannelTypeEnum.homeassistant: 'homeassistant',
  ChannelTypeEnum.apprise: 'apprise',
  ChannelTypeEnum.zulip: 'zulip',
  ChannelTypeEnum.mqtt: 'mqtt',
  ChannelTypeEnum.meshtastic: 'meshtastic',
  ChannelTypeEnum.meshcore: 'meshcore',
};

const _$ChannelMinSeverityEnumEnumMap = {
  ChannelMinSeverityEnum.info: 'info',
  ChannelMinSeverityEnum.warning: 'warning',
  ChannelMinSeverityEnum.critical: 'critical',
};
