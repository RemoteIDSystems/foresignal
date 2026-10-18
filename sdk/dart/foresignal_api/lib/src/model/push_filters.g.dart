// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'push_filters.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PushFiltersCWProxy {
  PushFilters events(List<PushFiltersEventsEnum>? events);

  PushFilters ruleIds(List<String>? ruleIds);

  PushFilters minSeverity(PushFiltersMinSeverityEnum? minSeverity);

  PushFilters tags(List<String>? tags);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PushFilters(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PushFilters(...).copyWith(id: 12, name: "My name")
  /// ```
  PushFilters call({
    List<PushFiltersEventsEnum>? events,
    List<String>? ruleIds,
    PushFiltersMinSeverityEnum? minSeverity,
    List<String>? tags,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPushFilters.copyWith(...)` or call `instanceOfPushFilters.copyWith.fieldName(value)` for a single field.
class _$PushFiltersCWProxyImpl implements _$PushFiltersCWProxy {
  const _$PushFiltersCWProxyImpl(this._value);

  final PushFilters _value;

  @override
  PushFilters events(List<PushFiltersEventsEnum>? events) =>
      call(events: events);

  @override
  PushFilters ruleIds(List<String>? ruleIds) => call(ruleIds: ruleIds);

  @override
  PushFilters minSeverity(PushFiltersMinSeverityEnum? minSeverity) =>
      call(minSeverity: minSeverity);

  @override
  PushFilters tags(List<String>? tags) => call(tags: tags);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PushFilters(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PushFilters(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PushFilters call({
    Object? events = const $CopyWithPlaceholder(),
    Object? ruleIds = const $CopyWithPlaceholder(),
    Object? minSeverity = const $CopyWithPlaceholder(),
    Object? tags = const $CopyWithPlaceholder(),
  }) {
    return PushFilters(
      events: events == const $CopyWithPlaceholder()
          ? _value.events
          // ignore: cast_nullable_to_non_nullable
          : events as List<PushFiltersEventsEnum>?,
      ruleIds: ruleIds == const $CopyWithPlaceholder()
          ? _value.ruleIds
          // ignore: cast_nullable_to_non_nullable
          : ruleIds as List<String>?,
      minSeverity: minSeverity == const $CopyWithPlaceholder()
          ? _value.minSeverity
          // ignore: cast_nullable_to_non_nullable
          : minSeverity as PushFiltersMinSeverityEnum?,
      tags: tags == const $CopyWithPlaceholder()
          ? _value.tags
          // ignore: cast_nullable_to_non_nullable
          : tags as List<String>?,
    );
  }
}

extension $PushFiltersCopyWith on PushFilters {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPushFilters.copyWith(...)` or `instanceOfPushFilters.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PushFiltersCWProxy get copyWith => _$PushFiltersCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PushFilters _$PushFiltersFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PushFilters', json, ($checkedConvert) {
      final val = PushFilters(
        events: $checkedConvert(
          'events',
          (v) => (v as List<dynamic>?)
              ?.map((e) => $enumDecode(_$PushFiltersEventsEnumEnumMap, e))
              .toList(),
        ),
        ruleIds: $checkedConvert(
          'ruleIds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        minSeverity: $checkedConvert(
          'minSeverity',
          (v) => $enumDecodeNullable(_$PushFiltersMinSeverityEnumEnumMap, v),
        ),
        tags: $checkedConvert(
          'tags',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$PushFiltersToJson(PushFilters instance) =>
    <String, dynamic>{
      'events': ?instance.events
          ?.map((e) => _$PushFiltersEventsEnumEnumMap[e]!)
          .toList(),
      'ruleIds': ?instance.ruleIds,
      'minSeverity': ?_$PushFiltersMinSeverityEnumEnumMap[instance.minSeverity],
      'tags': ?instance.tags,
    };

const _$PushFiltersEventsEnumEnumMap = {
  PushFiltersEventsEnum.alert: 'alert',
  PushFiltersEventsEnum.briefing: 'briefing',
  PushFiltersEventsEnum.system: 'system',
};

const _$PushFiltersMinSeverityEnumEnumMap = {
  PushFiltersMinSeverityEnum.info: 'info',
  PushFiltersMinSeverityEnum.warning: 'warning',
  PushFiltersMinSeverityEnum.critical: 'critical',
};
