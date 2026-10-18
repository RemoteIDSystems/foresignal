// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'declared_observability_kinds_value.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DeclaredObservabilityKindsValueCWProxy {
  DeclaredObservabilityKindsValue area(
    DeclaredObservabilityKindsValueArea area,
  );

  DeclaredObservabilityKindsValue thresholds(
    List<DeclaredObservabilityKindsValueThresholdsInner>? thresholds,
  );

  DeclaredObservabilityKindsValue typicalLatency(String? typicalLatency);

  DeclaredObservabilityKindsValue notes(String? notes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DeclaredObservabilityKindsValue(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DeclaredObservabilityKindsValue(...).copyWith(id: 12, name: "My name")
  /// ```
  DeclaredObservabilityKindsValue call({
    DeclaredObservabilityKindsValueArea area,
    List<DeclaredObservabilityKindsValueThresholdsInner>? thresholds,
    String? typicalLatency,
    String? notes,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDeclaredObservabilityKindsValue.copyWith(...)` or call `instanceOfDeclaredObservabilityKindsValue.copyWith.fieldName(value)` for a single field.
class _$DeclaredObservabilityKindsValueCWProxyImpl
    implements _$DeclaredObservabilityKindsValueCWProxy {
  const _$DeclaredObservabilityKindsValueCWProxyImpl(this._value);

  final DeclaredObservabilityKindsValue _value;

  @override
  DeclaredObservabilityKindsValue area(
    DeclaredObservabilityKindsValueArea area,
  ) => call(area: area);

  @override
  DeclaredObservabilityKindsValue thresholds(
    List<DeclaredObservabilityKindsValueThresholdsInner>? thresholds,
  ) => call(thresholds: thresholds);

  @override
  DeclaredObservabilityKindsValue typicalLatency(String? typicalLatency) =>
      call(typicalLatency: typicalLatency);

  @override
  DeclaredObservabilityKindsValue notes(String? notes) => call(notes: notes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DeclaredObservabilityKindsValue(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DeclaredObservabilityKindsValue(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DeclaredObservabilityKindsValue call({
    Object? area = const $CopyWithPlaceholder(),
    Object? thresholds = const $CopyWithPlaceholder(),
    Object? typicalLatency = const $CopyWithPlaceholder(),
    Object? notes = const $CopyWithPlaceholder(),
  }) {
    return DeclaredObservabilityKindsValue(
      area: area == const $CopyWithPlaceholder() || area == null
          ? _value.area
          // ignore: cast_nullable_to_non_nullable
          : area as DeclaredObservabilityKindsValueArea,
      thresholds: thresholds == const $CopyWithPlaceholder()
          ? _value.thresholds
          // ignore: cast_nullable_to_non_nullable
          : thresholds as List<DeclaredObservabilityKindsValueThresholdsInner>?,
      typicalLatency: typicalLatency == const $CopyWithPlaceholder()
          ? _value.typicalLatency
          // ignore: cast_nullable_to_non_nullable
          : typicalLatency as String?,
      notes: notes == const $CopyWithPlaceholder()
          ? _value.notes
          // ignore: cast_nullable_to_non_nullable
          : notes as String?,
    );
  }
}

extension $DeclaredObservabilityKindsValueCopyWith
    on DeclaredObservabilityKindsValue {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDeclaredObservabilityKindsValue.copyWith(...)` or `instanceOfDeclaredObservabilityKindsValue.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DeclaredObservabilityKindsValueCWProxy get copyWith =>
      _$DeclaredObservabilityKindsValueCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DeclaredObservabilityKindsValue _$DeclaredObservabilityKindsValueFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('DeclaredObservabilityKindsValue', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['area']);
  final val = DeclaredObservabilityKindsValue(
    area: $checkedConvert(
      'area',
      (v) => DeclaredObservabilityKindsValueArea.fromJson(
        v as Map<String, dynamic>,
      ),
    ),
    thresholds: $checkedConvert(
      'thresholds',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => DeclaredObservabilityKindsValueThresholdsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    typicalLatency: $checkedConvert('typicalLatency', (v) => v as String?),
    notes: $checkedConvert('notes', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$DeclaredObservabilityKindsValueToJson(
  DeclaredObservabilityKindsValue instance,
) => <String, dynamic>{
  'area': instance.area.toJson(),
  'thresholds': ?instance.thresholds?.map((e) => e.toJson()).toList(),
  'typicalLatency': ?instance.typicalLatency,
  'notes': ?instance.notes,
};
