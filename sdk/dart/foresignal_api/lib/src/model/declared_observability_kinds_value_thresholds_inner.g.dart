// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'declared_observability_kinds_value_thresholds_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DeclaredObservabilityKindsValueThresholdsInnerCWProxy {
  DeclaredObservabilityKindsValueThresholdsInner field(String field);

  DeclaredObservabilityKindsValueThresholdsInner min(num min);

  DeclaredObservabilityKindsValueThresholdsInner area(String? area);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DeclaredObservabilityKindsValueThresholdsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DeclaredObservabilityKindsValueThresholdsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  DeclaredObservabilityKindsValueThresholdsInner call({
    String field,
    num min,
    String? area,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDeclaredObservabilityKindsValueThresholdsInner.copyWith(...)` or call `instanceOfDeclaredObservabilityKindsValueThresholdsInner.copyWith.fieldName(value)` for a single field.
class _$DeclaredObservabilityKindsValueThresholdsInnerCWProxyImpl
    implements _$DeclaredObservabilityKindsValueThresholdsInnerCWProxy {
  const _$DeclaredObservabilityKindsValueThresholdsInnerCWProxyImpl(
    this._value,
  );

  final DeclaredObservabilityKindsValueThresholdsInner _value;

  @override
  DeclaredObservabilityKindsValueThresholdsInner field(String field) =>
      call(field: field);

  @override
  DeclaredObservabilityKindsValueThresholdsInner min(num min) => call(min: min);

  @override
  DeclaredObservabilityKindsValueThresholdsInner area(String? area) =>
      call(area: area);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DeclaredObservabilityKindsValueThresholdsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DeclaredObservabilityKindsValueThresholdsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DeclaredObservabilityKindsValueThresholdsInner call({
    Object? field = const $CopyWithPlaceholder(),
    Object? min = const $CopyWithPlaceholder(),
    Object? area = const $CopyWithPlaceholder(),
  }) {
    return DeclaredObservabilityKindsValueThresholdsInner(
      field: field == const $CopyWithPlaceholder() || field == null
          ? _value.field
          // ignore: cast_nullable_to_non_nullable
          : field as String,
      min: min == const $CopyWithPlaceholder() || min == null
          ? _value.min
          // ignore: cast_nullable_to_non_nullable
          : min as num,
      area: area == const $CopyWithPlaceholder()
          ? _value.area
          // ignore: cast_nullable_to_non_nullable
          : area as String?,
    );
  }
}

extension $DeclaredObservabilityKindsValueThresholdsInnerCopyWith
    on DeclaredObservabilityKindsValueThresholdsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDeclaredObservabilityKindsValueThresholdsInner.copyWith(...)` or `instanceOfDeclaredObservabilityKindsValueThresholdsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DeclaredObservabilityKindsValueThresholdsInnerCWProxy get copyWith =>
      _$DeclaredObservabilityKindsValueThresholdsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DeclaredObservabilityKindsValueThresholdsInner
_$DeclaredObservabilityKindsValueThresholdsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('DeclaredObservabilityKindsValueThresholdsInner', json, (
  $checkedConvert,
) {
  $checkKeys(json, requiredKeys: const ['field', 'min']);
  final val = DeclaredObservabilityKindsValueThresholdsInner(
    field: $checkedConvert('field', (v) => v as String),
    min: $checkedConvert('min', (v) => v as num),
    area: $checkedConvert('area', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$DeclaredObservabilityKindsValueThresholdsInnerToJson(
  DeclaredObservabilityKindsValueThresholdsInner instance,
) => <String, dynamic>{
  'field': instance.field,
  'min': instance.min,
  'area': ?instance.area,
};
