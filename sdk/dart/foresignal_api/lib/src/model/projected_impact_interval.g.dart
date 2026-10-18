// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'projected_impact_interval.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ProjectedImpactIntervalCWProxy {
  ProjectedImpactInterval lo(num lo);

  ProjectedImpactInterval hi(num hi);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProjectedImpactInterval(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProjectedImpactInterval(...).copyWith(id: 12, name: "My name")
  /// ```
  ProjectedImpactInterval call({num lo, num hi});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfProjectedImpactInterval.copyWith(...)` or call `instanceOfProjectedImpactInterval.copyWith.fieldName(value)` for a single field.
class _$ProjectedImpactIntervalCWProxyImpl
    implements _$ProjectedImpactIntervalCWProxy {
  const _$ProjectedImpactIntervalCWProxyImpl(this._value);

  final ProjectedImpactInterval _value;

  @override
  ProjectedImpactInterval lo(num lo) => call(lo: lo);

  @override
  ProjectedImpactInterval hi(num hi) => call(hi: hi);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProjectedImpactInterval(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProjectedImpactInterval(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ProjectedImpactInterval call({
    Object? lo = const $CopyWithPlaceholder(),
    Object? hi = const $CopyWithPlaceholder(),
  }) {
    return ProjectedImpactInterval(
      lo: lo == const $CopyWithPlaceholder() || lo == null
          ? _value.lo
          // ignore: cast_nullable_to_non_nullable
          : lo as num,
      hi: hi == const $CopyWithPlaceholder() || hi == null
          ? _value.hi
          // ignore: cast_nullable_to_non_nullable
          : hi as num,
    );
  }
}

extension $ProjectedImpactIntervalCopyWith on ProjectedImpactInterval {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfProjectedImpactInterval.copyWith(...)` or `instanceOfProjectedImpactInterval.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ProjectedImpactIntervalCWProxy get copyWith =>
      _$ProjectedImpactIntervalCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ProjectedImpactInterval _$ProjectedImpactIntervalFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ProjectedImpactInterval', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['lo', 'hi']);
  final val = ProjectedImpactInterval(
    lo: $checkedConvert('lo', (v) => v as num),
    hi: $checkedConvert('hi', (v) => v as num),
  );
  return val;
});

Map<String, dynamic> _$ProjectedImpactIntervalToJson(
  ProjectedImpactInterval instance,
) => <String, dynamic>{'lo': instance.lo, 'hi': instance.hi};
