// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'window_status.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WindowStatusCWProxy {
  WindowStatus states(WindowStatusStates states);

  WindowStatus groups(int groups);

  WindowStatus groupsTruncated(bool? groupsTruncated);

  WindowStatus learningUntil(DateTime? learningUntil);

  WindowStatus lastEvaluatedAt(DateTime? lastEvaluatedAt);

  WindowStatus lowConfidence(bool? lowConfidence);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WindowStatus(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WindowStatus(...).copyWith(id: 12, name: "My name")
  /// ```
  WindowStatus call({
    WindowStatusStates states,
    int groups,
    bool? groupsTruncated,
    DateTime? learningUntil,
    DateTime? lastEvaluatedAt,
    bool? lowConfidence,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWindowStatus.copyWith(...)` or call `instanceOfWindowStatus.copyWith.fieldName(value)` for a single field.
class _$WindowStatusCWProxyImpl implements _$WindowStatusCWProxy {
  const _$WindowStatusCWProxyImpl(this._value);

  final WindowStatus _value;

  @override
  WindowStatus states(WindowStatusStates states) => call(states: states);

  @override
  WindowStatus groups(int groups) => call(groups: groups);

  @override
  WindowStatus groupsTruncated(bool? groupsTruncated) =>
      call(groupsTruncated: groupsTruncated);

  @override
  WindowStatus learningUntil(DateTime? learningUntil) =>
      call(learningUntil: learningUntil);

  @override
  WindowStatus lastEvaluatedAt(DateTime? lastEvaluatedAt) =>
      call(lastEvaluatedAt: lastEvaluatedAt);

  @override
  WindowStatus lowConfidence(bool? lowConfidence) =>
      call(lowConfidence: lowConfidence);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WindowStatus(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WindowStatus(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  WindowStatus call({
    Object? states = const $CopyWithPlaceholder(),
    Object? groups = const $CopyWithPlaceholder(),
    Object? groupsTruncated = const $CopyWithPlaceholder(),
    Object? learningUntil = const $CopyWithPlaceholder(),
    Object? lastEvaluatedAt = const $CopyWithPlaceholder(),
    Object? lowConfidence = const $CopyWithPlaceholder(),
  }) {
    return WindowStatus(
      states: states == const $CopyWithPlaceholder() || states == null
          ? _value.states
          // ignore: cast_nullable_to_non_nullable
          : states as WindowStatusStates,
      groups: groups == const $CopyWithPlaceholder() || groups == null
          ? _value.groups
          // ignore: cast_nullable_to_non_nullable
          : groups as int,
      groupsTruncated: groupsTruncated == const $CopyWithPlaceholder()
          ? _value.groupsTruncated
          // ignore: cast_nullable_to_non_nullable
          : groupsTruncated as bool?,
      learningUntil: learningUntil == const $CopyWithPlaceholder()
          ? _value.learningUntil
          // ignore: cast_nullable_to_non_nullable
          : learningUntil as DateTime?,
      lastEvaluatedAt: lastEvaluatedAt == const $CopyWithPlaceholder()
          ? _value.lastEvaluatedAt
          // ignore: cast_nullable_to_non_nullable
          : lastEvaluatedAt as DateTime?,
      lowConfidence: lowConfidence == const $CopyWithPlaceholder()
          ? _value.lowConfidence
          // ignore: cast_nullable_to_non_nullable
          : lowConfidence as bool?,
    );
  }
}

extension $WindowStatusCopyWith on WindowStatus {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWindowStatus.copyWith(...)` or `instanceOfWindowStatus.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WindowStatusCWProxy get copyWith => _$WindowStatusCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

WindowStatus _$WindowStatusFromJson(Map<String, dynamic> json) =>
    $checkedCreate('WindowStatus', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['states', 'groups']);
      final val = WindowStatus(
        states: $checkedConvert(
          'states',
          (v) => WindowStatusStates.fromJson(v as Map<String, dynamic>),
        ),
        groups: $checkedConvert('groups', (v) => (v as num).toInt()),
        groupsTruncated: $checkedConvert('groupsTruncated', (v) => v as bool?),
        learningUntil: $checkedConvert(
          'learningUntil',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        lastEvaluatedAt: $checkedConvert(
          'lastEvaluatedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        lowConfidence: $checkedConvert('lowConfidence', (v) => v as bool?),
      );
      return val;
    });

Map<String, dynamic> _$WindowStatusToJson(WindowStatus instance) =>
    <String, dynamic>{
      'states': instance.states.toJson(),
      'groups': instance.groups,
      'groupsTruncated': ?instance.groupsTruncated,
      'learningUntil': ?instance.learningUntil?.toIso8601String(),
      'lastEvaluatedAt': ?instance.lastEvaluatedAt?.toIso8601String(),
      'lowConfidence': ?instance.lowConfidence,
    };
