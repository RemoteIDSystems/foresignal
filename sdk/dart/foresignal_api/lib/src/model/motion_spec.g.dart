// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'motion_spec.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MotionSpecCWProxy {
  MotionSpec speedField(String speedField);

  MotionSpec speedUnit(String speedUnit);

  MotionSpec courseField(String? courseField);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MotionSpec(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MotionSpec(...).copyWith(id: 12, name: "My name")
  /// ```
  MotionSpec call({String speedField, String speedUnit, String? courseField});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMotionSpec.copyWith(...)` or call `instanceOfMotionSpec.copyWith.fieldName(value)` for a single field.
class _$MotionSpecCWProxyImpl implements _$MotionSpecCWProxy {
  const _$MotionSpecCWProxyImpl(this._value);

  final MotionSpec _value;

  @override
  MotionSpec speedField(String speedField) => call(speedField: speedField);

  @override
  MotionSpec speedUnit(String speedUnit) => call(speedUnit: speedUnit);

  @override
  MotionSpec courseField(String? courseField) => call(courseField: courseField);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MotionSpec(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MotionSpec(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MotionSpec call({
    Object? speedField = const $CopyWithPlaceholder(),
    Object? speedUnit = const $CopyWithPlaceholder(),
    Object? courseField = const $CopyWithPlaceholder(),
  }) {
    return MotionSpec(
      speedField:
          speedField == const $CopyWithPlaceholder() || speedField == null
          ? _value.speedField
          // ignore: cast_nullable_to_non_nullable
          : speedField as String,
      speedUnit: speedUnit == const $CopyWithPlaceholder() || speedUnit == null
          ? _value.speedUnit
          // ignore: cast_nullable_to_non_nullable
          : speedUnit as String,
      courseField: courseField == const $CopyWithPlaceholder()
          ? _value.courseField
          // ignore: cast_nullable_to_non_nullable
          : courseField as String?,
    );
  }
}

extension $MotionSpecCopyWith on MotionSpec {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMotionSpec.copyWith(...)` or `instanceOfMotionSpec.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MotionSpecCWProxy get copyWith => _$MotionSpecCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MotionSpec _$MotionSpecFromJson(Map<String, dynamic> json) =>
    $checkedCreate('MotionSpec', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['speedField', 'speedUnit']);
      final val = MotionSpec(
        speedField: $checkedConvert('speedField', (v) => v as String),
        speedUnit: $checkedConvert('speedUnit', (v) => v as String),
        courseField: $checkedConvert('courseField', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$MotionSpecToJson(MotionSpec instance) =>
    <String, dynamic>{
      'speedField': instance.speedField,
      'speedUnit': instance.speedUnit,
      'courseField': ?instance.courseField,
    };
