// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'stats_grid.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StatsGridCWProxy {
  StatsGrid type(StatsGridTypeEnum type);

  StatsGrid cellSystem(StatsGridCellSystemEnum cellSystem);

  StatsGrid res(int? res);

  StatsGrid sizeKm(num? sizeKm);

  StatsGrid measure(String? measure);

  StatsGrid backend(StatsGridBackendEnum? backend);

  StatsGrid features(List<StatsGridFeaturesInner> features);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StatsGrid(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StatsGrid(...).copyWith(id: 12, name: "My name")
  /// ```
  StatsGrid call({
    StatsGridTypeEnum type,
    StatsGridCellSystemEnum cellSystem,
    int? res,
    num? sizeKm,
    String? measure,
    StatsGridBackendEnum? backend,
    List<StatsGridFeaturesInner> features,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStatsGrid.copyWith(...)` or call `instanceOfStatsGrid.copyWith.fieldName(value)` for a single field.
class _$StatsGridCWProxyImpl implements _$StatsGridCWProxy {
  const _$StatsGridCWProxyImpl(this._value);

  final StatsGrid _value;

  @override
  StatsGrid type(StatsGridTypeEnum type) => call(type: type);

  @override
  StatsGrid cellSystem(StatsGridCellSystemEnum cellSystem) =>
      call(cellSystem: cellSystem);

  @override
  StatsGrid res(int? res) => call(res: res);

  @override
  StatsGrid sizeKm(num? sizeKm) => call(sizeKm: sizeKm);

  @override
  StatsGrid measure(String? measure) => call(measure: measure);

  @override
  StatsGrid backend(StatsGridBackendEnum? backend) => call(backend: backend);

  @override
  StatsGrid features(List<StatsGridFeaturesInner> features) =>
      call(features: features);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StatsGrid(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StatsGrid(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StatsGrid call({
    Object? type = const $CopyWithPlaceholder(),
    Object? cellSystem = const $CopyWithPlaceholder(),
    Object? res = const $CopyWithPlaceholder(),
    Object? sizeKm = const $CopyWithPlaceholder(),
    Object? measure = const $CopyWithPlaceholder(),
    Object? backend = const $CopyWithPlaceholder(),
    Object? features = const $CopyWithPlaceholder(),
  }) {
    return StatsGrid(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as StatsGridTypeEnum,
      cellSystem:
          cellSystem == const $CopyWithPlaceholder() || cellSystem == null
          ? _value.cellSystem
          // ignore: cast_nullable_to_non_nullable
          : cellSystem as StatsGridCellSystemEnum,
      res: res == const $CopyWithPlaceholder()
          ? _value.res
          // ignore: cast_nullable_to_non_nullable
          : res as int?,
      sizeKm: sizeKm == const $CopyWithPlaceholder()
          ? _value.sizeKm
          // ignore: cast_nullable_to_non_nullable
          : sizeKm as num?,
      measure: measure == const $CopyWithPlaceholder()
          ? _value.measure
          // ignore: cast_nullable_to_non_nullable
          : measure as String?,
      backend: backend == const $CopyWithPlaceholder()
          ? _value.backend
          // ignore: cast_nullable_to_non_nullable
          : backend as StatsGridBackendEnum?,
      features: features == const $CopyWithPlaceholder() || features == null
          ? _value.features
          // ignore: cast_nullable_to_non_nullable
          : features as List<StatsGridFeaturesInner>,
    );
  }
}

extension $StatsGridCopyWith on StatsGrid {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStatsGrid.copyWith(...)` or `instanceOfStatsGrid.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StatsGridCWProxy get copyWith => _$StatsGridCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StatsGrid _$StatsGridFromJson(Map<String, dynamic> json) =>
    $checkedCreate('StatsGrid', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['type', 'cellSystem', 'features']);
      final val = StatsGrid(
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$StatsGridTypeEnumEnumMap, v),
        ),
        cellSystem: $checkedConvert(
          'cellSystem',
          (v) => $enumDecode(_$StatsGridCellSystemEnumEnumMap, v),
        ),
        res: $checkedConvert('res', (v) => (v as num?)?.toInt()),
        sizeKm: $checkedConvert('sizeKm', (v) => v as num?),
        measure: $checkedConvert('measure', (v) => v as String?),
        backend: $checkedConvert(
          'backend',
          (v) => $enumDecodeNullable(_$StatsGridBackendEnumEnumMap, v),
        ),
        features: $checkedConvert(
          'features',
          (v) => (v as List<dynamic>)
              .map(
                (e) =>
                    StatsGridFeaturesInner.fromJson(e as Map<String, dynamic>),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$StatsGridToJson(StatsGrid instance) => <String, dynamic>{
  'type': _$StatsGridTypeEnumEnumMap[instance.type]!,
  'cellSystem': _$StatsGridCellSystemEnumEnumMap[instance.cellSystem]!,
  'res': ?instance.res,
  'sizeKm': ?instance.sizeKm,
  'measure': ?instance.measure,
  'backend': ?_$StatsGridBackendEnumEnumMap[instance.backend],
  'features': instance.features.map((e) => e.toJson()).toList(),
};

const _$StatsGridTypeEnumEnumMap = {
  StatsGridTypeEnum.featureCollection: 'FeatureCollection',
};

const _$StatsGridCellSystemEnumEnumMap = {
  StatsGridCellSystemEnum.h3: 'h3',
  StatsGridCellSystemEnum.hex3857: 'hex3857',
};

const _$StatsGridBackendEnumEnumMap = {
  StatsGridBackendEnum.rollup: 'rollup',
  StatsGridBackendEnum.raw: 'raw',
};
