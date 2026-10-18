// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'style_category.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StyleCategoryCWProxy {
  StyleCategory code(int code);

  StyleCategory color(String color);

  StyleCategory label(String label);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StyleCategory(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StyleCategory(...).copyWith(id: 12, name: "My name")
  /// ```
  StyleCategory call({int code, String color, String label});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStyleCategory.copyWith(...)` or call `instanceOfStyleCategory.copyWith.fieldName(value)` for a single field.
class _$StyleCategoryCWProxyImpl implements _$StyleCategoryCWProxy {
  const _$StyleCategoryCWProxyImpl(this._value);

  final StyleCategory _value;

  @override
  StyleCategory code(int code) => call(code: code);

  @override
  StyleCategory color(String color) => call(color: color);

  @override
  StyleCategory label(String label) => call(label: label);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StyleCategory(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StyleCategory(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StyleCategory call({
    Object? code = const $CopyWithPlaceholder(),
    Object? color = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
  }) {
    return StyleCategory(
      code: code == const $CopyWithPlaceholder() || code == null
          ? _value.code
          // ignore: cast_nullable_to_non_nullable
          : code as int,
      color: color == const $CopyWithPlaceholder() || color == null
          ? _value.color
          // ignore: cast_nullable_to_non_nullable
          : color as String,
      label: label == const $CopyWithPlaceholder() || label == null
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as String,
    );
  }
}

extension $StyleCategoryCopyWith on StyleCategory {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStyleCategory.copyWith(...)` or `instanceOfStyleCategory.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StyleCategoryCWProxy get copyWith => _$StyleCategoryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StyleCategory _$StyleCategoryFromJson(Map<String, dynamic> json) =>
    $checkedCreate('StyleCategory', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['code', 'color', 'label']);
      final val = StyleCategory(
        code: $checkedConvert('code', (v) => (v as num).toInt()),
        color: $checkedConvert('color', (v) => v as String),
        label: $checkedConvert('label', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$StyleCategoryToJson(StyleCategory instance) =>
    <String, dynamic>{
      'code': instance.code,
      'color': instance.color,
      'label': instance.label,
    };
