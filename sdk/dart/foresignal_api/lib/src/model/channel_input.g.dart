// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'channel_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChannelInputCWProxy {
  ChannelInput name(String name);

  ChannelInput type(ChannelInputTypeEnum type);

  ChannelInput config(ChannelInputConfig? config);

  ChannelInput secrets(Map<String, String>? secrets);

  ChannelInput events(List<ChannelInputEventsEnum>? events);

  ChannelInput ruleIds(List<String>? ruleIds);

  ChannelInput minSeverity(ChannelInputMinSeverityEnum? minSeverity);

  ChannelInput titleTemplate(String? titleTemplate);

  ChannelInput bodyTemplate(String? bodyTemplate);

  ChannelInput waitForEnrichmentSeconds(int? waitForEnrichmentSeconds);

  ChannelInput enabled(bool? enabled);

  ChannelInput timezone(String? timezone);

  ChannelInput quietHours(QuietHours? quietHours);

  ChannelInput groupWindowSeconds(int? groupWindowSeconds);

  ChannelInput backlogCap(int? backlogCap);

  ChannelInput editInPlace(bool? editInPlace);

  ChannelInput deferWhileActiveSeconds(int? deferWhileActiveSeconds);

  ChannelInput acceptExercise(bool? acceptExercise);

  ChannelInput language(String? language);

  ChannelInput global(bool? global);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChannelInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChannelInput(...).copyWith(id: 12, name: "My name")
  /// ```
  ChannelInput call({
    String name,
    ChannelInputTypeEnum type,
    ChannelInputConfig? config,
    Map<String, String>? secrets,
    List<ChannelInputEventsEnum>? events,
    List<String>? ruleIds,
    ChannelInputMinSeverityEnum? minSeverity,
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
    bool? global,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChannelInput.copyWith(...)` or call `instanceOfChannelInput.copyWith.fieldName(value)` for a single field.
class _$ChannelInputCWProxyImpl implements _$ChannelInputCWProxy {
  const _$ChannelInputCWProxyImpl(this._value);

  final ChannelInput _value;

  @override
  ChannelInput name(String name) => call(name: name);

  @override
  ChannelInput type(ChannelInputTypeEnum type) => call(type: type);

  @override
  ChannelInput config(ChannelInputConfig? config) => call(config: config);

  @override
  ChannelInput secrets(Map<String, String>? secrets) => call(secrets: secrets);

  @override
  ChannelInput events(List<ChannelInputEventsEnum>? events) =>
      call(events: events);

  @override
  ChannelInput ruleIds(List<String>? ruleIds) => call(ruleIds: ruleIds);

  @override
  ChannelInput minSeverity(ChannelInputMinSeverityEnum? minSeverity) =>
      call(minSeverity: minSeverity);

  @override
  ChannelInput titleTemplate(String? titleTemplate) =>
      call(titleTemplate: titleTemplate);

  @override
  ChannelInput bodyTemplate(String? bodyTemplate) =>
      call(bodyTemplate: bodyTemplate);

  @override
  ChannelInput waitForEnrichmentSeconds(int? waitForEnrichmentSeconds) =>
      call(waitForEnrichmentSeconds: waitForEnrichmentSeconds);

  @override
  ChannelInput enabled(bool? enabled) => call(enabled: enabled);

  @override
  ChannelInput timezone(String? timezone) => call(timezone: timezone);

  @override
  ChannelInput quietHours(QuietHours? quietHours) =>
      call(quietHours: quietHours);

  @override
  ChannelInput groupWindowSeconds(int? groupWindowSeconds) =>
      call(groupWindowSeconds: groupWindowSeconds);

  @override
  ChannelInput backlogCap(int? backlogCap) => call(backlogCap: backlogCap);

  @override
  ChannelInput editInPlace(bool? editInPlace) => call(editInPlace: editInPlace);

  @override
  ChannelInput deferWhileActiveSeconds(int? deferWhileActiveSeconds) =>
      call(deferWhileActiveSeconds: deferWhileActiveSeconds);

  @override
  ChannelInput acceptExercise(bool? acceptExercise) =>
      call(acceptExercise: acceptExercise);

  @override
  ChannelInput language(String? language) => call(language: language);

  @override
  ChannelInput global(bool? global) => call(global: global);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChannelInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChannelInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChannelInput call({
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
    Object? global = const $CopyWithPlaceholder(),
  }) {
    return ChannelInput(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ChannelInputTypeEnum,
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
          : events as List<ChannelInputEventsEnum>?,
      ruleIds: ruleIds == const $CopyWithPlaceholder()
          ? _value.ruleIds
          // ignore: cast_nullable_to_non_nullable
          : ruleIds as List<String>?,
      minSeverity: minSeverity == const $CopyWithPlaceholder()
          ? _value.minSeverity
          // ignore: cast_nullable_to_non_nullable
          : minSeverity as ChannelInputMinSeverityEnum?,
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
      global: global == const $CopyWithPlaceholder()
          ? _value.global
          // ignore: cast_nullable_to_non_nullable
          : global as bool?,
    );
  }
}

extension $ChannelInputCopyWith on ChannelInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChannelInput.copyWith(...)` or `instanceOfChannelInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChannelInputCWProxy get copyWith => _$ChannelInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChannelInput _$ChannelInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ChannelInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['name', 'type']);
      final val = ChannelInput(
        name: $checkedConvert('name', (v) => v as String),
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$ChannelInputTypeEnumEnumMap, v),
        ),
        config: $checkedConvert(
          'config',
          (v) => v == null
              ? null
              : ChannelInputConfig.fromJson(v as Map<String, dynamic>),
        ),
        secrets: $checkedConvert(
          'secrets',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as String),
          ),
        ),
        events: $checkedConvert(
          'events',
          (v) => (v as List<dynamic>?)
              ?.map((e) => $enumDecode(_$ChannelInputEventsEnumEnumMap, e))
              .toList(),
        ),
        ruleIds: $checkedConvert(
          'ruleIds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        minSeverity: $checkedConvert(
          'minSeverity',
          (v) => $enumDecodeNullable(_$ChannelInputMinSeverityEnumEnumMap, v),
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
          (v) =>
              v == null ? null : QuietHours.fromJson(v as Map<String, dynamic>),
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
        global: $checkedConvert('global', (v) => v as bool?),
      );
      return val;
    });

Map<String, dynamic> _$ChannelInputToJson(
  ChannelInput instance,
) => <String, dynamic>{
  'name': instance.name,
  'type': _$ChannelInputTypeEnumEnumMap[instance.type]!,
  'config': ?instance.config?.toJson(),
  'secrets': ?instance.secrets,
  'events': ?instance.events
      ?.map((e) => _$ChannelInputEventsEnumEnumMap[e]!)
      .toList(),
  'ruleIds': ?instance.ruleIds,
  'minSeverity': ?_$ChannelInputMinSeverityEnumEnumMap[instance.minSeverity],
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
  'global': ?instance.global,
};

const _$ChannelInputTypeEnumEnumMap = {
  ChannelInputTypeEnum.webhook: 'webhook',
  ChannelInputTypeEnum.slack: 'slack',
  ChannelInputTypeEnum.mattermost: 'mattermost',
  ChannelInputTypeEnum.discord: 'discord',
  ChannelInputTypeEnum.teams: 'teams',
  ChannelInputTypeEnum.googleChat: 'google-chat',
  ChannelInputTypeEnum.telegram: 'telegram',
  ChannelInputTypeEnum.matrix: 'matrix',
  ChannelInputTypeEnum.ntfy: 'ntfy',
  ChannelInputTypeEnum.gotify: 'gotify',
  ChannelInputTypeEnum.pushover: 'pushover',
  ChannelInputTypeEnum.email: 'email',
  ChannelInputTypeEnum.webpush: 'webpush',
  ChannelInputTypeEnum.homeassistant: 'homeassistant',
  ChannelInputTypeEnum.apprise: 'apprise',
  ChannelInputTypeEnum.zulip: 'zulip',
  ChannelInputTypeEnum.mqtt: 'mqtt',
  ChannelInputTypeEnum.meshtastic: 'meshtastic',
  ChannelInputTypeEnum.meshcore: 'meshcore',
};

const _$ChannelInputEventsEnumEnumMap = {
  ChannelInputEventsEnum.alert: 'alert',
  ChannelInputEventsEnum.briefing: 'briefing',
  ChannelInputEventsEnum.system: 'system',
  ChannelInputEventsEnum.chroniclePeriodProposed: 'chronicle.proposed',
  ChannelInputEventsEnum.chroniclePeriodCapWarning: 'chronicle.cap_warning',
  ChannelInputEventsEnum.chroniclePeriodGap: 'chronicle.gap',
  ChannelInputEventsEnum.socialPeriodDivergence: 'social.divergence',
  ChannelInputEventsEnum.socialPeriodAttentionGap: 'social.attention_gap',
  ChannelInputEventsEnum.analystPeriodNote: 'analyst.note',
  ChannelInputEventsEnum.situationPeriodActivated: 'situation.activated',
  ChannelInputEventsEnum.situationPeriodEscalated: 'situation.escalated',
  ChannelInputEventsEnum.findingPeriodSignificant: 'finding.significant',
  ChannelInputEventsEnum.forecastPeriodResolved: 'forecast.resolved',
  ChannelInputEventsEnum.forecastPeriodResolutionNeeded:
      'forecast.resolution_needed',
  ChannelInputEventsEnum.hypothesisPeriodBandChanged: 'hypothesis.band_changed',
  ChannelInputEventsEnum.indicatorPeriodObserved: 'indicator.observed',
  ChannelInputEventsEnum.assumptionPeriodBroken: 'assumption.broken',
  ChannelInputEventsEnum.assessmentPeriodPublished: 'assessment.published',
  ChannelInputEventsEnum.mention: 'mention',
  ChannelInputEventsEnum.share: 'share',
  ChannelInputEventsEnum.reportPeriodReceived: 'report.received',
  ChannelInputEventsEnum.packagePeriodReceived: 'package.received',
  ChannelInputEventsEnum.chroniclePeriodAccessPeriodRequested:
      'chronicle.access.requested',
  ChannelInputEventsEnum.savedSearchPeriodMatches: 'saved-search.matches',
};

const _$ChannelInputMinSeverityEnumEnumMap = {
  ChannelInputMinSeverityEnum.info: 'info',
  ChannelInputMinSeverityEnum.warning: 'warning',
  ChannelInputMinSeverityEnum.critical: 'critical',
};
