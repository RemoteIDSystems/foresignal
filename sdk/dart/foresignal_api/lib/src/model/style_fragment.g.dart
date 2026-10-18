// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'style_fragment.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StyleFragmentCWProxy {
  StyleFragment version(StyleFragmentVersionEnum version);

  StyleFragment sources(Map<String, Map<String, Object>> sources);

  StyleFragment layers(List<Map<String, Object>> layers);

  StyleFragment metadata(StyleFragmentMetadata? metadata);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StyleFragment(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StyleFragment(...).copyWith(id: 12, name: "My name")
  /// ```
  StyleFragment call({
    StyleFragmentVersionEnum version,
    Map<String, Map<String, Object>> sources,
    List<Map<String, Object>> layers,
    StyleFragmentMetadata? metadata,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStyleFragment.copyWith(...)` or call `instanceOfStyleFragment.copyWith.fieldName(value)` for a single field.
class _$StyleFragmentCWProxyImpl implements _$StyleFragmentCWProxy {
  const _$StyleFragmentCWProxyImpl(this._value);

  final StyleFragment _value;

  @override
  StyleFragment version(StyleFragmentVersionEnum version) =>
      call(version: version);

  @override
  StyleFragment sources(Map<String, Map<String, Object>> sources) =>
      call(sources: sources);

  @override
  StyleFragment layers(List<Map<String, Object>> layers) =>
      call(layers: layers);

  @override
  StyleFragment metadata(StyleFragmentMetadata? metadata) =>
      call(metadata: metadata);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StyleFragment(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StyleFragment(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StyleFragment call({
    Object? version = const $CopyWithPlaceholder(),
    Object? sources = const $CopyWithPlaceholder(),
    Object? layers = const $CopyWithPlaceholder(),
    Object? metadata = const $CopyWithPlaceholder(),
  }) {
    return StyleFragment(
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as StyleFragmentVersionEnum,
      sources: sources == const $CopyWithPlaceholder() || sources == null
          ? _value.sources
          // ignore: cast_nullable_to_non_nullable
          : sources as Map<String, Map<String, Object>>,
      layers: layers == const $CopyWithPlaceholder() || layers == null
          ? _value.layers
          // ignore: cast_nullable_to_non_nullable
          : layers as List<Map<String, Object>>,
      metadata: metadata == const $CopyWithPlaceholder()
          ? _value.metadata
          // ignore: cast_nullable_to_non_nullable
          : metadata as StyleFragmentMetadata?,
    );
  }
}

extension $StyleFragmentCopyWith on StyleFragment {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStyleFragment.copyWith(...)` or `instanceOfStyleFragment.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StyleFragmentCWProxy get copyWith => _$StyleFragmentCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StyleFragment _$StyleFragmentFromJson(Map<String, dynamic> json) =>
    $checkedCreate('StyleFragment', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['version', 'sources', 'layers']);
      final val = StyleFragment(
        version: $checkedConvert(
          'version',
          (v) => $enumDecode(_$StyleFragmentVersionEnumEnumMap, v),
        ),
        sources: $checkedConvert(
          'sources',
          (v) => (v as Map<String, dynamic>).map(
            (k, e) => MapEntry(
              k,
              (e as Map<String, dynamic>).map(
                (k, e) => MapEntry(k, e as Object),
              ),
            ),
          ),
        ),
        layers: $checkedConvert(
          'layers',
          (v) => (v as List<dynamic>)
              .map(
                (e) => (e as Map<String, dynamic>).map(
                  (k, e) => MapEntry(k, e as Object),
                ),
              )
              .toList(),
        ),
        metadata: $checkedConvert(
          'metadata',
          (v) => v == null
              ? null
              : StyleFragmentMetadata.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$StyleFragmentToJson(StyleFragment instance) =>
    <String, dynamic>{
      'version': _$StyleFragmentVersionEnumEnumMap[instance.version]!,
      'sources': instance.sources,
      'layers': instance.layers,
      'metadata': ?instance.metadata?.toJson(),
    };

const _$StyleFragmentVersionEnumEnumMap = {StyleFragmentVersionEnum.number8: 8};
