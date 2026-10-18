// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'map_style_metadata.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MapStyleMetadataCWProxy {
  MapStyleMetadata foresignal(MapStyleMetadata? foresignal);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MapStyleMetadata(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MapStyleMetadata(...).copyWith(id: 12, name: "My name")
  /// ```
  MapStyleMetadata call({MapStyleMetadata? foresignal});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMapStyleMetadata.copyWith(...)` or call `instanceOfMapStyleMetadata.copyWith.fieldName(value)` for a single field.
class _$MapStyleMetadataCWProxyImpl implements _$MapStyleMetadataCWProxy {
  const _$MapStyleMetadataCWProxyImpl(this._value);

  final MapStyleMetadata _value;

  @override
  MapStyleMetadata foresignal(MapStyleMetadata? foresignal) =>
      call(foresignal: foresignal);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MapStyleMetadata(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MapStyleMetadata(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MapStyleMetadata call({Object? foresignal = const $CopyWithPlaceholder()}) {
    return MapStyleMetadata(
      foresignal: foresignal == const $CopyWithPlaceholder()
          ? _value.foresignal
          // ignore: cast_nullable_to_non_nullable
          : foresignal as MapStyleMetadata?,
    );
  }
}

extension $MapStyleMetadataCopyWith on MapStyleMetadata {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMapStyleMetadata.copyWith(...)` or `instanceOfMapStyleMetadata.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MapStyleMetadataCWProxy get copyWith => _$MapStyleMetadataCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MapStyleMetadata _$MapStyleMetadataFromJson(Map<String, dynamic> json) =>
    $checkedCreate('MapStyleMetadata', json, ($checkedConvert) {
      final val = MapStyleMetadata(
        foresignal: $checkedConvert(
          'foresignal',
          (v) => v == null
              ? null
              : MapStyleMetadata.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$MapStyleMetadataToJson(MapStyleMetadata instance) =>
    <String, dynamic>{'foresignal': ?instance.foresignal?.toJson()};
