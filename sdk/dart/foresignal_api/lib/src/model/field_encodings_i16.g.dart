// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'field_encodings_i16.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$FieldEncodingsI16CWProxy {
  FieldEncodingsI16 url(String? url);

  FieldEncodingsI16 scale(num? scale);

  FieldEncodingsI16 offset(num? offset);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FieldEncodingsI16(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FieldEncodingsI16(...).copyWith(id: 12, name: "My name")
  /// ```
  FieldEncodingsI16 call({String? url, num? scale, num? offset});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfFieldEncodingsI16.copyWith(...)` or call `instanceOfFieldEncodingsI16.copyWith.fieldName(value)` for a single field.
class _$FieldEncodingsI16CWProxyImpl implements _$FieldEncodingsI16CWProxy {
  const _$FieldEncodingsI16CWProxyImpl(this._value);

  final FieldEncodingsI16 _value;

  @override
  FieldEncodingsI16 url(String? url) => call(url: url);

  @override
  FieldEncodingsI16 scale(num? scale) => call(scale: scale);

  @override
  FieldEncodingsI16 offset(num? offset) => call(offset: offset);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FieldEncodingsI16(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FieldEncodingsI16(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  FieldEncodingsI16 call({
    Object? url = const $CopyWithPlaceholder(),
    Object? scale = const $CopyWithPlaceholder(),
    Object? offset = const $CopyWithPlaceholder(),
  }) {
    return FieldEncodingsI16(
      url: url == const $CopyWithPlaceholder()
          ? _value.url
          // ignore: cast_nullable_to_non_nullable
          : url as String?,
      scale: scale == const $CopyWithPlaceholder()
          ? _value.scale
          // ignore: cast_nullable_to_non_nullable
          : scale as num?,
      offset: offset == const $CopyWithPlaceholder()
          ? _value.offset
          // ignore: cast_nullable_to_non_nullable
          : offset as num?,
    );
  }
}

extension $FieldEncodingsI16CopyWith on FieldEncodingsI16 {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfFieldEncodingsI16.copyWith(...)` or `instanceOfFieldEncodingsI16.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$FieldEncodingsI16CWProxy get copyWith =>
      _$FieldEncodingsI16CWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

FieldEncodingsI16 _$FieldEncodingsI16FromJson(Map<String, dynamic> json) =>
    $checkedCreate('FieldEncodingsI16', json, ($checkedConvert) {
      final val = FieldEncodingsI16(
        url: $checkedConvert('url', (v) => v as String?),
        scale: $checkedConvert('scale', (v) => v as num?),
        offset: $checkedConvert('offset', (v) => v as num?),
      );
      return val;
    });

Map<String, dynamic> _$FieldEncodingsI16ToJson(FieldEncodingsI16 instance) =>
    <String, dynamic>{
      'url': ?instance.url,
      'scale': ?instance.scale,
      'offset': ?instance.offset,
    };
