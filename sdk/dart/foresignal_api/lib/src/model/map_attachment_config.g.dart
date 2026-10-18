// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'map_attachment_config.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MapAttachmentConfigCWProxy {
  MapAttachmentConfig attachMap(MapAttachmentConfigAttachMapEnum? attachMap);

  MapAttachmentConfig mapProducts(List<String>? mapProducts);

  MapAttachmentConfig mapRadiusKm(num? mapRadiusKm);

  MapAttachmentConfig maxAttachmentBytes(int? maxAttachmentBytes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MapAttachmentConfig(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MapAttachmentConfig(...).copyWith(id: 12, name: "My name")
  /// ```
  MapAttachmentConfig call({
    MapAttachmentConfigAttachMapEnum? attachMap,
    List<String>? mapProducts,
    num? mapRadiusKm,
    int? maxAttachmentBytes,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMapAttachmentConfig.copyWith(...)` or call `instanceOfMapAttachmentConfig.copyWith.fieldName(value)` for a single field.
class _$MapAttachmentConfigCWProxyImpl implements _$MapAttachmentConfigCWProxy {
  const _$MapAttachmentConfigCWProxyImpl(this._value);

  final MapAttachmentConfig _value;

  @override
  MapAttachmentConfig attachMap(MapAttachmentConfigAttachMapEnum? attachMap) =>
      call(attachMap: attachMap);

  @override
  MapAttachmentConfig mapProducts(List<String>? mapProducts) =>
      call(mapProducts: mapProducts);

  @override
  MapAttachmentConfig mapRadiusKm(num? mapRadiusKm) =>
      call(mapRadiusKm: mapRadiusKm);

  @override
  MapAttachmentConfig maxAttachmentBytes(int? maxAttachmentBytes) =>
      call(maxAttachmentBytes: maxAttachmentBytes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MapAttachmentConfig(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MapAttachmentConfig(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MapAttachmentConfig call({
    Object? attachMap = const $CopyWithPlaceholder(),
    Object? mapProducts = const $CopyWithPlaceholder(),
    Object? mapRadiusKm = const $CopyWithPlaceholder(),
    Object? maxAttachmentBytes = const $CopyWithPlaceholder(),
  }) {
    return MapAttachmentConfig(
      attachMap: attachMap == const $CopyWithPlaceholder()
          ? _value.attachMap
          // ignore: cast_nullable_to_non_nullable
          : attachMap as MapAttachmentConfigAttachMapEnum?,
      mapProducts: mapProducts == const $CopyWithPlaceholder()
          ? _value.mapProducts
          // ignore: cast_nullable_to_non_nullable
          : mapProducts as List<String>?,
      mapRadiusKm: mapRadiusKm == const $CopyWithPlaceholder()
          ? _value.mapRadiusKm
          // ignore: cast_nullable_to_non_nullable
          : mapRadiusKm as num?,
      maxAttachmentBytes: maxAttachmentBytes == const $CopyWithPlaceholder()
          ? _value.maxAttachmentBytes
          // ignore: cast_nullable_to_non_nullable
          : maxAttachmentBytes as int?,
    );
  }
}

extension $MapAttachmentConfigCopyWith on MapAttachmentConfig {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMapAttachmentConfig.copyWith(...)` or `instanceOfMapAttachmentConfig.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MapAttachmentConfigCWProxy get copyWith =>
      _$MapAttachmentConfigCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MapAttachmentConfig _$MapAttachmentConfigFromJson(Map<String, dynamic> json) =>
    $checkedCreate('MapAttachmentConfig', json, ($checkedConvert) {
      final val = MapAttachmentConfig(
        attachMap: $checkedConvert(
          'attachMap',
          (v) =>
              $enumDecodeNullable(_$MapAttachmentConfigAttachMapEnumEnumMap, v),
        ),
        mapProducts: $checkedConvert(
          'mapProducts',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        mapRadiusKm: $checkedConvert('mapRadiusKm', (v) => v as num? ?? 50),
        maxAttachmentBytes: $checkedConvert(
          'maxAttachmentBytes',
          (v) => (v as num?)?.toInt(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$MapAttachmentConfigToJson(
  MapAttachmentConfig instance,
) => <String, dynamic>{
  'attachMap': ?_$MapAttachmentConfigAttachMapEnumEnumMap[instance.attachMap],
  'mapProducts': ?instance.mapProducts,
  'mapRadiusKm': ?instance.mapRadiusKm,
  'maxAttachmentBytes': ?instance.maxAttachmentBytes,
};

const _$MapAttachmentConfigAttachMapEnumEnumMap = {
  MapAttachmentConfigAttachMapEnum.none: 'none',
  MapAttachmentConfigAttachMapEnum.static_: 'static',
};
