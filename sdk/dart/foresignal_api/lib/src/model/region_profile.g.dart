// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'region_profile.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RegionProfileCWProxy {
  RegionProfile regionCode(String regionCode);

  RegionProfile reportingDensity(num reportingDensity);

  RegionProfile densityDecile(int densityDecile);

  RegionProfile computedAt(DateTime computedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RegionProfile(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RegionProfile(...).copyWith(id: 12, name: "My name")
  /// ```
  RegionProfile call({
    String regionCode,
    num reportingDensity,
    int densityDecile,
    DateTime computedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRegionProfile.copyWith(...)` or call `instanceOfRegionProfile.copyWith.fieldName(value)` for a single field.
class _$RegionProfileCWProxyImpl implements _$RegionProfileCWProxy {
  const _$RegionProfileCWProxyImpl(this._value);

  final RegionProfile _value;

  @override
  RegionProfile regionCode(String regionCode) => call(regionCode: regionCode);

  @override
  RegionProfile reportingDensity(num reportingDensity) =>
      call(reportingDensity: reportingDensity);

  @override
  RegionProfile densityDecile(int densityDecile) =>
      call(densityDecile: densityDecile);

  @override
  RegionProfile computedAt(DateTime computedAt) => call(computedAt: computedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RegionProfile(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RegionProfile(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RegionProfile call({
    Object? regionCode = const $CopyWithPlaceholder(),
    Object? reportingDensity = const $CopyWithPlaceholder(),
    Object? densityDecile = const $CopyWithPlaceholder(),
    Object? computedAt = const $CopyWithPlaceholder(),
  }) {
    return RegionProfile(
      regionCode:
          regionCode == const $CopyWithPlaceholder() || regionCode == null
          ? _value.regionCode
          // ignore: cast_nullable_to_non_nullable
          : regionCode as String,
      reportingDensity:
          reportingDensity == const $CopyWithPlaceholder() ||
              reportingDensity == null
          ? _value.reportingDensity
          // ignore: cast_nullable_to_non_nullable
          : reportingDensity as num,
      densityDecile:
          densityDecile == const $CopyWithPlaceholder() || densityDecile == null
          ? _value.densityDecile
          // ignore: cast_nullable_to_non_nullable
          : densityDecile as int,
      computedAt:
          computedAt == const $CopyWithPlaceholder() || computedAt == null
          ? _value.computedAt
          // ignore: cast_nullable_to_non_nullable
          : computedAt as DateTime,
    );
  }
}

extension $RegionProfileCopyWith on RegionProfile {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRegionProfile.copyWith(...)` or `instanceOfRegionProfile.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RegionProfileCWProxy get copyWith => _$RegionProfileCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RegionProfile _$RegionProfileFromJson(Map<String, dynamic> json) =>
    $checkedCreate('RegionProfile', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'regionCode',
          'reportingDensity',
          'densityDecile',
          'computedAt',
        ],
      );
      final val = RegionProfile(
        regionCode: $checkedConvert('regionCode', (v) => v as String),
        reportingDensity: $checkedConvert('reportingDensity', (v) => v as num),
        densityDecile: $checkedConvert(
          'densityDecile',
          (v) => (v as num).toInt(),
        ),
        computedAt: $checkedConvert(
          'computedAt',
          (v) => DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$RegionProfileToJson(RegionProfile instance) =>
    <String, dynamic>{
      'regionCode': instance.regionCode,
      'reportingDensity': instance.reportingDensity,
      'densityDecile': instance.densityDecile,
      'computedAt': instance.computedAt.toIso8601String(),
    };
