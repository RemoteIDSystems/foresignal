// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'style_stop.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StyleStopCWProxy {
  StyleStop value(num value);

  StyleStop color(String color);

  StyleStop label(String? label);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StyleStop(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StyleStop(...).copyWith(id: 12, name: "My name")
  /// ```
  StyleStop call({num value, String color, String? label});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStyleStop.copyWith(...)` or call `instanceOfStyleStop.copyWith.fieldName(value)` for a single field.
class _$StyleStopCWProxyImpl implements _$StyleStopCWProxy {
  const _$StyleStopCWProxyImpl(this._value);

  final StyleStop _value;

  @override
  StyleStop value(num value) => call(value: value);

  @override
  StyleStop color(String color) => call(color: color);

  @override
  StyleStop label(String? label) => call(label: label);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StyleStop(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StyleStop(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StyleStop call({
    Object? value = const $CopyWithPlaceholder(),
    Object? color = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
  }) {
    return StyleStop(
      value: value == const $CopyWithPlaceholder() || value == null
          ? _value.value
          // ignore: cast_nullable_to_non_nullable
          : value as num,
      color: color == const $CopyWithPlaceholder() || color == null
          ? _value.color
          // ignore: cast_nullable_to_non_nullable
          : color as String,
      label: label == const $CopyWithPlaceholder()
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as String?,
    );
  }
}

extension $StyleStopCopyWith on StyleStop {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStyleStop.copyWith(...)` or `instanceOfStyleStop.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StyleStopCWProxy get copyWith => _$StyleStopCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StyleStop _$StyleStopFromJson(Map<String, dynamic> json) =>
    $checkedCreate('StyleStop', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['value', 'color']);
      final val = StyleStop(
        value: $checkedConvert('value', (v) => v as num),
        color: $checkedConvert('color', (v) => v as String),
        label: $checkedConvert('label', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$StyleStopToJson(StyleStop instance) => <String, dynamic>{
  'value': instance.value,
  'color': instance.color,
  'label': ?instance.label,
};
