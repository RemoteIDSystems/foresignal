// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'persist_reason_counts.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PersistReasonCountsCWProxy {
  PersistReasonCounts FIRST(int FIRST);

  PersistReasonCounts REAPPEAR(int REAPPEAR);

  PersistReasonCounts SIGNIFICANT(int SIGNIFICANT);

  PersistReasonCounts TOMBSTONE(int TOMBSTONE);

  PersistReasonCounts CHANGE(int CHANGE);

  PersistReasonCounts SAMPLE(int SAMPLE);

  PersistReasonCounts KEYFRAME(int KEYFRAME);

  PersistReasonCounts HEARTBEAT(int HEARTBEAT);

  PersistReasonCounts TRIGGER_EDGE(int TRIGGER_EDGE);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PersistReasonCounts(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PersistReasonCounts(...).copyWith(id: 12, name: "My name")
  /// ```
  PersistReasonCounts call({
    int FIRST,
    int REAPPEAR,
    int SIGNIFICANT,
    int TOMBSTONE,
    int CHANGE,
    int SAMPLE,
    int KEYFRAME,
    int HEARTBEAT,
    int TRIGGER_EDGE,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPersistReasonCounts.copyWith(...)` or call `instanceOfPersistReasonCounts.copyWith.fieldName(value)` for a single field.
class _$PersistReasonCountsCWProxyImpl implements _$PersistReasonCountsCWProxy {
  const _$PersistReasonCountsCWProxyImpl(this._value);

  final PersistReasonCounts _value;

  @override
  PersistReasonCounts FIRST(int FIRST) => call(FIRST: FIRST);

  @override
  PersistReasonCounts REAPPEAR(int REAPPEAR) => call(REAPPEAR: REAPPEAR);

  @override
  PersistReasonCounts SIGNIFICANT(int SIGNIFICANT) =>
      call(SIGNIFICANT: SIGNIFICANT);

  @override
  PersistReasonCounts TOMBSTONE(int TOMBSTONE) => call(TOMBSTONE: TOMBSTONE);

  @override
  PersistReasonCounts CHANGE(int CHANGE) => call(CHANGE: CHANGE);

  @override
  PersistReasonCounts SAMPLE(int SAMPLE) => call(SAMPLE: SAMPLE);

  @override
  PersistReasonCounts KEYFRAME(int KEYFRAME) => call(KEYFRAME: KEYFRAME);

  @override
  PersistReasonCounts HEARTBEAT(int HEARTBEAT) => call(HEARTBEAT: HEARTBEAT);

  @override
  PersistReasonCounts TRIGGER_EDGE(int TRIGGER_EDGE) =>
      call(TRIGGER_EDGE: TRIGGER_EDGE);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PersistReasonCounts(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PersistReasonCounts(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PersistReasonCounts call({
    Object? FIRST = const $CopyWithPlaceholder(),
    Object? REAPPEAR = const $CopyWithPlaceholder(),
    Object? SIGNIFICANT = const $CopyWithPlaceholder(),
    Object? TOMBSTONE = const $CopyWithPlaceholder(),
    Object? CHANGE = const $CopyWithPlaceholder(),
    Object? SAMPLE = const $CopyWithPlaceholder(),
    Object? KEYFRAME = const $CopyWithPlaceholder(),
    Object? HEARTBEAT = const $CopyWithPlaceholder(),
    Object? TRIGGER_EDGE = const $CopyWithPlaceholder(),
  }) {
    return PersistReasonCounts(
      FIRST: FIRST == const $CopyWithPlaceholder() || FIRST == null
          ? _value.FIRST
          // ignore: cast_nullable_to_non_nullable
          : FIRST as int,
      REAPPEAR: REAPPEAR == const $CopyWithPlaceholder() || REAPPEAR == null
          ? _value.REAPPEAR
          // ignore: cast_nullable_to_non_nullable
          : REAPPEAR as int,
      SIGNIFICANT:
          SIGNIFICANT == const $CopyWithPlaceholder() || SIGNIFICANT == null
          ? _value.SIGNIFICANT
          // ignore: cast_nullable_to_non_nullable
          : SIGNIFICANT as int,
      TOMBSTONE: TOMBSTONE == const $CopyWithPlaceholder() || TOMBSTONE == null
          ? _value.TOMBSTONE
          // ignore: cast_nullable_to_non_nullable
          : TOMBSTONE as int,
      CHANGE: CHANGE == const $CopyWithPlaceholder() || CHANGE == null
          ? _value.CHANGE
          // ignore: cast_nullable_to_non_nullable
          : CHANGE as int,
      SAMPLE: SAMPLE == const $CopyWithPlaceholder() || SAMPLE == null
          ? _value.SAMPLE
          // ignore: cast_nullable_to_non_nullable
          : SAMPLE as int,
      KEYFRAME: KEYFRAME == const $CopyWithPlaceholder() || KEYFRAME == null
          ? _value.KEYFRAME
          // ignore: cast_nullable_to_non_nullable
          : KEYFRAME as int,
      HEARTBEAT: HEARTBEAT == const $CopyWithPlaceholder() || HEARTBEAT == null
          ? _value.HEARTBEAT
          // ignore: cast_nullable_to_non_nullable
          : HEARTBEAT as int,
      TRIGGER_EDGE:
          TRIGGER_EDGE == const $CopyWithPlaceholder() || TRIGGER_EDGE == null
          ? _value.TRIGGER_EDGE
          // ignore: cast_nullable_to_non_nullable
          : TRIGGER_EDGE as int,
    );
  }
}

extension $PersistReasonCountsCopyWith on PersistReasonCounts {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPersistReasonCounts.copyWith(...)` or `instanceOfPersistReasonCounts.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PersistReasonCountsCWProxy get copyWith =>
      _$PersistReasonCountsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PersistReasonCounts _$PersistReasonCountsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PersistReasonCounts', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'FIRST',
          'REAPPEAR',
          'SIGNIFICANT',
          'TOMBSTONE',
          'CHANGE',
          'SAMPLE',
          'KEYFRAME',
          'HEARTBEAT',
          'TRIGGER_EDGE',
        ],
      );
      final val = PersistReasonCounts(
        FIRST: $checkedConvert('FIRST', (v) => (v as num).toInt()),
        REAPPEAR: $checkedConvert('REAPPEAR', (v) => (v as num).toInt()),
        SIGNIFICANT: $checkedConvert('SIGNIFICANT', (v) => (v as num).toInt()),
        TOMBSTONE: $checkedConvert('TOMBSTONE', (v) => (v as num).toInt()),
        CHANGE: $checkedConvert('CHANGE', (v) => (v as num).toInt()),
        SAMPLE: $checkedConvert('SAMPLE', (v) => (v as num).toInt()),
        KEYFRAME: $checkedConvert('KEYFRAME', (v) => (v as num).toInt()),
        HEARTBEAT: $checkedConvert('HEARTBEAT', (v) => (v as num).toInt()),
        TRIGGER_EDGE: $checkedConvert(
          'TRIGGER_EDGE',
          (v) => (v as num).toInt(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$PersistReasonCountsToJson(
  PersistReasonCounts instance,
) => <String, dynamic>{
  'FIRST': instance.FIRST,
  'REAPPEAR': instance.REAPPEAR,
  'SIGNIFICANT': instance.SIGNIFICANT,
  'TOMBSTONE': instance.TOMBSTONE,
  'CHANGE': instance.CHANGE,
  'SAMPLE': instance.SAMPLE,
  'KEYFRAME': instance.KEYFRAME,
  'HEARTBEAT': instance.HEARTBEAT,
  'TRIGGER_EDGE': instance.TRIGGER_EDGE,
};
