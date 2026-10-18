// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'event_study_result_events_inner_abnormal_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EventStudyResultEventsInnerAbnormalInnerCWProxy {
  EventStudyResultEventsInnerAbnormalInner offset(int offset);

  EventStudyResultEventsInnerAbnormalInner value(num? value);

  EventStudyResultEventsInnerAbnormalInner cumulative(num? cumulative);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventStudyResultEventsInnerAbnormalInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventStudyResultEventsInnerAbnormalInner(...).copyWith(id: 12, name: "My name")
  /// ```
  EventStudyResultEventsInnerAbnormalInner call({
    int offset,
    num? value,
    num? cumulative,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEventStudyResultEventsInnerAbnormalInner.copyWith(...)` or call `instanceOfEventStudyResultEventsInnerAbnormalInner.copyWith.fieldName(value)` for a single field.
class _$EventStudyResultEventsInnerAbnormalInnerCWProxyImpl
    implements _$EventStudyResultEventsInnerAbnormalInnerCWProxy {
  const _$EventStudyResultEventsInnerAbnormalInnerCWProxyImpl(this._value);

  final EventStudyResultEventsInnerAbnormalInner _value;

  @override
  EventStudyResultEventsInnerAbnormalInner offset(int offset) =>
      call(offset: offset);

  @override
  EventStudyResultEventsInnerAbnormalInner value(num? value) =>
      call(value: value);

  @override
  EventStudyResultEventsInnerAbnormalInner cumulative(num? cumulative) =>
      call(cumulative: cumulative);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventStudyResultEventsInnerAbnormalInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventStudyResultEventsInnerAbnormalInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EventStudyResultEventsInnerAbnormalInner call({
    Object? offset = const $CopyWithPlaceholder(),
    Object? value = const $CopyWithPlaceholder(),
    Object? cumulative = const $CopyWithPlaceholder(),
  }) {
    return EventStudyResultEventsInnerAbnormalInner(
      offset: offset == const $CopyWithPlaceholder() || offset == null
          ? _value.offset
          // ignore: cast_nullable_to_non_nullable
          : offset as int,
      value: value == const $CopyWithPlaceholder()
          ? _value.value
          // ignore: cast_nullable_to_non_nullable
          : value as num?,
      cumulative: cumulative == const $CopyWithPlaceholder()
          ? _value.cumulative
          // ignore: cast_nullable_to_non_nullable
          : cumulative as num?,
    );
  }
}

extension $EventStudyResultEventsInnerAbnormalInnerCopyWith
    on EventStudyResultEventsInnerAbnormalInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEventStudyResultEventsInnerAbnormalInner.copyWith(...)` or `instanceOfEventStudyResultEventsInnerAbnormalInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EventStudyResultEventsInnerAbnormalInnerCWProxy get copyWith =>
      _$EventStudyResultEventsInnerAbnormalInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EventStudyResultEventsInnerAbnormalInner
_$EventStudyResultEventsInnerAbnormalInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EventStudyResultEventsInnerAbnormalInner', json, (
      $checkedConvert,
    ) {
      $checkKeys(json, requiredKeys: const ['offset']);
      final val = EventStudyResultEventsInnerAbnormalInner(
        offset: $checkedConvert('offset', (v) => (v as num).toInt()),
        value: $checkedConvert('value', (v) => v as num?),
        cumulative: $checkedConvert('cumulative', (v) => v as num?),
      );
      return val;
    });

Map<String, dynamic> _$EventStudyResultEventsInnerAbnormalInnerToJson(
  EventStudyResultEventsInnerAbnormalInner instance,
) => <String, dynamic>{
  'offset': instance.offset,
  'value': ?instance.value,
  'cumulative': ?instance.cumulative,
};
