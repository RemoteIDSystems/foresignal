// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'precursor_spatial.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PrecursorSpatialCWProxy {
  PrecursorSpatial rule(PrecursorSpatialRuleEnum rule);

  PrecursorSpatial withinKm(num? withinKm);

  PrecursorSpatial regionLevel(PrecursorSpatialRegionLevelEnum? regionLevel);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorSpatial(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorSpatial(...).copyWith(id: 12, name: "My name")
  /// ```
  PrecursorSpatial call({
    PrecursorSpatialRuleEnum rule,
    num? withinKm,
    PrecursorSpatialRegionLevelEnum? regionLevel,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPrecursorSpatial.copyWith(...)` or call `instanceOfPrecursorSpatial.copyWith.fieldName(value)` for a single field.
class _$PrecursorSpatialCWProxyImpl implements _$PrecursorSpatialCWProxy {
  const _$PrecursorSpatialCWProxyImpl(this._value);

  final PrecursorSpatial _value;

  @override
  PrecursorSpatial rule(PrecursorSpatialRuleEnum rule) => call(rule: rule);

  @override
  PrecursorSpatial withinKm(num? withinKm) => call(withinKm: withinKm);

  @override
  PrecursorSpatial regionLevel(PrecursorSpatialRegionLevelEnum? regionLevel) =>
      call(regionLevel: regionLevel);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorSpatial(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorSpatial(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PrecursorSpatial call({
    Object? rule = const $CopyWithPlaceholder(),
    Object? withinKm = const $CopyWithPlaceholder(),
    Object? regionLevel = const $CopyWithPlaceholder(),
  }) {
    return PrecursorSpatial(
      rule: rule == const $CopyWithPlaceholder() || rule == null
          ? _value.rule
          // ignore: cast_nullable_to_non_nullable
          : rule as PrecursorSpatialRuleEnum,
      withinKm: withinKm == const $CopyWithPlaceholder()
          ? _value.withinKm
          // ignore: cast_nullable_to_non_nullable
          : withinKm as num?,
      regionLevel: regionLevel == const $CopyWithPlaceholder()
          ? _value.regionLevel
          // ignore: cast_nullable_to_non_nullable
          : regionLevel as PrecursorSpatialRegionLevelEnum?,
    );
  }
}

extension $PrecursorSpatialCopyWith on PrecursorSpatial {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPrecursorSpatial.copyWith(...)` or `instanceOfPrecursorSpatial.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PrecursorSpatialCWProxy get copyWith => _$PrecursorSpatialCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PrecursorSpatial _$PrecursorSpatialFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PrecursorSpatial', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['rule']);
      final val = PrecursorSpatial(
        rule: $checkedConvert(
          'rule',
          (v) => $enumDecode(_$PrecursorSpatialRuleEnumEnumMap, v),
        ),
        withinKm: $checkedConvert('withinKm', (v) => v as num?),
        regionLevel: $checkedConvert(
          'regionLevel',
          (v) =>
              $enumDecodeNullable(_$PrecursorSpatialRegionLevelEnumEnumMap, v),
        ),
      );
      return val;
    });

Map<String, dynamic> _$PrecursorSpatialToJson(PrecursorSpatial instance) =>
    <String, dynamic>{
      'rule': _$PrecursorSpatialRuleEnumEnumMap[instance.rule]!,
      'withinKm': ?instance.withinKm,
      'regionLevel':
          ?_$PrecursorSpatialRegionLevelEnumEnumMap[instance.regionLevel],
    };

const _$PrecursorSpatialRuleEnumEnumMap = {
  PrecursorSpatialRuleEnum.sameEntity: 'sameEntity',
  PrecursorSpatialRuleEnum.withinKm: 'withinKm',
  PrecursorSpatialRuleEnum.sameRegion: 'sameRegion',
  PrecursorSpatialRuleEnum.none: 'none',
};

const _$PrecursorSpatialRegionLevelEnumEnumMap = {
  PrecursorSpatialRegionLevelEnum.country: 'country',
  PrecursorSpatialRegionLevelEnum.admin1: 'admin1',
};
