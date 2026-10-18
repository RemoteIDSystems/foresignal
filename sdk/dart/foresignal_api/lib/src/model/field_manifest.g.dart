// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'field_manifest.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$FieldManifestCWProxy {
  FieldManifest schemaVersion(FieldManifestSchemaVersionEnum schemaVersion);

  FieldManifest model(String model);

  FieldManifest cycle(FieldManifestCycle cycle);

  FieldManifest level(String level);

  FieldManifest units(FieldManifestUnitsEnum units);

  FieldManifest grid(FieldManifestGrid grid);

  FieldManifest stale(bool stale);

  FieldManifest unavailable(bool unavailable);

  FieldManifest reason(String? reason);

  FieldManifest components(List<FieldManifestComponentsInner> components);

  FieldManifest byteLength(int byteLength);

  FieldManifest sha256(String sha256);

  FieldManifest attribution(Attribution? attribution);

  FieldManifest dataUrl(String dataUrl);

  FieldManifest encodings(FieldEncodings? encodings);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FieldManifest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FieldManifest(...).copyWith(id: 12, name: "My name")
  /// ```
  FieldManifest call({
    FieldManifestSchemaVersionEnum schemaVersion,
    String model,
    FieldManifestCycle cycle,
    String level,
    FieldManifestUnitsEnum units,
    FieldManifestGrid grid,
    bool stale,
    bool unavailable,
    String? reason,
    List<FieldManifestComponentsInner> components,
    int byteLength,
    String sha256,
    Attribution? attribution,
    String dataUrl,
    FieldEncodings? encodings,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfFieldManifest.copyWith(...)` or call `instanceOfFieldManifest.copyWith.fieldName(value)` for a single field.
class _$FieldManifestCWProxyImpl implements _$FieldManifestCWProxy {
  const _$FieldManifestCWProxyImpl(this._value);

  final FieldManifest _value;

  @override
  FieldManifest schemaVersion(FieldManifestSchemaVersionEnum schemaVersion) =>
      call(schemaVersion: schemaVersion);

  @override
  FieldManifest model(String model) => call(model: model);

  @override
  FieldManifest cycle(FieldManifestCycle cycle) => call(cycle: cycle);

  @override
  FieldManifest level(String level) => call(level: level);

  @override
  FieldManifest units(FieldManifestUnitsEnum units) => call(units: units);

  @override
  FieldManifest grid(FieldManifestGrid grid) => call(grid: grid);

  @override
  FieldManifest stale(bool stale) => call(stale: stale);

  @override
  FieldManifest unavailable(bool unavailable) => call(unavailable: unavailable);

  @override
  FieldManifest reason(String? reason) => call(reason: reason);

  @override
  FieldManifest components(List<FieldManifestComponentsInner> components) =>
      call(components: components);

  @override
  FieldManifest byteLength(int byteLength) => call(byteLength: byteLength);

  @override
  FieldManifest sha256(String sha256) => call(sha256: sha256);

  @override
  FieldManifest attribution(Attribution? attribution) =>
      call(attribution: attribution);

  @override
  FieldManifest dataUrl(String dataUrl) => call(dataUrl: dataUrl);

  @override
  FieldManifest encodings(FieldEncodings? encodings) =>
      call(encodings: encodings);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FieldManifest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FieldManifest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  FieldManifest call({
    Object? schemaVersion = const $CopyWithPlaceholder(),
    Object? model = const $CopyWithPlaceholder(),
    Object? cycle = const $CopyWithPlaceholder(),
    Object? level = const $CopyWithPlaceholder(),
    Object? units = const $CopyWithPlaceholder(),
    Object? grid = const $CopyWithPlaceholder(),
    Object? stale = const $CopyWithPlaceholder(),
    Object? unavailable = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
    Object? components = const $CopyWithPlaceholder(),
    Object? byteLength = const $CopyWithPlaceholder(),
    Object? sha256 = const $CopyWithPlaceholder(),
    Object? attribution = const $CopyWithPlaceholder(),
    Object? dataUrl = const $CopyWithPlaceholder(),
    Object? encodings = const $CopyWithPlaceholder(),
  }) {
    return FieldManifest(
      schemaVersion:
          schemaVersion == const $CopyWithPlaceholder() || schemaVersion == null
          ? _value.schemaVersion
          // ignore: cast_nullable_to_non_nullable
          : schemaVersion as FieldManifestSchemaVersionEnum,
      model: model == const $CopyWithPlaceholder() || model == null
          ? _value.model
          // ignore: cast_nullable_to_non_nullable
          : model as String,
      cycle: cycle == const $CopyWithPlaceholder() || cycle == null
          ? _value.cycle
          // ignore: cast_nullable_to_non_nullable
          : cycle as FieldManifestCycle,
      level: level == const $CopyWithPlaceholder() || level == null
          ? _value.level
          // ignore: cast_nullable_to_non_nullable
          : level as String,
      units: units == const $CopyWithPlaceholder() || units == null
          ? _value.units
          // ignore: cast_nullable_to_non_nullable
          : units as FieldManifestUnitsEnum,
      grid: grid == const $CopyWithPlaceholder() || grid == null
          ? _value.grid
          // ignore: cast_nullable_to_non_nullable
          : grid as FieldManifestGrid,
      stale: stale == const $CopyWithPlaceholder() || stale == null
          ? _value.stale
          // ignore: cast_nullable_to_non_nullable
          : stale as bool,
      unavailable:
          unavailable == const $CopyWithPlaceholder() || unavailable == null
          ? _value.unavailable
          // ignore: cast_nullable_to_non_nullable
          : unavailable as bool,
      reason: reason == const $CopyWithPlaceholder()
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String?,
      components:
          components == const $CopyWithPlaceholder() || components == null
          ? _value.components
          // ignore: cast_nullable_to_non_nullable
          : components as List<FieldManifestComponentsInner>,
      byteLength:
          byteLength == const $CopyWithPlaceholder() || byteLength == null
          ? _value.byteLength
          // ignore: cast_nullable_to_non_nullable
          : byteLength as int,
      sha256: sha256 == const $CopyWithPlaceholder() || sha256 == null
          ? _value.sha256
          // ignore: cast_nullable_to_non_nullable
          : sha256 as String,
      attribution: attribution == const $CopyWithPlaceholder()
          ? _value.attribution
          // ignore: cast_nullable_to_non_nullable
          : attribution as Attribution?,
      dataUrl: dataUrl == const $CopyWithPlaceholder() || dataUrl == null
          ? _value.dataUrl
          // ignore: cast_nullable_to_non_nullable
          : dataUrl as String,
      encodings: encodings == const $CopyWithPlaceholder()
          ? _value.encodings
          // ignore: cast_nullable_to_non_nullable
          : encodings as FieldEncodings?,
    );
  }
}

extension $FieldManifestCopyWith on FieldManifest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfFieldManifest.copyWith(...)` or `instanceOfFieldManifest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$FieldManifestCWProxy get copyWith => _$FieldManifestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

FieldManifest _$FieldManifestFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('FieldManifest', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'schemaVersion',
      'model',
      'cycle',
      'level',
      'units',
      'grid',
      'stale',
      'unavailable',
      'components',
      'byteLength',
      'sha256',
      'dataUrl',
    ],
  );
  final val = FieldManifest(
    schemaVersion: $checkedConvert(
      'schemaVersion',
      (v) => $enumDecode(_$FieldManifestSchemaVersionEnumEnumMap, v),
    ),
    model: $checkedConvert('model', (v) => v as String),
    cycle: $checkedConvert(
      'cycle',
      (v) => FieldManifestCycle.fromJson(v as Map<String, dynamic>),
    ),
    level: $checkedConvert('level', (v) => v as String),
    units: $checkedConvert(
      'units',
      (v) => $enumDecode(_$FieldManifestUnitsEnumEnumMap, v),
    ),
    grid: $checkedConvert(
      'grid',
      (v) => FieldManifestGrid.fromJson(v as Map<String, dynamic>),
    ),
    stale: $checkedConvert('stale', (v) => v as bool),
    unavailable: $checkedConvert('unavailable', (v) => v as bool),
    reason: $checkedConvert('reason', (v) => v as String?),
    components: $checkedConvert(
      'components',
      (v) => (v as List<dynamic>)
          .map(
            (e) => FieldManifestComponentsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    byteLength: $checkedConvert('byteLength', (v) => (v as num).toInt()),
    sha256: $checkedConvert('sha256', (v) => v as String),
    attribution: $checkedConvert(
      'attribution',
      (v) => v == null ? null : Attribution.fromJson(v as Map<String, dynamic>),
    ),
    dataUrl: $checkedConvert('dataUrl', (v) => v as String),
    encodings: $checkedConvert(
      'encodings',
      (v) =>
          v == null ? null : FieldEncodings.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$FieldManifestToJson(FieldManifest instance) =>
    <String, dynamic>{
      'schemaVersion':
          _$FieldManifestSchemaVersionEnumEnumMap[instance.schemaVersion]!,
      'model': instance.model,
      'cycle': instance.cycle.toJson(),
      'level': instance.level,
      'units': _$FieldManifestUnitsEnumEnumMap[instance.units]!,
      'grid': instance.grid.toJson(),
      'stale': instance.stale,
      'unavailable': instance.unavailable,
      'reason': ?instance.reason,
      'components': instance.components.map((e) => e.toJson()).toList(),
      'byteLength': instance.byteLength,
      'sha256': instance.sha256,
      'attribution': ?instance.attribution?.toJson(),
      'dataUrl': instance.dataUrl,
      'encodings': ?instance.encodings?.toJson(),
    };

const _$FieldManifestSchemaVersionEnumEnumMap = {
  FieldManifestSchemaVersionEnum.number1: 1,
};

const _$FieldManifestUnitsEnumEnumMap = {FieldManifestUnitsEnum.mSlashS: 'm/s'};
