// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_comparison_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialComparisonPageCWProxy {
  SocialComparisonPage items(List<SocialComparison> items);

  SocialComparisonPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialComparisonPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialComparisonPage(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialComparisonPage call({List<SocialComparison> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialComparisonPage.copyWith(...)` or call `instanceOfSocialComparisonPage.copyWith.fieldName(value)` for a single field.
class _$SocialComparisonPageCWProxyImpl
    implements _$SocialComparisonPageCWProxy {
  const _$SocialComparisonPageCWProxyImpl(this._value);

  final SocialComparisonPage _value;

  @override
  SocialComparisonPage items(List<SocialComparison> items) =>
      call(items: items);

  @override
  SocialComparisonPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialComparisonPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialComparisonPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialComparisonPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return SocialComparisonPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<SocialComparison>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $SocialComparisonPageCopyWith on SocialComparisonPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialComparisonPage.copyWith(...)` or `instanceOfSocialComparisonPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialComparisonPageCWProxy get copyWith =>
      _$SocialComparisonPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialComparisonPage _$SocialComparisonPageFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SocialComparisonPage', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['items', 'nextCursor']);
  final val = SocialComparisonPage(
    items: $checkedConvert(
      'items',
      (v) => (v as List<dynamic>)
          .map((e) => SocialComparison.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$SocialComparisonPageToJson(
  SocialComparisonPage instance,
) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': instance.nextCursor,
};
