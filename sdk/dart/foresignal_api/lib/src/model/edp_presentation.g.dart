// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edp_presentation.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EdpPresentationCWProxy {
  EdpPresentation color(String? color);

  EdpPresentation icon(String? icon);

  EdpPresentation labelField(String? labelField);

  EdpPresentation minZoom(int? minZoom);

  EdpPresentation maxZoom(int? maxZoom);

  EdpPresentation tileProperties(List<String>? tileProperties);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpPresentation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpPresentation(...).copyWith(id: 12, name: "My name")
  /// ```
  EdpPresentation call({
    String? color,
    String? icon,
    String? labelField,
    int? minZoom,
    int? maxZoom,
    List<String>? tileProperties,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEdpPresentation.copyWith(...)` or call `instanceOfEdpPresentation.copyWith.fieldName(value)` for a single field.
class _$EdpPresentationCWProxyImpl implements _$EdpPresentationCWProxy {
  const _$EdpPresentationCWProxyImpl(this._value);

  final EdpPresentation _value;

  @override
  EdpPresentation color(String? color) => call(color: color);

  @override
  EdpPresentation icon(String? icon) => call(icon: icon);

  @override
  EdpPresentation labelField(String? labelField) =>
      call(labelField: labelField);

  @override
  EdpPresentation minZoom(int? minZoom) => call(minZoom: minZoom);

  @override
  EdpPresentation maxZoom(int? maxZoom) => call(maxZoom: maxZoom);

  @override
  EdpPresentation tileProperties(List<String>? tileProperties) =>
      call(tileProperties: tileProperties);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpPresentation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpPresentation(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EdpPresentation call({
    Object? color = const $CopyWithPlaceholder(),
    Object? icon = const $CopyWithPlaceholder(),
    Object? labelField = const $CopyWithPlaceholder(),
    Object? minZoom = const $CopyWithPlaceholder(),
    Object? maxZoom = const $CopyWithPlaceholder(),
    Object? tileProperties = const $CopyWithPlaceholder(),
  }) {
    return EdpPresentation(
      color: color == const $CopyWithPlaceholder()
          ? _value.color
          // ignore: cast_nullable_to_non_nullable
          : color as String?,
      icon: icon == const $CopyWithPlaceholder()
          ? _value.icon
          // ignore: cast_nullable_to_non_nullable
          : icon as String?,
      labelField: labelField == const $CopyWithPlaceholder()
          ? _value.labelField
          // ignore: cast_nullable_to_non_nullable
          : labelField as String?,
      minZoom: minZoom == const $CopyWithPlaceholder()
          ? _value.minZoom
          // ignore: cast_nullable_to_non_nullable
          : minZoom as int?,
      maxZoom: maxZoom == const $CopyWithPlaceholder()
          ? _value.maxZoom
          // ignore: cast_nullable_to_non_nullable
          : maxZoom as int?,
      tileProperties: tileProperties == const $CopyWithPlaceholder()
          ? _value.tileProperties
          // ignore: cast_nullable_to_non_nullable
          : tileProperties as List<String>?,
    );
  }
}

extension $EdpPresentationCopyWith on EdpPresentation {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEdpPresentation.copyWith(...)` or `instanceOfEdpPresentation.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EdpPresentationCWProxy get copyWith => _$EdpPresentationCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EdpPresentation _$EdpPresentationFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EdpPresentation', json, ($checkedConvert) {
      final val = EdpPresentation(
        color: $checkedConvert('color', (v) => v as String?),
        icon: $checkedConvert('icon', (v) => v as String?),
        labelField: $checkedConvert('labelField', (v) => v as String?),
        minZoom: $checkedConvert('minZoom', (v) => (v as num?)?.toInt()),
        maxZoom: $checkedConvert('maxZoom', (v) => (v as num?)?.toInt()),
        tileProperties: $checkedConvert(
          'tileProperties',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$EdpPresentationToJson(EdpPresentation instance) =>
    <String, dynamic>{
      'color': ?instance.color,
      'icon': ?instance.icon,
      'labelField': ?instance.labelField,
      'minZoom': ?instance.minZoom,
      'maxZoom': ?instance.maxZoom,
      'tileProperties': ?instance.tileProperties,
    };
