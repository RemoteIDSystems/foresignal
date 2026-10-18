// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'field_encodings.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$FieldEncodingsCWProxy {
  FieldEncodings velocityJson(FieldEncodingsVelocityJson? velocityJson);

  FieldEncodings pngUv(FieldEncodingsPngUv? pngUv);

  FieldEncodings i16(FieldEncodingsI16? i16);

  FieldEncodings pngTiles(FieldEncodingsPngTiles? pngTiles);

  FieldEncodings tilejson(String? tilejson);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FieldEncodings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FieldEncodings(...).copyWith(id: 12, name: "My name")
  /// ```
  FieldEncodings call({
    FieldEncodingsVelocityJson? velocityJson,
    FieldEncodingsPngUv? pngUv,
    FieldEncodingsI16? i16,
    FieldEncodingsPngTiles? pngTiles,
    String? tilejson,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfFieldEncodings.copyWith(...)` or call `instanceOfFieldEncodings.copyWith.fieldName(value)` for a single field.
class _$FieldEncodingsCWProxyImpl implements _$FieldEncodingsCWProxy {
  const _$FieldEncodingsCWProxyImpl(this._value);

  final FieldEncodings _value;

  @override
  FieldEncodings velocityJson(FieldEncodingsVelocityJson? velocityJson) =>
      call(velocityJson: velocityJson);

  @override
  FieldEncodings pngUv(FieldEncodingsPngUv? pngUv) => call(pngUv: pngUv);

  @override
  FieldEncodings i16(FieldEncodingsI16? i16) => call(i16: i16);

  @override
  FieldEncodings pngTiles(FieldEncodingsPngTiles? pngTiles) =>
      call(pngTiles: pngTiles);

  @override
  FieldEncodings tilejson(String? tilejson) => call(tilejson: tilejson);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FieldEncodings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FieldEncodings(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  FieldEncodings call({
    Object? velocityJson = const $CopyWithPlaceholder(),
    Object? pngUv = const $CopyWithPlaceholder(),
    Object? i16 = const $CopyWithPlaceholder(),
    Object? pngTiles = const $CopyWithPlaceholder(),
    Object? tilejson = const $CopyWithPlaceholder(),
  }) {
    return FieldEncodings(
      velocityJson: velocityJson == const $CopyWithPlaceholder()
          ? _value.velocityJson
          // ignore: cast_nullable_to_non_nullable
          : velocityJson as FieldEncodingsVelocityJson?,
      pngUv: pngUv == const $CopyWithPlaceholder()
          ? _value.pngUv
          // ignore: cast_nullable_to_non_nullable
          : pngUv as FieldEncodingsPngUv?,
      i16: i16 == const $CopyWithPlaceholder()
          ? _value.i16
          // ignore: cast_nullable_to_non_nullable
          : i16 as FieldEncodingsI16?,
      pngTiles: pngTiles == const $CopyWithPlaceholder()
          ? _value.pngTiles
          // ignore: cast_nullable_to_non_nullable
          : pngTiles as FieldEncodingsPngTiles?,
      tilejson: tilejson == const $CopyWithPlaceholder()
          ? _value.tilejson
          // ignore: cast_nullable_to_non_nullable
          : tilejson as String?,
    );
  }
}

extension $FieldEncodingsCopyWith on FieldEncodings {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfFieldEncodings.copyWith(...)` or `instanceOfFieldEncodings.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$FieldEncodingsCWProxy get copyWith => _$FieldEncodingsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

FieldEncodings _$FieldEncodingsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('FieldEncodings', json, ($checkedConvert) {
      final val = FieldEncodings(
        velocityJson: $checkedConvert(
          'velocityJson',
          (v) => v == null
              ? null
              : FieldEncodingsVelocityJson.fromJson(v as Map<String, dynamic>),
        ),
        pngUv: $checkedConvert(
          'pngUv',
          (v) => v == null
              ? null
              : FieldEncodingsPngUv.fromJson(v as Map<String, dynamic>),
        ),
        i16: $checkedConvert(
          'i16',
          (v) => v == null
              ? null
              : FieldEncodingsI16.fromJson(v as Map<String, dynamic>),
        ),
        pngTiles: $checkedConvert(
          'pngTiles',
          (v) => v == null
              ? null
              : FieldEncodingsPngTiles.fromJson(v as Map<String, dynamic>),
        ),
        tilejson: $checkedConvert('tilejson', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$FieldEncodingsToJson(FieldEncodings instance) =>
    <String, dynamic>{
      'velocityJson': ?instance.velocityJson?.toJson(),
      'pngUv': ?instance.pngUv?.toJson(),
      'i16': ?instance.i16?.toJson(),
      'pngTiles': ?instance.pngTiles?.toJson(),
      'tilejson': ?instance.tilejson,
    };
