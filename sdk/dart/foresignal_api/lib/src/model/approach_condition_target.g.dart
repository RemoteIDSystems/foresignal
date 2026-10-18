// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'approach_condition_target.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ApproachConditionTargetCWProxy {
  ApproachConditionTarget placeId(String? placeId);

  ApproachConditionTarget point(Geometry? point);

  ApproachConditionTarget record(String? record);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ApproachConditionTarget(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ApproachConditionTarget(...).copyWith(id: 12, name: "My name")
  /// ```
  ApproachConditionTarget call({
    String? placeId,
    Geometry? point,
    String? record,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfApproachConditionTarget.copyWith(...)` or call `instanceOfApproachConditionTarget.copyWith.fieldName(value)` for a single field.
class _$ApproachConditionTargetCWProxyImpl
    implements _$ApproachConditionTargetCWProxy {
  const _$ApproachConditionTargetCWProxyImpl(this._value);

  final ApproachConditionTarget _value;

  @override
  ApproachConditionTarget placeId(String? placeId) => call(placeId: placeId);

  @override
  ApproachConditionTarget point(Geometry? point) => call(point: point);

  @override
  ApproachConditionTarget record(String? record) => call(record: record);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ApproachConditionTarget(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ApproachConditionTarget(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ApproachConditionTarget call({
    Object? placeId = const $CopyWithPlaceholder(),
    Object? point = const $CopyWithPlaceholder(),
    Object? record = const $CopyWithPlaceholder(),
  }) {
    return ApproachConditionTarget(
      placeId: placeId == const $CopyWithPlaceholder()
          ? _value.placeId
          // ignore: cast_nullable_to_non_nullable
          : placeId as String?,
      point: point == const $CopyWithPlaceholder()
          ? _value.point
          // ignore: cast_nullable_to_non_nullable
          : point as Geometry?,
      record: record == const $CopyWithPlaceholder()
          ? _value.record
          // ignore: cast_nullable_to_non_nullable
          : record as String?,
    );
  }
}

extension $ApproachConditionTargetCopyWith on ApproachConditionTarget {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfApproachConditionTarget.copyWith(...)` or `instanceOfApproachConditionTarget.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ApproachConditionTargetCWProxy get copyWith =>
      _$ApproachConditionTargetCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ApproachConditionTarget _$ApproachConditionTargetFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ApproachConditionTarget', json, ($checkedConvert) {
  final val = ApproachConditionTarget(
    placeId: $checkedConvert('placeId', (v) => v as String?),
    point: $checkedConvert(
      'point',
      (v) => v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
    ),
    record: $checkedConvert('record', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ApproachConditionTargetToJson(
  ApproachConditionTarget instance,
) => <String, dynamic>{
  'placeId': ?instance.placeId,
  'point': ?instance.point?.toJson(),
  'record': ?instance.record,
};
