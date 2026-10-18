// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ogc_link.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OgcLinkCWProxy {
  OgcLink href(String href);

  OgcLink rel(String rel);

  OgcLink type(String? type);

  OgcLink title(String? title);

  OgcLink templated(bool? templated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcLink(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcLink(...).copyWith(id: 12, name: "My name")
  /// ```
  OgcLink call({
    String href,
    String rel,
    String? type,
    String? title,
    bool? templated,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOgcLink.copyWith(...)` or call `instanceOfOgcLink.copyWith.fieldName(value)` for a single field.
class _$OgcLinkCWProxyImpl implements _$OgcLinkCWProxy {
  const _$OgcLinkCWProxyImpl(this._value);

  final OgcLink _value;

  @override
  OgcLink href(String href) => call(href: href);

  @override
  OgcLink rel(String rel) => call(rel: rel);

  @override
  OgcLink type(String? type) => call(type: type);

  @override
  OgcLink title(String? title) => call(title: title);

  @override
  OgcLink templated(bool? templated) => call(templated: templated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcLink(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcLink(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OgcLink call({
    Object? href = const $CopyWithPlaceholder(),
    Object? rel = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? templated = const $CopyWithPlaceholder(),
  }) {
    return OgcLink(
      href: href == const $CopyWithPlaceholder() || href == null
          ? _value.href
          // ignore: cast_nullable_to_non_nullable
          : href as String,
      rel: rel == const $CopyWithPlaceholder() || rel == null
          ? _value.rel
          // ignore: cast_nullable_to_non_nullable
          : rel as String,
      type: type == const $CopyWithPlaceholder()
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as String?,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      templated: templated == const $CopyWithPlaceholder()
          ? _value.templated
          // ignore: cast_nullable_to_non_nullable
          : templated as bool?,
    );
  }
}

extension $OgcLinkCopyWith on OgcLink {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOgcLink.copyWith(...)` or `instanceOfOgcLink.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OgcLinkCWProxy get copyWith => _$OgcLinkCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OgcLink _$OgcLinkFromJson(Map<String, dynamic> json) =>
    $checkedCreate('OgcLink', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['href', 'rel']);
      final val = OgcLink(
        href: $checkedConvert('href', (v) => v as String),
        rel: $checkedConvert('rel', (v) => v as String),
        type: $checkedConvert('type', (v) => v as String?),
        title: $checkedConvert('title', (v) => v as String?),
        templated: $checkedConvert('templated', (v) => v as bool?),
      );
      return val;
    });

Map<String, dynamic> _$OgcLinkToJson(OgcLink instance) => <String, dynamic>{
  'href': instance.href,
  'rel': instance.rel,
  'type': ?instance.type,
  'title': ?instance.title,
  'templated': ?instance.templated,
};
