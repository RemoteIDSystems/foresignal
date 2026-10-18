// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'style_meaning.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StyleMeaningCWProxy {
  StyleMeaning min(num? min);

  StyleMeaning max(num? max);

  StyleMeaning text(String text);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StyleMeaning(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StyleMeaning(...).copyWith(id: 12, name: "My name")
  /// ```
  StyleMeaning call({num? min, num? max, String text});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStyleMeaning.copyWith(...)` or call `instanceOfStyleMeaning.copyWith.fieldName(value)` for a single field.
class _$StyleMeaningCWProxyImpl implements _$StyleMeaningCWProxy {
  const _$StyleMeaningCWProxyImpl(this._value);

  final StyleMeaning _value;

  @override
  StyleMeaning min(num? min) => call(min: min);

  @override
  StyleMeaning max(num? max) => call(max: max);

  @override
  StyleMeaning text(String text) => call(text: text);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StyleMeaning(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StyleMeaning(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StyleMeaning call({
    Object? min = const $CopyWithPlaceholder(),
    Object? max = const $CopyWithPlaceholder(),
    Object? text = const $CopyWithPlaceholder(),
  }) {
    return StyleMeaning(
      min: min == const $CopyWithPlaceholder()
          ? _value.min
          // ignore: cast_nullable_to_non_nullable
          : min as num?,
      max: max == const $CopyWithPlaceholder()
          ? _value.max
          // ignore: cast_nullable_to_non_nullable
          : max as num?,
      text: text == const $CopyWithPlaceholder() || text == null
          ? _value.text
          // ignore: cast_nullable_to_non_nullable
          : text as String,
    );
  }
}

extension $StyleMeaningCopyWith on StyleMeaning {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStyleMeaning.copyWith(...)` or `instanceOfStyleMeaning.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StyleMeaningCWProxy get copyWith => _$StyleMeaningCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StyleMeaning _$StyleMeaningFromJson(Map<String, dynamic> json) =>
    $checkedCreate('StyleMeaning', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['text']);
      final val = StyleMeaning(
        min: $checkedConvert('min', (v) => v as num?),
        max: $checkedConvert('max', (v) => v as num?),
        text: $checkedConvert('text', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$StyleMeaningToJson(StyleMeaning instance) =>
    <String, dynamic>{
      'min': ?instance.min,
      'max': ?instance.max,
      'text': instance.text,
    };
