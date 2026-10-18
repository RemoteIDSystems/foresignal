// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_coverage_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialCoveragePageCWProxy {
  SocialCoveragePage items(List<SocialCoverage> items);

  SocialCoveragePage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialCoveragePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialCoveragePage(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialCoveragePage call({List<SocialCoverage> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialCoveragePage.copyWith(...)` or call `instanceOfSocialCoveragePage.copyWith.fieldName(value)` for a single field.
class _$SocialCoveragePageCWProxyImpl implements _$SocialCoveragePageCWProxy {
  const _$SocialCoveragePageCWProxyImpl(this._value);

  final SocialCoveragePage _value;

  @override
  SocialCoveragePage items(List<SocialCoverage> items) => call(items: items);

  @override
  SocialCoveragePage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialCoveragePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialCoveragePage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialCoveragePage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return SocialCoveragePage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<SocialCoverage>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $SocialCoveragePageCopyWith on SocialCoveragePage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialCoveragePage.copyWith(...)` or `instanceOfSocialCoveragePage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialCoveragePageCWProxy get copyWith =>
      _$SocialCoveragePageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialCoveragePage _$SocialCoveragePageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SocialCoveragePage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items', 'nextCursor']);
      final val = SocialCoveragePage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => SocialCoverage.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$SocialCoveragePageToJson(SocialCoveragePage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': instance.nextCursor,
    };
