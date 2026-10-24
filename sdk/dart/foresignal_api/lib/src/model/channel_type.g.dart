// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'channel_type.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChannelTypeCWProxy {
  ChannelType id(ChannelTypeIdEnum id);

  ChannelType name(String name);

  ChannelType description(String? description);

  ChannelType configFields(List<ChannelTypeConfigFieldsInner> configFields);

  ChannelType secretFields(List<ChannelTypeConfigFieldsInner> secretFields);

  ChannelType events(List<ChannelTypeEventsEnum>? events);

  ChannelType capabilities(ChannelTypeCapabilities? capabilities);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChannelType(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChannelType(...).copyWith(id: 12, name: "My name")
  /// ```
  ChannelType call({
    ChannelTypeIdEnum id,
    String name,
    String? description,
    List<ChannelTypeConfigFieldsInner> configFields,
    List<ChannelTypeConfigFieldsInner> secretFields,
    List<ChannelTypeEventsEnum>? events,
    ChannelTypeCapabilities? capabilities,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChannelType.copyWith(...)` or call `instanceOfChannelType.copyWith.fieldName(value)` for a single field.
class _$ChannelTypeCWProxyImpl implements _$ChannelTypeCWProxy {
  const _$ChannelTypeCWProxyImpl(this._value);

  final ChannelType _value;

  @override
  ChannelType id(ChannelTypeIdEnum id) => call(id: id);

  @override
  ChannelType name(String name) => call(name: name);

  @override
  ChannelType description(String? description) =>
      call(description: description);

  @override
  ChannelType configFields(List<ChannelTypeConfigFieldsInner> configFields) =>
      call(configFields: configFields);

  @override
  ChannelType secretFields(List<ChannelTypeConfigFieldsInner> secretFields) =>
      call(secretFields: secretFields);

  @override
  ChannelType events(List<ChannelTypeEventsEnum>? events) =>
      call(events: events);

  @override
  ChannelType capabilities(ChannelTypeCapabilities? capabilities) =>
      call(capabilities: capabilities);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChannelType(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChannelType(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChannelType call({
    Object? id = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? configFields = const $CopyWithPlaceholder(),
    Object? secretFields = const $CopyWithPlaceholder(),
    Object? events = const $CopyWithPlaceholder(),
    Object? capabilities = const $CopyWithPlaceholder(),
  }) {
    return ChannelType(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as ChannelTypeIdEnum,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
      configFields:
          configFields == const $CopyWithPlaceholder() || configFields == null
          ? _value.configFields
          // ignore: cast_nullable_to_non_nullable
          : configFields as List<ChannelTypeConfigFieldsInner>,
      secretFields:
          secretFields == const $CopyWithPlaceholder() || secretFields == null
          ? _value.secretFields
          // ignore: cast_nullable_to_non_nullable
          : secretFields as List<ChannelTypeConfigFieldsInner>,
      events: events == const $CopyWithPlaceholder()
          ? _value.events
          // ignore: cast_nullable_to_non_nullable
          : events as List<ChannelTypeEventsEnum>?,
      capabilities: capabilities == const $CopyWithPlaceholder()
          ? _value.capabilities
          // ignore: cast_nullable_to_non_nullable
          : capabilities as ChannelTypeCapabilities?,
    );
  }
}

extension $ChannelTypeCopyWith on ChannelType {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChannelType.copyWith(...)` or `instanceOfChannelType.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChannelTypeCWProxy get copyWith => _$ChannelTypeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChannelType _$ChannelTypeFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ChannelType', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['id', 'name', 'configFields', 'secretFields'],
      );
      final val = ChannelType(
        id: $checkedConvert(
          'id',
          (v) => $enumDecode(_$ChannelTypeIdEnumEnumMap, v),
        ),
        name: $checkedConvert('name', (v) => v as String),
        description: $checkedConvert('description', (v) => v as String?),
        configFields: $checkedConvert(
          'configFields',
          (v) => (v as List<dynamic>)
              .map(
                (e) => ChannelTypeConfigFieldsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
        secretFields: $checkedConvert(
          'secretFields',
          (v) => (v as List<dynamic>)
              .map(
                (e) => ChannelTypeConfigFieldsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
        events: $checkedConvert(
          'events',
          (v) => (v as List<dynamic>?)
              ?.map((e) => $enumDecode(_$ChannelTypeEventsEnumEnumMap, e))
              .toList(),
        ),
        capabilities: $checkedConvert(
          'capabilities',
          (v) => v == null
              ? null
              : ChannelTypeCapabilities.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ChannelTypeToJson(ChannelType instance) =>
    <String, dynamic>{
      'id': _$ChannelTypeIdEnumEnumMap[instance.id]!,
      'name': instance.name,
      'description': ?instance.description,
      'configFields': instance.configFields.map((e) => e.toJson()).toList(),
      'secretFields': instance.secretFields.map((e) => e.toJson()).toList(),
      'events': ?instance.events
          ?.map((e) => _$ChannelTypeEventsEnumEnumMap[e]!)
          .toList(),
      'capabilities': ?instance.capabilities?.toJson(),
    };

const _$ChannelTypeIdEnumEnumMap = {
  ChannelTypeIdEnum.webhook: 'webhook',
  ChannelTypeIdEnum.slack: 'slack',
  ChannelTypeIdEnum.mattermost: 'mattermost',
  ChannelTypeIdEnum.discord: 'discord',
  ChannelTypeIdEnum.teams: 'teams',
  ChannelTypeIdEnum.googleChat: 'google-chat',
  ChannelTypeIdEnum.telegram: 'telegram',
  ChannelTypeIdEnum.matrix: 'matrix',
  ChannelTypeIdEnum.ntfy: 'ntfy',
  ChannelTypeIdEnum.gotify: 'gotify',
  ChannelTypeIdEnum.pushover: 'pushover',
  ChannelTypeIdEnum.email: 'email',
  ChannelTypeIdEnum.webpush: 'webpush',
  ChannelTypeIdEnum.homeassistant: 'homeassistant',
  ChannelTypeIdEnum.apprise: 'apprise',
  ChannelTypeIdEnum.zulip: 'zulip',
  ChannelTypeIdEnum.mqtt: 'mqtt',
  ChannelTypeIdEnum.meshtastic: 'meshtastic',
  ChannelTypeIdEnum.meshcore: 'meshcore',
};

const _$ChannelTypeEventsEnumEnumMap = {
  ChannelTypeEventsEnum.alert: 'alert',
  ChannelTypeEventsEnum.briefing: 'briefing',
  ChannelTypeEventsEnum.system: 'system',
  ChannelTypeEventsEnum.chroniclePeriodProposed: 'chronicle.proposed',
  ChannelTypeEventsEnum.chroniclePeriodCapWarning: 'chronicle.cap_warning',
  ChannelTypeEventsEnum.chroniclePeriodGap: 'chronicle.gap',
  ChannelTypeEventsEnum.socialPeriodDivergence: 'social.divergence',
  ChannelTypeEventsEnum.socialPeriodAttentionGap: 'social.attention_gap',
  ChannelTypeEventsEnum.analystPeriodNote: 'analyst.note',
  ChannelTypeEventsEnum.situationPeriodActivated: 'situation.activated',
  ChannelTypeEventsEnum.situationPeriodEscalated: 'situation.escalated',
  ChannelTypeEventsEnum.findingPeriodSignificant: 'finding.significant',
  ChannelTypeEventsEnum.forecastPeriodResolved: 'forecast.resolved',
  ChannelTypeEventsEnum.forecastPeriodResolutionNeeded:
      'forecast.resolution_needed',
  ChannelTypeEventsEnum.hypothesisPeriodBandChanged: 'hypothesis.band_changed',
  ChannelTypeEventsEnum.indicatorPeriodObserved: 'indicator.observed',
  ChannelTypeEventsEnum.assumptionPeriodBroken: 'assumption.broken',
  ChannelTypeEventsEnum.assessmentPeriodPublished: 'assessment.published',
  ChannelTypeEventsEnum.mention: 'mention',
  ChannelTypeEventsEnum.share: 'share',
  ChannelTypeEventsEnum.reportPeriodReceived: 'report.received',
  ChannelTypeEventsEnum.packagePeriodReceived: 'package.received',
  ChannelTypeEventsEnum.chroniclePeriodAccessPeriodRequested:
      'chronicle.access.requested',
  ChannelTypeEventsEnum.savedSearchPeriodMatches: 'saved-search.matches',
};
