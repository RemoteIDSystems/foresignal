// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'style_fragment_metadata.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StyleFragmentMetadataCWProxy {
  StyleFragmentMetadata legends(Map<String, String>? legends);

  StyleFragmentMetadata attribution(List<Attribution>? attribution);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StyleFragmentMetadata(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StyleFragmentMetadata(...).copyWith(id: 12, name: "My name")
  /// ```
  StyleFragmentMetadata call({
    Map<String, String>? legends,
    List<Attribution>? attribution,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStyleFragmentMetadata.copyWith(...)` or call `instanceOfStyleFragmentMetadata.copyWith.fieldName(value)` for a single field.
class _$StyleFragmentMetadataCWProxyImpl
    implements _$StyleFragmentMetadataCWProxy {
  const _$StyleFragmentMetadataCWProxyImpl(this._value);

  final StyleFragmentMetadata _value;

  @override
  StyleFragmentMetadata legends(Map<String, String>? legends) =>
      call(legends: legends);

  @override
  StyleFragmentMetadata attribution(List<Attribution>? attribution) =>
      call(attribution: attribution);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StyleFragmentMetadata(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StyleFragmentMetadata(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StyleFragmentMetadata call({
    Object? legends = const $CopyWithPlaceholder(),
    Object? attribution = const $CopyWithPlaceholder(),
  }) {
    return StyleFragmentMetadata(
      legends: legends == const $CopyWithPlaceholder()
          ? _value.legends
          // ignore: cast_nullable_to_non_nullable
          : legends as Map<String, String>?,
      attribution: attribution == const $CopyWithPlaceholder()
          ? _value.attribution
          // ignore: cast_nullable_to_non_nullable
          : attribution as List<Attribution>?,
    );
  }
}

extension $StyleFragmentMetadataCopyWith on StyleFragmentMetadata {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStyleFragmentMetadata.copyWith(...)` or `instanceOfStyleFragmentMetadata.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StyleFragmentMetadataCWProxy get copyWith =>
      _$StyleFragmentMetadataCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StyleFragmentMetadata _$StyleFragmentMetadataFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('StyleFragmentMetadata', json, ($checkedConvert) {
  final val = StyleFragmentMetadata(
    legends: $checkedConvert(
      'legends',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as String)),
    ),
    attribution: $checkedConvert(
      'attribution',
      (v) => (v as List<dynamic>?)
          ?.map((e) => Attribution.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$StyleFragmentMetadataToJson(
  StyleFragmentMetadata instance,
) => <String, dynamic>{
  'legends': ?instance.legends,
  'attribution': ?instance.attribution?.map((e) => e.toJson()).toList(),
};
