// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'style_special.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StyleSpecialCWProxy {
  StyleSpecial kind(StyleSpecialKindEnum kind);

  StyleSpecial color(String color);

  StyleSpecial label(String label);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StyleSpecial(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StyleSpecial(...).copyWith(id: 12, name: "My name")
  /// ```
  StyleSpecial call({StyleSpecialKindEnum kind, String color, String label});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStyleSpecial.copyWith(...)` or call `instanceOfStyleSpecial.copyWith.fieldName(value)` for a single field.
class _$StyleSpecialCWProxyImpl implements _$StyleSpecialCWProxy {
  const _$StyleSpecialCWProxyImpl(this._value);

  final StyleSpecial _value;

  @override
  StyleSpecial kind(StyleSpecialKindEnum kind) => call(kind: kind);

  @override
  StyleSpecial color(String color) => call(color: color);

  @override
  StyleSpecial label(String label) => call(label: label);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StyleSpecial(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StyleSpecial(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StyleSpecial call({
    Object? kind = const $CopyWithPlaceholder(),
    Object? color = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
  }) {
    return StyleSpecial(
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as StyleSpecialKindEnum,
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

extension $StyleSpecialCopyWith on StyleSpecial {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStyleSpecial.copyWith(...)` or `instanceOfStyleSpecial.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StyleSpecialCWProxy get copyWith => _$StyleSpecialCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StyleSpecial _$StyleSpecialFromJson(Map<String, dynamic> json) =>
    $checkedCreate('StyleSpecial', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['kind', 'color', 'label']);
      final val = StyleSpecial(
        kind: $checkedConvert(
          'kind',
          (v) => $enumDecode(_$StyleSpecialKindEnumEnumMap, v),
        ),
        color: $checkedConvert('color', (v) => v as String),
        label: $checkedConvert('label', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$StyleSpecialToJson(StyleSpecial instance) =>
    <String, dynamic>{
      'kind': _$StyleSpecialKindEnumEnumMap[instance.kind]!,
      'color': instance.color,
      'label': instance.label,
    };

const _$StyleSpecialKindEnumEnumMap = {
  StyleSpecialKindEnum.noCoverage: 'noCoverage',
  StyleSpecialKindEnum.noEcho: 'noEcho',
  StyleSpecialKindEnum.noData: 'noData',
};
