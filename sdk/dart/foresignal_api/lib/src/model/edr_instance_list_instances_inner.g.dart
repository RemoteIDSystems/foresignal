// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edr_instance_list_instances_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EdrInstanceListInstancesInnerCWProxy {
  EdrInstanceListInstancesInner id(String id);

  EdrInstanceListInstancesInner title(String? title);

  EdrInstanceListInstancesInner extent(Map<String, Object>? extent);

  EdrInstanceListInstancesInner links(List<OgcLink> links);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdrInstanceListInstancesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdrInstanceListInstancesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  EdrInstanceListInstancesInner call({
    String id,
    String? title,
    Map<String, Object>? extent,
    List<OgcLink> links,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEdrInstanceListInstancesInner.copyWith(...)` or call `instanceOfEdrInstanceListInstancesInner.copyWith.fieldName(value)` for a single field.
class _$EdrInstanceListInstancesInnerCWProxyImpl
    implements _$EdrInstanceListInstancesInnerCWProxy {
  const _$EdrInstanceListInstancesInnerCWProxyImpl(this._value);

  final EdrInstanceListInstancesInner _value;

  @override
  EdrInstanceListInstancesInner id(String id) => call(id: id);

  @override
  EdrInstanceListInstancesInner title(String? title) => call(title: title);

  @override
  EdrInstanceListInstancesInner extent(Map<String, Object>? extent) =>
      call(extent: extent);

  @override
  EdrInstanceListInstancesInner links(List<OgcLink> links) =>
      call(links: links);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdrInstanceListInstancesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdrInstanceListInstancesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EdrInstanceListInstancesInner call({
    Object? id = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? extent = const $CopyWithPlaceholder(),
    Object? links = const $CopyWithPlaceholder(),
  }) {
    return EdrInstanceListInstancesInner(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      extent: extent == const $CopyWithPlaceholder()
          ? _value.extent
          // ignore: cast_nullable_to_non_nullable
          : extent as Map<String, Object>?,
      links: links == const $CopyWithPlaceholder() || links == null
          ? _value.links
          // ignore: cast_nullable_to_non_nullable
          : links as List<OgcLink>,
    );
  }
}

extension $EdrInstanceListInstancesInnerCopyWith
    on EdrInstanceListInstancesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEdrInstanceListInstancesInner.copyWith(...)` or `instanceOfEdrInstanceListInstancesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EdrInstanceListInstancesInnerCWProxy get copyWith =>
      _$EdrInstanceListInstancesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EdrInstanceListInstancesInner _$EdrInstanceListInstancesInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EdrInstanceListInstancesInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['id', 'links']);
  final val = EdrInstanceListInstancesInner(
    id: $checkedConvert('id', (v) => v as String),
    title: $checkedConvert('title', (v) => v as String?),
    extent: $checkedConvert(
      'extent',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    links: $checkedConvert(
      'links',
      (v) => (v as List<dynamic>)
          .map((e) => OgcLink.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$EdrInstanceListInstancesInnerToJson(
  EdrInstanceListInstancesInner instance,
) => <String, dynamic>{
  'id': instance.id,
  'title': ?instance.title,
  'extent': ?instance.extent,
  'links': instance.links.map((e) => e.toJson()).toList(),
};
