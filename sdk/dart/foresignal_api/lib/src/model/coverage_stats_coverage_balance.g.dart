// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'coverage_stats_coverage_balance.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CoverageStatsCoverageBalanceCWProxy {
  CoverageStatsCoverageBalance left(num left);

  CoverageStatsCoverageBalance center(num center);

  CoverageStatsCoverageBalance right(num right);

  CoverageStatsCoverageBalance unrated(num unrated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CoverageStatsCoverageBalance(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CoverageStatsCoverageBalance(...).copyWith(id: 12, name: "My name")
  /// ```
  CoverageStatsCoverageBalance call({
    num left,
    num center,
    num right,
    num unrated,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCoverageStatsCoverageBalance.copyWith(...)` or call `instanceOfCoverageStatsCoverageBalance.copyWith.fieldName(value)` for a single field.
class _$CoverageStatsCoverageBalanceCWProxyImpl
    implements _$CoverageStatsCoverageBalanceCWProxy {
  const _$CoverageStatsCoverageBalanceCWProxyImpl(this._value);

  final CoverageStatsCoverageBalance _value;

  @override
  CoverageStatsCoverageBalance left(num left) => call(left: left);

  @override
  CoverageStatsCoverageBalance center(num center) => call(center: center);

  @override
  CoverageStatsCoverageBalance right(num right) => call(right: right);

  @override
  CoverageStatsCoverageBalance unrated(num unrated) => call(unrated: unrated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CoverageStatsCoverageBalance(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CoverageStatsCoverageBalance(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CoverageStatsCoverageBalance call({
    Object? left = const $CopyWithPlaceholder(),
    Object? center = const $CopyWithPlaceholder(),
    Object? right = const $CopyWithPlaceholder(),
    Object? unrated = const $CopyWithPlaceholder(),
  }) {
    return CoverageStatsCoverageBalance(
      left: left == const $CopyWithPlaceholder() || left == null
          ? _value.left
          // ignore: cast_nullable_to_non_nullable
          : left as num,
      center: center == const $CopyWithPlaceholder() || center == null
          ? _value.center
          // ignore: cast_nullable_to_non_nullable
          : center as num,
      right: right == const $CopyWithPlaceholder() || right == null
          ? _value.right
          // ignore: cast_nullable_to_non_nullable
          : right as num,
      unrated: unrated == const $CopyWithPlaceholder() || unrated == null
          ? _value.unrated
          // ignore: cast_nullable_to_non_nullable
          : unrated as num,
    );
  }
}

extension $CoverageStatsCoverageBalanceCopyWith
    on CoverageStatsCoverageBalance {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCoverageStatsCoverageBalance.copyWith(...)` or `instanceOfCoverageStatsCoverageBalance.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CoverageStatsCoverageBalanceCWProxy get copyWith =>
      _$CoverageStatsCoverageBalanceCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CoverageStatsCoverageBalance _$CoverageStatsCoverageBalanceFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('CoverageStatsCoverageBalance', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['left', 'center', 'right', 'unrated']);
  final val = CoverageStatsCoverageBalance(
    left: $checkedConvert('left', (v) => v as num),
    center: $checkedConvert('center', (v) => v as num),
    right: $checkedConvert('right', (v) => v as num),
    unrated: $checkedConvert('unrated', (v) => v as num),
  );
  return val;
});

Map<String, dynamic> _$CoverageStatsCoverageBalanceToJson(
  CoverageStatsCoverageBalance instance,
) => <String, dynamic>{
  'left': instance.left,
  'center': instance.center,
  'right': instance.right,
  'unrated': instance.unrated,
};
