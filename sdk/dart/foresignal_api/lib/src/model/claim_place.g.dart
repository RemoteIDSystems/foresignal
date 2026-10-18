// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'claim_place.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ClaimPlaceCWProxy {
  ClaimPlace ref(String? ref);

  ClaimPlace name(String name);

  ClaimPlace geom(Geometry? geom);

  ClaimPlace precision(ClaimPlacePrecisionEnum precision);

  ClaimPlace confidence(num confidence);

  ClaimPlace regionCode(String? regionCode);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ClaimPlace(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ClaimPlace(...).copyWith(id: 12, name: "My name")
  /// ```
  ClaimPlace call({
    String? ref,
    String name,
    Geometry? geom,
    ClaimPlacePrecisionEnum precision,
    num confidence,
    String? regionCode,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfClaimPlace.copyWith(...)` or call `instanceOfClaimPlace.copyWith.fieldName(value)` for a single field.
class _$ClaimPlaceCWProxyImpl implements _$ClaimPlaceCWProxy {
  const _$ClaimPlaceCWProxyImpl(this._value);

  final ClaimPlace _value;

  @override
  ClaimPlace ref(String? ref) => call(ref: ref);

  @override
  ClaimPlace name(String name) => call(name: name);

  @override
  ClaimPlace geom(Geometry? geom) => call(geom: geom);

  @override
  ClaimPlace precision(ClaimPlacePrecisionEnum precision) =>
      call(precision: precision);

  @override
  ClaimPlace confidence(num confidence) => call(confidence: confidence);

  @override
  ClaimPlace regionCode(String? regionCode) => call(regionCode: regionCode);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ClaimPlace(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ClaimPlace(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ClaimPlace call({
    Object? ref = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? geom = const $CopyWithPlaceholder(),
    Object? precision = const $CopyWithPlaceholder(),
    Object? confidence = const $CopyWithPlaceholder(),
    Object? regionCode = const $CopyWithPlaceholder(),
  }) {
    return ClaimPlace(
      ref: ref == const $CopyWithPlaceholder()
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String?,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      geom: geom == const $CopyWithPlaceholder()
          ? _value.geom
          // ignore: cast_nullable_to_non_nullable
          : geom as Geometry?,
      precision: precision == const $CopyWithPlaceholder() || precision == null
          ? _value.precision
          // ignore: cast_nullable_to_non_nullable
          : precision as ClaimPlacePrecisionEnum,
      confidence:
          confidence == const $CopyWithPlaceholder() || confidence == null
          ? _value.confidence
          // ignore: cast_nullable_to_non_nullable
          : confidence as num,
      regionCode: regionCode == const $CopyWithPlaceholder()
          ? _value.regionCode
          // ignore: cast_nullable_to_non_nullable
          : regionCode as String?,
    );
  }
}

extension $ClaimPlaceCopyWith on ClaimPlace {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfClaimPlace.copyWith(...)` or `instanceOfClaimPlace.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ClaimPlaceCWProxy get copyWith => _$ClaimPlaceCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ClaimPlace _$ClaimPlaceFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ClaimPlace', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['name', 'precision', 'confidence']);
      final val = ClaimPlace(
        ref: $checkedConvert('ref', (v) => v as String?),
        name: $checkedConvert('name', (v) => v as String),
        geom: $checkedConvert(
          'geom',
          (v) =>
              v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
        ),
        precision: $checkedConvert(
          'precision',
          (v) => $enumDecode(_$ClaimPlacePrecisionEnumEnumMap, v),
        ),
        confidence: $checkedConvert('confidence', (v) => v as num),
        regionCode: $checkedConvert('regionCode', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ClaimPlaceToJson(ClaimPlace instance) =>
    <String, dynamic>{
      'ref': ?instance.ref,
      'name': instance.name,
      'geom': ?instance.geom?.toJson(),
      'precision': _$ClaimPlacePrecisionEnumEnumMap[instance.precision]!,
      'confidence': instance.confidence,
      'regionCode': ?instance.regionCode,
    };

const _$ClaimPlacePrecisionEnumEnumMap = {
  ClaimPlacePrecisionEnum.point: 'point',
  ClaimPlacePrecisionEnum.locality: 'locality',
  ClaimPlacePrecisionEnum.admin1: 'admin1',
  ClaimPlacePrecisionEnum.country: 'country',
};
