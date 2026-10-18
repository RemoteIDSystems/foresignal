// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'field_threshold.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$FieldThresholdCWProxy {
  FieldThreshold field(String field);

  FieldThreshold delta(num delta);

  FieldThreshold angular(bool? angular);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FieldThreshold(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FieldThreshold(...).copyWith(id: 12, name: "My name")
  /// ```
  FieldThreshold call({String field, num delta, bool? angular});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfFieldThreshold.copyWith(...)` or call `instanceOfFieldThreshold.copyWith.fieldName(value)` for a single field.
class _$FieldThresholdCWProxyImpl implements _$FieldThresholdCWProxy {
  const _$FieldThresholdCWProxyImpl(this._value);

  final FieldThreshold _value;

  @override
  FieldThreshold field(String field) => call(field: field);

  @override
  FieldThreshold delta(num delta) => call(delta: delta);

  @override
  FieldThreshold angular(bool? angular) => call(angular: angular);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FieldThreshold(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FieldThreshold(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  FieldThreshold call({
    Object? field = const $CopyWithPlaceholder(),
    Object? delta = const $CopyWithPlaceholder(),
    Object? angular = const $CopyWithPlaceholder(),
  }) {
    return FieldThreshold(
      field: field == const $CopyWithPlaceholder() || field == null
          ? _value.field
          // ignore: cast_nullable_to_non_nullable
          : field as String,
      delta: delta == const $CopyWithPlaceholder() || delta == null
          ? _value.delta
          // ignore: cast_nullable_to_non_nullable
          : delta as num,
      angular: angular == const $CopyWithPlaceholder()
          ? _value.angular
          // ignore: cast_nullable_to_non_nullable
          : angular as bool?,
    );
  }
}

extension $FieldThresholdCopyWith on FieldThreshold {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfFieldThreshold.copyWith(...)` or `instanceOfFieldThreshold.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$FieldThresholdCWProxy get copyWith => _$FieldThresholdCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

FieldThreshold _$FieldThresholdFromJson(Map<String, dynamic> json) =>
    $checkedCreate('FieldThreshold', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['field', 'delta']);
      final val = FieldThreshold(
        field: $checkedConvert('field', (v) => v as String),
        delta: $checkedConvert('delta', (v) => v as num),
        angular: $checkedConvert('angular', (v) => v as bool? ?? false),
      );
      return val;
    });

Map<String, dynamic> _$FieldThresholdToJson(FieldThreshold instance) =>
    <String, dynamic>{
      'field': instance.field,
      'delta': instance.delta,
      'angular': ?instance.angular,
    };
