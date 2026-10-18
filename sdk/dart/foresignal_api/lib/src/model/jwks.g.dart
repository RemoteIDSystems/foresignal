// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'jwks.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$JwksCWProxy {
  Jwks keys(List<Jwk> keys);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Jwks(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Jwks(...).copyWith(id: 12, name: "My name")
  /// ```
  Jwks call({List<Jwk> keys});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfJwks.copyWith(...)` or call `instanceOfJwks.copyWith.fieldName(value)` for a single field.
class _$JwksCWProxyImpl implements _$JwksCWProxy {
  const _$JwksCWProxyImpl(this._value);

  final Jwks _value;

  @override
  Jwks keys(List<Jwk> keys) => call(keys: keys);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Jwks(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Jwks(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Jwks call({Object? keys = const $CopyWithPlaceholder()}) {
    return Jwks(
      keys: keys == const $CopyWithPlaceholder() || keys == null
          ? _value.keys
          // ignore: cast_nullable_to_non_nullable
          : keys as List<Jwk>,
    );
  }
}

extension $JwksCopyWith on Jwks {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfJwks.copyWith(...)` or `instanceOfJwks.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$JwksCWProxy get copyWith => _$JwksCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Jwks _$JwksFromJson(Map<String, dynamic> json) =>
    $checkedCreate('Jwks', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['keys']);
      final val = Jwks(
        keys: $checkedConvert(
          'keys',
          (v) => (v as List<dynamic>)
              .map((e) => Jwk.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$JwksToJson(Jwks instance) => <String, dynamic>{
  'keys': instance.keys.map((e) => e.toJson()).toList(),
};
