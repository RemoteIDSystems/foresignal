// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'settings_bundle_channels_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SettingsBundleChannelsInnerCWProxy {
  SettingsBundleChannelsInner name(String name);

  SettingsBundleChannelsInner type(SettingsBundleChannelsInnerTypeEnum type);

  SettingsBundleChannelsInner config(ChannelInputConfig? config);

  SettingsBundleChannelsInner secrets(Map<String, String>? secrets);

  SettingsBundleChannelsInner events(
    List<SettingsBundleChannelsInnerEventsEnum>? events,
  );

  SettingsBundleChannelsInner ruleIds(List<String>? ruleIds);

  SettingsBundleChannelsInner minSeverity(
    SettingsBundleChannelsInnerMinSeverityEnum? minSeverity,
  );

  SettingsBundleChannelsInner titleTemplate(String? titleTemplate);

  SettingsBundleChannelsInner bodyTemplate(String? bodyTemplate);

  SettingsBundleChannelsInner waitForEnrichmentSeconds(
    int? waitForEnrichmentSeconds,
  );

  SettingsBundleChannelsInner enabled(bool? enabled);

  SettingsBundleChannelsInner timezone(String? timezone);

  SettingsBundleChannelsInner quietHours(QuietHours? quietHours);

  SettingsBundleChannelsInner groupWindowSeconds(int? groupWindowSeconds);

  SettingsBundleChannelsInner backlogCap(int? backlogCap);

  SettingsBundleChannelsInner editInPlace(bool? editInPlace);

  SettingsBundleChannelsInner deferWhileActiveSeconds(
    int? deferWhileActiveSeconds,
  );

  SettingsBundleChannelsInner acceptExercise(bool? acceptExercise);

  SettingsBundleChannelsInner language(String? language);

  SettingsBundleChannelsInner id(String id);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SettingsBundleChannelsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SettingsBundleChannelsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  SettingsBundleChannelsInner call({
    String name,
    SettingsBundleChannelsInnerTypeEnum type,
    ChannelInputConfig? config,
    Map<String, String>? secrets,
    List<SettingsBundleChannelsInnerEventsEnum>? events,
    List<String>? ruleIds,
    SettingsBundleChannelsInnerMinSeverityEnum? minSeverity,
    String? titleTemplate,
    String? bodyTemplate,
    int? waitForEnrichmentSeconds,
    bool? enabled,
    String? timezone,
    QuietHours? quietHours,
    int? groupWindowSeconds,
    int? backlogCap,
    bool? editInPlace,
    int? deferWhileActiveSeconds,
    bool? acceptExercise,
    String? language,
    String id,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSettingsBundleChannelsInner.copyWith(...)` or call `instanceOfSettingsBundleChannelsInner.copyWith.fieldName(value)` for a single field.
class _$SettingsBundleChannelsInnerCWProxyImpl
    implements _$SettingsBundleChannelsInnerCWProxy {
  const _$SettingsBundleChannelsInnerCWProxyImpl(this._value);

  final SettingsBundleChannelsInner _value;

  @override
  SettingsBundleChannelsInner name(String name) => call(name: name);

  @override
  SettingsBundleChannelsInner type(SettingsBundleChannelsInnerTypeEnum type) =>
      call(type: type);

  @override
  SettingsBundleChannelsInner config(ChannelInputConfig? config) =>
      call(config: config);

  @override
  SettingsBundleChannelsInner secrets(Map<String, String>? secrets) =>
      call(secrets: secrets);

  @override
  SettingsBundleChannelsInner events(
    List<SettingsBundleChannelsInnerEventsEnum>? events,
  ) => call(events: events);

  @override
  SettingsBundleChannelsInner ruleIds(List<String>? ruleIds) =>
      call(ruleIds: ruleIds);

  @override
  SettingsBundleChannelsInner minSeverity(
    SettingsBundleChannelsInnerMinSeverityEnum? minSeverity,
  ) => call(minSeverity: minSeverity);

  @override
  SettingsBundleChannelsInner titleTemplate(String? titleTemplate) =>
      call(titleTemplate: titleTemplate);

  @override
  SettingsBundleChannelsInner bodyTemplate(String? bodyTemplate) =>
      call(bodyTemplate: bodyTemplate);

  @override
  SettingsBundleChannelsInner waitForEnrichmentSeconds(
    int? waitForEnrichmentSeconds,
  ) => call(waitForEnrichmentSeconds: waitForEnrichmentSeconds);

  @override
  SettingsBundleChannelsInner enabled(bool? enabled) => call(enabled: enabled);

  @override
  SettingsBundleChannelsInner timezone(String? timezone) =>
      call(timezone: timezone);

  @override
  SettingsBundleChannelsInner quietHours(QuietHours? quietHours) =>
      call(quietHours: quietHours);

  @override
  SettingsBundleChannelsInner groupWindowSeconds(int? groupWindowSeconds) =>
      call(groupWindowSeconds: groupWindowSeconds);

  @override
  SettingsBundleChannelsInner backlogCap(int? backlogCap) =>
      call(backlogCap: backlogCap);

  @override
  SettingsBundleChannelsInner editInPlace(bool? editInPlace) =>
      call(editInPlace: editInPlace);

  @override
  SettingsBundleChannelsInner deferWhileActiveSeconds(
    int? deferWhileActiveSeconds,
  ) => call(deferWhileActiveSeconds: deferWhileActiveSeconds);

  @override
  SettingsBundleChannelsInner acceptExercise(bool? acceptExercise) =>
      call(acceptExercise: acceptExercise);

  @override
  SettingsBundleChannelsInner language(String? language) =>
      call(language: language);

  @override
  SettingsBundleChannelsInner id(String id) => call(id: id);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SettingsBundleChannelsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SettingsBundleChannelsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SettingsBundleChannelsInner call({
    Object? name = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? config = const $CopyWithPlaceholder(),
    Object? secrets = const $CopyWithPlaceholder(),
    Object? events = const $CopyWithPlaceholder(),
    Object? ruleIds = const $CopyWithPlaceholder(),
    Object? minSeverity = const $CopyWithPlaceholder(),
    Object? titleTemplate = const $CopyWithPlaceholder(),
    Object? bodyTemplate = const $CopyWithPlaceholder(),
    Object? waitForEnrichmentSeconds = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    Object? timezone = const $CopyWithPlaceholder(),
    Object? quietHours = const $CopyWithPlaceholder(),
    Object? groupWindowSeconds = const $CopyWithPlaceholder(),
    Object? backlogCap = const $CopyWithPlaceholder(),
    Object? editInPlace = const $CopyWithPlaceholder(),
    Object? deferWhileActiveSeconds = const $CopyWithPlaceholder(),
    Object? acceptExercise = const $CopyWithPlaceholder(),
    Object? language = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
  }) {
    return SettingsBundleChannelsInner(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as SettingsBundleChannelsInnerTypeEnum,
      config: config == const $CopyWithPlaceholder()
          ? _value.config
          // ignore: cast_nullable_to_non_nullable
          : config as ChannelInputConfig?,
      secrets: secrets == const $CopyWithPlaceholder()
          ? _value.secrets
          // ignore: cast_nullable_to_non_nullable
          : secrets as Map<String, String>?,
      events: events == const $CopyWithPlaceholder()
          ? _value.events
          // ignore: cast_nullable_to_non_nullable
          : events as List<SettingsBundleChannelsInnerEventsEnum>?,
      ruleIds: ruleIds == const $CopyWithPlaceholder()
          ? _value.ruleIds
          // ignore: cast_nullable_to_non_nullable
          : ruleIds as List<String>?,
      minSeverity: minSeverity == const $CopyWithPlaceholder()
          ? _value.minSeverity
          // ignore: cast_nullable_to_non_nullable
          : minSeverity as SettingsBundleChannelsInnerMinSeverityEnum?,
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
      enabled: enabled == const $CopyWithPlaceholder()
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool?,
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
      acceptExercise: acceptExercise == const $CopyWithPlaceholder()
          ? _value.acceptExercise
          // ignore: cast_nullable_to_non_nullable
          : acceptExercise as bool?,
      language: language == const $CopyWithPlaceholder()
          ? _value.language
          // ignore: cast_nullable_to_non_nullable
          : language as String?,
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
    );
  }
}

extension $SettingsBundleChannelsInnerCopyWith on SettingsBundleChannelsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSettingsBundleChannelsInner.copyWith(...)` or `instanceOfSettingsBundleChannelsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SettingsBundleChannelsInnerCWProxy get copyWith =>
      _$SettingsBundleChannelsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SettingsBundleChannelsInner _$SettingsBundleChannelsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SettingsBundleChannelsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['name', 'type', 'id']);
  final val = SettingsBundleChannelsInner(
    name: $checkedConvert('name', (v) => v as String),
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$SettingsBundleChannelsInnerTypeEnumEnumMap, v),
    ),
    config: $checkedConvert(
      'config',
      (v) => v == null
          ? null
          : ChannelInputConfig.fromJson(v as Map<String, dynamic>),
    ),
    secrets: $checkedConvert(
      'secrets',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as String)),
    ),
    events: $checkedConvert(
      'events',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) =>
                $enumDecode(_$SettingsBundleChannelsInnerEventsEnumEnumMap, e),
          )
          .toList(),
    ),
    ruleIds: $checkedConvert(
      'ruleIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    minSeverity: $checkedConvert(
      'minSeverity',
      (v) => $enumDecodeNullable(
        _$SettingsBundleChannelsInnerMinSeverityEnumEnumMap,
        v,
      ),
    ),
    titleTemplate: $checkedConvert('titleTemplate', (v) => v as String?),
    bodyTemplate: $checkedConvert('bodyTemplate', (v) => v as String?),
    waitForEnrichmentSeconds: $checkedConvert(
      'waitForEnrichmentSeconds',
      (v) => (v as num?)?.toInt() ?? 0,
    ),
    enabled: $checkedConvert('enabled', (v) => v as bool? ?? true),
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
    acceptExercise: $checkedConvert(
      'acceptExercise',
      (v) => v as bool? ?? false,
    ),
    language: $checkedConvert('language', (v) => v as String?),
    id: $checkedConvert('id', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$SettingsBundleChannelsInnerToJson(
  SettingsBundleChannelsInner instance,
) => <String, dynamic>{
  'name': instance.name,
  'type': _$SettingsBundleChannelsInnerTypeEnumEnumMap[instance.type]!,
  'config': ?instance.config?.toJson(),
  'secrets': ?instance.secrets,
  'events': ?instance.events
      ?.map((e) => _$SettingsBundleChannelsInnerEventsEnumEnumMap[e]!)
      .toList(),
  'ruleIds': ?instance.ruleIds,
  'minSeverity':
      ?_$SettingsBundleChannelsInnerMinSeverityEnumEnumMap[instance
          .minSeverity],
  'titleTemplate': ?instance.titleTemplate,
  'bodyTemplate': ?instance.bodyTemplate,
  'waitForEnrichmentSeconds': ?instance.waitForEnrichmentSeconds,
  'enabled': ?instance.enabled,
  'timezone': ?instance.timezone,
  'quietHours': ?instance.quietHours?.toJson(),
  'groupWindowSeconds': ?instance.groupWindowSeconds,
  'backlogCap': ?instance.backlogCap,
  'editInPlace': ?instance.editInPlace,
  'deferWhileActiveSeconds': ?instance.deferWhileActiveSeconds,
  'acceptExercise': ?instance.acceptExercise,
  'language': ?instance.language,
  'id': instance.id,
};

const _$SettingsBundleChannelsInnerTypeEnumEnumMap = {
  SettingsBundleChannelsInnerTypeEnum.webhook: 'webhook',
  SettingsBundleChannelsInnerTypeEnum.slack: 'slack',
  SettingsBundleChannelsInnerTypeEnum.mattermost: 'mattermost',
  SettingsBundleChannelsInnerTypeEnum.discord: 'discord',
  SettingsBundleChannelsInnerTypeEnum.teams: 'teams',
  SettingsBundleChannelsInnerTypeEnum.googleChat: 'google-chat',
  SettingsBundleChannelsInnerTypeEnum.telegram: 'telegram',
  SettingsBundleChannelsInnerTypeEnum.matrix: 'matrix',
  SettingsBundleChannelsInnerTypeEnum.ntfy: 'ntfy',
  SettingsBundleChannelsInnerTypeEnum.gotify: 'gotify',
  SettingsBundleChannelsInnerTypeEnum.pushover: 'pushover',
  SettingsBundleChannelsInnerTypeEnum.email: 'email',
  SettingsBundleChannelsInnerTypeEnum.webpush: 'webpush',
  SettingsBundleChannelsInnerTypeEnum.homeassistant: 'homeassistant',
  SettingsBundleChannelsInnerTypeEnum.apprise: 'apprise',
  SettingsBundleChannelsInnerTypeEnum.zulip: 'zulip',
  SettingsBundleChannelsInnerTypeEnum.mqtt: 'mqtt',
  SettingsBundleChannelsInnerTypeEnum.meshtastic: 'meshtastic',
  SettingsBundleChannelsInnerTypeEnum.meshcore: 'meshcore',
};

const _$SettingsBundleChannelsInnerEventsEnumEnumMap = {
  SettingsBundleChannelsInnerEventsEnum.alert: 'alert',
  SettingsBundleChannelsInnerEventsEnum.briefing: 'briefing',
  SettingsBundleChannelsInnerEventsEnum.system: 'system',
  SettingsBundleChannelsInnerEventsEnum.chroniclePeriodProposed:
      'chronicle.proposed',
  SettingsBundleChannelsInnerEventsEnum.chroniclePeriodCapWarning:
      'chronicle.cap_warning',
  SettingsBundleChannelsInnerEventsEnum.chroniclePeriodGap: 'chronicle.gap',
  SettingsBundleChannelsInnerEventsEnum.socialPeriodDivergence:
      'social.divergence',
  SettingsBundleChannelsInnerEventsEnum.socialPeriodAttentionGap:
      'social.attention_gap',
  SettingsBundleChannelsInnerEventsEnum.analystPeriodNote: 'analyst.note',
  SettingsBundleChannelsInnerEventsEnum.situationPeriodActivated:
      'situation.activated',
  SettingsBundleChannelsInnerEventsEnum.situationPeriodEscalated:
      'situation.escalated',
  SettingsBundleChannelsInnerEventsEnum.findingPeriodSignificant:
      'finding.significant',
  SettingsBundleChannelsInnerEventsEnum.forecastPeriodResolved:
      'forecast.resolved',
  SettingsBundleChannelsInnerEventsEnum.forecastPeriodResolutionNeeded:
      'forecast.resolution_needed',
  SettingsBundleChannelsInnerEventsEnum.hypothesisPeriodBandChanged:
      'hypothesis.band_changed',
  SettingsBundleChannelsInnerEventsEnum.indicatorPeriodObserved:
      'indicator.observed',
  SettingsBundleChannelsInnerEventsEnum.assumptionPeriodBroken:
      'assumption.broken',
  SettingsBundleChannelsInnerEventsEnum.assessmentPeriodPublished:
      'assessment.published',
  SettingsBundleChannelsInnerEventsEnum.mention: 'mention',
  SettingsBundleChannelsInnerEventsEnum.share: 'share',
  SettingsBundleChannelsInnerEventsEnum.reportPeriodReceived: 'report.received',
  SettingsBundleChannelsInnerEventsEnum.packagePeriodReceived:
      'package.received',
  SettingsBundleChannelsInnerEventsEnum.chroniclePeriodAccessPeriodRequested:
      'chronicle.access.requested',
  SettingsBundleChannelsInnerEventsEnum.savedSearchPeriodMatches:
      'saved-search.matches',
};

const _$SettingsBundleChannelsInnerMinSeverityEnumEnumMap = {
  SettingsBundleChannelsInnerMinSeverityEnum.info: 'info',
  SettingsBundleChannelsInnerMinSeverityEnum.warning: 'warning',
  SettingsBundleChannelsInnerMinSeverityEnum.critical: 'critical',
};
