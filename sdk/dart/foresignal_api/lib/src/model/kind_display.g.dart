// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'kind_display.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$KindDisplayCWProxy {
  KindDisplay titleTemplate(String? titleTemplate);

  KindDisplay icon(String? icon);

  KindDisplay colour(String? colour);

  KindDisplay sidc(String? sidc);

  KindDisplay rotateField(String? rotateField);

  KindDisplay sizeField(String? sizeField);

  KindDisplay colorField(String? colorField);

  KindDisplay iconField(String? iconField);

  KindDisplay iconCases(Map<String, String>? iconCases);

  KindDisplay colorCases(Map<String, String>? colorCases);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `KindDisplay(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// KindDisplay(...).copyWith(id: 12, name: "My name")
  /// ```
  KindDisplay call({
    String? titleTemplate,
    String? icon,
    String? colour,
    String? sidc,
    String? rotateField,
    String? sizeField,
    String? colorField,
    String? iconField,
    Map<String, String>? iconCases,
    Map<String, String>? colorCases,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfKindDisplay.copyWith(...)` or call `instanceOfKindDisplay.copyWith.fieldName(value)` for a single field.
class _$KindDisplayCWProxyImpl implements _$KindDisplayCWProxy {
  const _$KindDisplayCWProxyImpl(this._value);

  final KindDisplay _value;

  @override
  KindDisplay titleTemplate(String? titleTemplate) =>
      call(titleTemplate: titleTemplate);

  @override
  KindDisplay icon(String? icon) => call(icon: icon);

  @override
  KindDisplay colour(String? colour) => call(colour: colour);

  @override
  KindDisplay sidc(String? sidc) => call(sidc: sidc);

  @override
  KindDisplay rotateField(String? rotateField) =>
      call(rotateField: rotateField);

  @override
  KindDisplay sizeField(String? sizeField) => call(sizeField: sizeField);

  @override
  KindDisplay colorField(String? colorField) => call(colorField: colorField);

  @override
  KindDisplay iconField(String? iconField) => call(iconField: iconField);

  @override
  KindDisplay iconCases(Map<String, String>? iconCases) =>
      call(iconCases: iconCases);

  @override
  KindDisplay colorCases(Map<String, String>? colorCases) =>
      call(colorCases: colorCases);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `KindDisplay(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// KindDisplay(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  KindDisplay call({
    Object? titleTemplate = const $CopyWithPlaceholder(),
    Object? icon = const $CopyWithPlaceholder(),
    Object? colour = const $CopyWithPlaceholder(),
    Object? sidc = const $CopyWithPlaceholder(),
    Object? rotateField = const $CopyWithPlaceholder(),
    Object? sizeField = const $CopyWithPlaceholder(),
    Object? colorField = const $CopyWithPlaceholder(),
    Object? iconField = const $CopyWithPlaceholder(),
    Object? iconCases = const $CopyWithPlaceholder(),
    Object? colorCases = const $CopyWithPlaceholder(),
  }) {
    return KindDisplay(
      titleTemplate: titleTemplate == const $CopyWithPlaceholder()
          ? _value.titleTemplate
          // ignore: cast_nullable_to_non_nullable
          : titleTemplate as String?,
      icon: icon == const $CopyWithPlaceholder()
          ? _value.icon
          // ignore: cast_nullable_to_non_nullable
          : icon as String?,
      colour: colour == const $CopyWithPlaceholder()
          ? _value.colour
          // ignore: cast_nullable_to_non_nullable
          : colour as String?,
      sidc: sidc == const $CopyWithPlaceholder()
          ? _value.sidc
          // ignore: cast_nullable_to_non_nullable
          : sidc as String?,
      rotateField: rotateField == const $CopyWithPlaceholder()
          ? _value.rotateField
          // ignore: cast_nullable_to_non_nullable
          : rotateField as String?,
      sizeField: sizeField == const $CopyWithPlaceholder()
          ? _value.sizeField
          // ignore: cast_nullable_to_non_nullable
          : sizeField as String?,
      colorField: colorField == const $CopyWithPlaceholder()
          ? _value.colorField
          // ignore: cast_nullable_to_non_nullable
          : colorField as String?,
      iconField: iconField == const $CopyWithPlaceholder()
          ? _value.iconField
          // ignore: cast_nullable_to_non_nullable
          : iconField as String?,
      iconCases: iconCases == const $CopyWithPlaceholder()
          ? _value.iconCases
          // ignore: cast_nullable_to_non_nullable
          : iconCases as Map<String, String>?,
      colorCases: colorCases == const $CopyWithPlaceholder()
          ? _value.colorCases
          // ignore: cast_nullable_to_non_nullable
          : colorCases as Map<String, String>?,
    );
  }
}

extension $KindDisplayCopyWith on KindDisplay {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfKindDisplay.copyWith(...)` or `instanceOfKindDisplay.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$KindDisplayCWProxy get copyWith => _$KindDisplayCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

KindDisplay _$KindDisplayFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('KindDisplay', json, ($checkedConvert) {
  final val = KindDisplay(
    titleTemplate: $checkedConvert('titleTemplate', (v) => v as String?),
    icon: $checkedConvert('icon', (v) => v as String?),
    colour: $checkedConvert('colour', (v) => v as String?),
    sidc: $checkedConvert('sidc', (v) => v as String?),
    rotateField: $checkedConvert('rotateField', (v) => v as String?),
    sizeField: $checkedConvert('sizeField', (v) => v as String?),
    colorField: $checkedConvert('colorField', (v) => v as String?),
    iconField: $checkedConvert('iconField', (v) => v as String?),
    iconCases: $checkedConvert(
      'iconCases',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as String)),
    ),
    colorCases: $checkedConvert(
      'colorCases',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as String)),
    ),
  );
  return val;
});

Map<String, dynamic> _$KindDisplayToJson(KindDisplay instance) =>
    <String, dynamic>{
      'titleTemplate': ?instance.titleTemplate,
      'icon': ?instance.icon,
      'colour': ?instance.colour,
      'sidc': ?instance.sidc,
      'rotateField': ?instance.rotateField,
      'sizeField': ?instance.sizeField,
      'colorField': ?instance.colorField,
      'iconField': ?instance.iconField,
      'iconCases': ?instance.iconCases,
      'colorCases': ?instance.colorCases,
    };
