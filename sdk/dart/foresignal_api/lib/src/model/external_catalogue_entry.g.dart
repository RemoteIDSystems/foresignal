// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'external_catalogue_entry.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExternalCatalogueEntryCWProxy {
  ExternalCatalogueEntry provider(String? provider);

  ExternalCatalogueEntry layer(String? layer);

  ExternalCatalogueEntry protocol(ExternalCatalogueEntryProtocolEnum? protocol);

  ExternalCatalogueEntry externalTemplate(String externalTemplate);

  ExternalCatalogueEntry tilingScheme(String tilingScheme);

  ExternalCatalogueEntry tileSize(int tileSize);

  ExternalCatalogueEntry maxNativeZoom(int maxNativeZoom);

  ExternalCatalogueEntry format(ExternalCatalogueEntryFormatEnum? format);

  ExternalCatalogueEntry bounds(List<num> bounds);

  ExternalCatalogueEntry timeDimension(
    ExternalCatalogueEntryTimeDimension? timeDimension,
  );

  ExternalCatalogueEntry cors(String? cors);

  ExternalCatalogueEntry attribution(Attribution attribution);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExternalCatalogueEntry(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExternalCatalogueEntry(...).copyWith(id: 12, name: "My name")
  /// ```
  ExternalCatalogueEntry call({
    String? provider,
    String? layer,
    ExternalCatalogueEntryProtocolEnum? protocol,
    String externalTemplate,
    String tilingScheme,
    int tileSize,
    int maxNativeZoom,
    ExternalCatalogueEntryFormatEnum? format,
    List<num> bounds,
    ExternalCatalogueEntryTimeDimension? timeDimension,
    String? cors,
    Attribution attribution,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExternalCatalogueEntry.copyWith(...)` or call `instanceOfExternalCatalogueEntry.copyWith.fieldName(value)` for a single field.
class _$ExternalCatalogueEntryCWProxyImpl
    implements _$ExternalCatalogueEntryCWProxy {
  const _$ExternalCatalogueEntryCWProxyImpl(this._value);

  final ExternalCatalogueEntry _value;

  @override
  ExternalCatalogueEntry provider(String? provider) => call(provider: provider);

  @override
  ExternalCatalogueEntry layer(String? layer) => call(layer: layer);

  @override
  ExternalCatalogueEntry protocol(
    ExternalCatalogueEntryProtocolEnum? protocol,
  ) => call(protocol: protocol);

  @override
  ExternalCatalogueEntry externalTemplate(String externalTemplate) =>
      call(externalTemplate: externalTemplate);

  @override
  ExternalCatalogueEntry tilingScheme(String tilingScheme) =>
      call(tilingScheme: tilingScheme);

  @override
  ExternalCatalogueEntry tileSize(int tileSize) => call(tileSize: tileSize);

  @override
  ExternalCatalogueEntry maxNativeZoom(int maxNativeZoom) =>
      call(maxNativeZoom: maxNativeZoom);

  @override
  ExternalCatalogueEntry format(ExternalCatalogueEntryFormatEnum? format) =>
      call(format: format);

  @override
  ExternalCatalogueEntry bounds(List<num> bounds) => call(bounds: bounds);

  @override
  ExternalCatalogueEntry timeDimension(
    ExternalCatalogueEntryTimeDimension? timeDimension,
  ) => call(timeDimension: timeDimension);

  @override
  ExternalCatalogueEntry cors(String? cors) => call(cors: cors);

  @override
  ExternalCatalogueEntry attribution(Attribution attribution) =>
      call(attribution: attribution);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExternalCatalogueEntry(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExternalCatalogueEntry(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExternalCatalogueEntry call({
    Object? provider = const $CopyWithPlaceholder(),
    Object? layer = const $CopyWithPlaceholder(),
    Object? protocol = const $CopyWithPlaceholder(),
    Object? externalTemplate = const $CopyWithPlaceholder(),
    Object? tilingScheme = const $CopyWithPlaceholder(),
    Object? tileSize = const $CopyWithPlaceholder(),
    Object? maxNativeZoom = const $CopyWithPlaceholder(),
    Object? format = const $CopyWithPlaceholder(),
    Object? bounds = const $CopyWithPlaceholder(),
    Object? timeDimension = const $CopyWithPlaceholder(),
    Object? cors = const $CopyWithPlaceholder(),
    Object? attribution = const $CopyWithPlaceholder(),
  }) {
    return ExternalCatalogueEntry(
      provider: provider == const $CopyWithPlaceholder()
          ? _value.provider
          // ignore: cast_nullable_to_non_nullable
          : provider as String?,
      layer: layer == const $CopyWithPlaceholder()
          ? _value.layer
          // ignore: cast_nullable_to_non_nullable
          : layer as String?,
      protocol: protocol == const $CopyWithPlaceholder()
          ? _value.protocol
          // ignore: cast_nullable_to_non_nullable
          : protocol as ExternalCatalogueEntryProtocolEnum?,
      externalTemplate:
          externalTemplate == const $CopyWithPlaceholder() ||
              externalTemplate == null
          ? _value.externalTemplate
          // ignore: cast_nullable_to_non_nullable
          : externalTemplate as String,
      tilingScheme:
          tilingScheme == const $CopyWithPlaceholder() || tilingScheme == null
          ? _value.tilingScheme
          // ignore: cast_nullable_to_non_nullable
          : tilingScheme as String,
      tileSize: tileSize == const $CopyWithPlaceholder() || tileSize == null
          ? _value.tileSize
          // ignore: cast_nullable_to_non_nullable
          : tileSize as int,
      maxNativeZoom:
          maxNativeZoom == const $CopyWithPlaceholder() || maxNativeZoom == null
          ? _value.maxNativeZoom
          // ignore: cast_nullable_to_non_nullable
          : maxNativeZoom as int,
      format: format == const $CopyWithPlaceholder()
          ? _value.format
          // ignore: cast_nullable_to_non_nullable
          : format as ExternalCatalogueEntryFormatEnum?,
      bounds: bounds == const $CopyWithPlaceholder() || bounds == null
          ? _value.bounds
          // ignore: cast_nullable_to_non_nullable
          : bounds as List<num>,
      timeDimension: timeDimension == const $CopyWithPlaceholder()
          ? _value.timeDimension
          // ignore: cast_nullable_to_non_nullable
          : timeDimension as ExternalCatalogueEntryTimeDimension?,
      cors: cors == const $CopyWithPlaceholder()
          ? _value.cors
          // ignore: cast_nullable_to_non_nullable
          : cors as String?,
      attribution:
          attribution == const $CopyWithPlaceholder() || attribution == null
          ? _value.attribution
          // ignore: cast_nullable_to_non_nullable
          : attribution as Attribution,
    );
  }
}

extension $ExternalCatalogueEntryCopyWith on ExternalCatalogueEntry {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExternalCatalogueEntry.copyWith(...)` or `instanceOfExternalCatalogueEntry.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExternalCatalogueEntryCWProxy get copyWith =>
      _$ExternalCatalogueEntryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExternalCatalogueEntry _$ExternalCatalogueEntryFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ExternalCatalogueEntry', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'externalTemplate',
      'tilingScheme',
      'tileSize',
      'maxNativeZoom',
      'bounds',
      'attribution',
    ],
  );
  final val = ExternalCatalogueEntry(
    provider: $checkedConvert('provider', (v) => v as String?),
    layer: $checkedConvert('layer', (v) => v as String?),
    protocol: $checkedConvert(
      'protocol',
      (v) =>
          $enumDecodeNullable(_$ExternalCatalogueEntryProtocolEnumEnumMap, v),
    ),
    externalTemplate: $checkedConvert('externalTemplate', (v) => v as String),
    tilingScheme: $checkedConvert('tilingScheme', (v) => v as String),
    tileSize: $checkedConvert('tileSize', (v) => (v as num).toInt()),
    maxNativeZoom: $checkedConvert('maxNativeZoom', (v) => (v as num).toInt()),
    format: $checkedConvert(
      'format',
      (v) => $enumDecodeNullable(_$ExternalCatalogueEntryFormatEnumEnumMap, v),
    ),
    bounds: $checkedConvert(
      'bounds',
      (v) => (v as List<dynamic>).map((e) => e as num).toList(),
    ),
    timeDimension: $checkedConvert(
      'timeDimension',
      (v) => v == null
          ? null
          : ExternalCatalogueEntryTimeDimension.fromJson(
              v as Map<String, dynamic>,
            ),
    ),
    cors: $checkedConvert('cors', (v) => v as String?),
    attribution: $checkedConvert(
      'attribution',
      (v) => Attribution.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$ExternalCatalogueEntryToJson(
  ExternalCatalogueEntry instance,
) => <String, dynamic>{
  'provider': ?instance.provider,
  'layer': ?instance.layer,
  'protocol': ?_$ExternalCatalogueEntryProtocolEnumEnumMap[instance.protocol],
  'externalTemplate': instance.externalTemplate,
  'tilingScheme': instance.tilingScheme,
  'tileSize': instance.tileSize,
  'maxNativeZoom': instance.maxNativeZoom,
  'format': ?_$ExternalCatalogueEntryFormatEnumEnumMap[instance.format],
  'bounds': instance.bounds,
  'timeDimension': ?instance.timeDimension?.toJson(),
  'cors': ?instance.cors,
  'attribution': instance.attribution.toJson(),
};

const _$ExternalCatalogueEntryProtocolEnumEnumMap = {
  ExternalCatalogueEntryProtocolEnum.wmts: 'wmts',
  ExternalCatalogueEntryProtocolEnum.wms: 'wms',
  ExternalCatalogueEntryProtocolEnum.xyz: 'xyz',
};

const _$ExternalCatalogueEntryFormatEnumEnumMap = {
  ExternalCatalogueEntryFormatEnum.png: 'png',
  ExternalCatalogueEntryFormatEnum.jpg: 'jpg',
};
