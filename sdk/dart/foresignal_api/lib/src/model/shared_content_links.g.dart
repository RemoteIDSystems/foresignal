// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'shared_content_links.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SharedContentLinksCWProxy {
  SharedContentLinks geojson(String? geojson);

  SharedContentLinks tiles(String? tiles);

  SharedContentLinks download(String? download);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SharedContentLinks(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SharedContentLinks(...).copyWith(id: 12, name: "My name")
  /// ```
  SharedContentLinks call({String? geojson, String? tiles, String? download});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSharedContentLinks.copyWith(...)` or call `instanceOfSharedContentLinks.copyWith.fieldName(value)` for a single field.
class _$SharedContentLinksCWProxyImpl implements _$SharedContentLinksCWProxy {
  const _$SharedContentLinksCWProxyImpl(this._value);

  final SharedContentLinks _value;

  @override
  SharedContentLinks geojson(String? geojson) => call(geojson: geojson);

  @override
  SharedContentLinks tiles(String? tiles) => call(tiles: tiles);

  @override
  SharedContentLinks download(String? download) => call(download: download);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SharedContentLinks(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SharedContentLinks(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SharedContentLinks call({
    Object? geojson = const $CopyWithPlaceholder(),
    Object? tiles = const $CopyWithPlaceholder(),
    Object? download = const $CopyWithPlaceholder(),
  }) {
    return SharedContentLinks(
      geojson: geojson == const $CopyWithPlaceholder()
          ? _value.geojson
          // ignore: cast_nullable_to_non_nullable
          : geojson as String?,
      tiles: tiles == const $CopyWithPlaceholder()
          ? _value.tiles
          // ignore: cast_nullable_to_non_nullable
          : tiles as String?,
      download: download == const $CopyWithPlaceholder()
          ? _value.download
          // ignore: cast_nullable_to_non_nullable
          : download as String?,
    );
  }
}

extension $SharedContentLinksCopyWith on SharedContentLinks {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSharedContentLinks.copyWith(...)` or `instanceOfSharedContentLinks.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SharedContentLinksCWProxy get copyWith =>
      _$SharedContentLinksCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SharedContentLinks _$SharedContentLinksFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SharedContentLinks', json, ($checkedConvert) {
      final val = SharedContentLinks(
        geojson: $checkedConvert('geojson', (v) => v as String?),
        tiles: $checkedConvert('tiles', (v) => v as String?),
        download: $checkedConvert('download', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$SharedContentLinksToJson(SharedContentLinks instance) =>
    <String, dynamic>{
      'geojson': ?instance.geojson,
      'tiles': ?instance.tiles,
      'download': ?instance.download,
    };
