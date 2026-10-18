// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_platform_terms.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialPlatformTermsCWProxy {
  SocialPlatformTerms summary(String? summary);

  SocialPlatformTerms url(String? url);

  SocialPlatformTerms costs(String? costs);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialPlatformTerms(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialPlatformTerms(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialPlatformTerms call({String? summary, String? url, String? costs});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialPlatformTerms.copyWith(...)` or call `instanceOfSocialPlatformTerms.copyWith.fieldName(value)` for a single field.
class _$SocialPlatformTermsCWProxyImpl implements _$SocialPlatformTermsCWProxy {
  const _$SocialPlatformTermsCWProxyImpl(this._value);

  final SocialPlatformTerms _value;

  @override
  SocialPlatformTerms summary(String? summary) => call(summary: summary);

  @override
  SocialPlatformTerms url(String? url) => call(url: url);

  @override
  SocialPlatformTerms costs(String? costs) => call(costs: costs);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialPlatformTerms(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialPlatformTerms(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialPlatformTerms call({
    Object? summary = const $CopyWithPlaceholder(),
    Object? url = const $CopyWithPlaceholder(),
    Object? costs = const $CopyWithPlaceholder(),
  }) {
    return SocialPlatformTerms(
      summary: summary == const $CopyWithPlaceholder()
          ? _value.summary
          // ignore: cast_nullable_to_non_nullable
          : summary as String?,
      url: url == const $CopyWithPlaceholder()
          ? _value.url
          // ignore: cast_nullable_to_non_nullable
          : url as String?,
      costs: costs == const $CopyWithPlaceholder()
          ? _value.costs
          // ignore: cast_nullable_to_non_nullable
          : costs as String?,
    );
  }
}

extension $SocialPlatformTermsCopyWith on SocialPlatformTerms {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialPlatformTerms.copyWith(...)` or `instanceOfSocialPlatformTerms.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialPlatformTermsCWProxy get copyWith =>
      _$SocialPlatformTermsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialPlatformTerms _$SocialPlatformTermsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SocialPlatformTerms', json, ($checkedConvert) {
      final val = SocialPlatformTerms(
        summary: $checkedConvert('summary', (v) => v as String?),
        url: $checkedConvert('url', (v) => v as String?),
        costs: $checkedConvert('costs', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$SocialPlatformTermsToJson(
  SocialPlatformTerms instance,
) => <String, dynamic>{
  'summary': ?instance.summary,
  'url': ?instance.url,
  'costs': ?instance.costs,
};
