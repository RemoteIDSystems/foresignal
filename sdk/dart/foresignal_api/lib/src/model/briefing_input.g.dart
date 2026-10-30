// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'briefing_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BriefingInputCWProxy {
  BriefingInput name(String name);

  BriefingInput enabled(bool? enabled);

  BriefingInput schedule(String schedule);

  BriefingInput timezone(String? timezone);

  BriefingInput scope(BriefingScope? scope);

  BriefingInput lookback(String? lookback);

  BriefingInput mode(BriefingInputModeEnum? mode);

  BriefingInput instructions(String? instructions);

  BriefingInput channelIds(List<String>? channelIds);

  BriefingInput maxItemsPerKind(int? maxItemsPerKind);

  BriefingInput audio(BriefingInputAudio? audio);

  BriefingInput outputLanguage(String? outputLanguage);

  BriefingInput deliveryTargets(List<DeliveryTarget>? deliveryTargets);

  BriefingInput site(BriefingInputSite? site);

  BriefingInput breakingNews(BreakingNewsConfig? breakingNews);

  BriefingInput includeProvenance(bool? includeProvenance);

  BriefingInput includeCorroboration(bool? includeCorroboration);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BriefingInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BriefingInput(...).copyWith(id: 12, name: "My name")
  /// ```
  BriefingInput call({
    String name,
    bool? enabled,
    String schedule,
    String? timezone,
    BriefingScope? scope,
    String? lookback,
    BriefingInputModeEnum? mode,
    String? instructions,
    List<String>? channelIds,
    int? maxItemsPerKind,
    BriefingInputAudio? audio,
    String? outputLanguage,
    List<DeliveryTarget>? deliveryTargets,
    BriefingInputSite? site,
    BreakingNewsConfig? breakingNews,
    bool? includeProvenance,
    bool? includeCorroboration,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBriefingInput.copyWith(...)` or call `instanceOfBriefingInput.copyWith.fieldName(value)` for a single field.
class _$BriefingInputCWProxyImpl implements _$BriefingInputCWProxy {
  const _$BriefingInputCWProxyImpl(this._value);

  final BriefingInput _value;

  @override
  BriefingInput name(String name) => call(name: name);

  @override
  BriefingInput enabled(bool? enabled) => call(enabled: enabled);

  @override
  BriefingInput schedule(String schedule) => call(schedule: schedule);

  @override
  BriefingInput timezone(String? timezone) => call(timezone: timezone);

  @override
  BriefingInput scope(BriefingScope? scope) => call(scope: scope);

  @override
  BriefingInput lookback(String? lookback) => call(lookback: lookback);

  @override
  BriefingInput mode(BriefingInputModeEnum? mode) => call(mode: mode);

  @override
  BriefingInput instructions(String? instructions) =>
      call(instructions: instructions);

  @override
  BriefingInput channelIds(List<String>? channelIds) =>
      call(channelIds: channelIds);

  @override
  BriefingInput maxItemsPerKind(int? maxItemsPerKind) =>
      call(maxItemsPerKind: maxItemsPerKind);

  @override
  BriefingInput audio(BriefingInputAudio? audio) => call(audio: audio);

  @override
  BriefingInput outputLanguage(String? outputLanguage) =>
      call(outputLanguage: outputLanguage);

  @override
  BriefingInput deliveryTargets(List<DeliveryTarget>? deliveryTargets) =>
      call(deliveryTargets: deliveryTargets);

  @override
  BriefingInput site(BriefingInputSite? site) => call(site: site);

  @override
  BriefingInput breakingNews(BreakingNewsConfig? breakingNews) =>
      call(breakingNews: breakingNews);

  @override
  BriefingInput includeProvenance(bool? includeProvenance) =>
      call(includeProvenance: includeProvenance);

  @override
  BriefingInput includeCorroboration(bool? includeCorroboration) =>
      call(includeCorroboration: includeCorroboration);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BriefingInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BriefingInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BriefingInput call({
    Object? name = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    Object? schedule = const $CopyWithPlaceholder(),
    Object? timezone = const $CopyWithPlaceholder(),
    Object? scope = const $CopyWithPlaceholder(),
    Object? lookback = const $CopyWithPlaceholder(),
    Object? mode = const $CopyWithPlaceholder(),
    Object? instructions = const $CopyWithPlaceholder(),
    Object? channelIds = const $CopyWithPlaceholder(),
    Object? maxItemsPerKind = const $CopyWithPlaceholder(),
    Object? audio = const $CopyWithPlaceholder(),
    Object? outputLanguage = const $CopyWithPlaceholder(),
    Object? deliveryTargets = const $CopyWithPlaceholder(),
    Object? site = const $CopyWithPlaceholder(),
    Object? breakingNews = const $CopyWithPlaceholder(),
    Object? includeProvenance = const $CopyWithPlaceholder(),
    Object? includeCorroboration = const $CopyWithPlaceholder(),
  }) {
    return BriefingInput(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      enabled: enabled == const $CopyWithPlaceholder()
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool?,
      schedule: schedule == const $CopyWithPlaceholder() || schedule == null
          ? _value.schedule
          // ignore: cast_nullable_to_non_nullable
          : schedule as String,
      timezone: timezone == const $CopyWithPlaceholder()
          ? _value.timezone
          // ignore: cast_nullable_to_non_nullable
          : timezone as String?,
      scope: scope == const $CopyWithPlaceholder()
          ? _value.scope
          // ignore: cast_nullable_to_non_nullable
          : scope as BriefingScope?,
      lookback: lookback == const $CopyWithPlaceholder()
          ? _value.lookback
          // ignore: cast_nullable_to_non_nullable
          : lookback as String?,
      mode: mode == const $CopyWithPlaceholder()
          ? _value.mode
          // ignore: cast_nullable_to_non_nullable
          : mode as BriefingInputModeEnum?,
      instructions: instructions == const $CopyWithPlaceholder()
          ? _value.instructions
          // ignore: cast_nullable_to_non_nullable
          : instructions as String?,
      channelIds: channelIds == const $CopyWithPlaceholder()
          ? _value.channelIds
          // ignore: cast_nullable_to_non_nullable
          : channelIds as List<String>?,
      maxItemsPerKind: maxItemsPerKind == const $CopyWithPlaceholder()
          ? _value.maxItemsPerKind
          // ignore: cast_nullable_to_non_nullable
          : maxItemsPerKind as int?,
      audio: audio == const $CopyWithPlaceholder()
          ? _value.audio
          // ignore: cast_nullable_to_non_nullable
          : audio as BriefingInputAudio?,
      outputLanguage: outputLanguage == const $CopyWithPlaceholder()
          ? _value.outputLanguage
          // ignore: cast_nullable_to_non_nullable
          : outputLanguage as String?,
      deliveryTargets: deliveryTargets == const $CopyWithPlaceholder()
          ? _value.deliveryTargets
          // ignore: cast_nullable_to_non_nullable
          : deliveryTargets as List<DeliveryTarget>?,
      site: site == const $CopyWithPlaceholder()
          ? _value.site
          // ignore: cast_nullable_to_non_nullable
          : site as BriefingInputSite?,
      breakingNews: breakingNews == const $CopyWithPlaceholder()
          ? _value.breakingNews
          // ignore: cast_nullable_to_non_nullable
          : breakingNews as BreakingNewsConfig?,
      includeProvenance: includeProvenance == const $CopyWithPlaceholder()
          ? _value.includeProvenance
          // ignore: cast_nullable_to_non_nullable
          : includeProvenance as bool?,
      includeCorroboration: includeCorroboration == const $CopyWithPlaceholder()
          ? _value.includeCorroboration
          // ignore: cast_nullable_to_non_nullable
          : includeCorroboration as bool?,
    );
  }
}

extension $BriefingInputCopyWith on BriefingInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBriefingInput.copyWith(...)` or `instanceOfBriefingInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BriefingInputCWProxy get copyWith => _$BriefingInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BriefingInput _$BriefingInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('BriefingInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['name', 'schedule']);
      final val = BriefingInput(
        name: $checkedConvert('name', (v) => v as String),
        enabled: $checkedConvert('enabled', (v) => v as bool? ?? true),
        schedule: $checkedConvert('schedule', (v) => v as String),
        timezone: $checkedConvert('timezone', (v) => v as String? ?? 'UTC'),
        scope: $checkedConvert(
          'scope',
          (v) => v == null
              ? null
              : BriefingScope.fromJson(v as Map<String, dynamic>),
        ),
        lookback: $checkedConvert('lookback', (v) => v as String?),
        mode: $checkedConvert(
          'mode',
          (v) => $enumDecodeNullable(_$BriefingInputModeEnumEnumMap, v),
        ),
        instructions: $checkedConvert('instructions', (v) => v as String?),
        channelIds: $checkedConvert(
          'channelIds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        maxItemsPerKind: $checkedConvert(
          'maxItemsPerKind',
          (v) => (v as num?)?.toInt() ?? 5,
        ),
        audio: $checkedConvert(
          'audio',
          (v) => v == null
              ? null
              : BriefingInputAudio.fromJson(v as Map<String, dynamic>),
        ),
        outputLanguage: $checkedConvert('outputLanguage', (v) => v as String?),
        deliveryTargets: $checkedConvert(
          'deliveryTargets',
          (v) => (v as List<dynamic>?)
              ?.map((e) => DeliveryTarget.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        site: $checkedConvert(
          'site',
          (v) => v == null
              ? null
              : BriefingInputSite.fromJson(v as Map<String, dynamic>),
        ),
        breakingNews: $checkedConvert(
          'breakingNews',
          (v) => v == null
              ? null
              : BreakingNewsConfig.fromJson(v as Map<String, dynamic>),
        ),
        includeProvenance: $checkedConvert(
          'includeProvenance',
          (v) => v as bool? ?? false,
        ),
        includeCorroboration: $checkedConvert(
          'includeCorroboration',
          (v) => v as bool? ?? false,
        ),
      );
      return val;
    });

Map<String, dynamic> _$BriefingInputToJson(
  BriefingInput instance,
) => <String, dynamic>{
  'name': instance.name,
  'enabled': ?instance.enabled,
  'schedule': instance.schedule,
  'timezone': ?instance.timezone,
  'scope': ?instance.scope?.toJson(),
  'lookback': ?instance.lookback,
  'mode': ?_$BriefingInputModeEnumEnumMap[instance.mode],
  'instructions': ?instance.instructions,
  'channelIds': ?instance.channelIds,
  'maxItemsPerKind': ?instance.maxItemsPerKind,
  'audio': ?instance.audio?.toJson(),
  'outputLanguage': ?instance.outputLanguage,
  'deliveryTargets': ?instance.deliveryTargets?.map((e) => e.toJson()).toList(),
  'site': ?instance.site?.toJson(),
  'breakingNews': ?instance.breakingNews?.toJson(),
  'includeProvenance': ?instance.includeProvenance,
  'includeCorroboration': ?instance.includeCorroboration,
};

const _$BriefingInputModeEnumEnumMap = {
  BriefingInputModeEnum.digest: 'digest',
  BriefingInputModeEnum.llm: 'llm',
};
