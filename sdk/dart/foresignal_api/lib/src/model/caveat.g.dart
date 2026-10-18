// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'caveat.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CaveatCWProxy {
  Caveat code(String code);

  Caveat text(String text);

  Caveat data(Map<String, Object>? data);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Caveat(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Caveat(...).copyWith(id: 12, name: "My name")
  /// ```
  Caveat call({String code, String text, Map<String, Object>? data});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCaveat.copyWith(...)` or call `instanceOfCaveat.copyWith.fieldName(value)` for a single field.
class _$CaveatCWProxyImpl implements _$CaveatCWProxy {
  const _$CaveatCWProxyImpl(this._value);

  final Caveat _value;

  @override
  Caveat code(String code) => call(code: code);

  @override
  Caveat text(String text) => call(text: text);

  @override
  Caveat data(Map<String, Object>? data) => call(data: data);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Caveat(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Caveat(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Caveat call({
    Object? code = const $CopyWithPlaceholder(),
    Object? text = const $CopyWithPlaceholder(),
    Object? data = const $CopyWithPlaceholder(),
  }) {
    return Caveat(
      code: code == const $CopyWithPlaceholder() || code == null
          ? _value.code
          // ignore: cast_nullable_to_non_nullable
          : code as String,
      text: text == const $CopyWithPlaceholder() || text == null
          ? _value.text
          // ignore: cast_nullable_to_non_nullable
          : text as String,
      data: data == const $CopyWithPlaceholder()
          ? _value.data
          // ignore: cast_nullable_to_non_nullable
          : data as Map<String, Object>?,
    );
  }
}

extension $CaveatCopyWith on Caveat {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCaveat.copyWith(...)` or `instanceOfCaveat.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CaveatCWProxy get copyWith => _$CaveatCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Caveat _$CaveatFromJson(Map<String, dynamic> json) =>
    $checkedCreate('Caveat', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['code', 'text']);
      final val = Caveat(
        code: $checkedConvert('code', (v) => v as String),
        text: $checkedConvert('text', (v) => v as String),
        data: $checkedConvert(
          'data',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as Object),
          ),
        ),
      );
      return val;
    });

Map<String, dynamic> _$CaveatToJson(Caveat instance) => <String, dynamic>{
  'code': instance.code,
  'text': instance.text,
  'data': ?instance.data,
};
