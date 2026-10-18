// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ogc_landing_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OgcLandingPageCWProxy {
  OgcLandingPage title(String? title);

  OgcLandingPage description(String? description);

  OgcLandingPage attribution(String? attribution);

  OgcLandingPage links(List<OgcLink> links);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcLandingPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcLandingPage(...).copyWith(id: 12, name: "My name")
  /// ```
  OgcLandingPage call({
    String? title,
    String? description,
    String? attribution,
    List<OgcLink> links,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOgcLandingPage.copyWith(...)` or call `instanceOfOgcLandingPage.copyWith.fieldName(value)` for a single field.
class _$OgcLandingPageCWProxyImpl implements _$OgcLandingPageCWProxy {
  const _$OgcLandingPageCWProxyImpl(this._value);

  final OgcLandingPage _value;

  @override
  OgcLandingPage title(String? title) => call(title: title);

  @override
  OgcLandingPage description(String? description) =>
      call(description: description);

  @override
  OgcLandingPage attribution(String? attribution) =>
      call(attribution: attribution);

  @override
  OgcLandingPage links(List<OgcLink> links) => call(links: links);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcLandingPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcLandingPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OgcLandingPage call({
    Object? title = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? attribution = const $CopyWithPlaceholder(),
    Object? links = const $CopyWithPlaceholder(),
  }) {
    return OgcLandingPage(
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
      attribution: attribution == const $CopyWithPlaceholder()
          ? _value.attribution
          // ignore: cast_nullable_to_non_nullable
          : attribution as String?,
      links: links == const $CopyWithPlaceholder() || links == null
          ? _value.links
          // ignore: cast_nullable_to_non_nullable
          : links as List<OgcLink>,
    );
  }
}

extension $OgcLandingPageCopyWith on OgcLandingPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOgcLandingPage.copyWith(...)` or `instanceOfOgcLandingPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OgcLandingPageCWProxy get copyWith => _$OgcLandingPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OgcLandingPage _$OgcLandingPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('OgcLandingPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['links']);
      final val = OgcLandingPage(
        title: $checkedConvert('title', (v) => v as String?),
        description: $checkedConvert('description', (v) => v as String?),
        attribution: $checkedConvert('attribution', (v) => v as String?),
        links: $checkedConvert(
          'links',
          (v) => (v as List<dynamic>)
              .map((e) => OgcLink.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$OgcLandingPageToJson(OgcLandingPage instance) =>
    <String, dynamic>{
      'title': ?instance.title,
      'description': ?instance.description,
      'attribution': ?instance.attribution,
      'links': instance.links.map((e) => e.toJson()).toList(),
    };
