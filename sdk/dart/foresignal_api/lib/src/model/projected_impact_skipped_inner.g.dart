// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'projected_impact_skipped_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ProjectedImpactSkippedInnerCWProxy {
  ProjectedImpactSkippedInner impactFamily(String impactFamily);

  ProjectedImpactSkippedInner regionClass(String? regionClass);

  ProjectedImpactSkippedInner n(int? n);

  ProjectedImpactSkippedInner reason(String reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProjectedImpactSkippedInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProjectedImpactSkippedInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ProjectedImpactSkippedInner call({
    String impactFamily,
    String? regionClass,
    int? n,
    String reason,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfProjectedImpactSkippedInner.copyWith(...)` or call `instanceOfProjectedImpactSkippedInner.copyWith.fieldName(value)` for a single field.
class _$ProjectedImpactSkippedInnerCWProxyImpl
    implements _$ProjectedImpactSkippedInnerCWProxy {
  const _$ProjectedImpactSkippedInnerCWProxyImpl(this._value);

  final ProjectedImpactSkippedInner _value;

  @override
  ProjectedImpactSkippedInner impactFamily(String impactFamily) =>
      call(impactFamily: impactFamily);

  @override
  ProjectedImpactSkippedInner regionClass(String? regionClass) =>
      call(regionClass: regionClass);

  @override
  ProjectedImpactSkippedInner n(int? n) => call(n: n);

  @override
  ProjectedImpactSkippedInner reason(String reason) => call(reason: reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProjectedImpactSkippedInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProjectedImpactSkippedInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ProjectedImpactSkippedInner call({
    Object? impactFamily = const $CopyWithPlaceholder(),
    Object? regionClass = const $CopyWithPlaceholder(),
    Object? n = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
  }) {
    return ProjectedImpactSkippedInner(
      impactFamily:
          impactFamily == const $CopyWithPlaceholder() || impactFamily == null
          ? _value.impactFamily
          // ignore: cast_nullable_to_non_nullable
          : impactFamily as String,
      regionClass: regionClass == const $CopyWithPlaceholder()
          ? _value.regionClass
          // ignore: cast_nullable_to_non_nullable
          : regionClass as String?,
      n: n == const $CopyWithPlaceholder()
          ? _value.n
          // ignore: cast_nullable_to_non_nullable
          : n as int?,
      reason: reason == const $CopyWithPlaceholder() || reason == null
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String,
    );
  }
}

extension $ProjectedImpactSkippedInnerCopyWith on ProjectedImpactSkippedInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfProjectedImpactSkippedInner.copyWith(...)` or `instanceOfProjectedImpactSkippedInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ProjectedImpactSkippedInnerCWProxy get copyWith =>
      _$ProjectedImpactSkippedInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ProjectedImpactSkippedInner _$ProjectedImpactSkippedInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ProjectedImpactSkippedInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['impactFamily', 'reason']);
  final val = ProjectedImpactSkippedInner(
    impactFamily: $checkedConvert('impactFamily', (v) => v as String),
    regionClass: $checkedConvert('regionClass', (v) => v as String?),
    n: $checkedConvert('n', (v) => (v as num?)?.toInt()),
    reason: $checkedConvert('reason', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$ProjectedImpactSkippedInnerToJson(
  ProjectedImpactSkippedInner instance,
) => <String, dynamic>{
  'impactFamily': instance.impactFamily,
  'regionClass': ?instance.regionClass,
  'n': ?instance.n,
  'reason': instance.reason,
};
